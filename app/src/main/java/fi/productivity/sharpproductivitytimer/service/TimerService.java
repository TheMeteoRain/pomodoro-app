package fi.productivity.sharpproductivitytimer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.util.Calendar;

import fi.productivity.sharpproductivitytimer.MainActivity;
import fi.productivity.sharpproductivitytimer.R;
import fi.productivity.sharpproductivitytimer.data.DataHandler;
import fi.productivity.sharpproductivitytimer.data.Session;
import fi.productivity.sharpproductivitytimer.utils.Debug;
import fi.productivity.sharpproductivitytimer.utils.Utils;

public class TimerService extends Service {

    public final static int PUSH_NOTIFICATION_ID = 11;

    private final static int BACKGROUND_NOTIFICATION_ID = 10;
    private final static String TIMER_PAUSE = "PAUSE";
    private final static String TIMER_STOP = "STOP";

    private boolean isRunning;
  //  private boolean clockSound;
   // private boolean continuous;
    private boolean backgroundNotification;
    private boolean paused;
    private boolean pomodoroTimerOn;
    private int breakTime;
    private int minutes;
    private int seconds;
   // private int sessionsTillLongBreak;
    private int longBreakTime;
    private int notificationSoundId;
    private int tickSoundId;
    private int tockSoundId;
    private long timeleft;
    private long startTime;

    private SoundPool sp;
    private CountDownTimer timer;
    private NotificationManager notificationManager;
    private SharedPreferences settings;

    private IBinder binder;

    public TimerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = false;
       // clockSound = false;
       // continuous = false;
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
            sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        }
        notificationSoundId = sp.load(this, R.raw.notification, 1);
        tickSoundId = sp.load(this, R.raw.tick, 1);
        tockSoundId = sp.load(this, R.raw.tock, 1);
        Debug.print("TimerService", "CREATE SERVICE", 3, false, getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debug.print("TimerService", "SERVICE START", 3, false, getApplicationContext());
        final String action = intent.getAction();
        settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if (!isRunning) {
            Bundle extras;
            if ((extras = intent.getExtras()) != null) {
                if (extras.get("breakTime") != null) {
                    breakTime = extras.getInt("breakTime");
                }
                if (extras.get("longBreakTime") != null) {
                    longBreakTime = extras.getInt("longBreakTime");
                }
            }

            isRunning = true;
            startPomodoro();
        }

        if (action != null) {
            switch (action) {
                case TIMER_PAUSE:
                    Debug.print("TimerService", "PAUSED: " + paused, 2, false, getApplicationContext());
                    if (!paused) {
                        pauseTimer();
                    } else {
                        resumeTimer();
                    }
                    break;
                case TIMER_STOP:
                    onDestroy();
                    break;
            }
        }

        return START_NOT_STICKY;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public boolean isPomodoroTimerOn() {
        return pomodoroTimerOn;
    }

    public void pauseTimer() {
        Debug.print("TimerService", "TIMER: " + timer, 2, false, getApplicationContext());
        Debug.print("TimerService", "PAUSED: " + paused, 2, false, getApplicationContext());
        timer.cancel();
        paused = true;
    }

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

    public void backgroundNotificationOn() {
        backgroundNotification = true;
        startForeground(BACKGROUND_NOTIFICATION_ID, getNotification(Utils.formatTimer(getResources(), minutes, seconds)));
    }

    public void backgroundNotificationOff() {
        notificationManager.cancel(BACKGROUND_NOTIFICATION_ID);
        stopForeground(true);
        backgroundNotification = false;
    }

    private Notification getNotification(String text) {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("seconds", seconds);
        mainIntent.putExtra("minutes", minutes);
        mainIntent.putExtra("stop", true);
        mainIntent.putExtra("firstDialog", pomodoroTimerOn);
        mainIntent.putExtra("backgroundNotification", backgroundNotification);
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

        return new NotificationCompat.Builder(this)
                .setContentTitle(title)
                .setContentText(text)
                .addAction(R.drawable.ic_stat_stop, getString(R.string.notification_stop), piStop)
                .addAction(R.drawable.ic_stat_pause, getString(R.string.notification_pause), piPause)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(contentIntent).getNotification();
    }

    private void updateNotification() {
        Notification notification = getNotification(Utils.formatTimer(getResources(), minutes, seconds));
        notificationManager.notify(BACKGROUND_NOTIFICATION_ID, notification);
    }

    //// TODO: 10-Apr-17
    public void notifyUser(String text) {
        Debug.print("TimerService", "NOTIFY USER", 2, false, getApplicationContext());
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(text);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra("seconds", seconds);
        resultIntent.putExtra("minutes", minutes);
        resultIntent.putExtra("firstDialog", pomodoroTimerOn);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

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
                        BACKGROUND_NOTIFICATION_ID,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(PUSH_NOTIFICATION_ID, mBuilder.build());
    }

    private void requestActionFromUser() {
        Intent i = new Intent("fi.productivity.sharpproductivitytimer.MainActivity");
        i.putExtra("seconds", seconds);
        i.putExtra("minutes", minutes);
        sendBroadcast(i);
    }

    private void requestActionFromUser(boolean firstDialog) {
        Intent i = new Intent("fi.productivity.sharpproductivitytimer.MainActivity");
        i.putExtra("seconds", seconds);
        i.putExtra("minutes", minutes);
        i.putExtra("firstDialog", firstDialog);
        sendBroadcast(i);
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isBackgroundNotification() {
        return backgroundNotification;
    }

    public boolean isPaused() {
        return paused;
    }

    @Override
    public boolean stopService(Intent name) {
        Debug.print("TimerService", "STOP SERVICE", 3, false, getApplicationContext());
        onDestroy();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        Debug.print("TimerService", "KILL SERVICE", 3, false, getApplicationContext());
        isRunning = false;
        System.out.println(timeleft);
        System.out.println(startTime);
        if (timer instanceof PomodoroTimer && timeleft != startTime) {
            saveSession(true);
        }
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    public void startPomodoro() {
        pomodoroTimerOn = true;
        startTime = MainActivity.pomodoroTime * 60000;
        timer = new PomodoroTimer(startTime, 1000);
        timer.start();
    }

    public void startBreak() {
        pomodoroTimerOn = false;
        if (MainActivity.sessionCount % MainActivity.sessionsTillLongBreak == 0) {
            startTime = longBreakTime * 60000;
            timer = new BreakTimer(longBreakTime * 60000, 1000);
            timer.start();
        } else {
            startTime = breakTime * 60000;
            timer = new BreakTimer(breakTime * 60000, 1000);
            timer.start();
        }
    }

    public void saveSession(boolean stopped) {
        Debug.print("TimerService", "SAVE SESSION", 2, false, getApplicationContext());
        Calendar cal = Utils.getCalendarToday();

        Session session;
        if (stopped) {
            session = new Session(minutes, (60 - seconds), MainActivity.pomodoroTime, 0, cal.getTimeInMillis(), stopped);
        } else {
            session = new Session(MainActivity.pomodoroTime, breakTime, cal.getTimeInMillis(), stopped);
        }
        DataHandler.save(getApplicationContext(), session);
    }

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
                updateNotification();
            }

            requestActionFromUser();
        }

        @Override
        public void onFinish() {
            minutes = 0;
            seconds = 0;
            timeleft = 0;
            MainActivity.sessionCount++;
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(getString(R.string.pref_title_session_count), MainActivity.sessionCount);
            editor.apply();
            sp.play(notificationSoundId, 1, 1, 1, 0, 1);

            saveSession(false);
            notifyUser(getString(R.string.notification_title_pomodoro));

            if (MainActivity.continuous) {
                startBreak();
            } else {
                backgroundNotificationOff();
                requestActionFromUser(true);
            }
        }
    }

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
                updateNotification();
            }

            requestActionFromUser();
        }

        @Override
        public void onFinish() {
            minutes = 0;
            seconds = 0;
            timeleft = 0;
            sp.play(notificationSoundId, 1, 1, 1, 0, 1);

            notifyUser(getString(R.string.notification_title_break));

            if (MainActivity.continuous) {
                startPomodoro();
            } else {
                backgroundNotificationOff();
                requestActionFromUser(false);
            }
        }
    }
}
