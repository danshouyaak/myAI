package com.myAI.myAI.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
 
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;
import java.util.*;
 
/**
 * 日期工具类
 *
 */
public class DateUtil {
    /**
     * 日期格式器--年
     */
    public static final String DATE_FORMAT_YEAR = "yyyy";
    /**
     * 日期格式器
     */
    public static final String DATE_FORMAT_MONTH = "yyyy-MM";
    /**
     * 日期格式器
     */
    public static final String DATE_FORMAT_MONTH_DAY = "MM-dd";
    /**
     * 日期格式器
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    /**
     * 日期格式器
     */
    public static final String DATE_FORMAT_HH = "yyyy-MM-dd HH";
    /**
     * 日期格式器
     */
    public static final String DATE_FORMAT_MM = "yyyy-MM-dd HH:mm";
    /**
     * 时间格式器
     */
    public static final String TIME_FORMAT = "HH:mm:ss";
    /**
     * 日期时间格式器
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * 日期时间格式器
     */
    public static final String DATETIME_FORMAT_1 = "HH:mm:ss 00:00:00";
    /**
     * 日期戳
     */
    public static final String DATE_STAMP = "yyyyMMdd";
    /**
     * 时间戳
     */
    public static final String TIME_STAMP = "HHmmssSSS";
    /**
     * 时间戳（精确到秒）
     */
    public static final String TIME_STAMP_SECOND = "HHmmss";
    /**
     * 日期时间戳
     */
    public static final String DATETIME_STAMP = "yyyyMMddHHmmssSSS";
    /**
     * 日期时间戳（精确到秒）
     */
    public static final String DATETIME_STAMP_SECOND = "yyyyMMddHHmmss";
    /**
     * UTC时间戳
     */
    public static final SimpleDateFormat UTC_TIME_STAMP_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    /**
     * UTC时间格式化器
     */
    public static final SimpleDateFormat UTC_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    /**
     * GMT时间格式化器
     */
    public static final SimpleDateFormat GMT_FORMAT =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    public static final Integer HALF_YEAR = 6;
 
    static {
        UTC_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        UTC_TIME_STAMP_FORMAT.setTimeZone(TimeZone.getTimeZone("UTC"));
        GMT_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
 
    /**
     * 天
     */
    public static final String DAY_STR = "天";
    /**
     * 小时
     */
    public static final String HOUR_STR = "小时";
    /**
     * 分钟
     */
    public static final String MINUTE_STR = "分钟";
 
    /**
     * 将字符串解析成yyyy-MM-dd的日期
     *
     * @param value the value
     * @return the date
     */
    public static Date parseDate(String value) {
        try {
            return new SimpleDateFormat(DATE_FORMAT, Locale.CHINA).parse(value);
        } catch (ParseException e) {
            return new Date();
        }
    }
 
    /**
     * 将字符串解析成HH:mm:ss的时间
     *
     * @param value the value
     * @return the date
     */
    public static Date parseTime(String value) {
        try {
            return new SimpleDateFormat(TIME_FORMAT, Locale.CHINA).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
 
    /**
     * 将字符串解析成yyyy-MM-dd HH:mm:ss的日期时间
     *
     * @param value the value
     * @return the date
     */
    public static Date parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new SimpleDateFormat(DATETIME_FORMAT, Locale.CHINA).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
 
    /**
     * 将字符串解析成自定义格式的日期时间
     *
     * @param value  the value
     * @param format the format
     * @return the date
     */
    public static Date parseDateTime(String value, String format) {
        try {
            return new SimpleDateFormat(format, Locale.CHINA).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
 
    /**
     * 将字符串解析成yyyyMMddHHmmssSSS的日期时间
     *
     * @param value the value
     * @return the date
     */
    public static Date parseDateTimeStamp(String value) {
        try {
            return new SimpleDateFormat(DATETIME_STAMP, Locale.CHINA).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
 
    /**
     * 将字符串解析成yyyyMMddHHmmss的日期时间
     *
     * @param value the value
     * @return the date
     */
    public static Date parseDateTimeSecond(String value) {
        try {
            return new SimpleDateFormat(DATETIME_STAMP_SECOND, Locale.CHINA).parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
 
    /**
     * 将时间解析成yyyy-MM-dd的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_FORMAT, Locale.CHINA).format(date);
    }
 
    /**
     * 将时间解析成yyyy的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatDateToYear(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_FORMAT_YEAR, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期解析成HH:mm:ss的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(TIME_FORMAT, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期时间解析成yyyy-MM-dd HH:mm:ss的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATETIME_FORMAT, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期解析成yyyyMMddHHmmssSSS的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatDateTimestamp(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATETIME_STAMP, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期解析成yyyyMMddHHmmss的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatDateTimeSecond(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATETIME_STAMP_SECOND, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期解析成yyyyMMdd的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatDatestamp(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_STAMP, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期解析成HHmmssSSS的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatTimestamp(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(TIME_STAMP, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期解析成yyyy-MM-dd HH的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatDateHH(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_FORMAT_HH, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期解析成yyyy-MM-dd HH:mm的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatDateMM(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_FORMAT_MM, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期解析成yyyy-MM的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatYearMonth(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_FORMAT_MONTH, Locale.CHINA).format(date);
    }
 
    /**
     * 将日期解析成MM-dd的字符串
     *
     * @param date the date
     * @return the string
     */
    public static String formatMonthDay(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_FORMAT_MONTH_DAY, Locale.CHINA).format(date);
    }
 
    /**
     * 获取当天的开始时间, 如:2020-06-24 00:00:00
     *
     * @return the begin of date
     */
    public static Date getBeginOfDate() {
        return getBeginOfDate(null);
    }
 
    /**
     * 获取当天的结束时间, 如:2020-06-24 23:59:59
     *
     * @return the end of date
     */
    public static Date getEndOfDate() {
        return getEndOfDate(null);
    }
 
    /**
     * 获取指定日期的开始时间, 如:2020-06-25 00:00:00
     *
     * @param date the date
     * @return the begin of date
     */
    public static Date getBeginOfDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        return setDayToBegin(calendar).getTime();
    }
 
    /**
     * 获取指定日期的结束时间, 如:2020-02-25 23:59:59
     *
     * @param date the date
     * @return the end of date
     */
    public static Date getEndOfDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        return setDayToEnd(calendar).getTime();
    }
 
    /**
     * 获取当年天数.
     *
     * @return days
     */
    public static int getDaysInYear() {
        GregorianCalendar calendar = new GregorianCalendar();
        return calendar.isLeapYear(calendar.get(Calendar.YEAR)) ? 366 : 365;
    }
 
    /**
     * 格式化当前日期.
     *
     * @return date型时间 sys date of date
     */
    public static Date getSysDateOfDate() {
        return parseDateTime(formatDateTime(new Date()));
    }
 
    /**
     * 获取指定日期.
     *
     * @param calendarType Calendar.DATE、Calendar.MONTH、Calendar.YEAR等
     * @param num          对应日期类型对应的数量
     * @return 指定date型时间 比如：离现在30天前的日期、获取上个月日期
     */
    public static Date getDefineDate(int calendarType, int num) {
        Calendar cal = Calendar.getInstance();
        cal.add(calendarType, num);
        return cal.getTime();
    }
 
    /**
     * 获取天数差.
     *
     * @param specifiedDate 指定的时间，即需要比较的时间
     * @param largeDate     到期的时间，即比较大的时间
     * @return 天数差 计算指定时间与当前时间的天数差
     */
    public static int getDaysDifference(Date specifiedDate, Date largeDate) {
        Calendar cNow = Calendar.getInstance();
        Calendar cReturnDate = Calendar.getInstance();
        cNow.setTime(largeDate);
        cReturnDate.setTime(specifiedDate);
        setDayToBegin(cNow);
        setDayToBegin(cReturnDate);
        long todayMs = cNow.getTimeInMillis();
        long returnMs = cReturnDate.getTimeInMillis();
        long intervalMs = todayMs - returnMs;
        return (int) (intervalMs / (1000 * 86400));
    }
 
    /**
     * 比较日期.
     *
     * @param referenceDate 需要比较的时间 不能为空(null),需要正确的日期格式
     * @param date          被比较的时间 为空(null)则为当前时间
     * @param type          返回值类型,0为多少天，1为多少个月，2为多少年
     * @return the int
     * @throws ParseException the parse exception
     */
    public static int compareDate(String referenceDate, String date, int type)
            throws ParseException {
        int n = 0;
        String formatStyle = type == 1 ? DATE_FORMAT_MONTH : DATE_FORMAT;
 
        String dateStr = date;
        if (dateStr == null) {
            dateStr = formatDate(new Date());
        }
 
        DateFormat df = new SimpleDateFormat(formatStyle);
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(df.parse(referenceDate));
        c2.setTime(df.parse(dateStr));
 
        // 循环对比，直到相等，n 就是所要的结果
        while (!c1.after(c2)) {
            n++;
            if (type == 1) {
                // 比较月份，月份+1
                c1.add(Calendar.MONTH, 1);
            } else {
                // 比较天数，日期+1
                c1.add(Calendar.DATE, 1);
            }
        }
 
        n = n - 1;
 
        if (type == 2) {
            n = n / 365;
        }
 
        return n;
    }
 
    /**
     * 获取当前时间之前 /之后多少年/月/日的时间.
     *
     * @param format   日期格式
     * @param variable 正数代表时间之后，负数代表时间之前
     * @param field    单位，年，月，日
     * @return 时间
     * @see #getBeforeDays
     */
    @Deprecated
    public static String getDiffDate(String format, int variable, int field) {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.CHINA);
        c.set(field, c.get(field) + variable);
        return formatter.format(c.getTime());
    }
 
    /**
     * 获取指定时间相加天数后的时间.
     *
     * @param date   传入相加时间
     * @param field  时间格式
     * @param amount 日期或时间的数量
     * @return 相加后的时间 date
     * @see #getAfterDays
     */
    @Deprecated
    public static Date dateAdd(Date date, int field, int amount) {
        Calendar ca = Calendar.getInstance();
        if (date != null) {
            ca.setTime(date);
        }
        ca.add(field, amount);
        return ca.getTime();
    }
 
    /**
     * 获取当前时间相加天数的时间.
     *
     * @param field  时间格式
     * @param amount 相加的时间天数
     * @return 相加后的时间 date
     */
    public static Date dateAdd(int field, int amount) {
        return dateAdd(null, field, amount);
    }
 
    /**
     * 时间范围
     */
    public static class DateRange {
        /**
         * 开始时间
         */
        private Date begin;
        /**
         * 结束时间
         */
        private Date end;
 
        /**
         * Instantiates a new Date range.
         */
        public DateRange() {
        }
 
        /**
         * Instantiates a new Date range.
         *
         * @param begin the begin
         * @param end   the end
         */
        public DateRange(Date begin, Date end) {
            this.begin = begin;
            this.end = end;
        }
 
        /**
         * Gets begin.
         *
         * @return the begin
         */
        public Date getBegin() {
            return begin;
        }
 
        /**
         * Gets end.
         *
         * @return the end
         */
        public Date getEnd() {
            return end;
        }
    }
 
    /**
     * 获取指定日期的当天时间范围，如(2020-12-09 00:00:00 2020-12-12 23:59:59)
     *
     * @param date 时间,为空表示当前时间
     * @return 时间范围 day range
     */
    public static DateRange getDayRange(Date date) {
        DateRange range = new DateRange();
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
 
        range.begin = setDayToBegin(calendar).getTime();
        range.end = setDayToEnd(calendar).getTime();
 
        return range;
    }
 
    /**
     * 获取指定日期本周的时间范围，如(2020-12-14 00:00:00 2020-12-20 23:59:59)
     *
     * @param date 时间,为空表示当前时间
     * @return 时间范围 week range
     */
    public static DateRange getWeekRange(Date date) {
        DateRange range = new DateRange();
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
 
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        range.begin = setDayToBegin(calendar).getTime();
 
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 6);
        range.end = setDayToEnd(calendar).getTime();
 
        return range;
    }
 
    /**
     * 获取指定日期本月的时间范围，如(2020-12-01 00:00:00 2020-12-31 23:59:59)
     *
     * @param date 时间,为空表示当前时间
     * @return 时间范围 month range
     */
    public static DateRange getMonthRange(Date date) {
        DateRange range = new DateRange();
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        // 获取第一天
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        range.begin = setDayToBegin(calendar).getTime();
        // 获取最后一天
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 1);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
        range.end = setDayToEnd(calendar).getTime();
 
        return range;
    }
 
    /**
     * 获取指定日期本季度的时间范围,如(2020-10-01 00:00:00 2020-12-31 23:59:59)
     *
     * @param date 时间,为空表示当前时间
     * @return 时间范围 season range
     */
    public static DateRange getSeasonRange(Date date) {
        DateRange range = new DateRange();
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
 
        int month = calendar.get(Calendar.MONTH);
        calendar.set(Calendar.MONTH, month - (month % 3));
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        range.begin = setDayToBegin(calendar).getTime();
 
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + 3);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
        range.end = setDayToEnd(calendar).getTime();
 
        return range;
    }
 
    /**
     * 获取指定日期本半年的时间范围,如(2020-06-01 00:00:00 2020-12-31 23:59:59)
     *
     * @param date 时间,为空表示当前时间
     * @return 时间范围 half year range
     */
    public static DateRange getHalfYearRange(Date date) {
        DateRange range = new DateRange();
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
        int current = calendar.get(Calendar.MONTH) + 1;
        int month = Calendar.JANUARY;
        if (current > HALF_YEAR) {
            month = Calendar.JULY;
        }
        calendar.set(calendar.get(Calendar.YEAR), month, 1, 0, 0, 0);
        range.begin = calendar.getTime();
 
        calendar.set(Calendar.MONTH, month + 6);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
        range.end = setDayToEnd(calendar).getTime();
 
        return range;
    }
 
    /**
     * 获取指定日期本年的时间范围,如(2020-01-01 00:00:00 2020-12-31 23:59:59)
     *
     * @param date 时间,为空表示当前时间
     * @return 时间范围 year range
     */
    public static DateRange getYearRange(Date date) {
        DateRange range = new DateRange();
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
        }
 
        calendar.set(calendar.get(Calendar.YEAR), Calendar.JANUARY, 1, 0, 0, 0);
        range.begin = calendar.getTime();
 
        calendar.set(Calendar.MONTH, 12);
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - 1);
        range.end = setDayToEnd(calendar).getTime();
 
        return range;
    }
 
    /**
     * 获取指定年份本年的日期范围,如(2020-01-01 2020-12-31)
     *
     * @param year 年份,为空表示当前时间
     * @return 时间范围 year range
     */
    public static DateRange getYearDateRange(@NonNull String year) {
        DateRange range = new DateRange();
        String start = year + "-01-01";
        String end = year + "-12-31";
        range.begin = parseDate(start);
        range.end = parseDate(end);
        return range;
    }
 
    private static Calendar setDayToEnd(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return calendar;
    }
 
    private static Calendar setDayToBegin(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar;
    }
 
    /**
     * 判断时间date1是否在时间date2之前
     *
     * @param date1 时间
     * @param date2 时间
     * @return the boolean
     */
    public static boolean isDateBefore(String date1, String date2) {
        return Objects.requireNonNull(parseDateTime(date1)).before(parseDateTime(date2));
    }
 
    /**
     * 验证当前时间是否在指定时间范围内(例如9：00-22：30)
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return bool boolean
     */
    public static boolean isRangeTime(String startTime, String endTime) {
        boolean isRangeTime = false;
        String nowDay = formatDate(new Date());
        String start = startTime, end = endTime;
        start = nowDay + " " + start + ":00";
        end = nowDay + " " + end + ":00";
        // 开始时间不在结束时间前，表示时间进行了跨天设置
        if (!isDateBefore(start, end)) {
            // 跨天设置的时间需要当前时间大于结束时间并且小于开始时间
            if (isDateBefore(formatDateTime(new Date()), end)
                    || isDateBefore(start, formatDateTime(new Date()))) {
                isRangeTime = true;
            }
        } else {
            if (isDateBefore(formatDateTime(new Date()), end)
                    && isDateBefore(start, formatDateTime(new Date()))) {
                isRangeTime = true;
            }
        }
        return isRangeTime;
    }
 
    /**
     * utc时间转为date
     *
     * @param value "2004-08-04T19:09:02.768Z";
     * @return date date
     */
    public static Date parseUTCDateTimeStamp(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return UTC_TIME_STAMP_FORMAT.parse(value);
        } catch (ParseException e) {
            return null;
        }
    }
 
    /**
     * Format utc date time stamp string.
     *
     * @param date the date
     * @return the string
     */
    public static String formatUTCDateTimeStamp(Date date) {
        return UTC_TIME_STAMP_FORMAT.format(date);
    }
 
    /**
     * Format utc date time stamp string.
     *
     * @param dateStr yyyy-MM-dd HH:mm:ss
     * @return utc时间 string
     */
    public static String formatUTCDateTimeStamp(String dateStr) {
        Date d = parseDateTime(dateStr);
        return UTC_TIME_STAMP_FORMAT.format(d);
    }
 
    /**
     * 把日期解析为UTC时间 @param dates the dates
     *
     * @param dates the dates
     * @return the string
     */
    public static String formatUTCDate(Date... dates) {
        Date date = new Date();
        if (dates != null && dates.length > 0) {
            date = dates[0];
        }
        return UTC_FORMAT.format(date);
    }
 
    /**
     * 把日期解析为GMT时间 @param date the date
     *
     * @param date the date
     * @return the string
     */
    public static String formatGMTDate(Date date) {
        return GMT_FORMAT.format(date);
    }
 
    /**
     * 获取今天是一周的星期几
     *
     * @return day of week
     */
    public static int getDayOfWeek() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        // 星期天对应1，星期一从2开始
        if (day == 1) {
            return 7;
        }
        return day - 1;
    }
 
    /**
     * 获取某年某月的最后一天
     *
     * @param year  年
     * @param month 月
     * @return 最后一天 last day of month
     */
    public static Date getLastDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DATE));
        return cal.getTime();
    }
 
    /**
     * 获取某年某月的第一天
     *
     * @param year  年
     * @param month 月
     * @return 第一天 first day of month
     */
    public static Date getFirstDayOfMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getMinimum(Calendar.DATE));
        return cal.getTime();
    }
 
    /**
     * 获取当前时间
     *
     * @return the current time
     */
    public static String getCurrentTime() {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
 
    // ============================ 日期操作=============================
 
    /**
     * 获取开始日期n天前的日期
     *
     * @param date           开始日期为空，则初始化一个当前时间
     * @param daysToSubtract n
     * @return the before days
     */
    public static Date getBeforeDays(Date date, @NotNull Long daysToSubtract) {
        if (Objects.isNull(date)) {
            date = new Date();
        }
        return localDate2date(date2LocalDate(date).minusDays(daysToSubtract));
    }
 
    public static Date beforeDays(Date date, @NotNull Long daysToSubtract) {
        if (Objects.isNull(date)) {
            date = new Date();
        }
        return localDateTime2Date(date2LocalDateTime(date).minusDays(daysToSubtract));
    }
 
    /**
     * 获取开始日期n天后的日期.
     *
     * @param date           开始日期为空，则初始化一个当前时间
     * @param daysToSubtract the days to subtract
     * @return the after days
     */
    public static Date getAfterDays(Date date, @NotNull Long daysToSubtract) {
        if (Objects.isNull(date)) {
            date = new Date();
        }
        return localDate2date(date2LocalDate(date).plusDays(daysToSubtract));
    }
 
    /**
     * Gets before weeks.
     *
     * @param date            the date
     * @param weeksToSubtract the weeks to subtract
     * @return the before weeks
     */
    public static Date getBeforeWeeks(Date date, @NotNull Long weeksToSubtract) {
        if (Objects.isNull(date)) {
            date = new Date();
        }
        return localDate2date(date2LocalDate(date).minusWeeks(weeksToSubtract));
    }
 
    /**
     * Gets before months.
     *
     * @param date             the date
     * @param monthsToSubtract the months to subtract
     * @return the before months
     */
    public static Date getBeforeMonths(Date date, @NotNull Long monthsToSubtract) {
        if (Objects.isNull(date)) {
            date = new Date();
        }
        return localDate2date(date2LocalDate(date).minusMonths(monthsToSubtract));
    }
 
    /**
     * Gets before years.
     *
     * @param date            the date
     * @param yearsToSubtract the years to subtract
     * @return the before years
     */
    public static Date getBeforeYears(Date date, @NotNull Long yearsToSubtract) {
        if (Objects.isNull(date)) {
            date = new Date();
        }
        return localDate2date(date2LocalDate(date).minusMonths(yearsToSubtract));
    }
 
    /**
     * Date 2 local date time local date time.
     *
     * @param date the date
     * @return the local date time
     */
    public static LocalDateTime date2LocalDateTime(@NotNull Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }
 
    /**
     * Date 2 local date local date.
     *
     * @param date the date
     * @return the local date
     */
    public static LocalDate date2LocalDate(@NotNull Date date) {
        return date2LocalDateTime(date).toLocalDate();
    }
 
    /**
     * Local date time 2 date date.
     *
     * @param localDateTime the local date time
     * @return the date
     */
    public static Date localDateTime2Date(@NotNull LocalDateTime localDateTime) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }
 
    /**
     * Local date 2 date date.
     *
     * @param localDate the local date
     * @return the date
     */
    public static Date localDate2date(@NotNull LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }
 
    /**
     * Local date time 2 string string.
     *
     * @param date the date
     * @param fmt  the fmt
     * @return the string
     */
    public static String localDateTime2String(LocalDateTime date, DateTimeFormatter fmt) {
        if (Objects.isNull(fmt)) {
            fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
        }
        return date.format(fmt);
    }
 
    /**
     * 日期间隔.
     * Period类表示年、月、日时间间隔
     *
     * @param startDateInclusive 开始日期
     * @param endDateExclusive   结束日期
     * @param unit               年/月/日
     * @return 间隔 long
     */
    public static long between(
            LocalDate startDateInclusive, LocalDate endDateExclusive, TemporalUnit unit) {
        return Period.between(startDateInclusive, endDateExclusive).get(unit);
    }
 
    public static long between(Date start, Date end, TemporalUnit unit) {
        return between(date2LocalDateTime(start), date2LocalDateTime(end), unit);
    }
 
    /**
     * 时间间隔.
     * LocalDate、LoclaTime、LocalDateTime等都实现了Temporal接口
     * Duration是一个表示秒/纳秒的时间间隔。即Period基于日期值，而Duration基于时间值。 这两个类是为了日期、时间的度量服务的。<br>
     *
     * @param startInclusive the start inclusive
     * @param endExclusive   the end exclusive
     * @param unit           the unit
     * @return long long
     */
    public static long between(Temporal startInclusive, Temporal endExclusive, TemporalUnit unit) {
        return Duration.between(startInclusive, endExclusive).get(unit);
    }
 
    /**
     * 获取今天最小时间
     *
     * @return local date time
     */
    public static LocalDateTime todayMin() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
    }
 
    /**
     * Day min local date time.
     *
     * @param day the day
     * @return the local date time
     */
    public static LocalDateTime dayMin(LocalDate day) {
        return LocalDateTime.of(day, LocalTime.MIN);
    }
 
    /**
     * 获取今天最大时间
     *
     * @return local date time
     */
    public static LocalDateTime todayMax() {
        return LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
    }
 
    /**
     * Day max local date time.
     *
     * @param day the day
     * @return the local date time
     */
    public static LocalDateTime dayMax(LocalDate day) {
        return LocalDateTime.of(day, LocalTime.MAX);
    }
 
 
    /**
     * 获取某年某个季度的开始时间
     *
     * @param year    指定年份
     * @param quarter 指定季度（1,2,3,4）
     * @return date
     */
    public static Date getQuarterStartTime(final int year, final int quarter) {
        Calendar c = Calendar.getInstance();
        if (quarter <= 0 || quarter > 4) {
            return null;
        } else if (quarter == 1) {
            c.set(year, Calendar.JANUARY, 1);
        } else if (quarter == 2) {
            c.set(year, Calendar.APRIL, 1);
        } else if (quarter == 3) {
            c.set(year, Calendar.JULY, 1);
        } else {
            c.set(year, Calendar.OCTOBER, 1);
        }
        return c.getTime();
    }
 
    /**
     * 获取某年某个季度的结束时间
     *
     * @param year    指定年份
     * @param quarter 指定季度（1,2,3,4）
     * @return date
     */
    public static Date getQuarterEndTime(final int year, final int quarter) {
        Calendar c = Calendar.getInstance();
        if (quarter <= 0 || quarter > 4) {
            return null;
        } else if (quarter == 1) {
            c.set(year, Calendar.MARCH, 31);
        } else if (quarter == 2) {
            c.set(year, Calendar.JUNE, 30);
        } else if (quarter == 3) {
            c.set(year, Calendar.SEPTEMBER, 30);
        } else {
            c.set(year, Calendar.DECEMBER, 31);
        }
        return c.getTime();
    }
 
}