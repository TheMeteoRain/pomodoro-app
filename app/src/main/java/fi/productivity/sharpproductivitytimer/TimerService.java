package fi.productivity.sharpproductivitytimer;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;

import java.util.Date;

public class TimerService extends Service {

    private boolean isRunning;
    private boolean clockSound;
    private boolean continuous;
    private int pomodoroTime;
    private int breakTime;
    private int minutes;
    private int seconds;
    private int notificationSoundId;
    private int tickSoundId;
    private int tockSoundId;

    private SoundPool sp;
    private CountDownTimer timer;

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
        binder = new LocalBinder(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sp = new SoundPool.Builder().build();
        } else {
            sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        }
        notificationSoundId = sp.load(this, R.raw.notification, 1);
        tickSoundId = sp.load(this, R.raw.tick, 1);
        tockSoundId = sp.load(this, R.raw.tock, 1);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
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
            }

            isRunning = true;
            pomodoroTimer();
        }

        return START_NOT_STICKY;
    }

    public void pomodoroTimer() {
        timer = new CountDownTimer(pomodoroTime * (60000 - 50000), 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                seconds = (int) ((millisUntilFinished / 1000) % 60);
                minutes = (int) ((millisUntilFinished / (1000*60)) % 60);

                if (clockSound) {
                    if (seconds % 2 == 0) {
                        sp.play(tickSoundId, 1, 1, 1, 0, 1);
                    } else {
                        sp.play(tockSoundId, 1, 1, 1, 0, 1);
                    }
                }

                requestActionFromUser();
            }

            @Override
            public void onFinish() {
                seconds = 0;
                sp.play(notificationSoundId, 1, 1, 1, 0, 1);
                requestActionFromUser(true);
                Session session = new Session(pomodoroTime, breakTime, new Date().getTime());
                //FileHandler.save(this, session);
                System.out.println(session.toJson());
                System.out.println(new Date().getTime());
                //breakTimer();
                //onDestroy();
            }
        }.start();
    }

    public void breakTimer() {
        timer = new CountDownTimer(breakTime * (60000 - 55000), 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                seconds = (int) ((millisUntilFinished / 1000) % 60);
                minutes = (int) ((millisUntilFinished / (1000*60)) % 60);

                requestActionFromUser();
            }

            @Override
            public void onFinish() {
                seconds = 0;
                sp.play(notificationSoundId, 1, 1, 1, 0, 1);
                requestActionFromUser(false);

                //pomodoroTimer();
                //onDestroy();
            }
        }.start();
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

    @Override
    public boolean stopService(Intent name) {
        onDestroy();
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        System.out.println("kill");
        isRunning = false;
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }
}
