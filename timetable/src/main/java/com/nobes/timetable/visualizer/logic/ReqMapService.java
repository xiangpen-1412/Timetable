package com.nobes.timetable.visualizer.logic;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nobes.timetable.calendar.domain.NobesTimetableSequence;
import com.nobes.timetable.calendar.service.INobesTimetableSequenceService;
import com.nobes.timetable.visualizer.domain.NobesVisualizerCourse;
import com.nobes.timetable.visualizer.dto.VisualDTO;
import com.nobes.timetable.visualizer.logic.reqHelp.ReqService;
import com.nobes.timetable.visualizer.service.INobesVisualizerCourseService;
import com.nobes.timetable.visualizer.vo.ReqVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ReqMapService {

    @Resource
    INobesTimetableSequenceService iSequenceService;

    @Resource
    INobesVisualizerCourseService iNVisualizerCourseService;

    @Resource
    ReqService rService;

    /**
     * This function is used to get prerequisites/postrequisites/corequisites information of all the courses
     * in a selected program and selected plan
     *
     * @param visualDTO visualDTO containing the selected program name and selected plan name
     * @return a Hashmap which keys are course name and values are the course's requisites
     */
    public HashMap getCourseReqMap(VisualDTO visualDTO) throws Exception {

        HashMap<String, ReqVO> reqMap = new HashMap<>();

        String planName = visualDTO.getPlanName();
        String programName = visualDTO.getProgramName();

        // get all the terms and courses by the given program name and plan name
        List<NobesTimetableSequence> sequenceList = iSequenceService.list(new LambdaQueryWrapper<NobesTimetableSequence>()
                .eq(NobesTimetableSequence::getProgramName, programName)
                .eq(NobesTimetableSequence::getPlanName, planName));

        List<String> courseNameList = sequenceList.stream().map(NobesTimetableSequence::getCourseName).map(String::trim).collect(Collectors.toList());

        for (String course : courseNameList) {

            if (course.contains("(")) {
                course = course.substring(0, course.indexOf("("));
            }

            if (!(course.contains("COMP") || course.contains("ITS") || course.contains("PROG"))) {
                if (course.contains("or")) {

                    String[] orCases = Arrays.stream(course.split("or"))
                            .map(String::trim)
                            .toArray(String[]::new);

                    for (String orCase : orCases) {
                        getReq(reqMap, orCase);
                    }

                } else {
                    getReq(reqMap, course);
                }
            }
        }

        Set<String> reqKeySet = reqMap.keySet();

        // summarize the requisite map
        for (String key : reqKeySet) {
            ReqVO reqVO = reqMap.get(key);
            ArrayList<String> preRes = reqVO.getPreRe();
            ArrayList<String> coRes = reqVO.getCoRe();

            preRes = (ArrayList<String>) preRes.stream()
                    .flatMap(re -> re.contains("or") ? Arrays.stream(re.split("or")).map(String::trim) : Stream.of(re))
                    .collect(Collectors.toList());

            preRes = (ArrayList<String>) preRes.stream().distinct().collect(Collectors.toList());

            coRes = (ArrayList<String>) coRes.stream()
                    .flatMap(re -> re.contains("or") ? Arrays.stream(re.split("or")).map(String::trim) : Stream.of(re))
                    .collect(Collectors.toList());

            coRes = (ArrayList<String>) coRes.stream().distinct().collect(Collectors.toList());

            for (String re : preRes) {
                boolean b = reqMap.containsKey(re);

                if (reqMap.containsKey(re)) {
                    ArrayList<String> postRe = reqMap.get(re).getPostReq();
                    boolean match = postRe.stream().anyMatch(str -> str.contains(key));

                    if (!match) {
                        postRe.add(key);
                    }
                }
            }

            for (String re : coRes) {
                if (reqMap.containsKey(re)) {
                    ArrayList<String> cocoRe = reqMap.get(re).getCocoRe();

                    boolean match = cocoRe.stream().anyMatch(str -> str.contains(key));

                    if (!match) {
                        cocoRe.add(key);
                    }
                }
            }
        }

        return reqMap;
    }

    /**
     * get the requisites of a single course and add to map
     * */
    public void getReq(HashMap<String, ReqVO> reqMap, String course) {

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(course);
        matcher.find();
        String catalog = matcher.group(0);
        String subject = course.substring(0, course.indexOf(catalog.charAt(0)) - 1);

        NobesVisualizerCourse nobesVisualizerCourse = iNVisualizerCourseService.getOne(new LambdaQueryWrapper<NobesVisualizerCourse>()
                .eq(NobesVisualizerCourse::getCatalog, catalog)
                .eq(NobesVisualizerCourse::getSubject, subject), false);

        String courseDescription = nobesVisualizerCourse.getCourseDescription();

        // get the prerequisites and corequisites
        ArrayList<String> preReqs = rService.pullPreReqs(courseDescription);
        ArrayList<String> coReqs = rService.pullCoReqs(courseDescription);

        ArrayList<String> preRequisites = getOrCaseReq(preReqs);
        ArrayList<String> coRequisites = getOrCaseReq(coReqs);

        ReqVO reqVO = new ReqVO().setPreRe(preRequisites).setCoRe(coRequisites).setPostReq(new ArrayList<>()).setCocoRe(new ArrayList<>());

        reqMap.put(course, reqVO);
    }


    /**
     * Deal with or case
     */
    public ArrayList<String> getOrCaseReq(ArrayList<String> reqs) {

        for (String requisite : reqs) {

            // if there is 1 or more or in the req name like "MEC E 330 or 331"
            // turn it to "MEC E 330 or MEC E 331"
            if (requisite.contains("or")) {
                StringBuilder stringBuilder = new StringBuilder();

                String[] ors = Arrays.stream(requisite.split("or"))
                        .map(String::trim)
                        .toArray(String[]::new);

                stringBuilder.append(ors[0]);

                // get the department
                String dept = ors[0].replaceAll("\\d+", "").trim();

                int index = 1;
                while (index < ors.length) {
                    if (ors[index].matches("\\d+")) {
                        ors[index] = dept + " " + ors[index].trim();
                    }

                    stringBuilder.append(" or ").append(ors[index]);

                    index++;
                }

                reqs.set(reqs.indexOf(requisite), stringBuilder.toString());
            }
        }

        return reqs;
    }
}
