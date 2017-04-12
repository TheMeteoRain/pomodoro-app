package fi.productivity.sharpproductivitytimer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fi.productivity.sharpproductivitytimer.dialog.SessionDialogListener;
import fi.productivity.sharpproductivitytimer.dialog.SessionFirstDialog;
import fi.productivity.sharpproductivitytimer.dialog.SessionSecondDialog;

public class MainActivity extends AppCompatActivity implements SessionDialogListener {

    private boolean clockSoundOn;
    private boolean continuous;
    private int pomodoroTime;
    private int breakTime;
    private int seconds;
    private int minutes;
    private boolean isBounded;

    private TextView timerText;
    private Button timerButton;

    private Intent timerIntent;
    private TimerService timerService;
    private TimerReceiver timerReceiver;
    private TimerConnection timerConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isBounded = false;
        pomodoroTime = 1;
        breakTime = 1;
        clockSoundOn = false;
        continuous = false;

        timerText = (TextView) findViewById(R.id.timerText);
        timerButton = (Button) findViewById(R.id.timerButton);

        timerConnection = new TimerConnection();
        timerReceiver = new TimerReceiver();
        timerIntent = new Intent(MainActivity.this, TimerService.class);
        reset();
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(timerReceiver, new IntentFilter("fi.productivity.sharpproductivitytimer.MainActivity"));
        bindService(timerIntent, timerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(timerReceiver);

        if (isBounded) {
            unbindService(timerConnection);
            isBounded = false;
        }
    }

    public void startTimer(View view) {
        if (!timerService.isRunning()) {
            if (!isBounded) {
                bindService(timerIntent, timerConnection, Context.BIND_AUTO_CREATE);
            }
            timerIntent.putExtra("pomodoroTime", pomodoroTime);
            timerIntent.putExtra("breakTime", breakTime);
            timerIntent.putExtra("clockSound", clockSoundOn);
            timerIntent.putExtra("continuous", continuous);
            startService(timerIntent);
            timerButton.setText(R.string.timer_button_stop);
        } else {
            reset();
            if (isBounded) {
                unbindService(timerConnection);
                isBounded = false;
            }
            stopService(timerIntent);
            timerButton.setText(R.string.timer_button_start);
        }
    }

    private void reset() {
        if (pomodoroTime < 10) {
            timerText.setText(String.format(getResources().getStringArray(R.array.pomodoro_time)[1], pomodoroTime));
        } else {
            timerText.setText(String.format(getResources().getStringArray(R.array.pomodoro_time)[0], pomodoroTime));
        }
    }

    //// TODO: 10-Apr-17
    public void notifyUser() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(1, mBuilder.build());
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        timerService.breakTimer();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        timerService.pomodoroTimer();
    }

    @Override
    public void onDialogNeutralClick(DialogFragment dialog) {
        reset();
        if (isBounded) {
            unbindService(timerConnection);
            isBounded = false;
        }
        stopService(timerIntent);
        timerButton.setText(R.string.timer_button_start);
    }


    private class TimerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                if (extras.get("seconds") != null && extras.get("minutes") != null) {
                    seconds = extras.getInt("seconds");
                    minutes = extras.getInt("minutes");

                    if (seconds == 0) {
                        notifyUser();
                    }

                    printTimer();
                }

                if (extras.get("continuous") != null && extras.get("firstDialog") != null) {
                    boolean continuous = extras.getBoolean("continuous");
                    boolean firstDialog = extras.getBoolean("firstDialog");
                    if (!continuous) {
                        if (firstDialog) {
                            SessionFirstDialog dialog = new SessionFirstDialog();
                            dialog.setCancelable(false);
                            dialog.show(getSupportFragmentManager(), "session");
                        } else {
                            SessionSecondDialog dialog = new SessionSecondDialog();
                            dialog.setCancelable(false);
                            dialog.show(getSupportFragmentManager(), "break");
                        }
                    }
                }
            }
        }

        private void printTimer() {
            if (minutes < 10) {
                if (seconds < 10) {
                    timerText.setText(String.format(getResources().getStringArray(R.array.pomodoro_time)[5], minutes, seconds));
                } else {
                    timerText.setText(String.format(getResources().getStringArray(R.array.pomodoro_time)[3], minutes, seconds));
                }
            } else {
                if (seconds < 10) {
                    timerText.setText(String.format(getResources().getStringArray(R.array.pomodoro_time)[4], minutes, seconds));
                } else {
                    timerText.setText(String.format(getResources().getStringArray(R.array.pomodoro_time)[2], minutes, seconds));
                }
            }
        }
    }

    private class TimerConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            timerService = binder.getService();
            isBounded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBounded = false;
        }
    }
}
