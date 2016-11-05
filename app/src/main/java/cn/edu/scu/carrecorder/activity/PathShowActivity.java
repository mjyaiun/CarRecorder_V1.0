package cn.edu.scu.carrecorder.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.overlay.DrivingRouteOverlay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveStep;
import com.amap.api.trace.LBSTraceClient;
import com.amap.api.trace.TraceListener;
import com.amap.api.trace.TraceLocation;
import com.amap.api.trace.TraceOverlay;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import butterknife.ButterKnife;
import butterknife.InjectView;
import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.classes.CLatLonPoint;

public class PathShowActivity extends AppCompatActivity {
    @InjectView(R.id.path_name)
    TextView tv_pathname;
    List<CLatLonPoint> points;
    MapView mapView;
    AMap aMap;
    LBSTraceClient lbsClient;

    List<TraceLocation> traceLocations = new ArrayList<>();
    List<LatLonPoint> mapPoints = new ArrayList<>();

    private ConcurrentMap<Integer, TraceOverlay> mOverlayList = new ConcurrentHashMap<Integer, TraceOverlay>();
    private int mSequenceLineID = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_path_show);
        ButterKnife.inject(this);
        initToolbar();
        //getTraceLocations();
        getPoints();

        mapView = (MapView) findViewById(R.id.mapview_path);
        mapView.onCreate(savedInstanceState);

        aMap = mapView.getMap();

        drawOverlay();

        //drawLines(mapPoints);
        //traceGrasp();
    }

    private void drawOverlay() {
        CameraUpdate newPos = CameraUpdateFactory.newCameraPosition(new CameraPosition(
                                    points.get((points.size()-1) / 2).toMapPoint(), 18, 0, 0));
        aMap.animateCamera(newPos);

        DrivePath path = new DrivePath();
        List<DriveStep> steps = new ArrayList<>();
        for (int i=0; i < mapPoints.size() - 1; i += 2) {
            DriveStep step = new DriveStep();
            List<LatLonPoint> temp = new ArrayList<>();
            temp.add(mapPoints.get(i));
            temp.add(mapPoints.get(i + 1));
            step.setPolyline(temp);
            steps.add(step);
        }
        if (mapPoints.size() % 2 == 1) {
            DriveStep step = new DriveStep();
            List<LatLonPoint> temp = new ArrayList<>();
            temp.add(mapPoints.get(mapPoints.size()-1));
            step.setPolyline(temp);
            steps.add(step);
        }
        path.setSteps(steps);
        DrivingRouteOverlay overlay = new DrivingRouteOverlay(this, aMap, path, mapPoints.get(0), mapPoints.get(mapPoints.size() - 1));
        overlay.setNodeIconVisibility(false);
        overlay.addToMap();
    }

    private void drawLines(List<LatLng> points) {
        if (points.size() <= 1) {
            return;
        }
        int i;
        for (i = 1; i < points.size(); i ++) {
            drawLine(points.get(i-1), points.get(i));
        }
        CameraUpdate newPos = CameraUpdateFactory.newCameraPosition(new CameraPosition(points.get((int)((i-1) / 2)), 18, 0, 0));
        aMap.animateCamera(newPos);
    }

    private void drawLine(LatLng oldData,LatLng newData ) {
        // 绘制一个大地曲线
        aMap.addPolyline((new PolylineOptions())
                .add(oldData, newData)
                .geodesic(true).color(Color.BLUE));

    }

    public void getPoints() {
        points = getIntent().getParcelableArrayListExtra("Points");
        for (CLatLonPoint point: points
                ) {
            mapPoints.add(point.toLatLonPoint());
        }
    }

    /**
     * 调起一次轨迹纠偏
     */
    private void traceGrasp() {
        if (mOverlayList.containsKey(mSequenceLineID)) {
            TraceOverlay overlay = mOverlayList.get(mSequenceLineID);
            overlay.zoopToSpan();
            int status = overlay.getTraceStatus();
            String tipString = "";
            if (status == TraceOverlay.TRACE_STATUS_PROCESSING) {
                tipString = "该线路轨迹纠偏进行中...";
            } else if (status == TraceOverlay.TRACE_STATUS_FINISH) {
                tipString = "该线路轨迹已完成";
            } else if (status == TraceOverlay.TRACE_STATUS_FAILURE) {
                tipString = "该线路轨迹失败";
            } else if (status == TraceOverlay.TRACE_STATUS_PREPARE) {
                tipString = "该线路轨迹纠偏已经开始";
            }
            Toast.makeText(this.getApplicationContext(), tipString,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        TraceOverlay mTraceOverlay = new TraceOverlay(aMap);
        mOverlayList.put(mSequenceLineID, mTraceOverlay);
        List<LatLng> mapList = traceLocationToMap(traceLocations);
        mTraceOverlay.setProperCamera(mapList);

        lbsClient = new LBSTraceClient(this.getApplicationContext());
        lbsClient.queryProcessedTrace(mSequenceLineID, traceLocations,
                LBSTraceClient.TYPE_AMAP, new TraceListener() {
                    @Override
                    public void onRequestFailed(int lineID, String errorInfo) {
                        Toast.makeText(getApplicationContext(), errorInfo,
                                Toast.LENGTH_SHORT).show();
                        if (mOverlayList.containsKey(lineID)) {
                            TraceOverlay overlay = mOverlayList.get(lineID);
                            overlay.setTraceStatus(TraceOverlay.TRACE_STATUS_FAILURE);
                        }
                    }

                    @Override
                    public void onTraceProcessing(int lineID, int index, List<LatLng> segments) {
                        if (segments == null) {
                            return;
                        }
                        if (mOverlayList.containsKey(lineID)) {
                            TraceOverlay overlay = mOverlayList.get(lineID);
                            overlay.setTraceStatus(TraceOverlay.TRACE_STATUS_PROCESSING);
                            overlay.add(segments);
                        }
                    }

                    @Override
                    public void onFinished(int lineID, List<LatLng> linepoints, int distance,
                                           int watingtime) {
                        if (mOverlayList.containsKey(lineID)) {
                            TraceOverlay overlay = mOverlayList.get(lineID);
                            overlay.setTraceStatus(TraceOverlay.TRACE_STATUS_FINISH);
                            overlay.setDistance(distance);
                            overlay.setWaitTime(watingtime);
                            overlay.zoopToSpan();
                        }

                    }
                });
    }

    public List<LatLng> traceLocationToMap(List<TraceLocation> traceLocationList) {
        List<LatLng> mapList = new ArrayList<LatLng>();
        for (TraceLocation location : traceLocationList) {
            LatLng latlng = new LatLng(location.getLatitude(),
                    location.getLongitude());
            mapList.add(latlng);
        }
        return mapList;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    private void getTraceLocations() {
        points = getIntent().getParcelableArrayListExtra("Points");
        for (CLatLonPoint point: points
             ) {
            traceLocations.add(point.toAMapTraceLocation());
        }
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_path);
        setSupportActionBar(toolbar);
        String pathname = getIntent().getStringExtra("PathName");
        tv_pathname.setText(pathname);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
