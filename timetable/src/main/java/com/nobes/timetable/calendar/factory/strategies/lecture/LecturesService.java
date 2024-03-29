package com.nobes.timetable.calendar.factory.strategies.lecture;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nobes.timetable.calendar.domain.NobesTimetableAu;
import com.nobes.timetable.calendar.domain.NobesTimetableCourse;
import com.nobes.timetable.calendar.dto.CourseIdDTO;
import com.nobes.timetable.calendar.factory.strategies.UniComponentStrategy;
import com.nobes.timetable.calendar.service.INobesTimetableAuService;
import com.nobes.timetable.calendar.service.INobesTimetableCourseService;
import com.nobes.timetable.calendar.vo.CourseVO;
import com.nobes.timetable.calendar.vo.LectureVO;
import com.nobes.timetable.visualizer.domain.NobesVisualizerCourse;
import com.nobes.timetable.visualizer.service.INobesVisualizerCourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * This class represents a concrete implementation of the UniComponentStrategy interface for handling lecture information.
 * The lecture information is retrieved based on the course names, and the returned HashMap
 * contains the course name as key and an ArrayList of LectureVO objects as value.
 */
@Component(value = "1")
@Slf4j
public class LecturesService implements UniComponentStrategy {

    @Resource
    INobesTimetableCourseService courseSelectService;

    @Resource
    INobesTimetableAuService iNobesTimetableAuService;

    @Resource
    INobesVisualizerCourseService visualizerCourseService;

    @Resource
    com.nobes.timetable.calendar.factory.strategies.lecture.LecService lService;


    /**
     * rewrite the handle function in public interface to retrieve lecture information for the given course names and term
     *
     * @param names an ArrayList of course names
     * @param term  a string representing the selected term
     * @return a HashMap containing all the detailed lecture information for the given course names
     * @throws Exception if an error occurs while retrieving the lecture information
     */
    @Override
    public HashMap handle(ArrayList<String> names, String term) throws Exception {

        HashMap<String, ArrayList<LectureVO>> lecMap = new HashMap<>();

        for (int i = 0; i < names.size(); i++) {

            String courseName = names.get(i);

            if (courseName.equals("COMP")) {
                // if the course is a complementary selective
                CourseVO courseVO = new CourseVO();
                courseVO.setSubject("COMP");
                courseVO.setCourseName("COMP");
                lecMap.put(courseName, null);
            } else if (courseName.equals("ITS")) {
                // if the course is a ITS selective
                CourseVO courseVO = new CourseVO();
                courseVO.setSubject("ITS");
                courseVO.setCourseName("ITS");
                lecMap.put(courseName, null);
            } else if (courseName.contains("PROG")) {
                // if the course is a program elective
                CourseVO courseVO = new CourseVO();
                courseVO.setSubject("PROG");
                courseVO.setCourseName("PROG");
                lecMap.put(courseName, null);
            } else if (courseName.contains("WKEXP")) {
                // if the course is a coop term course
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(courseName);
                matcher.find();
                String section = matcher.group(0);

                LectureVO lectureVO = new LectureVO();
                ArrayList<LectureVO> lecs = new ArrayList<>();
                lectureVO.setLecName(courseName);
                ArrayList<String> times = new ArrayList<>();
                times.add("MON_08:00-18:00");
                times.add("TUE_08:00-18:00");
                times.add("WED_08:00-18:00");
                times.add("THU_08:00-18:00");
                times.add("FRI_08:00-18:00");
                lectureVO.setTimes(times);
                lectureVO.setSection(section);

                // get the work term course description
                NobesVisualizerCourse workTermCourse = visualizerCourseService.getOne(new LambdaQueryWrapper<NobesVisualizerCourse>()
                        .eq(NobesVisualizerCourse::getCatalog, section.trim())
                        .eq(NobesVisualizerCourse::getSubject, "WKEXP"), false);

                String description = "";

                if (workTermCourse != null) {
                    String progUnits = workTermCourse.getProgUnits();
                    String calcFeeIndex = workTermCourse.getCalcFeeIndex();
                    String duration = workTermCourse.getDuration();
                    String alphaHours = workTermCourse.getAlphaHours();
                    String courseDescription = workTermCourse.getCourseDescription();

                    description = "★ " + progUnits.replaceAll("[^0-9]", "") + " (fi " + calcFeeIndex + ") " + "(" + duration + ", " + alphaHours + ") " + courseDescription;
                }

                lectureVO.setDescp(description);

                lecs.add(lectureVO);
                lecMap.put(courseName, lecs);

            } else {
                // find the catalog and subject of the given course
                Pattern pattern = Pattern.compile("\\d+");
                Matcher matcher = pattern.matcher(courseName);
                matcher.find();
                String catalog = matcher.group(0);
                String subject = courseName.substring(0, courseName.indexOf(catalog.charAt(0)) - 1);

                NobesTimetableCourse course = courseSelectService.getOne(new LambdaQueryWrapper<NobesTimetableCourse>()
                        .eq(NobesTimetableCourse::getCatalog, catalog)
                        .eq(NobesTimetableCourse::getSubject, subject)
                        .eq(NobesTimetableCourse::getAppliedTerm, term), false);

                // in case of CH E 243A or CH E 243A
                if (course == null) {
                    course = courseSelectService.getOne(new LambdaQueryWrapper<NobesTimetableCourse>()
                            .like(NobesTimetableCourse::getCatalog, catalog + "_")
                            .eq(NobesTimetableCourse::getSubject, subject)
                            .eq(NobesTimetableCourse::getAppliedTerm, term), false);
                }

                // find the accreditation units information and store to AUCount
                NobesTimetableAu au = iNobesTimetableAuService.getOne(new LambdaQueryWrapper<NobesTimetableAu>()
                        .eq(NobesTimetableAu::getCourseName, subject + " " + catalog), false);

                HashMap<String, String> AUCount = new HashMap<>();

                if (au != null) {
                    AUCount = Stream.of(
                                    new AbstractMap.SimpleEntry<>("Math", au.getMath()),
                                    new AbstractMap.SimpleEntry<>("Natural Sciences", au.getNaturalSciences()),
                                    new AbstractMap.SimpleEntry<>("Complimentary Studies", au.getComplimentaryStudies()),
                                    new AbstractMap.SimpleEntry<>("Engineering Design", au.getEngineeringDesign()),
                                    new AbstractMap.SimpleEntry<>("Engineering Science", au.getEngineeringScience()),
                                    new AbstractMap.SimpleEntry<>("Other", au.getOther()),
                                    new AbstractMap.SimpleEntry<>("EDsp", au.getEDsp()),
                                    new AbstractMap.SimpleEntry<>("ESsp", au.getESsp())
                            )
                            .filter(entry -> !entry.getValue().equals("0")) // Filter out zero-valued strings
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, HashMap::new));
                }

                if (course != null) {

                    // find the course description
                    Integer courseId = course.getCourseId();
                    String courseTitle = course.getDescr();

                    if (!course.getLec().equals("0") && !course.getLec().equals("UNASSIGNED")) {
                        String coursename = subject + " " + catalog;
                        CourseIdDTO courseIdDTO = new CourseIdDTO();
                        courseIdDTO.setCourseId(courseId);

                        NobesVisualizerCourse nobesVisualizerCourse = visualizerCourseService.getOne(new LambdaQueryWrapper<NobesVisualizerCourse>()
                                .eq(NobesVisualizerCourse::getCatalog, catalog)
                                .eq(NobesVisualizerCourse::getSubject, subject), false);

                        String description = "";

                        if (nobesVisualizerCourse != null) {
                            if (!nobesVisualizerCourse.isNull()) {
                                String progUnits = nobesVisualizerCourse.getProgUnits();
                                String calcFeeIndex = nobesVisualizerCourse.getCalcFeeIndex();
                                String duration = nobesVisualizerCourse.getDuration();
                                String alphaHours = nobesVisualizerCourse.getAlphaHours();
                                String courseDescription = nobesVisualizerCourse.getCourseDescription();

                                description = "★ " + progUnits.replaceAll("[^0-9]", "") + " (fi " + calcFeeIndex + ") " + "(" + duration + ", " + alphaHours + ") " + courseDescription;

                            } else {
                                description = nobesVisualizerCourse.getCourseDescription();
                            }
                        } else {
                            description = course.getDescription();
                        }

                        ArrayList<LectureVO> lectures = lService.getLecture(courseIdDTO);

                        for (LectureVO lectureVO : lectures) {
                            lectureVO.setDescp(description);
                            lectureVO.setCourseTitle(courseName + " - " + courseTitle);
                            lectureVO.setAUCount(AUCount);
                        }

                        if (!lectures.isEmpty()) {
                            lecMap.put(coursename, lectures);
                        }
                    }
                } else {
                    lecMap.put(courseName, null);
                }

            }
        }

        return lecMap;
    }
}

