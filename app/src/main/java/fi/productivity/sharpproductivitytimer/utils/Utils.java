package fi.productivity.sharpproductivitytimer.utils;

import android.content.res.Resources;

import java.util.Calendar;
import java.util.TimeZone;

import fi.productivity.sharpproductivitytimer.R;

/**
 * Created by Akash on 24-Apr-17.
 */

public class Utils {
    public static Calendar getCalendarToday() {
        TimeZone zone = TimeZone.getTimeZone("UTC");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(zone);
        //cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        return cal;
    }

    public static Calendar getCalendarThisWeek() {
        TimeZone zone = TimeZone.getTimeZone("UTC");
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(zone);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.setFirstDayOfWeek(Calendar.MONDAY);

        return cal;
    }

    public static String formatTimer(Resources resources, int minutes, int seconds) {
        String text;

        if (minutes < 10) {
            if (seconds < 10) {
                text = String.format(resources.getStringArray(R.array.pomodoro_time)[5], minutes, seconds);
            } else {
                text = String.format(resources.getStringArray(R.array.pomodoro_time)[3], minutes, seconds);
            }
        } else {
            if (seconds < 10) {
                text = String.format(resources.getStringArray(R.array.pomodoro_time)[4], minutes, seconds);
            } else {
                text = String.format(resources.getStringArray(R.array.pomodoro_time)[2], minutes, seconds);
            }
        }

        return text;
    }

    public static String formatTimerByHour(Resources resources, int stringId,int hours, int minutes, int seconds) {
        String text;

        if (minutes < 10) {
            if (seconds < 10) {
                text = String.format(resources.getStringArray(R.array.stat_time_pomodoro)[0], resources.getString(stringId), hours, minutes, seconds);
            } else {
                text = String.format(resources.getStringArray(R.array.stat_time_pomodoro)[1], resources.getString(stringId), hours, minutes, seconds);
            }
        } else {
            if (seconds < 10) {
                text = String.format(resources.getStringArray(R.array.stat_time_pomodoro)[2], resources.getString(stringId), hours, minutes, seconds);
            } else {
                text = String.format(resources.getStringArray(R.array.stat_time_pomodoro)[3], resources.getString(stringId), hours, minutes, seconds);
            }
        }


        return text;
    }
}
