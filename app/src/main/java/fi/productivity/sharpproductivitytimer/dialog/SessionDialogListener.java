package fi.productivity.sharpproductivitytimer.dialog;

import android.support.v4.app.DialogFragment;

/**
 * Created by Akash on 10-Apr-17.
 */

public interface SessionDialogListener {
    void onDialogStartBreak();
    void onDialogSkipBreak();
    void onDialogClose();
}
