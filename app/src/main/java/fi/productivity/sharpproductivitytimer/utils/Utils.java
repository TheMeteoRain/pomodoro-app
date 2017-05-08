package fi.productivity.sharpproductivitytimer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import java.util.Calendar;
import java.util.Locale;

import fi.productivity.sharpproductivitytimer.R;


/**
 * Contains general methods.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public class Utils {
    /**
     * Get current calendar day.
     *
     * @return today.
     */
    public static Calendar getCalendarToday() {
        Calendar cal = Calendar.getInstance(Locale.GERMANY);
        return cal;
    }

    /**
     * Get current day, first day of the week set to monday.
     *
     * @return today.
     */
    public static Calendar getCalendarThisWeek() {
        Calendar cal = Calendar.getInstance(Locale.GERMANY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return cal;
    }

    /**
     * Format time to display as mm:ss.
     *
     * @param resources
     * @param minutes minutes to format
     * @param seconds seconds to format
     * @return formatted text
     */
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

    /**
     * Format time to display as (text): hh:mm:ss.
     *
     * @param resources
     * @param stringId text to include in
     * @param hours hours to format
     * @param minutes minutes to format
     * @param seconds seconds to format
     * @return formatted text
     */
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

    /**
     * Save user's un answered dialogue.
     *
     * @param context
     * @param answerWaiting true if user has not or has not yet answered to dialogue.
     */
    public static void saveDialogPendingAnswer(Context context, boolean answerWaiting) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("dialogPendingAnswer", answerWaiting);
        editor.apply();
    }
}
