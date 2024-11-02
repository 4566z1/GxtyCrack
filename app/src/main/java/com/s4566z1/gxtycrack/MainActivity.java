package com.s4566z1.gxtycrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_LOCATION_PERMISSION = 5555;
    static AMapLocation aMapLocation = null;
    static AMap aMap;
    MapView mapView;
    CameraActivity cameraActivity;
    AMapLocationClient locationClient;
    AMapLocationClientOption option = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 更新用户隐私凭据
        MapsInitializer.updatePrivacyShow(getApplicationContext(),true,true);
        MapsInitializer.updatePrivacyAgree(getApplicationContext(),true);

        // 检查是否具有定位权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 如果没有权限，请求定位权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_PHONE_STATE}, REQUEST_LOCATION_PERMISSION);
        }

        // 初始化定位按钮
        Button LocationButton = findViewById(R.id.button4);
        LocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(aMapLocation != null){
                    aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(aMapLocation.getLatitude(), aMapLocation.getLongitude()), 16, 0, 0)));
                }
            }
        });

        // 初始化添加轨迹点按钮
        UtilsActivity utilsActivity = new UtilsActivity(this);

        // 设置调试窗口滚动
        TextView textView = findViewById(R.id.textView2);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        // 初始化添加轨迹按钮
        Button AddMarkerButton = findViewById(R.id.button);
        AddMarkerButton.setOnClickListener(utilsActivity.new AddMarkerButtonListener());

        // 初始化删除轨迹点按钮
        Button RemoveMarkerButton = findViewById(R.id.button5);
        RemoveMarkerButton.setOnClickListener(utilsActivity.new RemoveMarkerButtonListener());

        // 初始化保存按钮
        Button saveSettingsButton = findViewById(R.id.button2);
        saveSettingsButton.setOnClickListener(utilsActivity.new SaveSettingsButtonListener());

        // 初始化加载配置按钮
        Button loadSettingButton = findViewById(R.id.button3);
        loadSettingButton.setOnClickListener(utilsActivity.new LoadSettingButtonListener());

        // 选择模式按钮
        RadioButton radioButtonZ = findViewById(R.id.radioButton2);
        RadioButton radioButtonT = findViewById(R.id.radioButton);
        radioButtonT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radioButtonZ.isChecked()){
                    radioButtonZ.setChecked(false);
                }
                else{
                    radioButtonZ.setChecked(true);
                }

                if(radioButtonZ.isChecked() && radioButtonT.isChecked()){
                    radioButtonT.setChecked(false);
                }
            }
        });
        radioButtonZ.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(radioButtonT.isChecked()){
                    radioButtonT.setChecked(false);
                }
                else{
                    radioButtonT.setChecked(true);
                }

                if(radioButtonZ.isChecked() && radioButtonT.isChecked()){
                    radioButtonZ.setChecked(false);
                }
            }
        });

        // 初始化地图
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        if(aMap == null) {
            aMap = mapView.getMap();
        }


        //初始化定位
        try{
            locationClient = new AMapLocationClient(this.getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
        }

        // 设置定位服务
        try{
            locationClient = new AMapLocationClient(this.getApplicationContext());
        }catch (Exception e){
            e.printStackTrace();
        }
        aMap.setLocationSource(new LocationSource() {
            @Override
            public void activate(OnLocationChangedListener onLocationChangedListener) {
                if(option == null)
                {
                    option = new AMapLocationClientOption();
                    // 设置GPS定位优先，即使设置高精度定位模式，它也会优先GPS在室内定位很差，最好不要设置，就默认的也就是false;
                    option.setGpsFirst(true);
                    //高精度定位模式
                    option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                }
                //设置定位，onLocationChanged就是这个接口的方法
                locationClient.setLocationListener(new AMapLocationListener() {
                    @Override
                    public void onLocationChanged(AMapLocation _aMapLocation) {
                        if (_aMapLocation != null) {
                            Log.d(String.valueOf(_aMapLocation.getErrorCode()), "test");
                            if (_aMapLocation.getErrorCode() == 0) {
                                //解析定位结果
                                aMapLocation = _aMapLocation;
                            }
                        }
                    }
                }
                );
                locationClient.setLocationOption(option);
                //开始定位
                locationClient.startLocation();
            }

            @Override
            public void deactivate() {
                if(locationClient.isStarted())
                    locationClient.stopLocation();
                locationClient = null;
            }
        });
        aMap.setMyLocationEnabled(true);

        // 设置中心点
        aMap.addOnMapLoadedListener(new AMap.OnMapLoadedListener(){
            @Override
            public void onMapLoaded(){
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.setFlat(true);
                markerOptions.anchor(0.5f, 0.5f);
                markerOptions.position(new LatLng(0, 0));
                Marker mPositionMark = aMap.addMarker(markerOptions);
                mPositionMark.showInfoWindow();//主动显示
                mPositionMark.setPositionByPixels(mapView.getWidth() / 2,mapView.getHeight() / 2);
            }
        });

        // 设置自动获取中心点坐标类
        cameraActivity = new CameraActivity();
        aMap.setOnCameraChangeListener(cameraActivity);
    }

    //高德地图重写
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }
}