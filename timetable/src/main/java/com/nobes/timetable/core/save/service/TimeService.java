package com.nobes.timetable.core.save.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class TimeService {
    public static String getTime(Cell cell) {

        String format;

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
            format = dateFormat.format(date);
        } else {
            format = null;
        }
        return format;
    }
}