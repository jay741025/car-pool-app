package com.carpool.dj.carpool.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.carpool.dj.carpool.R;
import com.carpool.dj.carpool.model.ImageLoader;
import com.carpool.dj.carpool.model.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


@SuppressLint("ViewHolder")
public class MsgListAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;

    private Activity activity;
    private JSONArray data;

    private ImageLoader imgLoader;

    // private String authToken ;
    private View vi;

    public MsgListAdapter(Activity a, JSONArray d) {
        activity = a;
        data = d;
        inflater = (LayoutInflater) activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        Log.i("", "getCount:"+data.length());
        return data.length();
    }

    @Override
    public Object getItem(int position) {
        Log.i("", "getItem:"+position);
        return position;
    }

    @Override
    public long getItemId(int position) {
        Log.i("", "getItemId:"+position);
        return position;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        vi = convertView;
        Log.i("", "getView");
        JSONObject oj;
        try {
            oj = data.getJSONObject(data.length() - 1 - position);
            vi = inflater.inflate(R.layout.msg_row, null);
            TextView msg = (TextView) vi.findViewById(R.id.msg);
            msg.setText(oj.getString("msg"));
            TextView time = (TextView) vi.findViewById(R.id.time);
            time.setText(oj.getString("time"));
            TextView name = (TextView) vi.findViewById(R.id.name);
            name.setTag(oj.getString("id"));
            name.setText(oj.getString("name"));

           //ProfilePictureView profilePictureView = (ProfilePictureView) vi
                  // .findViewById(R.id.profilePicture);
           //profilePictureView.setProfileId(oj.getString("pic").replace("facebook_",""));



        } catch (JSONException e1) {
            Utils.ExceptionHandler(e1, ContentActivity.tracker, activity);
            Utils.ShowErrorAlertDialog(activity, e1.getMessage());
        }

        return vi;
    }

}