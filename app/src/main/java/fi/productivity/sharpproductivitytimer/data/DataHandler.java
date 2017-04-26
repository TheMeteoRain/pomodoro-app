package fi.productivity.sharpproductivitytimer.data;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;

import fi.productivity.sharpproductivitytimer.debug.Debug;
import fi.productivity.sharpproductivitytimer.utils.Utils;

/**
 * Created by Akash on 10-Apr-17.
 */

public class DataHandler {

    private final static String filename = "data.json";
    private static JSONArray data;

    private Data today;
    private Data week;
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
        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        lastDayOfTheWeekEnd = cal.getTimeInMillis();
        System.out.println(todayStart);
        System.out.println(todayEnd);
        System.out.println(firstDayOfTheWeekStart);
        System.out.println(lastDayOfTheWeekEnd);

        today = new Data();
        week = new Data();
        total = new Data();
    }

    private void calculateStatistics() {
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject object = data.getJSONObject(i);
                long time = object.getLong("date");

                if (todayStart <= time && time <= todayEnd) {
                    System.out.println("DAY " + todayStart + " <= " + time + " && " + time + " <= " + todayEnd);
                    calculateStats(today, object);
                }

                if (firstDayOfTheWeekStart <= time && time <= lastDayOfTheWeekEnd) {
                    System.out.println("WEEK " + todayStart + " <= " + time + " && " + time + " <= " + lastDayOfTheWeekEnd);
                    calculateStats(week, object);
                }

                calculateStats(total, object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
            data.setBreakTime(data.getBreakTime() + breakTime);
            if (minutes == 0 && seconds == 0) {
                data.setPomodoroTimeMinutes(data.getPomodoroTimeMinutes() + pomodoroTime);
            } else {
                data.setPomodoroTimeMinutes(data.getPomodoroTimeMinutes() + (pomodoroTime - minutes));
                data.setPomodoroTimeSeconds(data.getPomodoroTimeSeconds() + seconds);
            }
            if (!stopped) {
                data.setSessionsCompleted(data.getSessionsCompleted() + 1);
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
}
