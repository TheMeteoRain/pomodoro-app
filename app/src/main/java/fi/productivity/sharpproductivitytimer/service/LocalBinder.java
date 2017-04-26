package fi.productivity.sharpproductivitytimer.service;

import android.os.Binder;

/**
 * Created by Akash on 16-Mar-17.
 */

public class LocalBinder extends Binder {

    private TimerService service;

    public LocalBinder(TimerService timerService) {
        service = timerService;
    }

    public TimerService getService() {
        return service;
    }

}
