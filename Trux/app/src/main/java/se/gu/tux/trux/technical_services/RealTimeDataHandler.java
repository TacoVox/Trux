package se.gu.tux.trux.technical_services;

import se.gu.tux.trux.datastructure.Data;
import se.gu.tux.trux.datastructure.Distance;
import se.gu.tux.trux.datastructure.Fuel;
import se.gu.tux.trux.datastructure.MetricData;
import se.gu.tux.trux.datastructure.Speed;


/**
 * Acts as an interface for the real time data. To get AGA values, ask getSignalData().
 * This class also defines what goes into the queue every POLL_INTERVAL seconds by DataPoller;
 * currently some metric data from AGA and location data from LocationService.
 *
 * Created by ivryashkov on 2015-03-24.
 */

public class RealTimeDataHandler
{
    AGADataParser rtdp;
    LocationService locationService;


    /**
     * Constructor.
     * @param locationService   The locationservice object.
     */
    public RealTimeDataHandler(LocationService locationService)
    {
        //rtdp = RealTimeDataParser.getInstance();
        this.locationService = locationService;
        rtdp = AGADataParser.getInstance();

    }

    /**
     * This method packages anything we want to send to the server - called by DataPoller every
     * POLL_INTERVAL seconds.
     * @return      An array with all the realtime data that is sent to the server regularly.
     */
    public Data[] getCurrentMetrics() {
        Data metricArray[] = new Data[4];
        metricArray[0] = getSignalData(new Fuel(0));
        metricArray[1] = getSignalData(new Speed(0));
        metricArray[2] = getSignalData(new Distance(0));
        metricArray[3] = locationService.getLocation();

        // Set timestamp for all data
        for (Data d : metricArray) {
            d.setTimeStamp(System.currentTimeMillis());
        }
        return metricArray;
    }


    /**
     * Returns signal data from AGA by providing a MetricData object.
     * @param md    A MetricData object with the corresponding signal id.
     * @return      A MetricData object with the corresponding value
     */
    public MetricData getSignalData(MetricData md) {
        md.setValue(rtdp.getValue(md.getSignalId()));
        md.setTimeStamp(System.currentTimeMillis());
        return md;
    }
} // end class RealTimeDataHandler
