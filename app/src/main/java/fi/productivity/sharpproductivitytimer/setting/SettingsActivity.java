package fi.productivity.sharpproductivitytimer.setting;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


/**
 * Settings activity.
 *
 * Allows user to change default settings.
 *
 * @author      Akash Singh
 * @version     %I%, %G%
 * @since       1.7
 */
public class SettingsActivity extends AppCompatActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
