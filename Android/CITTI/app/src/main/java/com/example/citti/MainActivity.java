package com.example.citti;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.XPositionMetric;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.*;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity implements myWorkerThread.Callback{

    private LinearLayout mLayoutTop;
    private LinearLayout mLayout;
    private MapView mMapView;
    private ArcGISMap mMap;
    private FeatureLayer mFeatureLayer;
    private Button mTestBtn;
    private GraphicsOverlay mGraphicsOverlay;
    private Interpreter mTflite;
    private myWorkerThread mWorkerThread;
    private CellularInfoUtility mCellularInfoUtility;

    private final int SRC_FILE = 0;
    private final int SRC_SIM = 1;
    private int CELL_INFO_SRC = SRC_SIM;
    private boolean mTesting = false;

    private int NUM_CLASS = 5;
    private float[][] labelProbArray = new float[1][NUM_CLASS];

    private int SEGSIZE = 20;
    private final String[] LABELS = {"Other","HSR", "MRT", "RW", "HW"};

    private double XMIN = 120;
    private double XMAX = 122;
    private double YMIN = 21.5;
    private double YMAX = 25.5;

    private final int LOCATION_REQUEST_CODE = 999;  // self-defined

    // For performance measurement
    private long mStartTime=0;
    private long mEndTime=0;
    private long mAcuTime=0;
    private int mCounter=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLayoutTop = (LinearLayout) findViewById(R.id.layout_top);
        mLayout = (LinearLayout) findViewById(R.id.layout);
        mTestBtn = (Button) findViewById(R.id.btn_test);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE+1);
        }

        // Load tflite model
        loadTfLiteModel();

        // Init DB
        new DBHelper(this, getApplicationContext(), "clear").execute();
        new DBHelper(this, getApplicationContext(), "init").execute();

        // Read a shapefile
        ShapefileFeatureTable shapefileFeatureTable = new ShapefileFeatureTable(getString(R.string.shapefile_path));
        mFeatureLayer = new FeatureLayer(shapefileFeatureTable);

//        while(true){
//            cellInfoHelper.fromFile();
//            cellInfoHelper.container.print();
//            SystemClock.sleep(500);
//
//            if(cellInfoHelper.container.size()<SEGSIZE)
//                continue;
//
//            // Accumulated enough segments
//            // Calculate GIS info for reference points
//            Point p = new Point(cellInfoHelper.container.get(0).cLon,cellInfoHelper.container.get(0).cLat);    //Modify: (clat, clng)

//        }
//        Point p = new Point(121.685653,24.537865);
//        double[] gisInfo = getGisInfo(p);   //(cx, cy, minx, miny, len)
//        Log.d("GIS", Arrays.toString(gisInfo));
        // Render GIS info
        //RenderGisInfo(gisInfo);



//        mPlot = (XYPlot) findViewById(R.id.plot);
//        Point p = new Point(120.59879,23.689012);
//        double[] gisInfo = getGisInfo(p);   //(cx, cy, minx, miny, len)
//        Log.d("GIS", Arrays.toString(gisInfo));
//        plot_graph(gisInfo);
//        savePlot myRunnable = new savePlot();
//        mPlot.post(myRunnable);     // save image after the view is ready


//        Log.d("TESTTEST","TEST");
//        p = new Point(120.993339,24.038148);
//        gisInfo = getGisInfo(p);   //(cx, cy, minx, miny, len)
//        Log.d("GIS", Arrays.toString(gisInfo));
//        plot_graph(gisInfo);
//        myRunnable = new savePlot();
//        mPlot.post(myRunnable);     // save image after the view is ready





    }

    private void loadTfLiteModel() {
        try {
            mTflite = new Interpreter(ModelUtility.loadModelFile(this));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("Init","Model loaded");
    }

    @Override
    protected void onDestroy() {
        mWorkerThread.quit();
        super.onDestroy();
    }


    private void next_round() {
        mStartTime = SystemClock.uptimeMillis();

        // Get cellular Information
        if(CELL_INFO_SRC==SRC_FILE){
            mCellularInfoUtility.fromFile();
        }
        else if(CELL_INFO_SRC==SRC_SIM){
            mCellularInfoUtility.fromSim();
        }
        Point p = new Point(mCellularInfoUtility.segment.container.get(0).cLon,mCellularInfoUtility.segment.container.get(0).cLat);    //Modify: (clat, clng)
        mCellularInfoUtility.segment.gis_info = getGisInfo(p);   //(cx, cy, minx, miny, len)

        XYPlot xyplot = new XYPlot(getApplicationContext(),"test");
        xyplot.setVisibility(View.INVISIBLE);
        mLayout.addView(xyplot);

        mWorkerThread.queueTask(xyplot, mCellularInfoUtility.segment);
    }

    @Override
    public void savePlot(XYPlot plot, float result_y_pos){
        Log.d("THREAD",Thread.currentThread().getName() + "- TID: " + String.valueOf(Thread.currentThread().getId()) + " with priority " + String.valueOf(Thread.currentThread().getPriority()));
        Log.d("CALLBACK","Callback function!!");
        Bitmap b = Bitmap.createBitmap(plot.getWidth(), plot.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(b);
        plot.draw(canvas);
        Bitmap b2 = Bitmap.createScaledBitmap(b, 128, 128, false);

        new TextView(getApplicationContext());

        // test file
        //Bitmap b_test = BitmapFactory.decodeFile("/data/data/com.example.citti/files/4.png");

        ByteBuffer imgData = ModelUtility.convertBitmapToByteBuffer(b2);

        long startTime = SystemClock.uptimeMillis();
        mTflite.run(imgData, labelProbArray);
        long endTime = SystemClock.uptimeMillis();
        Log.d("Inference", "Timecost to run model inference: " + Long.toString(endTime - startTime));
        Log.d("Inference", Arrays.toString(labelProbArray[0]));


        // Render Result
        Paint text_paint = new Paint();
        text_paint.setColor(Color.BLACK);
        text_paint.setTextSize(PixelUtils.spToPix(25));
        Paint line_paint = new Paint();
        line_paint.setColor(Color.TRANSPARENT);
        List<Float> fList = new ArrayList<>();
        for (int i = 0; i < labelProbArray[0].length; i++) {
            fList.add(labelProbArray[0][i]);
        }
        float vMax = Collections.max(fList);
        int idx = fList.indexOf(vMax);
        YValueMarker resultMarker = new YValueMarker(result_y_pos, LABELS[idx], new XPositionMetric(0.1f, XLayoutStyle.RELATIVE_TO_LEFT), line_paint, text_paint);
        plot.addMarker(resultMarker);



        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mLayout.getChildCount()>3){
                    mLayout.removeAllViews();
                    mLayout.addView(plot);
                }
                plot.setVisibility(View.VISIBLE);

                if(mTesting==true){
                    mEndTime = SystemClock.uptimeMillis();
                    mAcuTime += mEndTime - mStartTime;
                    mCounter++;
                    Log.d("TIME","Average process time: "+ String.valueOf(mAcuTime/mCounter) + "ms (" + String.valueOf(mCounter) +" times)");
                    next_round();
                }
            }
        },1000);



//            mPlot.clear();      // ???
//            mPlot.redraw();     //???



        // Save a graph
//            Log.d("OUPUT_PATH",getString(R.string.output_path));
//            try(FileOutputStream fos = new FileOutputStream(getString(R.string.output_path))) {
//                Bitmap b2 = Bitmap.createScaledBitmap(b, 128, 128, false);
//                b2.compress(Bitmap.CompressFormat.PNG, 100, fos);
//            } catch (IOException e) {
//                Log.d("ERR", "Exception");
//                e.printStackTrace();
//            }
    }

//    private void renderShapefile() {
//        // inflate MapView from layout
//        mMapView = findViewById(R.id.mapView);
//        mMap = new ArcGISMap(Basemap.createOpenStreetMap());
//        Viewpoint viewpoint = new Viewpoint(24, 121, 5000000);  // set an initial viewpoint
//        mMap.setInitialViewpoint(viewpoint);
//        mMapView.setMap(mMap);
//
//        // create the Symbol
//        SimpleLineSymbol lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 1.0f);
//        SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.YELLOW, lineSymbol);
//        // add the point with a symbol to graphics overlay and add overlay to map view
//
//        // create the Renderer
//        SimpleRenderer renderer = new SimpleRenderer(fillSymbol);
//
//        // set the Renderer on the Layer
//        mFeatureLayer.setRenderer(renderer);
//
//        // add the feature layer to the map
//        mMap.getOperationalLayers().add(mFeatureLayer);
//    }

    private double[] getGisInfo(Point p){
        // Find the geometry that p belongs to
        QueryParameters query = new QueryParameters();
        query.setGeometry(p);
        query.setSpatialRelationship(QueryParameters.SpatialRelationship.WITHIN);
        final ListenableFuture<FeatureQueryResult> future = mFeatureLayer.selectFeaturesAsync(query, FeatureLayer.SelectionMode.NEW);

        double[] gisInfo = new double[]{0,0,0,0,0};   //cx, cy, minx, miny, len

        FeatureQueryResult result = null;
        try {
            result = future.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        Iterator<Feature> iterator = result.iterator();
        Feature feature;

        if (iterator.hasNext()){    //"If" instead of "While": only consider the first queried geometry
            feature = iterator.next();
            Polygon poly = (Polygon) feature.getGeometry();
            Log.d("Centroid", GeometryEngine.labelPoint(poly).toJson());
            try {
                // centroid
                JSONObject lp_obj = new JSONObject(GeometryEngine.labelPoint(poly).toJson());
                gisInfo[0] = lp_obj.getDouble("x");     // cx
                gisInfo[1] = lp_obj.getDouble("y");     // cy
                // bounding
                Log.d("CONVEX", GeometryEngine.convexHull(feature.getGeometry()).toJson());
                List<Double> Xs = new ArrayList<Double>();
                List<Double> Ys = new ArrayList<Double>();
                JSONObject obj = new JSONObject(GeometryEngine.convexHull(feature.getGeometry()).toJson());
                JSONArray ring_arr = obj.getJSONArray("rings");
                for (int i = 0; i < ring_arr.length(); i++)
                {
                    JSONArray point_arr = ring_arr.getJSONArray(i);
                    for (int j = 0; j<point_arr.length(); j++){
                        JSONArray point = point_arr.getJSONArray(j);
                        Xs.add(point.getDouble(0));
                        Ys.add(point.getDouble(1));
                    }
                }
                double minx = Collections.min(Xs);   // min x
                minx = max(XMIN, minx);
                double miny = Collections.min(Ys);   // min y
                miny = max(YMIN, miny);
                double maxx = Collections.max(Xs);   // max x
                maxx = min(XMAX, maxx);
                double maxy = Collections.max(Ys);   // max y
                maxy = min(YMAX, maxy);
                double lenx = maxx - minx;
                double leny = maxy - miny;
                double square_len = max(lenx, leny);
                gisInfo[2] = minx;
                gisInfo[3] = miny;
                gisInfo[4] = square_len;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return gisInfo;
    }


    private void RenderGisInfo(double[] info) {
        //info: [cx, cy, minx, miny, len]
        mGraphicsOverlay = new GraphicsOverlay();
        double minx = info[2];
        double miny = info[3];
        double square_len = info[4];
        Point c = new Point(info[0],info[1], SpatialReferences.getWgs84());
        Point p0 = new Point(minx,miny, SpatialReferences.getWgs84());
        Point p1 = new Point(minx,miny+square_len, SpatialReferences.getWgs84());
        Point p2 = new Point(minx+square_len,miny, SpatialReferences.getWgs84());
        Point p3 = new Point(minx+square_len,miny+square_len, SpatialReferences.getWgs84());
        SimpleMarkerSymbol pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.RED, 10.0f);
        Graphic pointGraphic_c = new Graphic(c, pointSymbol);
        Graphic pointGraphic_p0 = new Graphic(p0, pointSymbol);
        Graphic pointGraphic_p1 = new Graphic(p1, pointSymbol);
        Graphic pointGraphic_p2 = new Graphic(p2, pointSymbol);
        Graphic pointGraphic_p3 = new Graphic(p3, pointSymbol);
        mGraphicsOverlay.getGraphics().addAll(Arrays.asList(pointGraphic_c, pointGraphic_p0, pointGraphic_p1, pointGraphic_p2, pointGraphic_p3));
        mMapView.getGraphicsOverlays().add(mGraphicsOverlay);
    }

    public void QueryDB(View view) {
        new DBHelper(this, getApplicationContext(), "query").execute();
    }

    public void InitDB(View view) {
        new DBHelper(this, getApplicationContext(), "init").execute();
    }

    public void ClearDB(View view) {
        new DBHelper(this, getApplicationContext(), "clear").execute();
    }

    public void Test(View view) {
        if(mTesting==false){
            mTestBtn.setText("Stop");
            mTesting = true;

            //Collect cellular information
            mCellularInfoUtility = new CellularInfoUtility(SEGSIZE, getApplicationContext(), this);
            mWorkerThread = new myWorkerThread(new Handler(Looper.getMainLooper()),this, SEGSIZE);
            mWorkerThread.start();
            mWorkerThread.prepareHandler();
            next_round();
        }
        else{
            mTestBtn.setText("Test");
            mTesting = false;
        }

        // Output graph
//        Log.d("TEST", String.valueOf(mPlot.getWidth()));
//        Bitmap b = Bitmap.createBitmap(mPlot.getWidth(), mPlot.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(b);
//        mPlot.draw(canvas);
//        Log.d("TEST",getString(R.string.output_path));
//        try(FileOutputStream fos = new FileOutputStream(getString(R.string.output_path))) {
//            Bitmap b2 = Bitmap.createScaledBitmap(b, 128, 128, false);
//            b2.compress(Bitmap.CompressFormat.PNG, 100, fos);
//        } catch (IOException e) {
//            Log.d("ERR", "Exception");
//            e.printStackTrace();
//        }

    }


//    @Override
//    protected void onPause() {
//        super.onPause();
//        mMapView.pause();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        mMapView.resume();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        mMapView.dispose();
//    }
}