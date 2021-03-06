package fi.productivity.sharpproductivitytimer.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import fi.productivity.sharpproductivitytimer.R;


/**
 * Pomodoro dialogue.
 *
 * Asked when user completes successfully pomodoro timer.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public class SessionFirstDialog extends DialogFragment implements SessionDialogListener {

    /**
     * Callback.
     */
    SessionDialogListener mListener;

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
                mListener = (SessionDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SessionDialogListener");
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
        builder.setView(inflater.inflate(R.layout.dialog_session_pomodoro, null))
                .setNeutralButton(R.string.dialog_button_close, (dialog, id) -> {
                    mListener.onDialogClose();
                })
                .setPositiveButton(R.string.dialog_button_start, (dialog, id) -> {
                    mListener.onDialogStartBreak();
                })
                .setNegativeButton(R.string.dialog_button_skip, (dialog, id) -> {
                    mListener.onDialogSkipBreak();
                });
        return builder.create();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDialogStartBreak() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDialogSkipBreak() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDialogClose() {

    }
}
