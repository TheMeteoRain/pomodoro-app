package fi.productivity.sharpproductivitytimer.debug;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import fi.productivity.sharpproductivitytimer.R;

public class Debug {
    private static int LEVEL;
    private static int DURATION = Toast.LENGTH_SHORT;

    public static void loadDebug(Context host) {
        LEVEL = Integer.parseInt(host.getResources().getString(R.string.debugLevel));
    }

    public static void print(String tag, String message, int level, boolean showInUi, Context host) {
        if (LEVEL >= level && showInUi) {
            Toast toast = Toast.makeText(host, message, DURATION);
            toast.show();
        } else if (LEVEL >= level) {
            Log.d(tag, message);
        }
    }
}