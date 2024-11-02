package com.s4566z1.gxtycrack;
import android.app.Application;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class HookActivity implements IXposedHookLoadPackage {
    public static String settingName = "settings.json";
    public static String ibeaconName = "ibeacon.json";
    public static String gpsInfoName = "gpsinfo.json";

    // 读取配置以及篡改封包
    public String FakeAndloadSettings(String runPageId, String UserId) throws Exception {
        // 初始化参数
        Gson gson = new Gson();
        Map<String, String> SavedJsonMap = new LinkedHashMap<>();
        JsonObject jsonObject = gson.fromJson(UtilsActivity.loadSettings(settingName), JsonObject.class);
        JsonArray ibeaconObject = gson.fromJson(UtilsActivity.loadSettings(ibeaconName), JsonArray.class);
        JsonArray gpuInfoObject = gson.fromJson(UtilsActivity.loadSettings(gpsInfoName), JsonArray.class);

        // 初始化时间
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Double duration = Double.parseDouble(jsonObject.get("duration").getAsString());
        Calendar endCalendar = Calendar.getInstance();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.SECOND, -duration.intValue());

        // 获取配置
        SavedJsonMap.put("bNode", ibeaconObject.toString());
        SavedJsonMap.put("buPin", jsonObject.get("buPin").getAsString());
        SavedJsonMap.put("duration", jsonObject.get("duration").getAsString());
        SavedJsonMap.put("endTime", dateFormat.format(endCalendar.getTime()));
        SavedJsonMap.put("frombp", jsonObject.get("frombp").getAsString());
        SavedJsonMap.put("goal", jsonObject.get("goal").getAsString());
        SavedJsonMap.put("real", jsonObject.get("real").getAsString());
        SavedJsonMap.put("runPageId", runPageId);
        SavedJsonMap.put("speed", jsonObject.get("speed").getAsString());
        SavedJsonMap.put("startTime", dateFormat.format(startCalendar.getTime()));
        SavedJsonMap.put("tNode", gpuInfoObject.toString());
        SavedJsonMap.put("totalNum", jsonObject.get("totalNum").getAsString());
        SavedJsonMap.put("track", jsonObject.get("track").getAsString());
        SavedJsonMap.put("trend", jsonObject.get("trend").getAsString());
        SavedJsonMap.put("type", jsonObject.get("type").getAsString());
        SavedJsonMap.put("userid", UserId);
        return gson.toJson(SavedJsonMap).replace("\\\"", "\"").replace("\"[", "[").replace("]\"", "]");
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam){
        if(loadPackageParam.packageName.equals("com.example.gita.gxty")){
            // 多DEX文件 HOOK
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Class <?> ModelRunClass = XposedHelpers.findClass("com.example.gita.gxty.model.Run", loadPackageParam.classLoader);
                    XposedHelpers.findAndHookMethod("com.example.gita.gxty.MyApplication", loadPackageParam.classLoader, "t", int.class, ModelRunClass, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            Gson gson = new Gson();
                            Object ModelRun = param.args[1];
                            if(ModelRun != null){
                                XposedBridge.log("开始拦截数据");
                                String ibeacon = gson.toJson(ModelRunClass.getField("ibeacon").get(ModelRun));
                                String gpsinfo = gson.toJson(ModelRunClass.getField("gpsinfo").get(ModelRun));

                                if(!ibeacon.equals("null"))
                                    UtilsActivity.saveFile(ibeaconName, ibeacon.getBytes(StandardCharsets.UTF_8));
                                if(!gpsinfo.equals("null"))
                                    UtilsActivity.saveFile(gpsInfoName, gpsinfo.getBytes(StandardCharsets.UTF_8));
                            }
                        }
                    });

                    // 绕过短距离上传限制
                    XposedHelpers.findAndHookMethod("com.example.gita.gxty.utils.t", loadPackageParam.classLoader, "f", new XC_MethodHook(){
                        //进行hook操作
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            // 栈回溯查看调用情况(不用这个会有其他BUG)
                            boolean is_target_activity = false;
                            Throwable ex = new Throwable();
                            StackTraceElement[] stackElements = ex.getStackTrace();
                            if (stackElements != null) {
                                for (StackTraceElement stackElement : stackElements) {
                                    if (stackElement.getClassName().contains("MyRuningActivity")) {
                                        is_target_activity = true;
                                        break;
                                    }
                                }
                            }

                            if(is_target_activity){
                                XposedBridge.log("正在HOOK短距离上传");
                                param.setResult(true);
                            }
                            super.afterHookedMethod(param);
                        }
                    });

                    // 拦截上传发送
                    XposedHelpers.findAndHookMethod("com.example.gita.gxty.utils.t", loadPackageParam.classLoader, "h", long.class, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log("正在拦截上传");

                            // 拦截和伪造
                            Gson gson = new Gson();
                            Object Datarun = param.getResult(); // 实例
                            Class<?> DataClass = Datarun.getClass(); // 实例表示的类

                            // 篡改
                            String test = FakeAndloadSettings("" + DataClass.getField("runPageId").get(Datarun), "" + DataClass.getField("userid").get(Datarun));
                            XposedBridge.log("json : " + test);

                            param.setResult(gson.fromJson(test, DataClass));
                            super.afterHookedMethod(param);
                        }
                    });

                    super.afterHookedMethod(param);
                }
            });
        }
    }
}
