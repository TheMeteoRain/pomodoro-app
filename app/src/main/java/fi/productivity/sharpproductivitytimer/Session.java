package fi.productivity.sharpproductivitytimer;

/**
 * Created by Akash on 10-Apr-17.
 */

public class Session {

    private int pomodoroTime;
    private int breakTime;
    private long date;

    public Session() {

    }

    public Session(int pomodoroTime, long date) {
        this.pomodoroTime = pomodoroTime;
        this.date = date;
    }

    public Session(int pomodoroTime, int breakTime, long date) {
        this.pomodoroTime = pomodoroTime;
        this.breakTime = breakTime;
        this.date = date;
    }

    public int getPomodoroTime() {
        return pomodoroTime;
    }

    public void setPomodoroTime(int pomodoroTime) {
        this.pomodoroTime = pomodoroTime;
    }

    public int getBreakTime() {
        return breakTime;
    }

    public void setBreakTime(int breakTime) {
        this.breakTime = breakTime;
    }

    public long getlong() {
        return date;
    }

    public void setlong(long date) {
        this.date = date;
    }
    
    public String toJson() {
        return "{\"pomodoroTime\": " + pomodoroTime + ", \"breakTime\": " + breakTime + ", \"date\": \"" + date + "\"}";
    }
}
