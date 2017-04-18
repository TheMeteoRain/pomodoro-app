package fi.productivity.sharpproductivitytimer;

import android.os.Binder;

/**
 * Created by Akash on 16-Mar-17.
 */

class LocalBinder extends Binder {

    private TimerService service;

    public LocalBinder(TimerService timerService) {
        service = timerService;
    }

    public TimerService getService() {
        return service;
    }

}
