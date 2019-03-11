package cn.com.startai.sharedcharger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.util.Arrays;

import cn.com.startai.mqttsdk.StartAI;
import cn.com.startai.mqttsdk.base.StartaiError;
import cn.com.startai.mqttsdk.busi.entity.C_0x8027;
import cn.com.startai.mqttsdk.busi.entity.type.Type;
import cn.com.startai.mqttsdk.listener.IOnCallListener;
import cn.com.startai.mqttsdk.mqtt.request.MqttPublishRequest;

public class MainActivity extends AppCompatActivity {
    CallbackManager callbackManager;
    private String TAG = "INCHARGER MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplication());
    }

    public void test(View view) {

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            Log.i(TAG, "已经登录 accessToken = " + accessToken);
            getLoginInfo(accessToken);
            return;
        }

        callbackManager = CallbackManager.Factory.create();
        Log.d(TAG, "test");
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d(TAG, "facebook login success " + loginResult.getAccessToken() + " " + loginResult.getRecentlyDeniedPermissions() + " " + loginResult.getRecentlyGrantedPermissions());

                        getLoginInfo(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        // App code
                        Log.d(TAG, "facebook login onCancel ");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Log.d(TAG, "facebook login onError " + exception.getMessage());
                        exception.printStackTrace();
                    }
                });


        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void getLoginInfo(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if (object != null) {

                    String id = object.optString("id");   //比如:1565455221565
                    String name = object.optString("name");  //比如：Zhang San
                    String first_name = object.optString("first_name");
                    String last_name = object.optString("last_name");

                    //获取用户头像
                    JSONObject object_pic = object.optJSONObject("picture");
                    JSONObject object_data = object_pic.optJSONObject("data");
                    String url = object_data.optString("url");
                    Log.i(TAG, object.toString());
                    Log.i(TAG, "id = " + id + " name = " + name + " firstName = " + first_name + " lastName = " + last_name + " url = " + url);

                    C_0x8027.Req.ContentBean req = new C_0x8027.Req.ContentBean();
                    req.setType(Type.Login.THIRD_FACEBOOK);

                    C_0x8027.Req.ContentBean.UserinfoBean userinfoBean = new C_0x8027.Req.ContentBean.UserinfoBean();
                    userinfoBean.setOpenid(id);
                    userinfoBean.setUnionid(id);
                    userinfoBean.setHeadimgurl(url);
                    userinfoBean.setNickname(name);

                    req.setUserinfo(userinfoBean);
                    StartAI.getInstance().getBaseBusiManager().loginWithThirdAccount(req, new IOnCallListener() {
                        @Override
                        public void onSuccess(MqttPublishRequest mqttPublishRequest) {

                        }

                        @Override
                        public void onFailed(MqttPublishRequest mqttPublishRequest, StartaiError startaiError) {

                        }

                        @Override
                        public boolean needUISafety() {
                            return false;
                        }
                    });

                }
            }
        });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,gender,birthday,email,picture,locale,updated_time,timezone,age_range,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();

    }


}
