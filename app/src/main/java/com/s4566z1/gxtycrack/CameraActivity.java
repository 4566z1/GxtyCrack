package com.s4566z1.gxtycrack;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;

public class CameraActivity implements AMap.OnCameraChangeListener {
    public static LatLng cameraPosition;

    public void onCameraChange(CameraPosition position){
        cameraPosition = position.target;
    }

    public void onCameraChangeFinish(CameraPosition position){

    }
}
