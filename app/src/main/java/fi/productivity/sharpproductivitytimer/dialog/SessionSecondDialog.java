package fi.productivity.sharpproductivitytimer.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

import fi.productivity.sharpproductivitytimer.R;

/**
 * Created by Akash on 10-Apr-17.
 */

public class SessionSecondDialog extends DialogFragment implements SessionDialogListener {

    SessionDialogListener mListener;

    @Override
    public void onStart() {
        super.onStart();
        Activity activity = getActivity();

        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (SessionDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.dialog_session, null))
                .setNeutralButton(R.string.dialog_button_close, (dialog, id) -> {
                    mListener.onDialogNeutralClick(SessionSecondDialog.this);
                })
                .setPositiveButton(R.string.dialog_button_start, (dialog, id) -> {
                    mListener.onDialogPositiveClick(SessionSecondDialog.this);
                });
        return builder.create();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {

    }
}
