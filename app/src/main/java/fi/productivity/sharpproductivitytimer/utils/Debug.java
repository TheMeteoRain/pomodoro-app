package fi.productivity.sharpproductivitytimer.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import fi.productivity.sharpproductivitytimer.R;

/**
 * Debug helper class.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public class Debug {

    /**
     * Debug level. Smaller means less debug information.
     */
    private static int LEVEL;

    /**
     * Length of debug toast.
     */
    private static int DURATION = Toast.LENGTH_SHORT;

    /**
     * Load debuggable number.
     *
     * @param host
     */
    public static void loadDebug(Context host) {
        LEVEL = Integer.parseInt(host.getResources().getString(R.string.debugLevel));
    }

    /**
     * Prints to a console or to in-app toasts about debug.
     *
     * @param tag from which class the debug came from.
     * @param message information about the debug.
     * @param level on what level does the debug falls to.
     * @param showInUi show in-app.
     * @param host
     */
    public static void print(String tag, String message, int level, boolean showInUi, Context host) {
        if (LEVEL >= level && showInUi) {
            Toast toast = Toast.makeText(host, message, DURATION);
            toast.show();
        } else if (LEVEL >= level) {
            Log.d(tag, message);
        }
    }
}