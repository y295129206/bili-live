package com.bililive.utils;

import com.bililive.Enum.DateTimePatternEnum;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 日期工具类（线程安全版）
 */
public class DateUtils {

    /**
     * 格式化 Date 对象为指定格式的字符串
     * @param date    要格式化的日期（null 返回空串）
     * @param pattern 日期格式模板（如 yyyyMMdd）
     * @return 格式化后的字符串
     */
    public static String format(Date date, String pattern) {
        // 空值处理
        if (Objects.isNull(date) || Objects.isNull(pattern)) {
            return "";
        }
        // 每次创建新的 SimpleDateFormat 实例，避免线程安全问题
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        // 格式化日期
        return sdf.format(date);
    }

    public static String getBeforDayDate(Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -day);
        return format(calendar.getTime(), DateTimePatternEnum.YYYYMMDD.getPattern());
    }
    public static String getBeforDayDate2(Integer day) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -day);
        return format(calendar.getTime(), DateTimePatternEnum.YYYY_MM_DD.getPattern());
    }
    // 测试方法
    public static void main(String[] args) {
        System.out.println(getBeforDayDate(1)); // 示例输出：20260313
    }

    public static List<String> getBeforDates(Integer  day) {
        LocalDate endDate = LocalDate.now();
        List< String> dateList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = day; i > 0; i--) {
            dateList.add(endDate.minusDays(i).format(formatter));
        }
        return dateList;
    }
}