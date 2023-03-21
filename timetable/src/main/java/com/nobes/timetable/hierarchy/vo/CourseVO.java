package com.nobes.timetable.hierarchy.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseVO {

    private String CourseName;

    private String AcadOrg;

    private String Term;

    private String subject;

    private String Catalog;

    private String Descr;

    private String ApprovedHrs;

    private String description;

    private ArrayList prerequisite;

    private ArrayList corequisite;

    private HashMap<String, Double> AU_Count;
}
