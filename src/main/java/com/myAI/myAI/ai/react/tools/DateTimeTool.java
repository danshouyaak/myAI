package com.myAI.myAI.ai.react.tools;

import com.myAI.myAI.ai.react.ReactTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

/**
 * 时间日期工具
 * 提供时间日期相关的查询、计算、格式化功能
 */
@Component
@Slf4j
public class DateTimeTool implements ReactTool {
    
    @Override
    public String getName() {
        return "DATETIME";
    }
    
    @Override
    public String getDescription() {
        return "时间日期工具，支持当前时间查询、日期计算、格式转换、时区转换等操作";
    }
    
    @Override
    public String execute(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                return getCurrentDateTime();
            }
            
            String[] parts = input.split("\\|");
            String operation = parts[0].trim().toLowerCase();
            
            switch (operation) {
                case "now":
                case "当前时间":
                    return getCurrentDateTime();
                case "format":
                case "格式化":
                    return formatDateTime(parts);
                case "add":
                case "加":
                    return addDateTime(parts);
                case "subtract":
                case "减":
                    return subtractDateTime(parts);
                case "diff":
                case "差值":
                    return calculateDifference(parts);
                case "timezone":
                case "时区":
                    return convertTimezone(parts);
                case "parse":
                case "解析":
                    return parseDateTime(parts);
                default:
                    return getCurrentDateTime();
            }
            
        } catch (Exception e) {
            log.error("时间日期处理失败", e);
            return "错误：时间日期处理失败 - " + e.getMessage();
        }
    }
    
    @Override
    public String getParameterDescription() {
        return "支持的操作格式：\n" +
               "- now/当前时间: 获取当前时间\n" +
               "- format|日期|格式: 格式化日期时间\n" +
               "- add|日期|数量|单位: 日期加法运算\n" +
               "- subtract|日期|数量|单位: 日期减法运算\n" +
               "- diff|日期1|日期2|单位: 计算两个日期的差值\n" +
               "- timezone|日期时间|源时区|目标时区: 时区转换\n" +
               "- parse|日期字符串: 解析日期字符串\n" +
               "单位支持: years, months, days, hours, minutes, seconds\n" +
               "示例: 'add|2024-01-01|7|days'";
    }
    
    @Override
    public boolean isAvailable() {
        return true;
    }
    
    /**
     * 获取当前日期时间
     */
    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        ZonedDateTime zonedNow = ZonedDateTime.now();
        
        return String.format(
            "当前时间信息:\n" +
            "- 本地时间: %s\n" +
            "- 时区: %s\n" +
            "- UTC时间: %s\n" +
            "- 时间戳: %d\n" +
            "- 星期: %s",
            now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            zonedNow.getZone().getId(),
            zonedNow.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            zonedNow.toInstant().toEpochMilli(),
            now.getDayOfWeek().toString()
        );
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(String[] parts) {
        if (parts.length < 3) {
            return "错误：格式化需要提供日期和格式，例如：format|2024-01-01 12:00:00|yyyy年MM月dd日";
        }
        
        try {
            String dateStr = parts[1].trim();
            String pattern = parts[2].trim();
            
            LocalDateTime dateTime = parseInputDateTime(dateStr);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            
            return String.format("格式化结果: %s", dateTime.format(formatter));
        } catch (Exception e) {
            return "错误：日期格式化失败 - " + e.getMessage();
        }
    }
    
    /**
     * 日期加法运算
     */
    private String addDateTime(String[] parts) {
        if (parts.length < 4) {
            return "错误：日期加法需要提供日期、数量和单位，例如：add|2024-01-01|7|days";
        }
        
        try {
            String dateStr = parts[1].trim();
            long amount = Long.parseLong(parts[2].trim());
            String unit = parts[3].trim().toLowerCase();
            
            LocalDateTime dateTime = parseInputDateTime(dateStr);
            LocalDateTime result = addToDateTime(dateTime, amount, unit);
            
            return String.format(
                "日期加法结果:\n" +
                "- 原始日期: %s\n" +
                "- 加上: %d %s\n" +
                "- 结果: %s",
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                amount, unit,
                result.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        } catch (Exception e) {
            return "错误：日期加法失败 - " + e.getMessage();
        }
    }
    
    /**
     * 日期减法运算
     */
    private String subtractDateTime(String[] parts) {
        if (parts.length < 4) {
            return "错误：日期减法需要提供日期、数量和单位，例如：subtract|2024-01-01|7|days";
        }
        
        try {
            String dateStr = parts[1].trim();
            long amount = Long.parseLong(parts[2].trim());
            String unit = parts[3].trim().toLowerCase();
            
            LocalDateTime dateTime = parseInputDateTime(dateStr);
            LocalDateTime result = addToDateTime(dateTime, -amount, unit);
            
            return String.format(
                "日期减法结果:\n" +
                "- 原始日期: %s\n" +
                "- 减去: %d %s\n" +
                "- 结果: %s",
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                amount, unit,
                result.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );
        } catch (Exception e) {
            return "错误：日期减法失败 - " + e.getMessage();
        }
    }
    
    /**
     * 计算两个日期的差值
     */
    private String calculateDifference(String[] parts) {
        if (parts.length < 3) {
            return "错误：计算差值需要提供两个日期，例如：diff|2024-01-01|2024-01-08";
        }
        
        try {
            String date1Str = parts[1].trim();
            String date2Str = parts[2].trim();
            String unit = parts.length > 3 ? parts[3].trim().toLowerCase() : "days";
            
            LocalDateTime date1 = parseInputDateTime(date1Str);
            LocalDateTime date2 = parseInputDateTime(date2Str);
            
            long difference = calculateDifferenceInUnit(date1, date2, unit);
            
            return String.format(
                "日期差值计算结果:\n" +
                "- 日期1: %s\n" +
                "- 日期2: %s\n" +
                "- 差值: %d %s",
                date1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                date2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                difference, unit
            );
        } catch (Exception e) {
            return "错误：日期差值计算失败 - " + e.getMessage();
        }
    }
    
    /**
     * 时区转换
     */
    private String convertTimezone(String[] parts) {
        if (parts.length < 4) {
            return "错误：时区转换需要提供日期时间、源时区和目标时区，例如：timezone|2024-01-01 12:00:00|Asia/Shanghai|America/New_York";
        }
        
        try {
            String dateTimeStr = parts[1].trim();
            String sourceZone = parts[2].trim();
            String targetZone = parts[3].trim();
            
            LocalDateTime localDateTime = parseInputDateTime(dateTimeStr);
            ZonedDateTime sourceZonedDateTime = localDateTime.atZone(ZoneId.of(sourceZone));
            ZonedDateTime targetZonedDateTime = sourceZonedDateTime.withZoneSameInstant(ZoneId.of(targetZone));
            
            return String.format(
                "时区转换结果:\n" +
                "- 源时间: %s (%s)\n" +
                "- 目标时间: %s (%s)",
                sourceZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                sourceZone,
                targetZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                targetZone
            );
        } catch (Exception e) {
            return "错误：时区转换失败 - " + e.getMessage();
        }
    }
    
    /**
     * 解析日期字符串
     */
    private String parseDateTime(String[] parts) {
        if (parts.length < 2) {
            return "错误：解析日期需要提供日期字符串，例如：parse|2024-01-01 12:00:00";
        }
        
        try {
            String dateStr = parts[1].trim();
            LocalDateTime dateTime = parseInputDateTime(dateStr);
            
            return String.format(
                "日期解析结果:\n" +
                "- 输入: %s\n" +
                "- 解析结果: %s\n" +
                "- 年: %d\n" +
                "- 月: %d\n" +
                "- 日: %d\n" +
                "- 时: %d\n" +
                "- 分: %d\n" +
                "- 秒: %d\n" +
                "- 星期: %s",
                dateStr,
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                dateTime.getYear(),
                dateTime.getMonthValue(),
                dateTime.getDayOfMonth(),
                dateTime.getHour(),
                dateTime.getMinute(),
                dateTime.getSecond(),
                dateTime.getDayOfWeek().toString()
            );
        } catch (Exception e) {
            return "错误：日期解析失败 - " + e.getMessage();
        }
    }
    
    /**
     * 解析输入的日期时间字符串
     */
    private LocalDateTime parseInputDateTime(String dateStr) {
        // 尝试多种常见格式
        String[] patterns = {
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm",
            "yyyy-MM-dd",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy/MM/dd HH:mm",
            "yyyy/MM/dd",
            "MM/dd/yyyy HH:mm:ss",
            "MM/dd/yyyy HH:mm",
            "MM/dd/yyyy"
        };
        
        for (String pattern : patterns) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                if (pattern.contains("HH:mm")) {
                    return LocalDateTime.parse(dateStr, formatter);
                } else {
                    return LocalDate.parse(dateStr, formatter).atStartOfDay();
                }
            } catch (DateTimeParseException ignored) {
                // 继续尝试下一个格式
            }
        }
        
        throw new IllegalArgumentException("无法解析日期格式: " + dateStr);
    }
    
    /**
     * 向日期时间添加指定单位的时间
     */
    private LocalDateTime addToDateTime(LocalDateTime dateTime, long amount, String unit) {
        switch (unit) {
            case "years":
            case "年":
                return dateTime.plusYears(amount);
            case "months":
            case "月":
                return dateTime.plusMonths(amount);
            case "days":
            case "日":
            case "天":
                return dateTime.plusDays(amount);
            case "hours":
            case "小时":
                return dateTime.plusHours(amount);
            case "minutes":
            case "分钟":
                return dateTime.plusMinutes(amount);
            case "seconds":
            case "秒":
                return dateTime.plusSeconds(amount);
            default:
                throw new IllegalArgumentException("不支持的时间单位: " + unit);
        }
    }
    
    /**
     * 计算两个日期在指定单位下的差值
     */
    private long calculateDifferenceInUnit(LocalDateTime date1, LocalDateTime date2, String unit) {
        switch (unit) {
            case "years":
            case "年":
                return ChronoUnit.YEARS.between(date1, date2);
            case "months":
            case "月":
                return ChronoUnit.MONTHS.between(date1, date2);
            case "days":
            case "日":
            case "天":
                return ChronoUnit.DAYS.between(date1, date2);
            case "hours":
            case "小时":
                return ChronoUnit.HOURS.between(date1, date2);
            case "minutes":
            case "分钟":
                return ChronoUnit.MINUTES.between(date1, date2);
            case "seconds":
            case "秒":
                return ChronoUnit.SECONDS.between(date1, date2);
            default:
                throw new IllegalArgumentException("不支持的时间单位: " + unit);
        }
    }
}
