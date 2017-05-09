package fi.productivity.sharpproductivitytimer.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fi.productivity.sharpproductivitytimer.utils.Debug;
import fi.productivity.sharpproductivitytimer.utils.Utils;


/**
 * Calculates and parses user data to usable Data objects.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public class DataHandler {

    /**
     * Filename where to save data.
     */
    private final static String filename = "data.json";
    /**
     * File information as a json array.
     */
    private static JSONArray data;

    /**
     * Today's total statistics.
     */
    private Data today;
    /**
     * This week's total statistics.
     */
    private Data week;
    /**
     * This week's statistics broken down to weekdays.
     */
    private List<Data> weekly;
    /**
     * All statistics.
     */
    private Data total;

    /**
     * Starting time of current day as milliseconds.
     */
    private long todayStart;
    /**
     * Ending time of current day as milliseconds.
     */
    private long todayEnd;
    /**
     * Starting time of current week as milliseconds.
     */
    private long firstDayOfTheWeekStart;
    /**
     * Ending time of current week as milliseconds.
     */
    private long lastDayOfTheWeekEnd;


    /**
     * Reads data file, initializes data and calculate statistics.
     *
     * @param context
     */
    public DataHandler(Context context) {
        read(context);
        initialize();
        calculateStatistics();
    }

    /**
     * Initializes data objects.
     */
    private void initialize() {
        today = new Data();
        week = new Data();
        total = new Data();
        weekly = new ArrayList<>();

        Calendar cal = Utils.getCalendarToday();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        todayStart = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        todayEnd = cal.getTimeInMillis();
        cal = Utils.getCalendarThisWeek();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        firstDayOfTheWeekStart = cal.getTimeInMillis();

        for (int i = 0; i < 7; i++) {
            Data data = new Data();
            data.setTime(cal.getTimeInMillis());
            weekly.add(data);

            if (i != 6) {
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
        }

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        lastDayOfTheWeekEnd = cal.getTimeInMillis();
        System.out.println(todayStart);
        System.out.println(todayEnd);
        System.out.println(firstDayOfTheWeekStart);
        System.out.println(lastDayOfTheWeekEnd);
    }

    /**
     * Arranges data statistics to their own categories.
     */
    private void calculateStatistics() {
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject object = data.getJSONObject(i);
                long time = object.getLong("date");

                if (todayStart <= time && time <= todayEnd) {
                    calculateStats(today, object);
                }

                if (firstDayOfTheWeekStart <= time && time <= lastDayOfTheWeekEnd) {
                    calculateStats(week, object);
                    weeklyStats(object);
                }

                calculateStats(total, object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculate statistics to weekdays.
     *
     * @param object object to whom extract data.
     */
    private void weeklyStats(JSONObject object) {
        try {
            long time = object.getLong("date");
            int minutes = object.getInt("minutes");
            int seconds = object.getInt("seconds");
            int pomodoroTime = object.getInt("pomodoroTime");
            int breakTime = object.getInt("breakTime");
            boolean stopped = object.getBoolean("stopped");

            Calendar cal = Utils.getCalendarThisWeek();
            cal.setTimeInMillis(firstDayOfTheWeekStart);
            boolean done = false;
            for (int i = 0; i < 7 && !done; i++) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long dayStart = cal.getTimeInMillis();
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);
                long dayEnd = cal.getTimeInMillis();

                if (dayStart <= time && time <= dayEnd) {
                    Data data = weekly.get(i);

                    data.setTime(dayStart);
                    data.setSessionsTotal(data.getSessionsTotal() + 1);
                    data.setBreakTimeMinutes(data.getBreakTimeMinutes() + breakTime);

                    if (minutes == 0 && seconds == 0) {
                        data.setPomodoroTimeMinutes(data.getPomodoroTimeMinutes() + pomodoroTime);
                    } else {
                        data.setPomodoroTimeSeconds(data.getPomodoroTimeSeconds() + seconds);
                    }

                    weekly.set(i, data);
                    done = true;
                }

                cal.add(Calendar.DATE, 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Calculate stats to given data object.
     *
     * @param data object to save data.
     * @param object object to whom extract data.
     */
    private void calculateStats(Data data, JSONObject object) {
        try {
            int minutes = object.getInt("minutes");
            int seconds = object.getInt("seconds");
            int pomodoroTime = object.getInt("pomodoroTime");
            int breakTime = object.getInt("breakTime");
            boolean stopped = object.getBoolean("stopped");

            data.setSessionsTotal(data.getSessionsTotal() + 1);
            data.setBreakTimeMinutes(data.getBreakTimeMinutes() + breakTime);

            if (minutes == 0 && seconds == 0) {
                data.setPomodoroTimeMinutes(data.getPomodoroTimeMinutes() + pomodoroTime);
            } else {
                //data.setPomodoroTimeMinutes(data.getPomodoroTimeMinutes() + (pomodoroTime - minutes));
                data.setPomodoroTimeSeconds(data.getPomodoroTimeSeconds() + seconds);
            }

            if (!stopped) {
                data.setSessionsCompleted(data.getSessionsCompleted() + 1);
            } else {
                data.setSessionsStopped(data.getSessionsStopped() + 1);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save data to file.
     *
     * @param context
     * @param session
     */
    public static void save(Context context, Session session) {
        Debug.print("DataHandler", "SAVE", 3, false, context);
        data = read(context);
        data.put(session.toJsonObject());

        try (FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(data.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Read data from file.
     *
     * @param context
     * @return file data.
     */
    public static JSONArray read(Context context) {
        Debug.print("DataHandler", "READ", 3, false, context);
        data = new JSONArray();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(context.openFileInput(filename)))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                data = new JSONArray(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Debug.print("DataHandler", data.toString(), 2, false, context);
        return data;
    }

    /**
     * Get file data.
     *
     * @return file data.
     */
    public JSONArray getData() {
        return data;
    }

    /**
     * Get statistics from today.
     *
     * @return today's statistics.
     */
    public Data getToday() {
        return today;
    }

    /**
     * Get statistics from this week.
     *
     * @return this week's statistics.
     */
    public Data getWeek() {
        return week;
    }

    /**
     * Get total statistics.
     *
     * @return total statistics.
     */
    public Data getTotal() {
        return total;
    }

    /**
     * Get this week's statistics parsed to weekdays.
     *
     * @return this week's weekdays statistics.
     */
    public List<Data> getWeekly() {
        return weekly;
    }

    /**
     * Get first day of the week.
     *
     * @return first day of the week.
     */
    public long getFirstDayOfTheWeekStart() {
        return firstDayOfTheWeekStart;
    }

    /**
     * Get last day of the week.
     *
     * @return last day of the week.
     */
    public long getLastDayOfTheWeekEnd() {
        return lastDayOfTheWeekEnd;
    }
}
