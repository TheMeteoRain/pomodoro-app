package fi.productivity.sharpproductivitytimer;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;

import fi.productivity.sharpproductivitytimer.data.Data;
import fi.productivity.sharpproductivitytimer.data.DataHandler;

public class StatActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private static DataHandler dataHandler;
    private static JSONArray data;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);

        dataHandler = new DataHandler(getApplicationContext());
        data = dataHandler.getData();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_stat, container, false);
           // textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 1) {
                TextView textSessionTotal = (TextView) rootView.findViewById(R.id.stat_session_total);
                TextView textSessionCompleted = (TextView) rootView.findViewById(R.id.stat_session_completed);
                TextView textSessionStopped = (TextView) rootView.findViewById(R.id.stat_session_stopped);
                TextView textTimePomodoro = (TextView) rootView.findViewById(R.id.stat_time_pomodoro);
                TextView textTimeBreak = (TextView) rootView.findViewById(R.id.stat_time_break);
                GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

                Data today = dataHandler.getToday();

                textSessionTotal.setText(String.format(getString(R.string.stat_session_total), today.getSessionsTotal()));
                textSessionCompleted.setText(String.format(getString(R.string.stat_session_completed), today.getSessionsCompleted()));
                textSessionStopped.setText(String.format(getString(R.string.stat_session_stopped), today.getSessionsStopped()));
                textTimePomodoro.setText(String.format(getString(R.string.stat_time_pomodoro), today.getPomodoroTimeMinutes(), today.getPomodoroTimeSeconds()));
                textTimeBreak.setText(String.format(getString(R.string.stat_time_break), today.getBreakTime()));

                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                        new DataPoint(0, 1),
                        new DataPoint(3, 5),
                        new DataPoint(6, 7),
                });
                graph.addSeries(series);
            }
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                TextView textSessionTotal = (TextView) rootView.findViewById(R.id.stat_session_total);
                TextView textSessionCompleted = (TextView) rootView.findViewById(R.id.stat_session_completed);
                TextView textSessionStopped = (TextView) rootView.findViewById(R.id.stat_session_stopped);
                TextView textTimePomodoro = (TextView) rootView.findViewById(R.id.stat_time_pomodoro);
                TextView textTimeBreak = (TextView) rootView.findViewById(R.id.stat_time_break);
                Data week = dataHandler.getWeek();

                textSessionTotal.setText(String.format(getString(R.string.stat_session_total), week.getSessionsTotal()));
                textSessionCompleted.setText(String.format(getString(R.string.stat_session_completed), week.getSessionsCompleted()));
                textSessionStopped.setText(String.format(getString(R.string.stat_session_stopped), week.getSessionsStopped()));
                textTimePomodoro.setText(String.format(getString(R.string.stat_time_pomodoro), week.getPomodoroTimeMinutes(), week.getPomodoroTimeSeconds()));
                textTimeBreak.setText(String.format(getString(R.string.stat_time_break), week.getBreakTime()));
            }
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
                TextView textSessionTotal = (TextView) rootView.findViewById(R.id.stat_session_total);
                TextView textSessionCompleted = (TextView) rootView.findViewById(R.id.stat_session_completed);
                TextView textSessionStopped = (TextView) rootView.findViewById(R.id.stat_session_stopped);
                TextView textTimePomodoro = (TextView) rootView.findViewById(R.id.stat_time_pomodoro);
                TextView textTimeBreak = (TextView) rootView.findViewById(R.id.stat_time_break);
                Data total = dataHandler.getTotal();

                textSessionTotal.setText(String.format(getString(R.string.stat_session_total), total.getSessionsTotal()));
                textSessionCompleted.setText(String.format(getString(R.string.stat_session_completed), total.getSessionsCompleted()));
                textSessionStopped.setText(String.format(getString(R.string.stat_session_stopped), total.getSessionsStopped()));

                textTimePomodoro.setText(String.format(getString(R.string.stat_time_pomodoro), total.getPomodoroTimeMinutes(), total.getPomodoroTimeSeconds()));
                textTimeBreak.setText(String.format(getString(R.string.stat_time_break), total.getBreakTime()));
            }
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "THIS DAY";
                case 1:
                    return "THIS WEEK";
                case 2:
                    return "TOTAL";
            }
            return null;
        }
    }
}
