package fi.productivity.sharpproductivitytimer.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import fi.productivity.sharpproductivitytimer.R;


/**
 * Session counter dialogue.
 *
 * Asked when user presses session counter in MainActivity.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public class SessionCountDialog extends DialogFragment implements SessionCountDialogListener {

    /**
     * Callback.
     */
    SessionCountDialogListener mListener;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            if (activity instanceof SessionDialogListener)
                mListener = (SessionCountDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SessionCountDialogListener");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_session_count, null))
                .setPositiveButton(R.string.dialog_session_count_button_ok, (dialog, id) -> {
                    mListener.onDialogSessionsReset();
                })
                .setNeutralButton(R.string.dialog_session_count_button_close, (dialog, id) -> {

                });
        return builder.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDialogSessionsReset() {

    }
}
