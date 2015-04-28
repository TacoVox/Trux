package se.gu.tux.trux.gui.detailedStats;

import android.app.Fragment;
import android.view.View;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import se.gu.tux.trux.datastructure.DetailedStatsBundle;
import se.gu.tux.trux.datastructure.MetricData;
import tux.gu.se.trux.R;

/**
 * Created by dennis on 2015-04-24.
 */
public abstract class DetailedStatsFragment extends Fragment {
    public abstract void setValues(final DetailedStatsBundle stats);
    public abstract void hideLoading();
}
