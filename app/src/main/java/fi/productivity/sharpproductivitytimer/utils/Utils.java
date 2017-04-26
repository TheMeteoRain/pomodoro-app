package fi.productivity.sharpproductivitytimer.utils;

import java.util.Calendar;
import java.util.TimeZone;

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

        return cal;
    }
}
