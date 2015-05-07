package com.carpool.dj.carpool.activity;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.carpool.dj.carpool.R;
import com.carpool.dj.carpool.application.App;
import com.carpool.dj.carpool.model.Utils;
import com.carpool.dj.carpool.view.BadgeView;
import com.facebook.login.LoginManager;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Locale;


public class ContentActivity extends FragmentActivity {

	public static BadgeView badge;
	public static Activity activity;
	private static Menu contentMenu;
	public static Tracker tracker;
    private final int REQ_CODE_SPEECH_INPUT = 3;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		super.onConfigurationChanged(newConfig);
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			// 什麼都不用寫
		} else {
			// 什麼都不用寫
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(Utils.UserName.equals("")){
            Intent intent = new Intent(getApplicationContext(),
                    MainActivity.class);
            startActivity(intent);
        }
		setContentView(R.layout.content_main);


		activity = this;
		Utils.setNowActivity(activity);
		// Get tracker.
		tracker = ((App) getApplication()).getTracker(App.TrackerName.APP_TRACKER);
        tracker.enableAdvertisingIdCollection(true);
		// Set screen name.
		// Where path is a String representing the screen name.
        tracker.setScreenName("ContentActivity");
		// Send a screen view.
        tracker.send(new HitBuilders.AppViewBuilder().build());

		FragmentTabHost tabHost = (FragmentTabHost) findViewById(android.R.id.tabhost);
		tabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		// 1
		tabHost.addTab(
                tabHost.newTabSpec(getString(R.string.map)).setIndicator(getString(R.string.map), null
                ),
                MapFragment.class, null);
		// 2
		tabHost.addTab(
				tabHost.newTabSpec(getString(R.string.msg)).setIndicator(getString(R.string.msg),null
						),
                MsgFragment.class, null);
        // 2
        tabHost.addTab(
                tabHost.newTabSpec(getString(R.string.friends)).setIndicator(getString(R.string.friends),null
                ),
                FriendsFragment.class, null);
		// 3
		tabHost.addTab(
				tabHost.newTabSpec(getString(R.string.online)).setIndicator(getString(R.string.online),
                        null),
                OnlineFragment.class, null);
		// 4
		tabHost.addTab(
				tabHost.newTabSpec(getString(R.string.more)).setIndicator(getString(R.string.more),
                        null),
                MoreFragment.class, null);



        Locale l = Locale.getDefault();
        String language = l.getLanguage();

        if (!"zh".equals(language)) {
            final TabWidget tw = (TabWidget) tabHost.findViewById(android.R.id.tabs);
            for (int i = 0; i < tw.getChildCount(); ++i) {
                final View tabView = tw.getChildTabViewAt(i);
                final TextView tv = (TextView) tabView.findViewById(android.R.id.title);
                tv.setTextSize(10);
            }
        }

	}

    public static Menu getContentMenu() {
        return contentMenu;
    }

    @Override
    protected void onPause() {

        if (MapFragment.mTimer !=null){
            MapFragment.mTimer.cancel();
        }
        Log.i("", "SCREEN TURNED OFF");
        super.onPause();
    }

    @Override
    protected void onResume() {

        if(MapFragment.onStatus == 1){
            MapFragment.setTimerTask();
        }
        if(MapFragment.login) {
            //MapFragment.screen_turned_on();
        }
        Log.i("", "SCREEN TURNED ON");
        super.onResume();
    }

    public static void setContentMenu(Menu contentMenu) {
        ContentActivity.contentMenu = contentMenu;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        setContentMenu(menu);
        getMenuInflater().inflate(R.menu.car_pool, menu);
        return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Utils.ShowAlertDialog(activity , getString(R.string.msg) ,getString(R.string.areyouleft)  ,
               new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog,
                                    int which) {
                    LoginManager.getInstance().logOut();
                    MapFragment.login =false;
                    MapFragment.onStatus = 0;
                    Utils.circlesBak = null ;
                    finish();
                }
            }, getString(R.string.yes)  , null  , getString(R.string.cancelled)  , null ) ;

		}
		return super.onKeyDown(keyCode, event);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.path_setting:
                Utils.promptSpeechInput(activity, getString(R.string.wouldyouliketogo));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQ_CODE_SPEECH_INPUT:
                if (resultCode == RESULT_OK && null != data) {

                    final ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    DialogInterface.OnClickListener click1 = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            MapFragment.getLocationInfo(result.get(0),MapFragment.latitude_m,MapFragment.longitude_m,true);
                        }
                    };

                    final EditText input = new EditText(activity);

                    input.setText(result.get(0), TextView.BufferType.EDITABLE);

                    Utils.ShowAlertDialog(activity,  getString(R.string.wheredoyougo),  "",click1 ,getString(R.string.yes), null,getString(R.string.cancelled), input);

                }
                break;
        }
    }

	@Override
	public void onStart() {
		super.onStart();
        MapFragment.mCircles =null;
        FriendsFragment.getFriends();

	}



}
