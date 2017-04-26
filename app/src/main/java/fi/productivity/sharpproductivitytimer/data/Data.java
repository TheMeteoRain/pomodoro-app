package fi.productivity.sharpproductivitytimer.data;

/**
 * Created by Akash on 23-Apr-17.
 */

public class Data {
    private int sessionsTotal;
    private int sessionsCompleted;
    private int sessionsStopped;
    private int pomodoroTimeMinutes;
    private int pomodoroTimeSeconds;
    private int breakTime;

    public Data() {
        sessionsTotal = 0;
        sessionsCompleted = 0;
        sessionsStopped = 0;
        pomodoroTimeMinutes = 0;
        pomodoroTimeSeconds = 0;
        breakTime = 0;
    }

    public int getSessionsTotal() {
        return sessionsTotal;
    }

    public void setSessionsTotal(int sessionsTotal) {
        this.sessionsTotal = sessionsTotal;
    }

    public int getSessionsCompleted() {
        return sessionsCompleted;
    }

    public void setSessionsCompleted(int sessionsCompleted) {
        this.sessionsCompleted = sessionsCompleted;
        this.sessionsStopped = this.sessionsTotal - this.sessionsCompleted;
    }

    public int getPomodoroTimeMinutes() {
        return pomodoroTimeMinutes;
    }

    public void setPomodoroTimeMinutes(int pomodoroTime) {
        this.pomodoroTimeMinutes = pomodoroTime;
    }

    public int getPomodoroTimeSeconds() {
        return pomodoroTimeSeconds;
    }

    public void setPomodoroTimeSeconds(int pomodoroTimeSeconds) {
        this.pomodoroTimeSeconds = pomodoroTimeSeconds;
        if (this.pomodoroTimeSeconds >= 60 && this.pomodoroTimeSeconds % 60 != 0) {
            this.pomodoroTimeMinutes++;
            this.pomodoroTimeSeconds = this.pomodoroTimeSeconds % 60;
        }
    }

    public int getBreakTime() {
        return breakTime;
    }

    public void setBreakTime(int breakTime) {
        this.breakTime = breakTime;
    }

    public int getSessionsStopped() {
        return sessionsStopped;
    }
}
