package com.s4566z1.gxtycrack;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Environment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;


class MyLatlng{
        public double latitude;
        public double longitude;
        public double speed;
}

class Trend{
        float x;
        double y;
}

public class UtilsActivity {
        private static LinkedList<LatLng> TrackList = new LinkedList<>();
        private static LinkedList<Polyline> Polylines = new LinkedList<>();
        private static LinkedList<Polyline> finishPolylines = new LinkedList<>();

        private static Marker startMarker = null;
        private static Marker endMarker = null;
        private static boolean isSaved = false;
        final static String settingName = "settings.json";
        final static String settingFolder = "Fuck高校体育";
        final static String settingPath = settingFolder + "/" + settingName;
        final private Activity MainContext;

        UtilsActivity(Activity _MainContext){
                MainContext = _MainContext;
        }

        // 伪造数据结构并保存
        public void saveSettings(
                Double buPin,
                Double distance,
                Double duration,
                Double speed,
                Long TotalNum,
                String Peisu
        ){
                // 初始化配置
                Gson gson = new Gson();
                LinkedList<MyLatlng> savedTracklist = new LinkedList<>();
                LinkedList<Trend> trends = new LinkedList<>();
                LinkedList<MyLatlng> tNodes = new LinkedList<>();

                Random randomEngine = new Random();
                RadioButton radioButtonT = MainContext.findViewById(R.id.radioButton);
                int runtype = radioButtonT.isChecked() ? 1 : 2;

                // 伪造轨迹列表
                for (LatLng latLng : TrackList){
                        MyLatlng myLatlng = new MyLatlng();
                        myLatlng.latitude = latLng.latitude;
                        myLatlng.longitude = latLng.longitude;
                        myLatlng.speed = speed + (randomEngine.nextDouble()*-0.3+0.3);
                        savedTracklist.addLast(myLatlng);
                }

                // 伪造trend
                for (float i = 0.1f; i <= distance / 1000; i += 0.1f){
                        Trend trend = new Trend();
                        trend.x = i;
                        trend.y = 50 + (randomEngine.nextDouble()*-0.15+0.15);
                        trends.addLast(trend);
                }
                // 体育锻炼额外增加信标点
                if(runtype == 1 && savedTracklist.size() != 0){
                        // 随机取点
                        for (int i = 0;i < 3;i++){
                                MyLatlng myLatlng = new MyLatlng();
                                MyLatlng latlng = savedTracklist.get(randomEngine.nextInt(savedTracklist.size()));
                                myLatlng.latitude = latlng.latitude;
                                myLatlng.longitude = latlng.longitude;
                                myLatlng.speed = latlng.speed;
                                tNodes.addLast(myLatlng);
                        }
                }

                // 保存配置
                Map<String, String> SavedJsonMap = new LinkedHashMap<>();
                SavedJsonMap.put("bNode", "[]");
                SavedJsonMap.put("buPin", String.format( "%.1f", buPin));
                SavedJsonMap.put("duration", String.valueOf(duration));
                SavedJsonMap.put("endTime", "");
                SavedJsonMap.put("frombp", "0");
                SavedJsonMap.put("goal", "2.00");
                SavedJsonMap.put("real", String.valueOf(distance));
                SavedJsonMap.put("runPageId", "");
                SavedJsonMap.put("speed", Peisu);
                SavedJsonMap.put("startTime", "");
                SavedJsonMap.put("tNode", gson.toJson(tNodes));
                SavedJsonMap.put("totalNum", String.valueOf(TotalNum));
                SavedJsonMap.put("track", gson.toJson(savedTracklist));
                SavedJsonMap.put("trend", gson.toJson(trends));
                SavedJsonMap.put("type", String.valueOf(runtype));
                SavedJsonMap.put("userid", "");

                saveFile(settingName, gson.toJson(SavedJsonMap).getBytes(StandardCharsets.UTF_8));
        }

        public static void saveFile(String FileName, byte[] Data){
                File directory = new File(Environment.getExternalStorageDirectory(), settingFolder);
                File file = new File(Environment.getExternalStorageDirectory(), settingFolder + "/" + FileName);
                try {
                        // 创建文件夹
                        if(!directory.exists())
                                directory.mkdirs();
                        // 读写文件
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(Data);
                        fos.close();
                } catch (Exception e) {
                        e.printStackTrace();
                }
        }

        public static String loadSettings(String FilePath){
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                File file = new File(Environment.getExternalStorageDirectory(), settingFolder + "/" + FilePath);
                int buffer;

                try{
                        FileInputStream io = new FileInputStream(file);
                        while((buffer = io.read()) != -1){
                                outputStream.write(buffer);
                        }
                        io.close();
                }catch(Exception e){
                        e.printStackTrace();
                }

                return outputStream.toString();
        }

        private void reset(){
                for (Polyline polyline : finishPolylines){
                        polyline.remove();
                }
                for (Polyline polyline : Polylines){
                        polyline.remove();
                }

                if(startMarker == null && endMarker == null){
                        isSaved = false;
                        return;
                }

                startMarker.destroy();
                endMarker.destroy();
                Polylines.clear();
                finishPolylines.clear();
                TrackList.clear();
                startMarker = null;
                endMarker = null;
                isSaved = false;
        }

        // 获取总路程
        private Double getTotalDistance(){
                double TotalDistance = 0.0;
                for (Polyline polyline : Polylines){
                        List<LatLng> points = polyline.getPoints();
                        TotalDistance += AMapUtils.calculateLineDistance(points.get(0),points.get(1));
                }
                return TotalDistance;
        }

        private String getPeiSu(Double duration, Double distance){
                double Peisu = (duration / 60) / (distance / 1000);
                Double PeisuIntPart = Math.floor(Peisu);
                double PeisuFloatPart = (Peisu - PeisuIntPart)*60;
                return PeisuIntPart.intValue() + "\u0027" + Math.round(PeisuFloatPart) + "\u0027\u0027";
        }


        private void fixPolyLine() {
                Random randomEngine = new Random();
                List<LatLng> points = null;

                EditText editText1 = MainContext.findViewById(R.id.editText);
                EditText editText2 = MainContext.findViewById(R.id.editText2);
                EditText editText3 = MainContext.findViewById(R.id.editText3);

                int min = Integer.parseInt(editText1.getText().toString());
                int max = Integer.parseInt(editText2.getText().toString());
                int step = Integer.parseInt(editText3.getText().toString());

                // 清空线段和点集合
                for (Polyline polyline : Polylines) {
                        polyline.remove();
                }
                TrackList.clear();

                // 重构线段
                for (int i = 0; i < Polylines.size(); i++) {
                        // 添加轨迹头部
                        TrackList.addLast(Polylines.get(i).getPoints().get(0));
                        points = Polylines.get(i).getPoints();

                        // 初始化基本信息
                        //         distance = AMapUtils.calculateLineDistance(points.get(0),points.get(1));
                        Point startPoint = MainActivity.aMap.getProjection().toScreenLocation(points.get(0));
                        Point endPoint = MainActivity.aMap.getProjection().toScreenLocation(points.get(1));

                        int startX = 0;
                        int startY = 0;
                        double a = (endPoint.y - startPoint.y);
                        double b = (endPoint.x - startPoint.x);
                        double c = endPoint.x * startPoint.y - endPoint.y * startPoint.x;
                        double length = Math.abs(a) > Math.abs(b) ? a : b;
                        int randIndex = 0;

                        for (int k = 0; k < Math.abs(length); k++) {
                                // Bresenham算法(需要优化)
                                if (Math.abs(a) < Math.abs(b)) {
                                        // X 距离差大
                                        if (length > 0) {
                                                startX = startPoint.x + k;
                                                startY = -(int) (((-(a / b) * startX - (c / b)) + 0.5));
                                        } else {
                                                startX = startPoint.x - k;
                                                startY = -(int) ((-(a / b) * startX - (c / b)) + 0.5);
                                        }
                                } else if (Math.abs(a) > Math.abs(b)) {
                                        // Y 距离差大
                                        if (length > 0) {
                                                startY = startPoint.y + k;
                                                startX = (int) ((-(b / a) * -startY - (c / a)) + 0.5);
                                        } else {
                                                startY = startPoint.y - k;
                                                startX = (int) ((-(b / a) * -startY - (c / a)) + 0.5);
                                        }
                                } else {
                                        // 修复45角
                                        startX = (b > 0) ? startPoint.x + k : startPoint.x - k;
                                        startY = (a > 0) ? startPoint.y + k : startPoint.y - k;
                                }
                                // 每隔step加抖动
                                Point point = new Point();
                                if(randIndex == k){
                                        point.set(startX + (int)(randomEngine.nextFloat() * (max - min + 1)), startY + (int)(randomEngine.nextFloat() * (max - min + 1)));
                                        randIndex += step;
                                }
                                else
                                        point.set(startX, startY);
                                TrackList.addLast(MainActivity.aMap.getProjection().fromScreenLocation(point));
                        }
                        // 闭合集合尾部
                        TrackList.addLast(Polylines.get(i).getPoints().get(1));
                }

                finishPolylines.addLast(MainActivity.aMap.addPolyline(new PolylineOptions().addAll(TrackList).width(10).color(Color.argb(255, 0, 255, 0))));
        }
        public class LoadSettingButtonListener implements View.OnClickListener{
                @Override
                public void onClick(View view){
                        // 初始化
                        Gson gson = new Gson();
                        TextView textView = MainContext.findViewById(R.id.textView2);
                        JsonObject jsonObject = gson.fromJson(loadSettings(settingName), JsonObject.class);
                        Map<String, String> SavedJsonMap = new LinkedHashMap<>();

                        SavedJsonMap.put("bNode",  jsonObject.get("bNode").getAsString());
                        SavedJsonMap.put("buPin", jsonObject.get("buPin").getAsString());
                        SavedJsonMap.put("duration", jsonObject.get("duration").getAsString());
                        SavedJsonMap.put("endTime", jsonObject.get("endTime").getAsString());
                        SavedJsonMap.put("frombp", jsonObject.get("frombp").getAsString());
                        SavedJsonMap.put("goal", jsonObject.get("goal").getAsString());
                        SavedJsonMap.put("real", jsonObject.get("real").getAsString());
                        SavedJsonMap.put("runPageId", jsonObject.get("runPageId").getAsString());
                        SavedJsonMap.put("speed", jsonObject.get("speed").getAsString());
                        SavedJsonMap.put("startTime", jsonObject.get("startTime").getAsString());
                        SavedJsonMap.put("tNode", jsonObject.get("tNode").getAsString());
                        SavedJsonMap.put("totalNum", jsonObject.get("totalNum").getAsString());
                        SavedJsonMap.put("track", jsonObject.get("track").getAsString());
                        SavedJsonMap.put("trend", jsonObject.get("trend").getAsString());
                        SavedJsonMap.put("type", jsonObject.get("type").getAsString());
                        SavedJsonMap.put("userid", jsonObject.get("userid").getAsString());
                        textView.setText(gson.toJson(SavedJsonMap).replace("\\\"", "\"").replace("\"[", "[").replace("]\"", "]"));
                }
        }

        public class SaveSettingsButtonListener implements View.OnClickListener{
                @Override
                public void onClick(View view){
                        if(isSaved){
                                return;
                        }

                        // 初始化控件
                        TextView textView = MainContext.findViewById(R.id.textView2);
                        EditText editText = MainContext.findViewById(R.id.EditText);
                        CheckBox checkBox = MainContext.findViewById(R.id.checkBox2);

                        Double speed = Double.parseDouble(editText.getText().toString());
                        Double bufu = 0.93;
                        Double distance = getTotalDistance();
                        Double buPin = speed / bufu * 60;
                        Double duration = distance / speed;
                        Long TotalNum = Math.round(distance / bufu);
                        String Peisu = getPeiSu(duration, distance);

                        textView.setText(String.format("speed: %f \ndistance: %f \nbuPin: %f \nTotalNum: %d \nPeisu: %s\nDuration: %f", speed, distance, buPin, TotalNum, Peisu, duration));

                        // 坐标插值
                        if(checkBox.isChecked())
                                fixPolyLine();

                        // 保存配置
                        saveSettings(buPin, distance, duration, speed, TotalNum, Peisu);
                        isSaved = true;
                }
        }

        // 添加轨迹点
        public class AddMarkerButtonListener implements View.OnClickListener {
                @Override
                public void onClick(View view){
                        if(isSaved){
                                reset();
                        }

                        TextView textView = MainContext.findViewById(R.id.textView2);

                        LatLng position = CameraActivity.cameraPosition;
                        if(TrackList.size() >= 1){
                                Polylines.addLast(MainActivity.aMap.addPolyline(new PolylineOptions().add(TrackList.getLast(), position).width(10).color(Color.argb(255, 0, 255, 0))));
                        }
                        TrackList.addLast(position);

                        if(startMarker != null){
                                if(endMarker != null)
                                        endMarker.destroy();
                                endMarker = MainActivity.aMap.addMarker(new MarkerOptions().position(position).title("终点"));
                                endMarker.showInfoWindow();
                        }
                        if(startMarker == null){
                                startMarker = MainActivity.aMap.addMarker(new MarkerOptions().position(position).title("起点"));
                                startMarker.showInfoWindow();
                        }

                        if(Polylines.size() > 0)
                                textView.setText("当前距离: " + getTotalDistance() + "M");
                }
        }

        // 删除轨迹点
        public class RemoveMarkerButtonListener implements View.OnClickListener{
                @Override
                public void onClick(View view){
                        if(isSaved){
                                reset();
                                return;
                        }
                        if(TrackList.size() == 0 || Polylines.size() == 0){
                                // 删除起始点
                                if(TrackList.size() == 1){
                                        TrackList.removeLast();
                                        startMarker.destroy();
                                        startMarker = null;
                                }
                                return;
                        }
                        // 删除当前线段
                        Polylines.getLast().remove();
                        Polylines.removeLast();

                        // 删除点
                        TrackList.removeLast();

                        // 处理标志
                        endMarker.destroy();
                        if(TrackList.size() != 1){
                                endMarker = MainActivity.aMap.addMarker(new MarkerOptions().position(TrackList.getLast()).title("终点"));
                                endMarker.showInfoWindow();
                        }
                }
        }
}