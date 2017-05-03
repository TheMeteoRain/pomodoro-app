package fi.productivity.sharpproductivitytimer;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import fi.productivity.sharpproductivitytimer.utils.Debug;
import fi.productivity.sharpproductivitytimer.dialog.SessionDialogListener;
import fi.productivity.sharpproductivitytimer.dialog.SessionFirstDialog;
import fi.productivity.sharpproductivitytimer.dialog.SessionSecondDialog;
import fi.productivity.sharpproductivitytimer.service.LocalBinder;
import fi.productivity.sharpproductivitytimer.service.TimerService;
import fi.productivity.sharpproductivitytimer.setting.SettingsActivity;
import fi.productivity.sharpproductivitytimer.utils.Utils;

public class MainActivity extends AppCompatActivity implements SessionDialogListener {

    public static int pomodoroTime;
    public static int sessionCount;
    public static int sessionsTillLongBreak;
    public static boolean pomodoroSoundOn;
    public static boolean breakSoundOn;
    public static boolean continuous;

    private boolean backgroundNotification;
  //  private boolean pomodoroSoundOn;
    //private boolean continuous;
   // private int pomodoroTime;
    private int breakTime;
    private int seconds;
    private int minutes;
    private int longBreakTime;
   // private int sessionsTillLongBreak;
    private boolean isBounded;
    private boolean paused;
    private boolean firstDialog;

    private ProgressBar timerProgress;
    private TextView timerText;
    private TextView timerTitle;
    private Button timerButtonPlay;
    private Button timerButtonPause;
    private DialogFragment dialog;
    private MenuItem sessions;

    private Intent timerIntent;
    private TimerService timerService;
    private TimerReceiver timerReceiver;
    private TimerConnection timerConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Debug.loadDebug(this);
        Debug.print("MainActivity", "CREATE", 3, false, this);

        loadPreferences();

        isBounded = false;
        paused = false;
        backgroundNotification = false;

        timerTitle = (TextView) findViewById(R.id.timerTitle);
        timerProgress = (ProgressBar) findViewById(R.id.timerProgress);
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
        Debug.print("MainActivity", "START", 3, false, this);
        registerReceiver(timerReceiver, new IntentFilter("fi.productivity.sharpproductivitytimer.MainActivity"));
        bindService(timerIntent, timerConnection, Context.BIND_AUTO_CREATE);

        Bundle extras;
        if ((extras = getIntent().getExtras()) != null) {
            if (extras.get("minutes") != null) {
                minutes = extras.getInt("minutes");
                Debug.print("MainActivity", "MINUTES " + minutes, 2, false, this);
            }
            if (extras.get("seconds") != null) {
                seconds = extras.getInt("seconds");
                Debug.print("MainActivity", "SECONDS " + seconds, 2, false, this);
            }
            if (extras.get("stop") != null) {
                // Debug.print("MainActivity", "STOP");
            }
            if (extras.get("backgroundNotification") != null) {
                Debug.print("MainActivity", "notification " + extras.getBoolean("backgroundNotification"), 2, false, this);
                backgroundNotification = extras.getBoolean("backgroundNotification");
            }
            if (extras.get("firstDialog") != null) {
                Debug.print("MainActivity", "FIRST", 3, false, this);
                Debug.print("MainActivity", "BOOLEAN" + extras.getBoolean("firstDialog"), 2, false, this);
                firstDialog = extras.getBoolean("firstDialog");
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Debug.print("MainActivity", "RESUME", 3, false, this);
        loadPreferences();
        closeNotification();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Debug.print("MainActivity", "PAUSE", 3, false, this);

        Debug.print("MainActivity", "MINUTES " + minutes, 2, false, this);
        Debug.print("MainActivity", "SECONDS " + timerService.getSeconds(), 2, false, this);
        Debug.print("MainActivity", "RUNNING " + timerService.isRunning(), 2, false, this);
        if (timerService.isRunning() && seconds != 0) {
            timerService.backgroundNotificationOn();
            backgroundNotification = true;
        }

        closeNotification();
        closeDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Debug.print("MainActivity", "STOP", 3, false, this);
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
        Debug.print("MainActivity", "DESTROY", 3, false, this);
        timerService.backgroundNotificationOff();
        stopService(timerIntent);
        super.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Debug.print("MainActivity", "LOAD", 3, false, this);

        paused = savedInstanceState.getBoolean("paused");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Debug.print("MainActivity", "SAVE", 3, false, this);
        outState.putBoolean("paused", paused);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Debug.print("MainActivity", "MENU CREATE", 3, false, this);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Debug.print("MainActivity", "MENU PREPARE", 3, false, this);
        sessions = menu.findItem(R.id.action_sessions);
        updateSessionCounter(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Debug.print("MainActivity", "MENU ITEM SELECTED", 3, false, this);
        switch (item.getItemId()) {
            case (R.id.action_settings):
                Intent intentSetting = new Intent(this, SettingsActivity.class);
                startActivity(intentSetting);
                return true;
            case (R.id.action_sessions):
                sessionCount = 0;
                updateSessionCounter(true);
                return true;
            case (R.id.action_stats):
                Intent intentStat = new Intent(this, StatActivity.class);
                startActivity(intentStat);
        }
        return false;
    }

    private void closeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(TimerService.PUSH_NOTIFICATION_ID);
    }

    private void loadPreferences() {
        Debug.print("MainActivity", "LOAD PREFERENCES", 2, false, this);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        pomodoroTime = Integer.parseInt(settings.getString("pomodoro_time", getString(R.string.pref_default_pomodoro_time)));
        breakTime = Integer.parseInt(settings.getString("break_time", getString(R.string.pref_default_break_time)));
        longBreakTime = Integer.parseInt(settings.getString("long_break_time", getString(R.string.pref_default_long_break_time)));
        sessionsTillLongBreak = Integer.parseInt(settings.getString("sessions_till_long_break", getString(R.string.pref_default_sessions)));
        pomodoroSoundOn = settings.getBoolean("pomodoro_clock_sound", false);
        breakSoundOn = settings.getBoolean("break_clock_sound", false);
        continuous = settings.getBoolean("continuous_mode", false);
        sessionCount = settings.getInt("sessionCount", 0);
    }

    private void turnProgressOn() {
        timerProgress.setVisibility(View.VISIBLE);
    }

    private void turnProgressOff() {
        timerProgress.setVisibility(View.GONE);
    }

    private void turnTimerTitleOn() {
        timerTitle.setVisibility(View.VISIBLE);

        if (timerService.isPomodoroTimerOn()) {
            timerTitle.setText(R.string.timer_title_pomodoro);
        } else if (sessionCount % sessionsTillLongBreak == 0) {
            timerTitle.setText(R.string.timer_title_long_break);
        } else {
            timerTitle.setText(R.string.timer_title_break);
        }
    }

    private void turnTimerTitleOff() {
        timerTitle.setVisibility(View.INVISIBLE);
    }

    // play and stop
    private void startTimer() {
        if (!timerService.isRunning()) {
            if (!isBounded) {
                bindService(timerIntent, timerConnection, Context.BIND_AUTO_CREATE);
            }
          //  timerIntent.putExtra("pomodoroTime", pomodoroTime);
            timerIntent.putExtra("breakTime", breakTime);
           // timerIntent.putExtra("clockSound", pomodoroSoundOn);
            timerIntent.putExtra("longBreakTime", longBreakTime);
         //   timerIntent.putExtra("sessionsTillLongBreak", sessionsTillLongBreak);
            startService(timerIntent);
            timerButtonPlay.setText(R.string.timer_button_stop);
            turnProgressOn();
            turnTimerTitleOn();
        } else {
            reset();
            boundService();
            stopService(timerIntent);
            timerButtonPause.setText(R.string.timer_button_pause);
            timerButtonPlay.setText(R.string.timer_button_start);
            turnProgressOff();
            turnTimerTitleOff();
        }
    }

    // pause and resume
    private void pauseTimer() {
        if (timerService.isRunning()) {
            if (!timerService.isPaused()) {
                paused = true;
                turnProgressOff();
                timerService.pauseTimer();
                timerButtonPause.setText(R.string.timer_button_resume);
            } else {
                paused = false;
                turnProgressOn();
                timerService.resumeTimer();
                timerButtonPause.setText(R.string.timer_button_pause);
            }
        }
    }

    public void printTimer() {
        timerText.setText(Utils.formatTimer(getResources(), minutes, seconds));
    }

    private void reset() {
        Debug.print("MainActivity", "RESET", 2, false, this);
        Debug.print("MainActivity", "MINUTES " + minutes, 2, false, this);
        minutes = pomodoroTime;
        Debug.print("MainActivity", "MINUTES " + minutes, 2, false, this);
        seconds = 0;
        printTimer();
    }

    @Override
    public void onDialogStartBreak() {
        timerService.startBreak();
        turnTimerTitleOn();
        turnProgressOn();
    }

    @Override
    public void onDialogSkipBreak() {
        timerService.startPomodoro();
        turnTimerTitleOn();
        turnProgressOn();
    }

    @Override
    public void onDialogClose() {
        printTimer();
        boundService();
        stopService(timerIntent);
        timerButtonPlay.setText(R.string.timer_button_start);
        turnProgressOff();
        turnTimerTitleOff();
    }

    public void updateSessionCounter(boolean reset) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);


        if (!reset) {
            sessionCount = settings.getInt(getString(R.string.pref_title_session_count), 0);
        } else {
            SharedPreferences.Editor editor = settings.edit();
            sessionCount = 0;
            editor.putInt(getString(R.string.pref_title_session_count), sessionCount);
            editor.apply();
        }

        Debug.print("MainActivity", "SESSION COUNTER " + sessionCount, 2, false, this);
        sessions.setTitle(Integer.toString(sessionCount));
    }

    public void timerButtonPlay(View view) {
        startTimer();
    }

    public void timerButtonPause(View view) {
        pauseTimer();
    }

    public void openDialog() {
        reset();
        turnTimerTitleOff();
        turnProgressOff();
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

                if (extras.get("firstDialog") != null) {
                    firstDialog = extras.getBoolean("firstDialog");
                    updateSessionCounter(false);

                    if (!continuous) {
                        Debug.print("MainActivity", "OPEN DIALOG", 2, false, getApplicationContext());
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
                Debug.print("MainActivity", "TIMER SERVICE: " + timerService, 3, false, getApplicationContext());
                timerService.backgroundNotificationOff();
                backgroundNotification = false;
            }

            minutes = timerService.getMinutes();
            seconds = timerService.getSeconds();
            firstDialog = timerService.isPomodoroTimerOn();

            printTimer();

            if (timerService.isRunning()) {
                turnProgressOn();
                turnTimerTitleOn();
                Debug.print("MainActivity", "DIALOGS", 2, false, getApplicationContext());
                Debug.print("MainActivity", "MINUTES " + minutes, 2, false, getApplicationContext());
                Debug.print("MainActivity", "SECONDS " + seconds, 2, false, getApplicationContext());
                if (minutes == 0 && seconds == 0) {
                    Debug.print("MainActivity", "OPEN DIALOG", 2, false, getApplicationContext());
                    openDialog();
                }
            } else {
                turnProgressOff();
                turnTimerTitleOff();
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
