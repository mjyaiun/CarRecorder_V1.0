package cn.edu.scu.carrecorder.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.MyLocationStyle;
import com.amap.api.maps2d.model.PolylineOptions;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.edu.scu.carrecorder.R;
import cn.edu.scu.carrecorder.classes.WheelPath;
import cn.edu.scu.carrecorder.util.PublicDate;

public class LocateFragment extends Fragment implements LocationSource,
        AMapLocationListener{
    /**
     * AMapV2地图中介绍如何使用mapview显示地图
     */
//声明变量
    private View view;
    private UiSettings mUiSettings;  //控制ui
    private MapView mapView;
    private AMap aMap;
    private LatLng oldLatLng;
    //声明AMapLocationClient类对象
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    private OnLocationChangedListener mListener;
    public AMapLocationClientOption mLocationOption = null;
    private int locateCount = 0;
    private int stopCount = 0;
    private WheelPath path = new WheelPath(null, new ArrayList<cn.edu.scu.carrecorder.classes.LatLonPoint>());

    public void setLineDrawingOn(boolean lineDrawingOn) {
        if (lineDrawingOn) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date date = new Date();
            path.setName("Path_" + sdf.format(date));
        } else {
            PublicDate.paths.add(path);
            saveWheelPath(PublicDate.paths);
            path.clear();
        }
        this.lineDrawingOn = lineDrawingOn;
    }
    private boolean pathRecOn = false;
    private boolean lineDrawingOn = false;
    private boolean powerSavingOn = false;
    private boolean autoStopOn = false;
    private int autoStopInterval = 0;

    private void saveWheelPath(List<WheelPath> paths) {
        try {
            FileOutputStream fos = getActivity().openFileOutput("WheelPath", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            for (WheelPath path: paths) {
                oos.writeObject(path);
            }
            oos.writeObject(null);
            oos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.locate_frag, null);
        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);// 必须要写
        SharedPreferences sp = getActivity().getSharedPreferences("AppConfig", Context.MODE_PRIVATE);
        pathRecOn = sp.getBoolean("PathRecOn", true);
        powerSavingOn = sp.getBoolean("PowerSaving", true);
        autoStopOn = sp.getBoolean("AutoStopOn", true);
        autoStopInterval = sp.getInt("AutoStopInterval", PublicDate.defaultInterval);
        init();
        return  view;
    }

    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
            mUiSettings = aMap.getUiSettings();
            setUpMap();
        }
    }

    @Override
    public void onLocationChanged(final AMapLocation amapLocation) {

        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                locateCount ++;
                mListener.onLocationChanged(amapLocation);// 显示系统小蓝点
                LatLng newLatLng = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                if(locateCount <= 2){
                    return;
                }
                //位置有变化
                if(! oldLatLng.equals(newLatLng)){
                    float distance = AMapUtils.calculateLineDistance(oldLatLng, newLatLng);

                    if (powerSavingOn ) {
                        if (distance >= 165) {
                            return;
                        } else if (distance <= 30) {
                            stopCount += 5;
                        } else {
                            stopCount = 0;
                        }
                    } else {
                        if (distance >= 65) {
                            return;
                        } else if (distance <= 10) {
                            stopCount ++;
                        } else {
                            stopCount = 0;
                        }
                    }

                    if (autoStopOn && lineDrawingOn && stopCount >= (autoStopInterval / 1000)) {
                        RecordFragment.getFragment().stopRecording();
                        stopCount = 0;
                    }

                    Log.e("Amap", amapLocation.getLatitude() + "," + amapLocation.getLongitude());
                    if (lineDrawingOn) {
                        drawLine(oldLatLng ,newLatLng );
                        if (pathRecOn) {
                            path.addLatLonPoint(new cn.edu.scu.carrecorder.classes.LatLonPoint(
                                    amapLocation.getLatitude(), amapLocation.getLongitude()));
                        }
                    }

                    oldLatLng = newLatLng;
                    float speed = amapLocation.getSpeed();
                    getPosSpeInfo(amapLocation, speed);
                }

            } else {
                String errText = "定位失败," + amapLocation.getErrorCode()+ ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr",errText);
            }
        }
    }

    private void getPosSpeInfo(AMapLocation amapLocation, final float speed) {
        GeocodeSearch geocodeSearch = new GeocodeSearch(getActivity());
        geocodeSearch.setOnGeocodeSearchListener(new GeocodeSearch.OnGeocodeSearchListener() {
            @Override
            public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
                if(i == 1000) {
                    if(regeocodeResult != null && regeocodeResult.getRegeocodeAddress() != null
                            && regeocodeResult.getRegeocodeAddress().getFormatAddress() != null) {
                        RegeocodeAddress result = regeocodeResult.getRegeocodeAddress();
                        String address = result.getCity() + result.getDistrict() + result.getTownship()
                                + result.getRoads().get(0).getName();
                        String a = result.getFormatAddress();
                        RecordFragment.getFragment().refreshInfo(address, String.format("%.2f", speed));
                    }
                }
            }

            @Override
            public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {

            }
        });
        RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(amapLocation.getLatitude(),amapLocation.getLongitude()), 200,GeocodeSearch.GPS);
        geocodeSearch.getFromLocationAsyn(query);
    }

    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mLocationClient == null) {
            mLocationClient = new AMapLocationClient(getActivity());
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mLocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationOption.setInterval(2000);
            //设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mLocationClient.startLocation();
        }
    }

    public void changeLocatRate(int interval) {
        mLocationClient.stopLocation();
        mLocationOption.setInterval(interval);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        deactivate();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**绘制两个坐标点之间的线段,从以前位置到现在位置*/
    private void drawLine(LatLng oldData,LatLng newData ) {

        // 绘制一个大地曲线
        aMap.addPolyline((new PolylineOptions())
                .add(oldData, newData)
                .geodesic(true).color(Color.GREEN));

    }

    private void setUpMap() {
        MyLocationStyle myLocationStyle = new MyLocationStyle();

        myLocationStyle.myLocationIcon(BitmapDescriptorFactory
                .fromResource(R.drawable.location_marker));// 设置小蓝点的图标
        myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色
        myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色

        aMap.getUiSettings().setZoomControlsEnabled(false);
        aMap.getUiSettings().setCompassEnabled(false);
        aMap.getUiSettings().setScaleControlsEnabled(false);
        aMap.setMyLocationStyle(myLocationStyle);
        aMap.setLocationSource(this);// 设置定位监听
        mUiSettings.setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.moveCamera(CameraUpdateFactory.zoomTo(20));
    }

}