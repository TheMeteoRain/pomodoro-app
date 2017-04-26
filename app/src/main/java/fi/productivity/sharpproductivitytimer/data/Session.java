package fi.productivity.sharpproductivitytimer.data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Akash on 10-Apr-17.
 */

public class Session {

    private int minutes;
    private int seconds;
    private int pomodoroTime;
    private int breakTime;
    private long date;
    private boolean stopped;

    public Session() {

    }

    public Session(int pomodoroTime, int breakTime, long date, boolean stopped) {
        this.minutes = 0;
        this.seconds = 0;
        this.pomodoroTime = pomodoroTime;
        this.breakTime = breakTime;
        this.date = date;
        this.stopped = stopped;
    }

    public Session(int minutes, int seconds, int pomodoroTime, int breakTime, long date, boolean stopped) {
        this.minutes = minutes;
        this.seconds = seconds;
        this.pomodoroTime = pomodoroTime;
        this.breakTime = breakTime;
        this.date = date;
        this.stopped = stopped;
    }

    private JSONObject json() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("minutes", minutes);
            jsonObject.put("seconds", seconds);
            jsonObject.put("pomodoroTime", pomodoroTime);
            jsonObject.put("breakTime", breakTime);
            jsonObject.put("date", date);
            jsonObject.put("stopped", stopped);

            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public String toJson() {
        return json().toString();
    }

    public JSONObject toJsonObject() {
        return json();
    }
}
