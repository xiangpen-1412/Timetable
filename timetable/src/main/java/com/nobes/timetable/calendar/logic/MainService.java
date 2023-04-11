package com.nobes.timetable.calendar.logic;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nobes.timetable.core.utils.OrikaUtils;
import com.nobes.timetable.calendar.domain.*;
import com.nobes.timetable.calendar.service.INobesTimetableAuService;
import com.nobes.timetable.calendar.vo.CourseVO;
import com.nobes.timetable.calendar.vo.LabVO;
import com.nobes.timetable.calendar.vo.LectureVO;
import com.nobes.timetable.calendar.vo.SemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Component
@Slf4j
public class MainService {

    @Resource
    INobesTimetableAuService iNobesTimetableAuService;

    public CourseVO getCourseObj(NobesTimetableCourse nobesTimetableCourse) throws Exception {
        CourseVO courseVO = OrikaUtils.convert(nobesTimetableCourse, CourseVO.class);

        String courseName = courseVO.getSubject() + " " + courseVO.getCatalog();
        courseVO.setCourseName(courseName);
        NobesTimetableAu courseAU = iNobesTimetableAuService.getOne(new LambdaQueryWrapper<NobesTimetableAu>()
                .eq(NobesTimetableAu::getCourseName, courseName));

        HashMap<String, Double> auCount = new HashMap<>();
        auCount.put("Math", Double.parseDouble(courseAU.getMath()));
        auCount.put("Natural Sciences", Double.parseDouble(courseAU.getNaturalSciences()));
        auCount.put("Complimentary Studies", Double.parseDouble(courseAU.getComplimentaryStudies()));
        auCount.put("Engineering Science", Double.parseDouble(courseAU.getEngineeringScience()));
        auCount.put("Engineering Design", Double.parseDouble(courseAU.getEngineeringDesign()));
        auCount.put("Others", Double.parseDouble(courseAU.getOther()));
        auCount.put("ES(sp)", Double.parseDouble(courseAU.getESsp()));
        auCount.put("ED(sp)", Double.parseDouble(courseAU.getEDsp()));

        courseVO.setAUCount(auCount);

        return courseVO;
    }


    public LectureVO getLectureObj(NobesTimetableLecture nobesTimetableLecture) {
        LectureVO lectureVO = OrikaUtils.convert(nobesTimetableLecture, LectureVO.class);

        String lectureName = nobesTimetableLecture.getSubject()
                + nobesTimetableLecture.getCatalog() + " "
                + nobesTimetableLecture.getComponent() + " "
                + nobesTimetableLecture.getSect();

        lectureVO.setLecName(lectureName);
        lectureVO.setSection(nobesTimetableLecture.getSect());

        String mon = nobesTimetableLecture.getMon();
        String tues = nobesTimetableLecture.getTues();
        String wed = nobesTimetableLecture.getWed();
        String thurs = nobesTimetableLecture.getThrus();
        String fri = nobesTimetableLecture.getFri();

        ArrayList<String> lecs = new ArrayList<>();
        HashMap<String, String> weekdays = new HashMap<>();
        weekdays.put("MON", mon);
        weekdays.put("TUE", tues);
        weekdays.put("WED", wed);
        weekdays.put("THU", thurs);
        weekdays.put("FRI", fri);

        weekdays.forEach((key, value) -> {
            if (value.equals("Y")) {
                lecs.add(key);
            }
        });

        String hrsFrom = nobesTimetableLecture.getHrsFrom();
        String hrsTo = nobesTimetableLecture.getHrsTo();

        if (hrsFrom.equals(hrsTo)) {
            return null;
        }

        String hrs = hrsFrom + "-" + hrsTo;


        for (int i = 0; i < lecs.size(); i++) {
            lecs.set(i, lecs.get(i) + "_" + hrs);
        }

        lectureVO.setTimes(lecs);

        return lectureVO;
    }


    public LabVO getLabObj(NobesTimetableLab nobesTimetableLab) {

        LabVO labVO = OrikaUtils.convert(nobesTimetableLab, LabVO.class);

        String labName = nobesTimetableLab.getSubject()
                + nobesTimetableLab.getCatalog() + " "
                + nobesTimetableLab.getComponent() + " "
                + nobesTimetableLab.getSect();

        String sect = nobesTimetableLab.getSect();

        String mon = nobesTimetableLab.getMon();
        String tues = nobesTimetableLab.getTues();
        String wed = nobesTimetableLab.getWed();
        String thurs = nobesTimetableLab.getThrus();
        String fri = nobesTimetableLab.getFri();

        ArrayList<String> labs = new ArrayList<>();
        HashMap<String, String> weekdays = new HashMap<>();
        weekdays.put("MON", mon);
        weekdays.put("TUE", tues);
        weekdays.put("WED", wed);
        weekdays.put("THU", thurs);
        weekdays.put("FRI", fri);

        weekdays.forEach((key, value) -> {
            if (value.equals("Y")) {
                labs.add(key);
            }
        });

        String hrsFrom = nobesTimetableLab.getHrsFrom();
        String hrsTo = nobesTimetableLab.getHrsTo();

        if (hrsFrom.equals(hrsTo)) {
            return null;
        }

        String hrs = hrsFrom + "-" + hrsTo;


        for (int i = 0; i < labs.size(); i++) {
            labs.set(i, labs.get(i) + "_" + hrs);
        }

        labVO.setTimes(labs);
        labVO.setLabName(labName);
        labVO.setSection(sect);

        return labVO;
    }


    public SemVO getSemObj(NobesTimetableSem nobesTimetableSem) {
        SemVO semVO = OrikaUtils.convert(nobesTimetableSem, SemVO.class);

        String labName = nobesTimetableSem.getSubject()
                + nobesTimetableSem.getCatalog() + " "
                + nobesTimetableSem.getComponent() + " "
                + nobesTimetableSem.getSect();


        String sect = nobesTimetableSem.getSect();

        String mon = nobesTimetableSem.getMon();
        String tues = nobesTimetableSem.getTues();
        String wed = nobesTimetableSem.getWed();
        String thurs = nobesTimetableSem.getThrus();
        String fri = nobesTimetableSem.getFri();

        ArrayList<String> sems = new ArrayList<>();
        HashMap<String, String> weekdays = new HashMap<>();
        weekdays.put("MON", mon);
        weekdays.put("TUE", tues);
        weekdays.put("WED", wed);
        weekdays.put("THU", thurs);
        weekdays.put("FRI", fri);

        weekdays.forEach((key, value) -> {
            if (value.equals("Y")) {
                sems.add(key);
            }
        });

        String hrsFrom = nobesTimetableSem.getHrsFrom();
        String hrsTo = nobesTimetableSem.getHrsTo();

        if (hrsFrom.equals(hrsTo)) {
            return null;
        }

        String hrs = hrsFrom + "-" + hrsTo;


        for (int i = 0; i < sems.size(); i++) {
            sems.set(i, sems.get(i) + "_" + hrs);
        }

        semVO.setTimes(sems);
        semVO.setSemName(labName);
        semVO.setSection(sect);

        return semVO;
    }

}