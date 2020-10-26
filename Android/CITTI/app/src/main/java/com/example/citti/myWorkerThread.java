package com.example.citti;

import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.LogPrinter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.LayoutManager;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//https://blog.nikitaog.me/2014/10/18/android-looper-handler-handlerthread-ii/

public class myWorkerThread extends HandlerThread {
    private Handler mWorkerHandler;
    private Handler mResponseHandler;
    private static final String TAG = myWorkerThread.class.getSimpleName();
    private Map<XYPlot, CellularInfoUtility.Segment> mRequestMap = new HashMap<>();
    private Callback mCallback;
    private int SEGSIZE;

    public interface Callback{
        public void savePlot(XYPlot plot, float result_y_pos);
    }

    public myWorkerThread(Handler responseHandler, Callback callback, int seg){
        super(TAG);
        mResponseHandler = responseHandler;
        mCallback = callback;
        SEGSIZE = seg;
    }


    public void queueTask(XYPlot plot, CellularInfoUtility.Segment segment){
        mRequestMap.put(plot, segment);
        Log.d(TAG, Arrays.toString(segment.gis_info) + "added to the queue");
        mWorkerHandler.obtainMessage(0, plot).sendToTarget();  // 0 is dummy here
//        mWorkerHandler.dump(new LogPrinter(Log.DEBUG, "mWorkerHandler"), "PREFIX");
//        mResponseHandler.dump(new LogPrinter(Log.DEBUG, "mResponseHandler"), "PREFIX");

    }

    public void prepareHandler(){
        mWorkerHandler = new Handler(getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                XYPlot xyplot = (XYPlot) message.obj;
                CellularInfoUtility.Segment segment = mRequestMap.get(xyplot);
                Log.d(TAG, String.format("Processing %s", Arrays.toString(segment.gis_info)));
                handleRequest(xyplot,segment);
                //message.recycle();
                return true;
            }
        });
    }

    private void handleRequest(XYPlot xyplot, CellularInfoUtility.Segment segment) {
        Log.d("THREAD",Thread.currentThread().getName() + "- TID: " + String.valueOf(Thread.currentThread().getId()) + " with priority " + String.valueOf(Thread.currentThread().getPriority()));
        float result_y_pos = plot_graph(xyplot, segment);
        mRequestMap.remove(xyplot);
        xyplot.post(new Runnable() {
            @Override
            public void run() {
                mCallback.savePlot(xyplot, result_y_pos);
            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private float plot_graph(XYPlot xyplot, CellularInfoUtility.Segment segment){
        long startTime = SystemClock.uptimeMillis();
        double[] info = segment.gis_info;
        //info: [cx, cy, minx, miny, len]
        double cx = info[0];
        double cy = info[1];
        double minx = info[2];
        double miny = info[3];
        double len = info[4];
        SimpleXYSeries series1 = new SimpleXYSeries("series1");
        series1.addLast(cx,cy);
        SimpleXYSeries series2 = new SimpleXYSeries("series1");
        for(int i=0;i<segment.container.size();i++){
            series2.addLast(segment.container.get(i).cLon, segment.container.get(i).cLat );
        }


        xyplot.setDomainBoundaries(minx, minx+len, BoundaryMode.FIXED);   // x-axis
        xyplot.setRangeBoundaries(miny, miny+len, BoundaryMode.FIXED);    // y-axis

        // create formatters to use for drawing a series using LineAndPointRenderer
        // and configure them from xml:
        LineAndPointFormatter lpf1 = new LineAndPointFormatter(null, Color.RED, null, null);
        LineAndPointFormatter lpf2 = new LineAndPointFormatter(null, Color.argb(255/SEGSIZE,92, 55, 150), null, null);
        // add each series to the xyplot:
        xyplot.addSeries(series1, lpf1);
        xyplot.addSeries(series2, lpf2);

        LayoutManager l = xyplot.getLayoutManager();
        l.remove(xyplot.getDomainLabelWidget());
        l.remove(xyplot.getRangeLabelWidget());
        l.remove(xyplot.getLegendWidget());

        // Remove boarder
        XYGraphWidget g = xyplot.getGraphWidget();
        g.setDomainGridLinePaint(null);
        g.setRangeGridLinePaint(null);
        g.setRangeLabelWidth(0);
        g.setDomainLabelWidth(0);
        g.position(-0.5f, XLayoutStyle.RELATIVE_TO_RIGHT,
                -0.5f, YLayoutStyle.RELATIVE_TO_BOTTOM,
                AnchorPosition.CENTER);
        g.setSize(new SizeMetrics(
                0, SizeLayoutType.FILL,
                0, SizeLayoutType.FILL));

        g.setPadding(0, 0, 0, 0);
        g.setMargins(0, 0, 0, 0);
        g.setGridPadding(0, 0, 0, 0);
        xyplot.setPlotMargins(0, 0, 0, 0);
        xyplot.setPlotPadding(0, 0, 0, 0);

        xyplot.getLayoutParams().width = 400;
        xyplot.getLayoutParams().height = 400;

        float result_y_pos = (float)(miny + len*0.8f);
        long endTime = SystemClock.uptimeMillis();
        Log.d("Plot", "Timecost to plot: " + Long.toString(endTime - startTime));

        return result_y_pos;

    }
}
