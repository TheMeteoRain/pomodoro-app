package fi.productivity.sharpproductivitytimer.service;

import android.os.Binder;


/**
 * Delegates Time Service.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public class LocalBinder extends Binder {

    /**
     * Time Service.
     */
    private TimerService service;

    /**
     * Set Time Service.
     *
     * @param timerService Time Service.
     */
    public LocalBinder(TimerService timerService) {
        service = timerService;
    }

    /**
     * Get Time Service.
     *
     * @return Time Service.
     */
    public TimerService getService() {
        return service;
    }

}
