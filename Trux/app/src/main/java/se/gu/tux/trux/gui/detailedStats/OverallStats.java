package se.gu.tux.trux.gui.detailedStats;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.os.Bundle;

import se.gu.tux.trux.appplication.DataHandler;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.Speed;
import se.gu.tux.trux.gui.detailedStats.FragmentPageAdapterOverall;
import tux.gu.se.trux.R;

public class OverallStats extends FragmentActivity {

    private ViewPager viewpager;
    private volatile FragmentPageAdapterOverall ft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewpager = new ViewPager(this);
        viewpager.setId(R.id.pager);
        setContentView(viewpager);
        ft = new FragmentPageAdapterOverall(getSupportFragmentManager());
        viewpager.setAdapter(ft);

        Thread t = new Thread(new Runnable() {
            Speed s = new Speed(0);
            Fuel f = new Fuel(0);
            Distance d = new Distance(0);

            @Override
            public void run() {
                // Wait for ALL bundles to be cached by datahandler
                while (!(DataHandler.getInstance().detailedStatsReady(s)
                        && DataHandler.getInstance().detailedStatsReady(f)
                        && DataHandler.getInstance().detailedStatsReady(d)
                        && ft.getOGW().hasLoaded()
                        && ft.getOTW().hasLoaded())) {
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ft.getOTW().setValues(DataHandler.getInstance().getDetailedStats(s),
                                DataHandler.getInstance().getDetailedStats(f),
                                DataHandler.getInstance().getDetailedStats(d));
                        ft.getOGW().setValues(DataHandler.getInstance().getDetailedStats(s),
                                DataHandler.getInstance().getDetailedStats(f),
                                DataHandler.getInstance().getDetailedStats(d));
                    }
                });
            }
        });
        t.start();


        /*viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageSelected(int arg0) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }

        });
        */
    }
}
