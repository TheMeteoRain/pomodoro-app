package fi.productivity.sharpproductivitytimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

public class TimerService extends Service {

    private final static int BACKGROUND_NOTIFICATION_ID = 10;
    private final static String TIMER_PAUSE = "PAUSE";
    private final static String TIMER_STOP = "STOP";

    private boolean isRunning;
    private boolean clockSound;
    private boolean continuous;
    private boolean backgroundNotification;
    private boolean paused;
    private boolean pomodoroTimerOn;
    private int pomodoroTime;
    private int breakTime;
    private int minutes;
    private int seconds;
    private int sessionsTillLongBreak;
    private int sessionCount;
    private int longBreakTime;
    private int notificationSoundId;
    private int tickSoundId;
    private int tockSoundId;
    private long timeleft;

    private SoundPool sp;
    private CountDownTimer timer;
    private NotificationManager notificationManager;

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
        clockSound = false;
        continuous = false;
        backgroundNotification = false;
        paused = false;
        binder = new LocalBinder(this);
        timeleft = 0;
        pomodoroTimerOn = false;
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        minutes = 1;
        seconds = 0;
        sessionCount = 0;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sp = new SoundPool.Builder().build();
        } else {
            sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        }
        notificationSoundId = sp.load(this, R.raw.notification, 1);
        tickSoundId = sp.load(this, R.raw.tick, 1);
        tockSoundId = sp.load(this, R.raw.tock, 1);
        System.out.println("CREATE SERVICE");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("SERVICE START");
        final String action = intent.getAction();

        if (!isRunning) {
            Bundle extras;
            if ((extras = intent.getExtras()) != null) {
                if (extras.get("clockSound") != null) {
                    clockSound = extras.getBoolean("clockSound");
                }
                if (extras.get("pomodoroTime") != null) {
                    pomodoroTime = extras.getInt("pomodoroTime");
                }
                if (extras.get("breakTime") != null) {
                    breakTime = extras.getInt("breakTime");
                }
                if (extras.get("continuous") != null) {
                    continuous = extras.getBoolean("continuous");
                }
                if (extras.get("longBreakTime") != null) {
                    longBreakTime = extras.getInt("longBreakTime");
                }
                if (extras.get("sessionsTillLongBreak") != null) {
                    sessionsTillLongBreak = extras.getInt("sessionsTillLongBreak");
                }
            }

            isRunning = true;
            startPomodoro();
        }

        if (action != null) {
            switch (action) {
                case TIMER_PAUSE:
                    System.out.println(paused);
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
        System.out.println(timer);
        System.out.println(paused);
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
        startForeground(BACKGROUND_NOTIFICATION_ID, getNotification(notificationText()));
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


        CharSequence title = getText(R.string.app_name);
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

    private String notificationText() {
        String text;

        if (minutes < 10) {
            if (seconds < 10) {
                text = String.format(getResources().getStringArray(R.array.pomodoro_time)[5], minutes, seconds);
            } else {
                text = String.format(getResources().getStringArray(R.array.pomodoro_time)[3], minutes, seconds);
            }
        } else {
            if (seconds < 10) {
                text = String.format(getResources().getStringArray(R.array.pomodoro_time)[4], minutes, seconds);
            } else {
                text = String.format(getResources().getStringArray(R.array.pomodoro_time)[2], minutes, seconds);
            }
        }

        return text;
    }

    private void updateNotification() {
        Notification notification = getNotification(notificationText());
        notificationManager.notify(BACKGROUND_NOTIFICATION_ID, notification);
    }

    //// TODO: 10-Apr-17
    public void notifyUser() {
        System.out.println("NOTIFY USER");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

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
        int mId = 1;
// mId allows you to update the notification later on.
        mNotificationManager.notify(mId, mBuilder.build());
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
        i.putExtra("continuous", continuous);
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
        onDestroy();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        System.out.println("KILL SERVICE");
        isRunning = false;
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }

    public void startPomodoro() {
        pomodoroTimerOn = true;
        timer = new PomodoroTimer(pomodoroTime * 60000 / 2, 1000);
        timer.start();
    }

    public void startBreak() {
        pomodoroTimerOn = false;
        if (sessionCount % sessionsTillLongBreak == 0) {
            timer = new BreakTimer(longBreakTime * 60000 / 2, 1000);
            timer.start();
        } else {
            timer = new BreakTimer(breakTime * 60000 / 2, 1000);
            timer.start();
        }

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

            if (clockSound) {
                if (seconds % 2 == 0) {
                    sp.play(tickSoundId, 1, 1, 1, 0, 1);
                } else {
                    sp.play(tockSoundId, 1, 1, 1, 0, 1);
                }
            }

            if (backgroundNotification) {
                System.out.println("SERVICE UPDATE POMODORO");
                updateNotification();
            }

            requestActionFromUser();
        }

        @Override
        public void onFinish() {
            minutes = 0;
            seconds = 0;
            timeleft = 0;
            sessionCount++;
            sp.play(notificationSoundId, 1, 1, 1, 0, 1);


            //Session session = new Session(pomodoroTime, breakTime, new Date().getTime());
            //FileHandler.save(this, session);
            //System.out.println(session.toJson());
            //System.out.println(new Date().getTime());
            backgroundNotificationOff();
            notifyUser();

            if (continuous) {
                startBreak();
            } else {
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

            if (backgroundNotification) {
                System.out.println("SERVICE UPDATE BREAK");
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

            backgroundNotificationOff();
            notifyUser();

            if (continuous) {
                startPomodoro();
            } else {
                requestActionFromUser(false);
            }
        }
    }
}
