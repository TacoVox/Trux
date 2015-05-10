package se.gu.tux.trux.application;

import com.jjoe64.graphview.series.DataPoint;

import se.gu.tux.trux.datastructure.MetricData;

/**
 * Created by jerker on 2015-04-28.
 */
public class DetailedStatsBundle {
    private MetricData today;
    private MetricData week;
    private MetricData month;
    private MetricData total;
    private DataPoint[] graphPoints;

    public DetailedStatsBundle(MetricData today, MetricData week, MetricData month, MetricData total, DataPoint[] graphPoints) {
        this.today = today;
        this.week = week;
        this.month = month;
        this.total = total;
        this.graphPoints = graphPoints;
    }

    public MetricData getToday() {
        return today;
    }

    public void setToday(MetricData today) {
        this.today = today;
    }

    public MetricData getWeek() {
        return week;
    }

    public void setWeek(MetricData week) {
        this.week = week;
    }

    public MetricData getMonth() {
        return month;
    }

    public void setMonth(MetricData month) {
        this.month = month;
    }

    public MetricData getTotal() {
        return total;
    }

    public void setTotal(MetricData total) {
        this.total = total;
    }

    public DataPoint[] getGraphPoints() {
        return graphPoints;
    }

    public void setGraphPoints(DataPoint[] graphPoints) {
        this.graphPoints = graphPoints;
    }
}
