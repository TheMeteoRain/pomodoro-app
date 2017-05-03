package fi.productivity.sharpproductivitytimer.data;

/**
 * Created by Akash on 23-Apr-17.
 */

public class Data {
    private int sessionsTotal;
    private int sessionsCompleted;
    private int sessionsStopped;
    private int pomodoroTimeHours;
    private int pomodoroTimeMinutes;
    private int pomodoroTimeSeconds;
    private int breakTimeMinutes;
    private int breakTimeHours;
    private long time;

    public Data() {
        sessionsTotal = 0;
        sessionsCompleted = 0;
        sessionsStopped = 0;
        pomodoroTimeHours = 0;
        pomodoroTimeMinutes = 0;
        pomodoroTimeSeconds = 0;
        breakTimeMinutes = 0;
        breakTimeHours = 0;
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
        if (this.pomodoroTimeMinutes > 59 && this.pomodoroTimeMinutes % 59 != 0) {
            this.pomodoroTimeHours++;
            this.pomodoroTimeMinutes = this.pomodoroTimeMinutes % 60;
        }
    }

    public int getPomodoroTimeSeconds() {
        return pomodoroTimeSeconds;
    }

    public void setPomodoroTimeSeconds(int pomodoroTimeSeconds) {
        this.pomodoroTimeSeconds = pomodoroTimeSeconds;
        if (this.pomodoroTimeSeconds > 59 && this.pomodoroTimeSeconds % 59 != 0) {
            this.pomodoroTimeMinutes++;
            this.pomodoroTimeSeconds = this.pomodoroTimeSeconds % 60;
        }
    }

    public int getBreakTimeMinutes() {
        return breakTimeMinutes;
    }

    public void setBreakTimeMinutes(int breakTimeMinutes) {
        this.breakTimeMinutes = breakTimeMinutes;
        if (this.breakTimeMinutes > 59 && this.breakTimeMinutes % 59 != 0) {
            this.breakTimeHours++;
            this.breakTimeMinutes = this.breakTimeMinutes % 60;
        }
    }

    public int getSessionsStopped() {
        return sessionsStopped;
    }

    public void setSessionsStopped(int sessionsStopped) {
        this.sessionsStopped = sessionsStopped;
    }

    public int getPomodoroTimeHours() {
        return pomodoroTimeHours;
    }

    public void setPomodoroTimeHours(int pomodoroTimeHours) {
        this.pomodoroTimeHours = pomodoroTimeHours;
    }

    public int getBreakTimeHours() {
        return breakTimeHours;
    }

    public void setBreakTimeHours(int breakTimeHours) {
        this.breakTimeHours = breakTimeHours;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
