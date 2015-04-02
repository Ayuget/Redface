package com.ayuget.redface.util;

import android.content.Context;

import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {
    public static Date fromHTMLDate(String year, String month, String day, String hours, String minutes, String seconds) {
        return new GregorianCalendar(
                Integer.parseInt(year),
                Integer.parseInt(month) - 1,
                Integer.parseInt(day),
                Integer.parseInt(hours),
                Integer.parseInt(minutes),
                Integer.parseInt(seconds)
        ).getTime();
    }

    public static Date fromHTMLDate(String year, String month, String day, String hours, String minutes) {
        return fromHTMLDate(year, month, day, hours, minutes, "0");
    }

    public static String formatLocale(Context context, Date d) {
        return android.text.format.DateUtils.formatDateTime(context, d.getTime(), android.text.format.DateUtils.FORMAT_NUMERIC_DATE | android.text.format.DateUtils.FORMAT_SHOW_TIME | android.text.format.DateUtils.FORMAT_SHOW_DATE | android.text.format.DateUtils.FORMAT_SHOW_YEAR);
    }
}
