package fi.productivity.sharpproductivitytimer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

import fi.productivity.sharpproductivitytimer.MainActivity;
import fi.productivity.sharpproductivitytimer.R;
import fi.productivity.sharpproductivitytimer.data.DataHandler;
import fi.productivity.sharpproductivitytimer.data.Session;
import fi.productivity.sharpproductivitytimer.utils.Debug;
import fi.productivity.sharpproductivitytimer.utils.Utils;


/**
 * Time Service handles all the logic from starting a timer to ending it
 * and informing about it to the Main Activity.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.8
 */
public class TimerService extends Service {

    /**
     * Push notification id.
     */
    public final static int PUSH_NOTIFICATION_ID = 11;

    /**
     * Background notification id.
     */
    private final static int BACKGROUND_NOTIFICATION_ID = 10;

    /**
     * Background notification's pause action.
     */
    private final static String TIMER_PAUSE = "PAUSE";

    /**
     * Background notification's stop action.
     */
    private final static String TIMER_STOP = "STOP";

    /**
     * Background notification's closable action.
     */
    private final static String TIMER_CLOSABLE = "CLOSABLE";

    /**
     * Checker for if service is running.
     */
    private boolean isRunning;

    /**
     * Checker for if service is currently destroyable.
     */
    private boolean closable;

    /**
     * If true, a notification with timer is shown for user when application is minimized.
     * If false, they are not permitted.
     */
    private boolean backgroundNotification;

    /**
     * Represents if timer is currently paused.
     */
    private boolean paused;

    /**
     * Represent if pomodoro timer is currently active, and not break timer.
     */
    private boolean pomodoroTimerOn;

    /**
     * Current minutes timer has.
     */
    private int minutes;

    /**
     * Current seconds timer has.
     */
    private int seconds;

    /**
     * An id of notification sound that plays after each successfully finished timer.
     */
    private int notificationSoundId;

    /**
     * An id of tick sound.
     */
    private int tickSoundId;

    /**
     * An id of tock sound.
     */
    private int tockSoundId;

    /**
     * Current time left in milliseconds.
     */
    private long timeleft;

    /**
     * Starting time of timer.
     */
    private long startTime;

    /**
     * SoundPool used to play sounds for user.
     */
    private SoundPool sp;

    /**
     * Represents each running timer.
     */
    private CountDownTimer timer;

    /**
     * Notification manager used to notify, update or cancel notifications.
     */
    private NotificationManager notificationManager;

    /**
     * Used to bind MainActivity and TimerService together.
     */
    private IBinder binder;

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = false;
        closable = true;
        backgroundNotification = false;
        paused = false;
        binder = new LocalBinder(this);
        timeleft = 0;
        pomodoroTimerOn = true;
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        minutes = MainActivity.pomodoroTime;
        seconds = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sp = new SoundPool.Builder().build();
        } else {
            sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 1);
        }
        notificationSoundId = sp.load(this, R.raw.notification, 1);
        tickSoundId = sp.load(this, R.raw.tick, 1);
        tockSoundId = sp.load(this, R.raw.tock, 1);
        Debug.print("TimerService", "CREATE SERVICE", 3, false, getApplicationContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debug.print("TimerService", "SERVICE START", 3, false, getApplicationContext());
        final String action = intent.getAction();

        if (!isRunning) {
            isRunning = true;
            startPomodoro();
        }

        if (action != null) {
            switch (action) {
                case TIMER_PAUSE:
                    Debug.print("TimerService", "PAUSED: " + paused, 2, false, getApplicationContext());
                    if (!paused) {
                        pauseTimer();
                        updateNotification(paused);
                    } else {
                        resumeTimer();
                        updateNotification(paused);
                    }
                    break;
                case TIMER_STOP:
                    Debug.print("TimerService", "STOPPED ", 2, false, getApplicationContext());
                    onDestroy();
                    break;
                case TIMER_CLOSABLE:
                    Debug.print("TimerService", "NOT CLOSABLE", 2, false, getApplicationContext());
                    setClosable(false);
                    break;
            }
        }

        return START_STICKY;
    }

    /**
     * Get closable status of this service.
     *
     * @return is service closable.
     */
    public boolean isClosable() {
        return closable;
    }

    /**
     * Change this service's closable status.
     * Not meant to be used freely.
     *
     * @param closable this service's closable value.
     */
    public void setClosable(boolean closable) {
        this.closable = closable;
    }

    /**
     * Get this service's currently running timer's minutes.
     *
     * @return currently running timer's minutes.
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Get this service's currently running timer's seconds.
     *
     * @return currently running timer's seconds.
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * Is the current timer running a pomodoro timer.
     *
     * @return is pomodoro timer on.
     */
    public boolean isPomodoroTimerOn() {
        return pomodoroTimerOn;
    }

    /**
     * Pause currently running timer.
     */
    public void pauseTimer() {
        Debug.print("TimerService", "TIMER: " + timer, 2, false, getApplicationContext());
        Debug.print("TimerService", "PAUSED: " + paused, 2, false, getApplicationContext());
        timer.cancel();
        paused = true;
    }

    /**
     * Resume currently paused timer.
     */
    public void resumeTimer() {
        if (timer instanceof PomodoroTimer) {
            timer = new PomodoroTimer(timeleft, 1000);
        }
        if (timer instanceof BreakTimer) {
            timer = new BreakTimer(timeleft, 1000);
        }
        timer.start();
        paused = false;
    }

    /**
     * Turn background notifications on.
     * Used to inform user about the timers outside of the application,
     * when user minimizes application.
     */
    public void backgroundNotificationOn() {
        backgroundNotification = true;
        startForeground(BACKGROUND_NOTIFICATION_ID, getNotification(Utils.formatTimer(getResources(), minutes, seconds), paused));
    }

    /**
     * Turn background notification off.
     * Used when user comes back to the application.
     */
    public void backgroundNotificationOff() {
        notificationManager.cancel(BACKGROUND_NOTIFICATION_ID);
        stopForeground(true);
        backgroundNotification = false;
    }

    /**
     * Background notification builder.
     *
     * This is the notification that is shown for the user,
     * when he/she has minimized the applicaiton.
     *
     * Contains title of the timer and running clock itself.
     *
     * @param text the text of the notification's content.
     * @param pause determines pause action's icon and text. If true, show resume else show pause.
     * @return background notification.
     */
    private Notification getNotification(String text, boolean pause) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("seconds", seconds);
        mainIntent.putExtra("minutes", minutes);
        mainIntent.putExtra("firstDialog", pomodoroTimerOn);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent pauseIntent = new Intent(this, TimerService.class);
        pauseIntent.setAction(TimerService.TIMER_PAUSE);
        PendingIntent piPause = PendingIntent.getService(this, BACKGROUND_NOTIFICATION_ID, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent stopIntent = new Intent(this, TimerService.class);
        stopIntent.setAction(TimerService.TIMER_STOP);
        PendingIntent piStop = PendingIntent.getService(this, BACKGROUND_NOTIFICATION_ID, stopIntent, PendingIntent.FLAG_ONE_SHOT);


        CharSequence title = getText(R.string.app_name) + ": " + getString(R.string.timer_title_pomodoro).toLowerCase();
        if (!pomodoroTimerOn) {
            title = getText(R.string.app_name) + ": " + getString(R.string.timer_title_break).toLowerCase();
        }
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                BACKGROUND_NOTIFICATION_ID, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

         NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .addAction(R.drawable.ic_stat_stop, getString(R.string.notification_stop), piStop)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher_white)
                .setContentIntent(contentIntent);

        if (pause) {
            notificationBuilder.addAction(R.drawable.ic_stat_play, getString(R.string.notification_resume), piPause);
        } else {
            notificationBuilder.addAction(R.drawable.ic_stat_pause, getString(R.string.notification_pause), piPause);
        }


        return notificationBuilder.build();
    }

    /**
     * Used to update background notification's running timer.
     *
     * @param pause determines pause action's icon and text. If true, show resume else show pause.
     */
    private void updateNotification(boolean pause) {
        Notification notification = getNotification(Utils.formatTimer(getResources(), minutes, seconds), pause);
        notificationManager.notify(BACKGROUND_NOTIFICATION_ID, notification);
    }

    /**
     * Push notification.
     *
     * Used only when user has completed a session.
     * One time notification after every session, if user does not have a continuous mode on.
     *
     * @param text the text of the notification's content.
     */
    public void notifyUser(String text) {
        if (!MainActivity.continuous) {
            Utils.saveDialogPendingAnswer(this, true);
        }

        Debug.print("TimerService", "NOTIFY USER", 2, false, getApplicationContext());
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("seconds", seconds);
        mainIntent.putExtra("minutes", minutes);
        mainIntent.putExtra("firstDialog", pomodoroTimerOn);
        mainIntent.setAction(TimerService.TIMER_CLOSABLE);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this,
                PUSH_NOTIFICATION_ID, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher_white)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(text)
                        .setContentIntent(contentIntent);

        notificationManager.notify(PUSH_NOTIFICATION_ID, mBuilder.build());
    }

    /**
     * A broadcast to activate dialogue.
     *
     * @param firstDialog If true, show pomodoro timer dialogue to the user else break dialogue.
     */
    private void requestInputFromUser(boolean firstDialog) {
        Intent i = new Intent("fi.productivity.sharpproductivitytimer.MainActivity");
        i.putExtra("seconds", seconds);
        i.putExtra("minutes", minutes);
        i.putExtra("firstDialog", firstDialog);
        i.putExtra("finished", true);
        sendBroadcast(i);
    }

    /**
     * A broadcast to update activity timer.
     */
    private void updateActivity() {
        Intent i = new Intent("fi.productivity.sharpproductivitytimer.MainActivity");
        i.putExtra("seconds", seconds);
        i.putExtra("minutes", minutes);
        sendBroadcast(i);
    }

    /**
     * A broadcast that tells if timer either just started or finished.
     *
     * Both timerStarted and timerFinished can't be true.
     *
     * @param timerStarted
     * @param timerFinished
     */
    private void updateActivity(boolean timerStarted, boolean timerFinished) {
        Intent i = new Intent("fi.productivity.sharpproductivitytimer.MainActivity");
        i.putExtra("seconds", seconds);
        i.putExtra("minutes", minutes);
        i.putExtra("started", timerStarted);
        i.putExtra("finished", timerFinished);
        sendBroadcast(i);
    }

    /**
     * Get if this service is currently running.
     *
     * @return this service's running status.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Get time left of currently running timer.
     *
     * @return timer's timeleft
     */
    public long getTimeleft() {
        return timeleft;
    }

    /**
     * Check if background notification are on?
     *
     * @return is background notifications on?
     */
    public boolean isBackgroundNotification() {
        return backgroundNotification;
    }

    /**
     * Check if currently running timer is paused.
     *
     * @return is timer paused?
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean stopService(Intent name) {
        Debug.print("TimerService", "STOP SERVICE", 3, false, getApplicationContext());
        onDestroy();
        return super.stopService(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        Debug.print("TimerService", "KILL SERVICE", 3, false, getApplicationContext());
        isRunning = false;
        Debug.print("TimerService", "TIMELEFT " + timeleft, 2, false, getApplicationContext());
        Debug.print("TimerService", "START TIME " + startTime, 2, false, getApplicationContext());
        if (timer instanceof PomodoroTimer && timeleft != startTime) {
            saveSession(true);
        }
        if (timer != null) {
            timer.cancel();
        }
        backgroundNotificationOff();
        super.onDestroy();
    }

    /**
     * Start pomodoro timer.
     */
    public void startPomodoro() {
        pomodoroTimerOn = true;
        updateActivity(true, false);
        startTime = MainActivity.pomodoroTime * 60000;
        timer = new PomodoroTimer(startTime, 1000);
        timer.start();
    }

    /**
     * Start either normal break or long break timer.
     *
     * Depending on user's number of sessions till long break and how many sessions he/she has
     * lasted.
     */
    public void startBreak() {
        pomodoroTimerOn = false;
        updateActivity(true, false);
        if (MainActivity.sessionCount % MainActivity.sessionsTillLongBreak == 0) {
            startTime = MainActivity.longBreakTime * 60000;
            timer = new BreakTimer(startTime, 1000);
            timer.start();
        } else {
            startTime = MainActivity.breakTime * 60000;
            timer = new BreakTimer(startTime, 1000);
            timer.start();
        }
    }

    /**
     * Save session to a internal file.
     *
     * @param stopped was timer interrupted?
     */
    public void saveSession(boolean stopped) {
        Debug.print("TimerService", "SAVE SESSION", 2, false, getApplicationContext());
        Calendar cal = Utils.getCalendarToday();
        Debug.print("TimerService", "TIME " + cal.getTimeInMillis(), 2, false, getApplicationContext());

        Session session;
        if (stopped) {
            session = new Session(minutes, (60 - seconds), MainActivity.pomodoroTime, 0, cal.getTimeInMillis());
        } else {
            session = new Session(MainActivity.pomodoroTime, MainActivity.breakTime, cal.getTimeInMillis());
        }
        DataHandler.save(getApplicationContext(), session);
    }

    /**
     * Inner CountDownTimer class to handle pomodoro timer.
     */
    private class PomodoroTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public PomodoroTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onTick(long millisUntilFinished) {
            timeleft = millisUntilFinished;
            seconds = (int) ((millisUntilFinished / 1000) % 60);
            minutes = (int) ((millisUntilFinished / (1000*60)) % 60);

            if (MainActivity.pomodoroSoundOn) {
                if (seconds % 2 == 0) {
                    sp.play(tickSoundId, 1, 1, 1, 0, 1);
                } else {
                    sp.play(tockSoundId, 1, 1, 1, 0, 1);
                }
            }

            if (backgroundNotification) {
                Debug.print("TimerService", "SERVICE UPDATE POMODORO", 2, false, getApplicationContext());
                updateNotification(paused);
            }

            updateActivity();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onFinish() {
            minutes = 0;
            seconds = 0;
            timeleft = 0;
            MainActivity.sessionCount++;
            SharedPreferences.Editor editor =
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
            editor.putInt(getString(R.string.pref_title_session_count), MainActivity.sessionCount);
            editor.apply();

            sp.play(notificationSoundId, 1, 1, 1, 0, 1);
            saveSession(false);
            notifyUser(getString(R.string.notification_title_pomodoro));

            if (MainActivity.continuous) {
                updateActivity(false, true);
                startBreak();
            } else {
                backgroundNotificationOff();
                requestInputFromUser(true);
            }
        }
    }

    /**
     * Inner CountDownTimer class to handle break timer.
     */
    private class BreakTimer extends CountDownTimer {

        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public BreakTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onTick(long millisUntilFinished) {
            timeleft = millisUntilFinished;
            seconds = (int) ((millisUntilFinished / 1000) % 60);
            minutes = (int) ((millisUntilFinished / (1000*60)) % 60);

            if (MainActivity.breakSoundOn) {
                if (seconds % 2 == 0) {
                    sp.play(tickSoundId, 1, 1, 1, 0, 1);
                } else {
                    sp.play(tockSoundId, 1, 1, 1, 0, 1);
                }
            }

            if (backgroundNotification) {
                Debug.print("TimerService", "SERVICE UPDATE BREAK", 2, false, getApplicationContext());
                updateNotification(paused);
            }

            updateActivity();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onFinish() {
            minutes = 0;
            seconds = 0;
            timeleft = 0;

            sp.play(notificationSoundId, 1, 1, 1, 0, 1);
            notifyUser(getString(R.string.notification_title_break));

            if (MainActivity.continuous) {
                updateActivity(false, true);
                startPomodoro();
            } else {
                backgroundNotificationOff();
                requestInputFromUser(false);
            }
        }
    }
}
