package cn.com.startai.baseloginlibs;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONObject;

import java.util.Arrays;

import cn.com.startai.baseloginlibs.util.ImageUtils;
import cn.com.startai.baseloginlibs.util.LoginType;
import cn.com.startai.baseloginlibs.util.ThirdInfoManager;
import cn.com.startai.mqttsdk.StartAI;
import cn.com.startai.mqttsdk.base.StartaiError;
import cn.com.startai.mqttsdk.busi.entity.C_0x8027;
import cn.com.startai.mqttsdk.listener.IOnCallListener;
import cn.com.startai.mqttsdk.mqtt.request.MqttPublishRequest;
import cn.com.startai.mqttsdk.utils.SJsonUtils;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private CallbackManager callbackManager;
    private ImageView ivHeadImage;
    private TextView tvNickName;
    private int RC_SIGN_IN = 9003; // google login requestCode
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar1);
        toolbar.setTitle("订单列表");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        TextView tvPkgName = findViewById(R.id.tv_pkgname);
        tvNickName = findViewById(R.id.tv_nickname);
        ivHeadImage = findViewById(R.id.iv_headimg);
        tvPkgName.setText(getResources().getText(R.string.app_name) + " : " + getApplication().getPackageName());


        ivHeadImage.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {


                switch (LoginType.currentLoginType) {


                    case LoginType.TYPE_ALIPAY:

                        break;
                    case LoginType.TYPE_FACEBOOK:

                        LoginManager.getInstance().logOut();


                        break;
                    case LoginType.TYPE_GOOGLE:

                        mGoogleSignInClient.signOut();


                        break;
                    case LoginType.TYPE_TWITTER:

                        break;
                    case LoginType.TYPE_WECHAT:

                        break;
                }

                showDefaultUI();

                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_login, menu);
//        menu.getItem(0).setTitle("微信登录");
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.m_wx_login) {

            loginWhtiWX();

            return true;
        } else if (id == R.id.m_zfb_login) {


            loginWhtiAliPay();

            return true;
        } else if (id == R.id.m_twitter_login) {
            loginWhtiTwitter();

        } else if (id == R.id.m_facebook_login) {

            loginWithFacebook();


        } else if (id == R.id.m_google_login) {

            loginWithGoogle();
        }
        return super.onOptionsItemSelected(item);
    }





//-------facebook login start----------

    public void loginWithFacebook() {

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            Log.i(TAG, "已经登录 accessToken = " + accessToken);
            LoginType.currentLoginType = LoginType.TYPE_FACEBOOK;
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


    public void getLoginInfo(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                if (object != null) {


                    LoginType.currentLoginType = LoginType.TYPE_FACEBOOK;

                    Log.d(TAG, object.toString());

                    C_0x8027.Req.ContentBean req = new C_0x8027.Req.ContentBean();
                    req.fromFacebookJSONObject(object);

                    showLoginSuccess(req.getUserinfo().getNickname(), req.getUserinfo().getHeadimgurl());


                    //调用 startAI的第三方登录接口
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


    //-------facebook login end----------


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {

            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //-----------google login start------------

    private void loginWithGoogle() {


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        Log.d(TAG, "t1 = " + System.currentTimeMillis());
        Log.d(TAG, "last login account = " + SJsonUtils.toJson(lastSignedInAccount));
        Log.d(TAG, "t2 = " + System.currentTimeMillis());

        if (lastSignedInAccount != null) {
            LoginType.currentLoginType = LoginType.TYPE_GOOGLE;
            Uri photoUrl = lastSignedInAccount.getPhotoUrl();
            if (photoUrl == null) {
                showLoginSuccess(lastSignedInAccount.getDisplayName(), "");
            } else {
                showLoginSuccess(lastSignedInAccount.getDisplayName(), photoUrl.toString());
            }
            return;
        }


        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {


            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Uri photoUrl = account.getPhotoUrl();

            Log.d(TAG, "account = " + SJsonUtils.toJson(account));
            Log.d(TAG, "getPhotoUrl  = " + photoUrl);


            if (photoUrl == null) {

                showLoginSuccess(account.getDisplayName(), "");
            } else {
                showLoginSuccess(account.getDisplayName(), account.getPhotoUrl().toString());
            }

            LoginType.currentLoginType = LoginType.TYPE_GOOGLE;

        } catch (ApiException e) {
            e.printStackTrace();
            showLoginFailed(e.getMessage());

        }
    }

    //-----------google login end------------

    //---------twitter login start-----------





    private void loginWhtiTwitter() {


        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(ThirdInfoManager.getInstance().getTwitterDeveloperInfo().getTwitterApiKey(), ThirdInfoManager.getInstance().getTwitterDeveloperInfo().getTwitterApiSecrite()))
                .debug(BuildConfig.DEBUG)
                .build();
        Twitter.initialize(config);


        loginTwitter(MainActivity.this, new LoginCallback() {
            @Override
            public void onSuccess(User user, String email, String twitterSecret, String twitterToken) {
                String imageProfileUrl = user.profileImageUrl;
                String userName = user.name;

                String profileUrl = imageProfileUrl.replace("_normal", "");

                Log.d(TAG, "loginTwitter onSuccess");
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,"twitter login failed:" + e.getMessage());
            }
        });
    }


    private TwitterAuthClient mTwitterAuthClient;

    public interface LoginCallback {
        void onSuccess(User user, String email, String twitterSecret, String twitterToken);

        void onFailure(Exception e);
    }

    public void loginTwitter(Activity activity, final LoginCallback callback) {
        if (mTwitterAuthClient == null) {
            mTwitterAuthClient = new TwitterAuthClient();
        }
        mTwitterAuthClient.authorize(activity, new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                String name = result.data.getUserName();
                long userId = result.data.getUserId();
                Log.d(TAG, "loginTwitter name =" + name + ", userId=" + userId);
                getTwitterUserEmail(userId, callback);
            }

            @Override
            public void failure(TwitterException e) {
                Log.d(TAG, "loginTwitter failure e=" + e.getMessage());
                callback.onFailure(e);
            }
        });
    }

    private void getTwitterUserEmail(final long userId, final LoginCallback callback) {
        final TwitterSession activeSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        mTwitterAuthClient.requestEmail(activeSession, new Callback<String>() {

            @Override
            public void success(Result<String> result) {
                String email = result.data;
                Log.d(TAG, "getTwitterUserEmail email::" + email);
                getTwitterUserInfo(userId, email, callback);
            }

            @Override
            public void failure(TwitterException e) {
                getTwitterUserInfo(userId, "", callback);
                Log.d(TAG, "getTwitterUserEmail failure::" + e.getMessage());
            }
        });

    }

    private void getTwitterUserInfo(final long userId, final String email, final LoginCallback callback) {
        final TwitterSession activeSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        final String twitterSecret = activeSession.getAuthToken().secret;
        final String twitterToken = activeSession.getAuthToken().token;
        MyTwitterApiClient client = new MyTwitterApiClient(activeSession);
        client.getCustomService().show(userId).enqueue(new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                User data = result.data;
                callback.onSuccess(data, email, twitterSecret, twitterToken);
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
                callback.onFailure(e);
            }
        });
    }

    static class MyTwitterApiClient extends TwitterApiClient {
        public MyTwitterApiClient(TwitterSession session) {
            super(session);
        }

        /**
         * Provide CustomService with defined endpoints
         */
        public CustomService getCustomService() {
            return getService(CustomService.class);
        }

        // example users/show service endpoint
        interface CustomService {
            @GET("/1.1/users/show.json")
            Call<User> show(@Query("user_id") long id);
        }
    }






    //---------twitter login end-----------

    private void loginWhtiAliPay() {

    }

    private void loginWhtiWX() {

    }

    void showLoginSuccess(final String nickName, final String url) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageUtils.loadImage(getApplicationContext(), ivHeadImage, url);
                tvNickName.setText("已登录:" + nickName);

            }
        });
    }

    void showLoginFailed(final String errMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ivHeadImage.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
                tvNickName.setText("登录失败 :" + errMsg);

            }
        });
    }

    void showDefaultUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ivHeadImage.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
                tvNickName.setText("未登录");
            }
        });
    }

}
