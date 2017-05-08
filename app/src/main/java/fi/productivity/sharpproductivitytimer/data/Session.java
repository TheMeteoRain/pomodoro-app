package fi.productivity.sharpproductivitytimer.data;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * A class representation of session.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public class Session {

    /**
     * Number of minutes session lasted if session was interrupted.
     */
    private int minutes;
    /**
     * Number of seconds session lasted if session was interrupted.
     */
    private int seconds;
    /**
     * Number of minutes session lasted if it completed.
     */
    private int pomodoroTime;
    /**
     * Number of minutes session's break time lasted if it completed.
     */
    private int breakTime;
    /**
     * Date value in milliseconds.
     */
    private long date;
    /**
     * True if session was interrupted.
     */
    private boolean stopped;

    /**
     * Session was not interrupted. Minutes and seconds set to zero.
     *
     * @param pomodoroTime pomodoro minutes.
     * @param breakTime break time minutes.
     * @param date date in milliseconds.
     */
    public Session(int pomodoroTime, int breakTime, long date) {
        this.minutes = 0;
        this.seconds = 0;
        this.pomodoroTime = pomodoroTime;
        this.breakTime = breakTime;
        this.date = date;
        this.stopped = false;
    }

    /**
     * Session was interrupted. Manually set leftover minutes and seconds.
     *
     * @param minutes leftover minutes.
     * @param seconds leftover seconds.
     * @param pomodoroTime pomodoro minutes.
     * @param breakTime break time minutes.
     * @param date date in milliseconds.
     */
    public Session(int minutes, int seconds, int pomodoroTime, int breakTime, long date) {
        this.minutes = minutes;
        this.seconds = seconds;
        this.pomodoroTime = pomodoroTime;
        this.breakTime = breakTime;
        this.date = date;
        this.stopped = true;
    }

    /**
     * Get json object from session.
     *
     * @return json object representation of sessions class.
     */
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

    /**
     * Get session json object as a string.
     *
     * @return session as a string.
     */
    public String toJson() {
        return json().toString();
    }

    /**
     * Get session object as a json.
     *
     * @return session json object.
     */
    public JSONObject toJsonObject() {
        return json();
    }
}
