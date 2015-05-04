package com.carpool.dj.carpool.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.carpool.dj.carpool.R;
import com.carpool.dj.carpool.application.App;
import com.carpool.dj.carpool.model.HttpClientRequest;
import com.carpool.dj.carpool.model.Utils;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.Sharer;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;



public class MainActivity extends Activity {
        // ActionBarActivity
        //implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    //private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in .
     */
    //private CharSequence mTitle;
    private Activity activity ;
    private Tracker tracker;

    //public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console, as described in "Getting Started."
     */
    String SENDER_ID = "575768152775";



    GoogleCloudMessaging gcm;
    //AtomicInteger msgId = new AtomicInteger();
    //SharedPreferences prefs;
    Context context;
    String regid;

    String Tag ="main";

    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    //private ProfilePictureView profilePictureView;
    //private TextView greeting;
    private ProfileTracker profileTracker;
    private ProgressBar progressBar ;
    private Button button ;
    private LoginButton loginButton ;

    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d("HelloFacebook", "Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
            String title = getString(R.string.error);
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d("HelloFacebook", "Success!");
            if (result.getPostId() != null) {
                String title = getString(R.string.success);
                String id = result.getPostId();
                String alertMessage = getString(R.string.successfully_posted_post, id);
                showResult(title, alertMessage);
            }
        }

        private void showResult(String title, String alertMessage) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(alertMessage)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showHashKey(this);


        FacebookSdk.sdkInitialize(this.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();



        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        //handlePendingAction();
                        updateUI();
                        login();
                    }

                    @Override
                    public void onCancel() {


                       // if (pendingAction != PendingAction.NONE) {
                            showAlert();
                            //pendingAction = PendingAction.NONE;
                        //}
                        updateUI();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        //if (pendingAction != PendingAction.NONE
                              //  && exception instanceof FacebookAuthorizationException) {
                            showAlert();
                           // pendingAction = PendingAction.NONE;
                       // }
                        updateUI();
                    }

                    private void showAlert() {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.cancelled)
                                .setMessage(R.string.permission_not_granted)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                });

        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(
                callbackManager,
                shareCallback);


        setContentView(R.layout.activity_main);


        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
            }
        };

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        button = (Button) findViewById(R.id.button);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tracker.send(new HitBuilders.EventBuilder().setCategory("Button")
                        .setAction("customerLogin").setLabel("login").build());

                final EditText input = new EditText(activity);

                input.setText(Utils.UserName, TextView.BufferType.EDITABLE);

                Utils.ShowAlertDialog(activity, getString(R.string.pleaseEnter),  getString(R.string.yourName),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if ("".equals(input.getText().toString())) {
                                    Toast.makeText(
                                            activity,
                                            getString(R.string.pleaseEnter)
                                                    + getString(R.string.yourName),
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }
                                Utils.UserPic ="";
                                Utils.UserName =input.getText().toString();
                                login();
                            }
                        }, "確認", null, "取消", input);




            }
        });


        //profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        //greeting = (TextView) findViewById(R.id.greeting);

        Log.i(Tag, "onCreate");

        //mNavigationDrawerFragment = (NavigationDrawerFragment)
               // getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        //mTitle = getTitle();

        // Set up the drawer.
        //mNavigationDrawerFragment.setUp(
                //R.id.navigation_drawer,
               // (DrawerLayout) findViewById(R.id.drawer_layout));

        activity =this;

        Utils.getSimSerialNumber(activity) ;
        Utils.setNowActivity(activity);

        context = getApplicationContext();

        // Get tracker.
        tracker = ((App) getApplication()).getTracker(App.TrackerName.APP_TRACKER);
        tracker.enableAdvertisingIdCollection(true);

        // Set screen name.
        // Where path is a String representing the screen name.
        tracker.setScreenName("MainActivity");
        // Send a screen view.
        tracker.send(new HitBuilders.AppViewBuilder().build());


        // Check device for Play Services APK. If check succeeds, proceed with
        //  GCM registration.
        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(context);

            if (regid.isEmpty()) {
                registerInBackground();
            }
            Log.i(Tag, "regid is "+regid);
            tracker.send(new HitBuilders.EventBuilder().setCategory("GCM")
                    .setAction("getRegistrationId").setLabel("success").build());

        } else {
            Log.i(Tag, "No valid Google Play Services APK found.");
            tracker.send(new HitBuilders.EventBuilder().setCategory("GCM")
                    .setAction("getRegistrationId").setLabel("No valid").build());
        }

        setCarInfo();
    }


    /**
     * Logout From Facebook
     */


    @Override
    protected void onStart() {

        super.onStart();

        //LoginManager.getInstance().logOut();

        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;
        Profile profile = Profile.getCurrentProfile();
        if (enableButtons && profile != null) {
            //profilePictureView.setProfileId(profile.getId());
            //Utils.UserPic ="portrait_10http://graph.facebook.com/"+profile.getId()+"/picture";
            Utils.UserPic ="facebook_"+profile.getId();
            Utils.UserName =profile.getFirstName();
            Log.i("","profile Id :"+profile.getId());
            //greeting.setText(getString(R.string.hello_user, profile.getFirstName()));
            progressBar.setVisibility(View.VISIBLE);
            button.setVisibility(View.INVISIBLE);
            loginButton.setVisibility(View.INVISIBLE);
            login();

        } else {
            //profilePictureView.setProfileId(null);
            //greeting.setText(null);
            progressBar.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
        }

    }

    protected void login() {

        progressBar.setVisibility(View.VISIBLE);

        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                try {
                    Bundle data = msg.getData();
                    // String jSonData = data.getString("Json_Data");
                    Log.i("","status-->"+ data.getInt("status"));
                    Log.i("", "result-->" + data.getString("Json_Data"));

                    if(data.getInt("status") == 200 ){
                        Intent intent = new Intent(getApplicationContext(),
                                ContentActivity.class);
                        startActivity(intent);
                    }else{
                        LoginManager.getInstance().logOut();
                        progressBar.setVisibility(View.INVISIBLE);
                        button.setVisibility(View.VISIBLE);
                        loginButton.setVisibility(View.VISIBLE);
                        Utils.ShowErrorAlertDialog(activity,getString(R.string.RequestNot200));
                    }

                } catch (Exception e) {
                    Utils.ExceptionHandler(e, tracker, activity);
                }

            }
        };

        if ("".equals(Utils.UserName) || "".equals(regid)) {
            return;
        }

        HttpClientRequest request = new HttpClientRequest(activity,
                Utils.APIUrl
                        + "car/createAccount",
                HttpClientRequest.POST, handler);
        HttpEntity postEntity = null;
        try {
            postEntity = new StringEntity("{\"accountId\":\""
                    + Utils.getSimSerialNumber(activity)
                    + "\",\"accountName\":\"" + URLEncoder.encode(Utils.UserName, "UTF-8")
                    + "\",\"accountPic\":\"" + Utils.UserPic
                    + "\" ,\"regClientType\":\"android\",\"regClientId\":\""
                    + regid + "\" }", "UTF-8");

        } catch (UnsupportedEncodingException e) {
            Utils.ExceptionHandler(e, tracker, activity);
        }

        request.setHttpEntity(postEntity);
        new Thread(request).start();

    }

    public static void showHashKey(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    "com.carpool.dj.carpool", PackageManager.GET_SIGNATURES); //Your            package name here
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.i("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (NoSuchAlgorithmException e) {
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;
        Profile profile = Profile.getCurrentProfile();
        if (enableButtons && profile != null) {
            //profilePictureView.setProfileId(profile.getId());
            //Utils.UserPic ="portrait_10http://graph.facebook.com/"+profile.getId()+"/picture";
            Utils.UserPic ="facebook_"+profile.getId();
            Utils.UserName =profile.getFirstName();
            Log.i("","profile Id :"+profile.getId());
            //greeting.setText(getString(R.string.hello_user, profile.getFirstName()));

        } else {
            //profilePictureView.setProfileId(null);
            //greeting.setText(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(Tag, "Registration not found.");
            tracker.send(new HitBuilders.EventBuilder().setCategory("GCM")
                    .setAction("getRegistrationId").setLabel("not found").build());
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing registration ID is not guaranteed to work with
        // the new app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(Tag, "App version changed.");
            tracker.send(new HitBuilders.EventBuilder().setCategory("GCM")
                    .setAction("getRegistrationId").setLabel("App version changed").build());
            return "";
        }
        return registrationId;
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void,Void,String>() {

            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;

                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    sendRegistrationIdToBackend();

                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the registration ID - no need to register again.
                    storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }


            protected void onPostExecute(String msg) {
                Log.i(Tag, msg);
                tracker.send(new HitBuilders.EventBuilder().setCategory("GCM")
                        .setAction("registerInBackground").setLabel("onPostExecute").build());

                //login();
            }
        }.execute(null, null, null);

    }

    /**
     * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
     * or CCS to send messages to your app. Not needed for this demo since the
     * device sends upstream messages to a server that echoes back the message
     * using the 'from' address in the message.
     */
    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(Tag, "Saving regId on app version " + appVersion);
        tracker.send(new HitBuilders.EventBuilder().setCategory("GCM")
                .setAction("storeRegistrationId").setLabel("appVersion").build());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the registration ID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(Tag, "This device is not supported.");
                tracker.send(new HitBuilders.EventBuilder().setCategory("GCM")
                        .setAction("checkPlayServices").setLabel("not supported").build());
                Utils.ShowErrorAlertDialog(this, "This device is not supported.");
                //finish();
            }
            return false;
        }
        return true;
    }

    /*
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments

        Intent intent =new Intent() ;
        switch(position) {
            default:
            case 0:


                break;
            case 1:
                intent = new Intent(getApplicationContext(),
                        CarPoolActivity.class);
                startActivity(intent);
                break;
        }


    }
    */

    /*
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }
    */

    /*
    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }
    */


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("HandlerLeak")
    protected  void setCarInfo() {

        try {

            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    try {
                        // Bundle data = msg.getData();
                        // String jSonData = data.getString("Json_Data");
                        // int status = data.getInt("status");
                        // Log.i("", "result-->" + data.getString("Json_Data"));
                        // Log.i("", "status-->" + status);

                    } catch (Exception e) {
                        Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
                    }

                }
            };

            HttpClientRequest request = new HttpClientRequest(Utils.nowActivity,
                    Utils.APIUrl + "car/setCarInfo", HttpClientRequest.POST,
                    handler);
            HttpEntity postEntity = null;
            try {
                postEntity = new StringEntity("{\"accountId\":\""
                        + Utils.getSimSerialNumber(Utils.nowActivity)
                        + "\",\"accountName\":\"" + Utils.UserName
                        + "\",\"accountPic\":\"" + Utils.UserPic
                        + "\",\"transport\":" + 1 + ",\"status\":"
                        + 0 + ",\"latitude\":\""
                        + 0 + "\""
                        + ",\"longitude\":\""
                        + 0
                        + "\" }", "UTF-8");

            } catch (UnsupportedEncodingException e) {
                Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
            }

            request.setHttpEntity(postEntity);
            new Thread(request).start();

        } catch (Exception e) {
            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
