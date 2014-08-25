package com.tencent.tgiapp1.common;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by levin on 7/2/14.
 */
public class DateUtils {

    /**
     * 获取两个日期相差的天数，忽略时，分，秒
     * @param fDate
     * @param oDate
     * @return
     */
    public static int getDaysOfTwo(Date fDate, Date oDate) {
        Calendar aCalendar = Calendar.getInstance();
        aCalendar.setTime(fDate);
        int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
        aCalendar.setTime(oDate);
        int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
        return day2 - day1;
    }

    /**
     * 获取两个日期相隔的天数，不忽略时，分，秒
     * @param fDate
     * @param oDate
     * @return
     */
    public static int getIntervalDays(Date fDate, Date oDate) {
        if (null == fDate || null == oDate) {
            return -1;
        }
        long intervalMilli = oDate.getTime() - fDate.getTime();
        return (int) (intervalMilli / (24 * 60 * 60 * 1000));
    }
}
