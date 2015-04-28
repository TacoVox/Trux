package se.gu.tux.trux.gui.detailedStats;

import android.app.Fragment;

import se.gu.tux.trux.appplication.DetailedStatsBundle;

/**
 * Created by dennis on 2015-04-24.
 */
public abstract class DetailedStatsFragment extends Fragment {
    public abstract void setValues(final DetailedStatsBundle stats);
    public abstract void hideLoading();
    public abstract boolean hasLoaded();
}
