package fi.productivity.sharpproductivitytimer;

import android.graphics.Color;
import android.os.Bundle;
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
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import org.json.JSONArray;

import java.util.Date;
import java.util.List;

import fi.productivity.sharpproductivitytimer.data.Data;
import fi.productivity.sharpproductivitytimer.data.DataHandler;
import fi.productivity.sharpproductivitytimer.utils.Debug;
import fi.productivity.sharpproductivitytimer.utils.Utils;

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
                createStatsForTab(rootView, dataHandler.getToday());
                drawWeeklyGraph(rootView);
            }
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 2) {
                createStatsForTab(rootView, dataHandler.getWeek());
                drawWeeklyGraph(rootView);
            }
            if (getArguments().getInt(ARG_SECTION_NUMBER) == 3) {
                createStatsForTab(rootView, dataHandler.getTotal());
            }
            return rootView;
        }

        private void createStatsForTab(View rootView, Data data) {
            TextView textSessionTotal = (TextView) rootView.findViewById(R.id.stat_session_total);
            TextView textSessionCompleted = (TextView) rootView.findViewById(R.id.stat_session_completed);
            TextView textSessionStopped = (TextView) rootView.findViewById(R.id.stat_session_stopped);
            TextView textTimePomodoro = (TextView) rootView.findViewById(R.id.stat_time_pomodoro);
            TextView textTimeBreak = (TextView) rootView.findViewById(R.id.stat_time_break);
            TextView textWeekTitle = (TextView) rootView.findViewById(R.id.stat_weekly_title);
            GraphView graph = (GraphView) rootView.findViewById(R.id.graph);

            textSessionTotal.setText(String.format(getString(R.string.stat_session_total), data.getSessionsTotal()));
            textSessionCompleted.setText(String.format(getString(R.string.stat_session_completed), data.getSessionsCompleted()));
            textSessionStopped.setText(String.format(getString(R.string.stat_session_stopped), data.getSessionsStopped()));
            textTimePomodoro.setText(Utils.formatTimerByHour(getResources(), R.string.stat_time_pomodoro_title, data.getPomodoroTimeHours(), data.getPomodoroTimeMinutes(), data.getPomodoroTimeSeconds()));
            textTimeBreak.setText(Utils.formatTimerByHour(getResources(), R.string.stat_time_break_title, data.getBreakTimeHours(), data.getBreakTimeMinutes(), 0));
            graph.setVisibility(View.INVISIBLE);
            textWeekTitle.setVisibility(View.INVISIBLE);
        }

        private void drawWeeklyGraph(View rootView) {
            GraphView graph = (GraphView) rootView.findViewById(R.id.graph);
            TextView textWeekTitle = (TextView) rootView.findViewById(R.id.stat_weekly_title);
            graph.setVisibility(View.VISIBLE);
            textWeekTitle.setVisibility(View.VISIBLE);

            List<Data> weekly = dataHandler.getWeekly();
            DataPoint[] dataPoints = new DataPoint[7];
            for (int i = 0; i < weekly.size(); i++) {
                Data d = weekly.get(i);
                double minutes = d.getPomodoroTimeMinutes() + d.getPomodoroTimeHours() * 60 + d.getPomodoroTimeSeconds() / 100d;
                minutes = (double) Math.round(minutes * 100) / 100;
                dataPoints[i] = new DataPoint(new Date(d.getTime()), minutes);
            }

            for (int i = 0; i < weekly.size(); i++) {
                Debug.print("StatActivity", new Date(weekly.get(i).getTime()) + "   " + i + "  " + weekly.get(i).getTime(), 3, false, getContext());
            }
            Debug.print("StatActivity", "Size " + weekly.size(), 3, false, getContext());

            BarGraphSeries<DataPoint> series = new BarGraphSeries<>(dataPoints);
            graph.addSeries(series);

            series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                @Override
                public int get(DataPoint data) {
                    return Color.rgb((int) data.getX()*255/4, (int) Math.abs(data.getY()*255/6), 100);
                }
            });

            series.setSpacing(10);
            series.setDrawValuesOnTop(true);
            series.setValuesOnTopColor(Color.RED);
            graph.addSeries(series);

            // set date label formatter
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
            graph.getGridLabelRenderer().setNumHorizontalLabels(3); // only 4 because of the space
            graph.getGridLabelRenderer().setVerticalAxisTitle("min");
            graph.getGridLabelRenderer().setVerticalAxisTitleTextSize(60);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMaxY(series.getHighestValueY() + series.getHighestValueY() / 10);

            // set manual x bounds to have nice steps
            graph.getViewport().setMinX(dataHandler.getFirstDayOfTheWeekStart());
            graph.getViewport().setMaxX(dataHandler.getLastDayOfTheWeekEnd());
            graph.getViewport().setXAxisBoundsManual(true);

            // as we use dates as labels, the human rounding to nice readable numbers
            // is not necessary
            graph.getGridLabelRenderer().setHumanRounding(false);
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
                    return "TODAY";
                case 1:
                    return "THIS WEEK";
                case 2:
                    return "TOTAL";
            }
            return null;
        }
    }
}
