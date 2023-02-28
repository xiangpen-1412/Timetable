package com.nobes.timetable.hierarchy.logic;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nobes.timetable.core.entity.ResultBody;
import com.nobes.timetable.core.utils.OrikaUtils;
import com.nobes.timetable.hierarchy.domain.NobesTimetableCourse;
import com.nobes.timetable.hierarchy.domain.NobesTimetableLab;
import com.nobes.timetable.hierarchy.domain.NobesTimetableLecture;
import com.nobes.timetable.hierarchy.domain.NobesTimetableSem;
import com.nobes.timetable.hierarchy.dto.CourseDTO;
import com.nobes.timetable.hierarchy.service.INobesTimetableCourseService;
import com.nobes.timetable.hierarchy.service.INobesTimetableLabService;
import com.nobes.timetable.hierarchy.service.INobesTimetableLectureService;
import com.nobes.timetable.hierarchy.service.INobesTimetableSemService;
import com.nobes.timetable.hierarchy.vo.LabAndSem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class CourseService {

    @Resource
    INobesTimetableCourseService iNobesTimetableCourseService;

    @Resource
    INobesTimetableLectureService iNobesTimetableLectureService;

    @Resource
    INobesTimetableLabService iNobesTimetableLabService;

    @Resource
    INobesTimetableSemService iNobesTimetableSemService;

    @Autowired
    SeminarService seminarService;

    @Autowired
    LabService labService;

    public HashMap getCourse(CourseDTO courseDTO) {
        String courseName = courseDTO.getCourseName();

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(courseName);
        matcher.find();
        String catalog = matcher.group(0);
        String subject = courseName.substring(0, courseName.indexOf(catalog.charAt(0)) - 1);

        NobesTimetableCourse course = iNobesTimetableCourseService.getOne(new LambdaQueryWrapper<NobesTimetableCourse>()
                .eq(NobesTimetableCourse::getCatalog, catalog)
                .eq(NobesTimetableCourse::getSubject, subject)
        );

        String approvedHrs = course.getApprovedHrs();

        Boolean hasLec = false;
        Boolean hasLab = false;
        Boolean hasSem = false;

        String zero = "0";

        if (approvedHrs.contains("-")) {
            String[] split = approvedHrs.split("-");
            if (!split[0].equals(zero)) {
                hasLec = true;
            }
            if (!split[1].equals(zero)) {
                hasSem = true;
            }
            if (split.length > 2) {
                if (!split[2].equals(zero)) {
                    hasLab = true;
                }
            }
        } else {
            hasLec = true;
        }

        Integer courseId = course.getCourseId();

        // map for courses whose main part is lecture
        HashMap<String, HashMap<String, HashMap<String, ArrayList<LabAndSem>>>> map = new HashMap<>();
        HashMap<String, HashMap<String, ArrayList<LabAndSem>>> courseMap = new HashMap<>();
        HashMap<String, ArrayList<LabAndSem>> subMap = new HashMap<>();

        // map for courses whose main part is lab or sem
        HashMap<String, HashMap<String, LabAndSem>> map1 = new HashMap<>();
        HashMap<String, LabAndSem> map2 = new HashMap<>();

        // for errors
        HashMap<String, String> map3 = new HashMap<>();

        log.info(hasLec.toString());
        log.info(hasLab.toString());
        log.info(hasSem.toString());

        // if the main part of the course if a lecture
        if (hasLec) {

            List<NobesTimetableLecture> sectionList = iNobesTimetableLectureService.list(new LambdaQueryWrapper<NobesTimetableLecture>()
                    .eq(NobesTimetableLecture::getCourseId, courseId));

            // if the course has seminars
            if (hasSem) {
                for (NobesTimetableLecture lecture : sectionList) {
                    String sect = lecture.getSect();
                    String key = " Sem";

                    ArrayList<LabAndSem> seminar = seminarService.getSeminar(lecture, iNobesTimetableSemService);

                    subMap.put(key, seminar);

                    String key1 = subject + " " + catalog + " " + sect;
                    courseMap.put(key1, subMap);

                }

                map.put(courseName, courseMap);
            }

            // if the course has labs
            if (hasLab) {
                for (NobesTimetableLecture lecture : sectionList) {
                    String sect = lecture.getSect();
                    String key = " Lab";

                    ArrayList<LabAndSem> lab = labService.getLab(lecture, iNobesTimetableLabService);

                    subMap.put(key, lab);

                    String key1 = subject + " " + catalog + " " + sect;
                    courseMap.put(key1, subMap);

                }

                map.put(courseName, courseMap);
            }

        } else if (hasLab) {
            List<NobesTimetableLab> labList = iNobesTimetableLabService.list(new LambdaQueryWrapper<NobesTimetableLab>()
                    .eq(NobesTimetableLab::getCourseId, courseId));

            for (NobesTimetableLab lab : labList) {

                String component = lab.getComponent();
                String sect = lab.getSect();
                StringBuilder labName = new StringBuilder();
                StringBuilder duration = new StringBuilder();

                String mon = lab.getMon();
                String tues = lab.getTues();
                String wed = lab.getWed();
                String thurs = lab.getThrus();
                String fri = lab.getFri();

                HashMap<String, String> weekdays = new HashMap<>();
                weekdays.put("Monday", mon);
                weekdays.put("Tuesday", tues);
                weekdays.put("Wednesday", wed);
                weekdays.put("Thursday", thurs);
                weekdays.put("Friday", fri);

                weekdays.forEach((key, value) -> {
                    if (value.equals("Y")) {
                        duration.append(key).append(",");
                    }
                });

                duration.deleteCharAt(duration.length() - 1);
                String subject1 = lab.getSubject();
                String catalog1 = lab.getCatalog();

                labName.append(subject1)
                        .append(" ")
                        .append(catalog1)
                        .append(" ")
                        .append(component)
                        .append(" ")
                        .append(sect);

                LabAndSem lab1 = OrikaUtils.convert(lab, LabAndSem.class);

                lab1.setName(labName.toString()).setDate(duration.toString());

                map2.put(labName.toString(), lab1);
            }

            map1.put(courseName, map2);
        } else if (hasSem) {

            List<NobesTimetableSem> semList = iNobesTimetableSemService.list(new LambdaQueryWrapper<NobesTimetableSem>()
                    .eq(NobesTimetableSem::getCourseId, courseId));

            for (NobesTimetableSem sem : semList) {

                String component = sem.getComponent();
                String sect = sem.getSect();
                StringBuilder labName = new StringBuilder();
                StringBuilder duration = new StringBuilder();

                String mon = sem.getMon();
                String tues = sem.getTues();
                String wed = sem.getWed();
                String thurs = sem.getThrus();
                String fri = sem.getFri();

                HashMap<String, String> weekdays = new HashMap<>();
                weekdays.put("Monday", mon);
                weekdays.put("Tuesday", tues);
                weekdays.put("Wednesday", wed);
                weekdays.put("Thursday", thurs);
                weekdays.put("Friday", fri);

                weekdays.forEach((key, value) -> {
                    if (value.equals("Y")) {
                        duration.append(key).append(",");
                    }
                });

                duration.deleteCharAt(duration.length() - 1);
                String subject1 = sem.getSubject();
                String catalog1 = sem.getCatalog();

                labName.append(subject1)
                        .append(" ")
                        .append(catalog1)
                        .append(" ")
                        .append(component)
                        .append(" ")
                        .append(sect);

                LabAndSem sem1 = OrikaUtils.convert(sem, LabAndSem.class);

                sem1.setName(labName.toString()).setDate(duration.toString());

                map2.put(labName.toString(), sem1);
            }

            map1.put(courseName, map2);
        } else {
            map3.put("error", "Invalid Course");
        }

        if (!map.isEmpty()) {
            return map;
        } else if (!map1.isEmpty()) {
            return map1;
        } else {
            return map3;
        }
    }

}