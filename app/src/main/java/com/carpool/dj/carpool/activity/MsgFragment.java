
package com.carpool.dj.carpool.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.carpool.dj.carpool.R;
import com.carpool.dj.carpool.application.App;
import com.carpool.dj.carpool.application.App.TrackerName;
import com.carpool.dj.carpool.model.Utils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This shows how to draw circles on a map.
 */
@SuppressLint("CutPasteId")
public class MsgFragment extends Fragment  {


    public static Tracker tracker;
    public static JSONArray resultList = new JSONArray();
    private static ListView listView;

    public static void setList() {


        try {
            Log.i("","MsgFragment setList");
            if (MapFragment.gps.canGetLocation()) {
                Log.i("","MsgFragment canGetLocation");
                listView.setAdapter(null);
                Log.i("","resultList length=>"+resultList.length());
                MsgListAdapter adapter = new MsgListAdapter(Utils.nowActivity,
                resultList);
                listView.setAdapter(adapter);
                Log.i("","setAdapter");

            }
        } catch (Exception e) {
            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
        }

    }
    @SuppressLint("SimpleDateFormat")
    public static void putList(String pic, String name, String id, String msg) {

        JSONObject json = new JSONObject();
        try {
            json.put("pic", pic);
            json.put("name", name);
            json.put("id", id);

            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd/ HH:mm:ss");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);

            json.put("time", reportDate);
            json.put("msg", msg);
            resultList.put(resultList.length(), json);

        } catch (JSONException e) {
            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
        }
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i("","MsgFragment onActivityCreated");

        try {
            listView = (ListView) this.getView().findViewById(R.id.listView);
        } catch (Exception e) {
            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
        }
    }
    @Override
    public void onAttach(Activity a) {

        super.onAttach(Utils.nowActivity);

        Log.i("","MsgFragment onAttach");
		try {

			// Get tracker.
			tracker = ((App) Utils.nowActivity.getApplication()).getTracker(TrackerName.APP_TRACKER);
            tracker.enableAdvertisingIdCollection(true);

			// Set screen name.
			// Where path is a String representing the screen name.
            tracker.setScreenName("MsgFragment");
			// Send a screen view.
            tracker.send(new HitBuilders.AppViewBuilder().build());




		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}

	}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (ContentActivity.getContentMenu() != null) {
            ContentActivity.getContentMenu().clear();
            Utils.nowActivity.getMenuInflater().inflate(R.menu.global,
                    ContentActivity.getContentMenu());
        }
        return inflater.inflate(R.layout.fragment_msg, container, false);
    }



	@Override
    public void onStart() {
		super.onStart();
        Log.i("","MsgFragment onStart");
        setList();

	}

		
}
