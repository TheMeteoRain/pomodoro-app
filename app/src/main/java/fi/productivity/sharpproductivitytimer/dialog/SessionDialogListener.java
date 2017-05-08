package fi.productivity.sharpproductivitytimer.dialog;


/**
 * A callback interface for MainActivity, SessionFirstDialog and SessionSecondDialog.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public interface SessionDialogListener {
    /**
     * Start break timer.
     */
    void onDialogStartBreak();

    /**
     * Skip break and start pomodoro timer.
     */
    void onDialogSkipBreak();

    /**
     * Destroy Time Service and do nothing.
     */
    void onDialogClose();
}
