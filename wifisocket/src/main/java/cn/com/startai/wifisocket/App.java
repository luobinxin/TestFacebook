package cn.com.startai.wifisocket;

import android.app.Application;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import cn.com.startai.baseloginlibs.BaseApplication;
import cn.com.startai.baseloginlibs.util.ThirdInfoManager;

/**
 * Created by Robin on 2019/2/27.
 * qq: 419109715 彬影
 */

public class App extends BaseApplication {

    private String TAG = "App";

    @Override
    public void onCreate() {


        ThirdInfoManager.getInstance().initTwitterDeveloper(new ThirdInfoManager.TwitterDeveloperInfo("RL1hIt2WQ3NW5SpiiJduhOgfX","XA3QMIpjd0Z6tfoejMsGGgeadzB3bncXakk4mluQFvQlnv3uoc"));

        super.onCreate();

        Log.d(TAG, "App onCreate");



    }
}
