/*
 * Copyright 2015 Ayuget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
