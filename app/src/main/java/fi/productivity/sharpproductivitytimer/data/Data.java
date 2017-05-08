package fi.productivity.sharpproductivitytimer.data;


/**
 * A class representation of user data.
 *
 * Includes sessions, pomodoro and break time timers.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public class Data {
    /**
     * Total number of sessions.
     */
    private int sessionsTotal;
    /**
     * Total number of sessions completed.
     */
    private int sessionsCompleted;
    /**
     * Total number of sessions stopped.
     */
    private int sessionsStopped;
    /**
     * Total number of pomodoro hours.
     */
    private int pomodoroTimeHours;
    /**
     * Remaining pomodoro minutes.
     */
    private int pomodoroTimeMinutes;
    /**
     * Remaining pomodoro seconds.
     */
    private int pomodoroTimeSeconds;
    /**
     * Remaining break minutes.
     */
    private int breakTimeMinutes;
    /**
     * Total number of break hours.
     */
    private int breakTimeHours;
    /**
     * Date in milliseconds.
     */
    private long time;

    /**
     * Default constructor. Set all values to zero.
     */
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

    /**
     * Get total number of sessions.
     *
     * @return sessions.
     */
    public int getSessionsTotal() {
        return sessionsTotal;
    }

    /**
     * Set number of total sessions.
     *
     * @param sessionsTotal sessions number.
     */
    public void setSessionsTotal(int sessionsTotal) {
        this.sessionsTotal = sessionsTotal;
    }

    /**
     * Get total number of sessions completed.
     *
     * @return completed sessions.
     */
    public int getSessionsCompleted() {
        return sessionsCompleted;
    }

    /**
     * Set number of total completed sessions.
     * Number of stopped sessions are automatically calculated by sessions total and completed.
     *
     * @param sessionsCompleted number of sessions completed
     */
    public void setSessionsCompleted(int sessionsCompleted) {
        this.sessionsCompleted = sessionsCompleted;
        this.sessionsStopped = this.sessionsTotal - this.sessionsCompleted;
    }

    /**
     * Get remaining pomodoro minutes.
     *
     * @return pomodoro minutes.
     */
    public int getPomodoroTimeMinutes() {
        return pomodoroTimeMinutes;
    }

    /**
     * Set pomodoro minutes.
     * Pomodoro hours are automatically calculated from minutes.
     *
     * @param pomodoroTime
     */
    public void setPomodoroTimeMinutes(int pomodoroTime) {
        this.pomodoroTimeMinutes = pomodoroTime;
        if (this.pomodoroTimeMinutes > 59 && this.pomodoroTimeMinutes % 59 != 0) {
            this.pomodoroTimeHours++;
            this.pomodoroTimeMinutes = this.pomodoroTimeMinutes % 60;
        }
    }

    /**
     * Get remaining pomodoro seconds.
     *
     * @return pomodoro seconds.
     */
    public int getPomodoroTimeSeconds() {
        return pomodoroTimeSeconds;
    }

    /**
     * Set remaining pomodoro seconds.
     * Pomodoro minutes are automatically calculated from seconds.
     *
     * @param pomodoroTimeSeconds pomodoro seconds.
     */
    public void setPomodoroTimeSeconds(int pomodoroTimeSeconds) {
        this.pomodoroTimeSeconds = pomodoroTimeSeconds;
        if (this.pomodoroTimeSeconds > 59 && this.pomodoroTimeSeconds % 59 != 0) {
            this.pomodoroTimeMinutes++;
            this.pomodoroTimeSeconds = this.pomodoroTimeSeconds % 60;
        }
    }

    /**
     * Get remaining break minutes.
     *
     * @return break minutes.
     */
    public int getBreakTimeMinutes() {
        return breakTimeMinutes;
    }

    /**
     * Set remaining break minutes.
     * Break hours are automatically calculated from minutes.
     *
     * @param breakTimeMinutes break minutes.
     */
    public void setBreakTimeMinutes(int breakTimeMinutes) {
        this.breakTimeMinutes = breakTimeMinutes;
        if (this.breakTimeMinutes > 59 && this.breakTimeMinutes % 59 != 0) {
            this.breakTimeHours++;
            this.breakTimeMinutes = this.breakTimeMinutes % 60;
        }
    }

    /**
     * Get total number of sessions stopped.
     *
     * @return
     */
    public int getSessionsStopped() {
        return sessionsStopped;
    }

    /**
     * Set number of sessions stopped.
     *
     * @param sessionsStopped sessions stopped.
     */
    public void setSessionsStopped(int sessionsStopped) {
        this.sessionsStopped = sessionsStopped;
    }

    /**
     * Get total number of pomodoro hours
     *
     * @return pomodoro hours.
     */
    public int getPomodoroTimeHours() {
        return pomodoroTimeHours;
    }

    /**
     * Set number of hours pomodoro has lasted.
     *
     * @param pomodoroTimeHours pomodoro hours.
     */
    public void setPomodoroTimeHours(int pomodoroTimeHours) {
        this.pomodoroTimeHours = pomodoroTimeHours;
    }

    /**
     * Get total number of break hours.
     *
     * @return break hours.
     */
    public int getBreakTimeHours() {
        return breakTimeHours;
    }

    /**
     * Set number of hours breaks has lasted.
     *
     * @param breakTimeHours
     */
    public void setBreakTimeHours(int breakTimeHours) {
        this.breakTimeHours = breakTimeHours;
    }

    /**
     * Get time in milliseconds.
     *
     * @return milliseconds.
     */
    public long getTime() {
        return time;
    }

    /**
     * Set time in milliseconds.
     *
     * @param time milliseconds.
     */
    public void setTime(long time) {
        this.time = time;
    }
}
