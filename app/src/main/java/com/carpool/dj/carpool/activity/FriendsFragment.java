
package com.carpool.dj.carpool.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.carpool.dj.carpool.R;
import com.carpool.dj.carpool.application.App;
import com.carpool.dj.carpool.application.App.TrackerName;
import com.carpool.dj.carpool.model.HttpClientRequest;
import com.carpool.dj.carpool.model.Utils;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * This shows how to draw circles on a map.
 */
@SuppressLint("CutPasteId")
public class FriendsFragment extends Fragment  {


    public static Tracker tracker;
    private static ListView listView;
    public static JSONArray resultList = new JSONArray();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i("","FriendsFragment onActivityCreated");

        try {
            listView = (ListView) this.getView().findViewById(R.id.listView);
        } catch (Exception e) {
            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
        }
    }
    @Override
    public void onAttach(Activity a) {

        super.onAttach(Utils.nowActivity);

        Log.i("","FriendsFragment onAttach");
		try {

			// Get tracker.
			tracker = ((App) Utils.nowActivity.getApplication()).getTracker(TrackerName.APP_TRACKER);
            tracker.enableAdvertisingIdCollection(true);

			// Set screen name.
			// Where path is a String representing the screen name.
            tracker.setScreenName("FriendsFragment");
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
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }



	@Override
    public void onStart() {
		super.onStart();
        Log.i("", "FriendsFragment onStart");
        getFriends();

	}

    public static void setList() {


        try {
            Log.i("","FriendsFragment setList");
            if (MapFragment.gps.canGetLocation()) {
                Log.i("","FriendsFragment canGetLocation");
                if(listView == null){
                    return;
                }
                listView.setAdapter(null);
                Log.i("","resultList length=>"+resultList.length());
                FriendsListAdapter adapter = new FriendsListAdapter(Utils.nowActivity,
                        resultList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                            long arg3) {

                        String accountId =  String.valueOf(arg1.findViewById(R.id.name).getTag());
                        try {
                            TextView name = (TextView) arg1.findViewById(R.id.name);
                            MapFragment.replay( name.getTag().toString(), name.getText().toString());
                        } catch (Exception e) {
                            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
                        }
                    }
                });
                Log.i("","setAdapter");

            }
        } catch (Exception e) {
            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
        }

    }


    @SuppressLint("SimpleDateFormat")
    public static void putList(String pic, String name, String id, String status) {

        JSONObject json = new JSONObject();
        try {
            json.put("pic", pic);
            json.put("name", name);
            json.put("id", id);
            json.put("status", status);

            //SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd/ HH:mm:ss");
            //Date today = Calendar.getInstance().getTime();
            //String reportDate = df.format(today);

            //json.put("time", reportDate);
            //json.put("msg", msg);
            resultList.put(resultList.length(), json);

        } catch (JSONException e) {
            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
        }
    }


    public static void getFriends() {

            try {

                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        try {
                            Bundle data = msg.getData();
                            String jSonData = data.getString("Json_Data");
                            int status = data.getInt("status");
                            Log.i("", "result-->" + data.getString("Json_Data"));
                            Log.i("", "status-->" + status);

                            if (status == 200 && !"".equals(jSonData)) {
                                JSONArray json = new JSONArray(jSonData);
                                resultList = new JSONArray();
                                for (int i = 0; i < json.length(); i++) {
                                    putList(json.getJSONObject(i)
                                            .getString("accountPic"), json.getJSONObject(i)
                                            .getString("accountName"), json.getJSONObject(i)
                                            .getString("accountId"),json.getJSONObject(i)
                                            .getString("status"));
                                }

                            }
                            setList();


                        } catch (Exception e) {
                            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
                        }

                    }
                };

                HttpClientRequest request = new HttpClientRequest(Utils.nowActivity,
                        Utils.APIUrl + "car/getCarFriends?accountId="+Utils.getSimSerialNumber(Utils.nowActivity), HttpClientRequest.GET,
                        handler);


                new Thread(request).start();

            } catch (Exception e) {
                Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
            }

        }


    }
