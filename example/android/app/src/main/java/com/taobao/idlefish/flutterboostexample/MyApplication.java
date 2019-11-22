package com.taobao.idlefish.flutterboostexample;

import android.app.Application;
import android.content.Context;

import android.util.Log;

import androidx.annotation.NonNull;

import com.idlefish.flutterboost.*;

import java.util.Map;

import com.idlefish.flutterboost.interfaces.INativeRouter;
import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugins.GeneratedPluginRegistrant;

public class MyApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        INativeRouter router =new INativeRouter() {
            @Override
            public void openContainer(Context context, String url, Map<String, Object> urlParams, int requestCode, Map<String, Object> exts) {
               String  assembleUrl=Utils.assembleUrl(url,urlParams);
                PageRouter.openPageByUrl(context,assembleUrl, urlParams);
            }

        };

        NewFlutterBoost.BoostLifecycleListener lifecycleListener= new NewFlutterBoost.BoostLifecycleListener() {
            @Override
            public void onEngineCreated() {

            }

            @Override
            public void onPluginsRegistered() {
                MethodChannel mMethodChannel = new MethodChannel( NewFlutterBoost.instance().engineProvider().getDartExecutor(), "methodChannel");
                Log.e("MyApplication","MethodChannel create");
                TextPlatformViewPlugin.register(NewFlutterBoost.instance().getPluginRegistry().registrarFor("TextPlatformViewPlugin"));

            }

            @Override
            public void onEngineDestroy() {

            }
        };

        NewFlutterBoost.BoostPluginRegistrant boostPluginRegistrant = new NewFlutterBoost.BoostPluginRegistrant() {
            @Override
            public void registerWith(PluginRegistry registry) {
                GeneratedPluginRegistrant.registerWith(registry);
            }

            @Override
            public void registerWith(@NonNull FlutterEngine flutterEngine) {

            }
        };

        Platform platform= new NewFlutterBoost
                .ConfigBuilder(this,router)
                .isDebug(true)
                .whenEngineStart(NewFlutterBoost.ConfigBuilder.ANY_ACTIVITY_CREATED)
                .renderMode(FlutterView.RenderMode.texture)
                .lifecycleListener(lifecycleListener)
                .boostPluginRegistrant(boostPluginRegistrant)
                .build();

        NewFlutterBoost.instance().init(platform);



    }
}
