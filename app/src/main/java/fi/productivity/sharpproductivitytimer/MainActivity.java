package fi.productivity.sharpproductivitytimer;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import fi.productivity.sharpproductivitytimer.dialog.SessionDialogListener;
import fi.productivity.sharpproductivitytimer.dialog.SessionFirstDialog;
import fi.productivity.sharpproductivitytimer.dialog.SessionSecondDialog;

public class MainActivity extends AppCompatActivity implements SessionDialogListener {

    private boolean backgroundNotification;
    private boolean clockSoundOn;
    private boolean continuous;
    private int pomodoroTime;
    private int breakTime;
    private int seconds;
    private int minutes;
    private int longBreakTime;
    private int sessionsTillLongBreak;
    private boolean isBounded;
    private boolean paused;
    private boolean firstDialog;

    private TextView timerText;
    private Button timerButtonPlay;
    private Button timerButtonPause;
    private DialogFragment dialog;

    private Intent timerIntent;
    private TimerService timerService;
    private TimerReceiver timerReceiver;
    private TimerConnection timerConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        System.out.println("CREATE");

        loadPreferences();

        isBounded = false;
        paused = false;
        backgroundNotification = false;

        timerText = (TextView) findViewById(R.id.timerText);
        timerButtonPlay = (Button) findViewById(R.id.timerButtonPlay);
        timerButtonPause = (Button) findViewById(R.id.timerButtonPause);

        timerConnection = new TimerConnection();
        timerReceiver = new TimerReceiver();
        timerIntent = new Intent(MainActivity.this, TimerService.class);

        reset();
    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("START");
        registerReceiver(timerReceiver, new IntentFilter("fi.productivity.sharpproductivitytimer.MainActivity"));
        bindService(timerIntent, timerConnection, Context.BIND_AUTO_CREATE);

        Bundle extras;
        if ((extras = getIntent().getExtras()) != null) {
            if (extras.get("minutes") != null) {
                minutes = extras.getInt("minutes");
                System.out.println("MINUTES " + minutes);
            }
            if (extras.get("seconds") != null) {
                seconds = extras.getInt("seconds");
                System.out.println("SECONDS " + seconds);
            }
            if (extras.get("stop") != null) {
                // System.out.println("STOP");
            }
            if (extras.get("backgroundNotification") != null) {
                System.out.println("notification " + extras.getBoolean("backgroundNotification"));
                backgroundNotification = extras.getBoolean("backgroundNotification");
            }
            if (extras.get("firstDialog") != null) {
                System.out.println("FIRST");
                System.out.println(extras.getBoolean("firstDialog"));
                firstDialog = extras.getBoolean("firstDialog");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("RESUME");
        loadPreferences();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("PAUSE");

        System.out.println("MINUTES " + minutes);
        System.out.println("SECONDS " + timerService.getSeconds());
        System.out.println("RUNNING " + timerService.isRunning());
        if (timerService.isRunning() && seconds != 0) {
            timerService.backgroundNotificationOn();
            backgroundNotification = true;
        }

        closeDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("STOP");
        unregisterReceiver(timerReceiver);
        boundService();
    }

    private void boundService() {
        if (isBounded) {
            unbindService(timerConnection);
            isBounded = false;
        }
    }

    @Override
    protected void onDestroy() {
        System.out.println("DESTROY");
        timerService.backgroundNotificationOff();
        stopService(timerIntent);
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        System.out.println("LOAD");

        paused = savedInstanceState.getBoolean("paused");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        System.out.println("SAVE");
        outState.putBoolean("paused", paused);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case (R.id.action_settings):
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
        }
        return false;
    }

    private void loadPreferences() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        pomodoroTime = Integer.parseInt(settings.getString("pomodoro_time", getString(R.string.pref_default_pomodoro_time)));
        breakTime = Integer.parseInt(settings.getString("break_time", getString(R.string.pref_default_break_time)));
        longBreakTime = Integer.parseInt(settings.getString("long_break_time", getString(R.string.pref_default_long_break_time)));
        sessionsTillLongBreak = Integer.parseInt(settings.getString("sessions", getString(R.string.pref_default_sessions)));
        clockSoundOn = settings.getBoolean("clock_sound", false);
        continuous = settings.getBoolean("continuous_mode", false);
    }

    // play and stop
    private void startTimer() {
        if (!timerService.isRunning()) {
            if (!isBounded) {
                bindService(timerIntent, timerConnection, Context.BIND_AUTO_CREATE);
            }
            timerIntent.putExtra("pomodoroTime", pomodoroTime);
            timerIntent.putExtra("breakTime", breakTime);
            timerIntent.putExtra("clockSound", clockSoundOn);
            timerIntent.putExtra("continuous", continuous);
            timerIntent.putExtra("longBreakTime", longBreakTime);
            timerIntent.putExtra("sessionsTillLongBreak", sessionsTillLongBreak);
            startService(timerIntent);
            timerButtonPlay.setText(R.string.timer_button_stop);
        } else {
            printTimer();
            boundService();
            stopService(timerIntent);
            timerButtonPause.setText(R.string.timer_button_pause);
            timerButtonPlay.setText(R.string.timer_button_start);
        }
    }

    // pause and resume
    private void pauseTimer() {
        if (timerService.isRunning()) {
            if (!timerService.isPaused()) {
                paused = true;
                timerService.pauseTimer();
                timerButtonPause.setText(R.string.timer_button_resume);
            } else {
                paused = false;
                timerService.resumeTimer();
                timerButtonPause.setText(R.string.timer_button_pause);
            }
        }
    }

    public void printTimer() {
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

    private void reset() {
        System.out.println("RESET");
        System.out.println(minutes);
        minutes = pomodoroTime;
        System.out.println(minutes);
        seconds = 0;
        printTimer();
    }

    @Override
    public void onDialogStartBreak() {
        timerService.startBreak();
    }

    @Override
    public void onDialogSkipBreak() {
        timerService.startPomodoro();
    }

    @Override
    public void onDialogClose() {
        printTimer();
        boundService();
        stopService(timerIntent);
        timerButtonPlay.setText(R.string.timer_button_start);
    }

    public void timerButtonPlay(View view) {
        startTimer();
    }

    public void timerButtonPause(View view) {
        pauseTimer();
    }

    public void openDialog() {
        reset();
        if (firstDialog) {
            dialog = new SessionFirstDialog();
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), "session");
        } else {
            dialog = new SessionSecondDialog();
            dialog.setCancelable(false);
            dialog.show(getSupportFragmentManager(), "break");
        }
    }

    public void closeDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    private class TimerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras;

            if ((extras = intent.getExtras()) != null) {
                if (extras.get("seconds") != null && extras.get("minutes") != null) {
                    seconds = extras.getInt("seconds");
                    minutes = extras.getInt("minutes");

                    printTimer();
                }

                if (extras.get("continuous") != null && extras.get("firstDialog") != null) {
                    continuous = extras.getBoolean("continuous");
                    firstDialog = extras.getBoolean("firstDialog");
                    if (!continuous) {
                        System.out.println("x");
                        openDialog();
                    }
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

            if (backgroundNotification) {
                System.out.println(timerService);
                timerService.backgroundNotificationOff();
                backgroundNotification = false;
            }

            minutes = timerService.getMinutes();
            seconds = timerService.getSeconds();
            firstDialog = timerService.isPomodoroTimerOn();

            printTimer();
            System.out.println("DIALOGS");
            System.out.println(minutes);
            System.out.println(seconds);
            if (minutes == 0 && seconds == 0) {
                System.out.println(5);
                openDialog();
            }

            if (timerService.isPaused()) {
                timerButtonPause.setText(R.string.timer_button_resume);
            } else {
                timerButtonPause.setText(R.string.timer_button_pause);
            }

            if (!timerService.isRunning()) {
                timerButtonPlay.setText(R.string.timer_button_start);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBounded = false;
        }
    }
}
