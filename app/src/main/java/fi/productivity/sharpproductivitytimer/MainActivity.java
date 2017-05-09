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

import fi.productivity.sharpproductivitytimer.dialog.SessionCountDialog;
import fi.productivity.sharpproductivitytimer.dialog.SessionCountDialogListener;
import fi.productivity.sharpproductivitytimer.dialog.SessionDialogListener;
import fi.productivity.sharpproductivitytimer.dialog.SessionFirstDialog;
import fi.productivity.sharpproductivitytimer.dialog.SessionSecondDialog;
import fi.productivity.sharpproductivitytimer.service.LocalBinder;
import fi.productivity.sharpproductivitytimer.service.TimerService;
import fi.productivity.sharpproductivitytimer.setting.SettingsActivity;
import fi.productivity.sharpproductivitytimer.utils.Debug;
import fi.productivity.sharpproductivitytimer.utils.Utils;

/**
 * Application's main activity, handles logic between service and activity.
 *
 * User is able to navigate from this activity to stats and settings.
 * From this activity user is able to start and pause timers.
 *
 * Timer's title and running timer is shown to user. Also the amount of sessions user has lasted.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.8
 */
public class MainActivity extends AppCompatActivity implements SessionDialogListener, SessionCountDialogListener {

    /**
     * Pomodoro time, represent how long user's sessions will last.
     */
    public static int pomodoroTime;

    /**
     * Break time, comes right after pomodoro time, allows user to relax during sessions.
     */
    public static int breakTime;

    /**
     * Long break time functions exactly like break time but will occur only user has reached
     * certain amount of sessions.
     */
    public static int longBreakTime;

    /**
     * Represents the amount of sessions user has lasted.
     */
    public static int sessionCount;

    /**
     * Number of sessions user must reach in order to activate long break time.
     */
    public static int sessionsTillLongBreak;

    /**
     * If set to true user will hear clock sounds during pomodoro timers.
     */
    public static boolean pomodoroSoundOn;

    /**
     * If set to true user will hear clock sounds during break timers.
     */
    public static boolean breakSoundOn;

    /**
     * If set to true user does not have to press any dialogues in order to continue.
     */
    public static boolean continuous;

    /**
     * Current seconds timer has.
     */
    private int seconds;

    /**
     * Current minutes timer has.
     */
    private int minutes;

    /**
     * Checker for when Timer Service is bounded.
     */
    private boolean isBounded;

    /**
     * When dialogue is shown after timer has ended, this will determine which dialogue is prompted.
     */
    private boolean firstDialog;

    /**
     * Keeps check if user has answered a dialogue after timer has ended.
     * Will be always true if user does not answer dialogue.
     */
    private boolean dialogPendingAnswer;

    /**
     * Circular progress bar shown only when timer is running.
     */
    private ProgressBar timerProgress;

    /**
     * View that shows current time on the timer for the user.
     */
    private TextView timerText;

    /**
     * Tells user what timer is currently running.
     */
    private TextView timerTitle;

    /**
     * Play button to start and/or stop timer.
     */
    private Button timerButtonPlay;

    /**
     * Pause button to pause and/or resume timer.
     */
    private Button timerButtonPause;

    /**
     * Holds dialogues that come after timers.
     */
    private DialogFragment dialog;

    /**
     * Shows the number of sessions user has lasted.
     */
    private MenuItem sessions;

    /**
     * Timer Service intent.
     */
    private Intent timerIntent;

    /**
     * Timer Service.
     */
    private TimerService timerService;

    /**
     * Handles all broadcasts coming from Timer Service.
     */
    private TimerReceiver timerReceiver;

    /**
     * Binds Timer Service.
     */
    private TimerConnection timerConnection;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Debug.loadDebug(this);
        Debug.print("MainActivity", "CREATE", 3, false, this);

        isBounded = false;

        timerTitle = (TextView) findViewById(R.id.timerTitle);
        timerProgress = (ProgressBar) findViewById(R.id.timerProgress);
        timerText = (TextView) findViewById(R.id.timerText);
        timerButtonPlay = (Button) findViewById(R.id.timerButtonPlay);
        timerButtonPause = (Button) findViewById(R.id.timerButtonPause);

        timerConnection = new TimerConnection();
        timerReceiver = new TimerReceiver();
        timerIntent = new Intent(MainActivity.this, TimerService.class);

        reset();
        loadPreferences();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {
        super.onStart();
        Debug.print("MainActivity", "START", 3, false, this);
        registerReceiver(timerReceiver, new IntentFilter("fi.productivity.sharpproductivitytimer.MainActivity"));
        bindService(timerIntent, timerConnection, Context.BIND_AUTO_CREATE);
        onNewIntent(getIntent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Debug.print("MainActivity", "ON NEW INTENT", 2, false, this);
        Bundle extras;
        if ((extras = intent.getExtras()) != null) {

            if (extras.get("firstDialog") != null) {
                timerService.backgroundNotificationOff();
                firstDialog = extras.getBoolean("firstDialog");
                Debug.print("MainActivity", "FIRST DIALOG " + firstDialog, 2, false, this);
            }

            if (extras.get("seconds") != null && extras.get("minutes") != null) {
                seconds = extras.getInt("seconds");
                minutes = extras.getInt("minutes");

                Debug.print("MainActivity", "MINUTES " + minutes, 3, false, this);
                Debug.print("MainActivity", "SECONDS " + seconds, 3, false, this);
                printTimer();
            }

            if (extras.get("finished") != null) {
                if (extras.getBoolean("finished")) {
                    updateSessionCounter(false);
                    if (!continuous) {
                        openDialog();
                    }
                }
            }

            if (extras.get("started") != null) {
                if (extras.getBoolean("started")) {
                    turnTimerTitleOn();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume() {
        super.onResume();
        Debug.print("MainActivity", "RESUME", 3, false, this);
        loadPreferences();
        closePushNotification();

        if (minutes == 0 && seconds == 0) {
            reset();
        }

        if (dialogPendingAnswer) {
            openDialog();
        }

        if (sessions != null) {
            updateSessionCounter(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause() {
        super.onPause();
        Debug.print("MainActivity", "PAUSE", 3, false, this);

        Debug.print("MainActivity", "MINUTES " + minutes, 2, false, this);
        Debug.print("MainActivity", "SECONDS " + timerService.getSeconds(), 2, false, this);
        Debug.print("MainActivity", "RUNNING " + timerService.isRunning(), 2, false, this);
        if (timerService.isRunning() && timerService.getTimeleft() != 0) {
            timerService.backgroundNotificationOn();
        }

        closePushNotification();
        closeDialog();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {
        super.onStop();
        Debug.print("MainActivity", "STOP", 3, false, this);
        unregisterReceiver(timerReceiver);
        unBoundService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy() {
        Debug.print("MainActivity", "DESTROY", 3, false, this);
        timerService.backgroundNotificationOff();
        Debug.print("MainActivity", "SERVICE CLOSABLE: " + timerService.isClosable(), 3, false, this);
        if (timerService.isClosable()) {
            stopService(timerIntent);
        } else {
            timerService.setClosable(true);
        }
        Utils.saveDialogPendingAnswer(this, false);
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Debug.print("MainActivity", "MENU CREATE", 3, false, this);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Debug.print("MainActivity", "MENU PREPARE", 3, false, this);
        sessions = menu.findItem(R.id.action_sessions);
        updateSessionCounter(false);
        return true;
    }

    /**
     * {@inheritDoc}
     */
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
                DialogFragment dialog = new SessionCountDialog();
                dialog.show(getSupportFragmentManager(), "sessionCount");
                return true;
            case (R.id.action_stats):
                Intent intentStat = new Intent(this, StatActivity.class);
                startActivity(intentStat);
        }
        return false;
    }

    /**
     * Un bounds Timer Service if it has been bounded.
     */
    private void unBoundService() {
        if (isBounded) {
            unbindService(timerConnection);
            isBounded = false;
        }
    }

    /**
     * Closes active push notification if such exists.
     */
    private void closePushNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(TimerService.PUSH_NOTIFICATION_ID);
    }

    /**
     * Load application settings.
     */
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
        sessionCount = settings.getInt(getString(R.string.pref_title_session_count), 0);
        dialogPendingAnswer = settings.getBoolean("dialogPendingAnswer", false);
    }

    /**
     * Sets progress bar visible if it isn't already.
     */
    private void turnProgressOn() {
        if (timerProgress.getVisibility() != View.VISIBLE) {
            timerProgress.setVisibility(View.VISIBLE);
        }
    }


    /**
     * Sets progress bar invisible if it isn't already.
     */
    private void turnProgressOff() {
        if (timerProgress.getVisibility() != View.INVISIBLE) {
            timerProgress.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Sets timer title visible if it isn't already and sets right title depending on
     * current timer (pomodoro / break).
     */
    private void turnTimerTitleOn() {
        if (timerTitle.getVisibility() != View.VISIBLE) {
            timerTitle.setVisibility(View.VISIBLE);
        }

        if (timerService.isPomodoroTimerOn()) {
            timerTitle.setText(R.string.timer_title_pomodoro);
        } else if (sessionCount % sessionsTillLongBreak == 0) {
            timerTitle.setText(R.string.timer_title_long_break);
        } else {
            timerTitle.setText(R.string.timer_title_break);
        }
    }

    /**
     * Sets progress bar invisible if it isn't already.
     */
    private void turnTimerTitleOff() {
        timerTitle.setVisibility(View.INVISIBLE);
    }

    /**
     * If timer service is not running, then start the service otherwise stop the service.
     */
    private void startTimer() {
        if (!timerService.isRunning()) {
            if (!isBounded) {
                bindService(timerIntent, timerConnection, Context.BIND_AUTO_CREATE);
            }
            startService(timerIntent);
            timerButtonPlay.setText(R.string.timer_button_stop);
            turnProgressOn();
            turnTimerTitleOn();
        } else {
            reset();
            unBoundService();
            stopService(timerIntent);
            timerButtonPause.setText(R.string.timer_button_pause);
            timerButtonPlay.setText(R.string.timer_button_start);
            turnProgressOff();
            turnTimerTitleOff();
        }
    }

    /**
     * If timer service is running and this method is called, pause current timer.
     * Second call on the method will resume the timer.
     */
    private void pauseTimer() {
        if (timerService.isRunning()) {
            if (!timerService.isPaused()) {
                turnProgressOff();
                timerService.pauseTimer();
                timerButtonPause.setText(R.string.timer_button_resume);
            } else {
                turnProgressOn();
                timerService.resumeTimer();
                timerButtonPause.setText(R.string.timer_button_pause);
            }
        }
    }

    /**
     * Display current time on timerText component.
     */
    public void printTimer() {
        timerText.setText(Utils.formatTimer(getResources(), minutes, seconds));
    }

    /**
     * Loads user default time and displays the time on timerText component.
     */
    private void reset() {
        Debug.print("MainActivity", "RESET", 2, false, this);
        Debug.print("MainActivity", "MINUTES " + minutes, 2, false, this);
        minutes = pomodoroTime;
        Debug.print("MainActivity", "MINUTES " + minutes, 2, false, this);
        seconds = 0;
        printTimer();
    }

    /**
     * Either fetches or resets current session counter number.
     *
     * @param reset determines if session counter is fetched or reseted.
     *              If true, number is reseted. If false, former number is fetched.
     */
    public void updateSessionCounter(boolean reset) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        if (reset) {
            SharedPreferences.Editor editor = settings.edit();
            sessionCount = 0;
            editor.putInt(getString(R.string.pref_title_session_count), sessionCount);
            editor.apply();
        } else {
            sessionCount = settings.getInt(getString(R.string.pref_title_session_count), 0);
        }

        Debug.print("MainActivity", "SESSION COUNTER " + sessionCount, 2, false, this);
        sessions.setTitle(Integer.toString(sessionCount));
    }

    /**
     * On click method for View classes, starts and/or stops TimerService.
     *
     * @param view any class that inherits view.
     */
    public void timerButtonPlay(View view) {
        startTimer();
    }

    /**
     * On click method for View classes, pauses and/or resumes TimerService timer.
     *
     * @param view any class that inherits view.
     */
    public void timerButtonPause(View view) {
        pauseTimer();
    }

    /**
     * Opens a dialogue depending on the last timer that ended.
     * Saves unanswered dialogue boolean to SharedPreferences in case of user minimizes application.
     * If user did not answer the dialogue first time, he/she will be prompted with the same
     * dialogue on opening of the application.
     */
    public void openDialog() {
        reset();
        turnTimerTitleOff();
        turnProgressOff();
        if (!dialogPendingAnswer) {
            Utils.saveDialogPendingAnswer(this, true);
        }

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

    /**
     * Dismisses dialogues that are invoked from timer endings.
     */
    public void closeDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDialogStartBreak() {
        timerService.startBreak();
        timerButtonPlay.setText(R.string.timer_button_stop);
        turnTimerTitleOn();
        turnProgressOn();
        Utils.saveDialogPendingAnswer(this, false);
        closePushNotification();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDialogSkipBreak() {
        timerService.startPomodoro();
        timerButtonPlay.setText(R.string.timer_button_stop);
        turnTimerTitleOn();
        turnProgressOn();
        Utils.saveDialogPendingAnswer(this, false);
        closePushNotification();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDialogClose() {
        unBoundService();
        stopService(timerIntent);
        timerButtonPlay.setText(R.string.timer_button_start);
        turnProgressOff();
        turnTimerTitleOff();
        Utils.saveDialogPendingAnswer(this, false);
        closePushNotification();
        reset();
        printTimer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDialogSessionsReset() {
        updateSessionCounter(true);
    }

    /**
     * Inner class that handles receiving broadcasts from TimerService.
     */
    private class TimerReceiver extends BroadcastReceiver {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            firstDialog = timerService.isPomodoroTimerOn();
            onNewIntent(intent);
        }
    }

    /**
     * Inner class that handles binding between MainActivity and TimerService.
     */
    private class TimerConnection implements ServiceConnection {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Debug.print("MainActivity", "SERVICE CONNECTED", 3, false, getApplicationContext());
            LocalBinder binder = (LocalBinder) service;
            timerService = binder.getService();
            isBounded = true;

            if (timerService.isBackgroundNotification()) {
                Debug.print("MainActivity", "TIMER SERVICE: " + timerService, 3, false, getApplicationContext());
                timerService.backgroundNotificationOff();
            }

            minutes = timerService.getMinutes();
            seconds = timerService.getSeconds();
            firstDialog = timerService.isPomodoroTimerOn();

            printTimer();

            if (!timerService.isRunning()) {
                turnProgressOff();
                turnTimerTitleOff();
                timerButtonPlay.setText(R.string.timer_button_start);
                reset();
            } else {
                turnTimerTitleOn();
               // turnProgressOn();
              //  turnTimerTitleOn();
               // timerButtonPlay.setText(R.string.timer_button_stop);
            }

            if (timerService.isPaused()) {
                turnProgressOff();
                timerButtonPause.setText(R.string.timer_button_resume);
            } else {
                timerButtonPause.setText(R.string.timer_button_pause);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Debug.print("MainActivity", "SERVICE DISCONNECT", 3, false, getApplicationContext());
            isBounded = false;
        }
    }
}
