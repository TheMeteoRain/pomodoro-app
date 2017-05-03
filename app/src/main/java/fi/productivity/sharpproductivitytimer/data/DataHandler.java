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
 * Created by Akash on 10-Apr-17.
 */

public class DataHandler {

    private final static String filename = "data.json";
    private static JSONArray data;

    private Data today;
    private Data week;
    private List<Data> weekly;
    private Data total;

    private long todayStart;
    private long todayEnd;
    private long firstDayOfTheWeekStart;
    private long lastDayOfTheWeekEnd;


    public DataHandler(Context context) {
        read(context);
        initialize();
        calculateStatistics();
    }

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

    private void calculateStatistics() {
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject object = data.getJSONObject(i);
                long time = object.getLong("date");

                if (todayStart <= time && time <= todayEnd) {
                   // System.out.println("DAY " + todayStart + " <= " + time + " && " + time + " <= " + todayEnd);
                    calculateStats(today, object);
                }

                if (firstDayOfTheWeekStart <= time && time <= lastDayOfTheWeekEnd) {
                  //  System.out.println("WEEK " + firstDayOfTheWeekStart + " <= " + time + " && " + time + " <= " + lastDayOfTheWeekEnd);
                    calculateStats(week, object);
                    weeklyStats(weekly, object);
                }

                calculateStats(total, object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void weeklyStats(List<Data> weekly, JSONObject object) {
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

    public JSONArray getData() {
        return data;
    }

    public Data getToday() {
        return today;
    }

    public Data getWeek() {
        return week;
    }

    public Data getTotal() {
        return total;
    }

    public List<Data> getWeekly() {
        return weekly;
    }

    public long getFirstDayOfTheWeekStart() {
        return firstDayOfTheWeekStart;
    }

    public long getLastDayOfTheWeekEnd() {
        return lastDayOfTheWeekEnd;
    }
}
