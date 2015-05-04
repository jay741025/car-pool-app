package com.carpool.dj.carpool.model;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager.BadTokenException;
import android.widget.Toast;

import com.carpool.dj.carpool.application.App;
import com.carpool.dj.carpool.application.App.TrackerName;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HttpClientRequest implements Runnable {

    public static final String GCM = "gcm";
    public static final String GET = "get";
    public static final String POST = "post";
    private final Context context;
    private final Handler handler;
    private final String method;
    private final String url;
    public Map<String, String> params;
    HttpClient client;
    @SuppressLint("HandlerLeak")
    private Handler errorHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            try {
                Bundle data = msg.getData();
                // Utils.ShowErrorAlertDialog(context,
                // data.getString("ErrorMsg"));
                Toast.makeText(context, "伺服器有異常，請稍後再試!",
                        Toast.LENGTH_LONG).show();
                Tracker t = ((App) context.getApplicationContext())
                        .getTracker(TrackerName.APP_TRACKER);
                t.enableAdvertisingIdCollection(true);
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("HttpClientRequest")
                        .setAction("errorHandler")
                        .setLabel(data.getString("ErrorMsg")).build());

            } catch (BadTokenException e) {

            }
        }

    };
    // private ProgressDialog pd;
    private HttpEntity postEntity;
    private int status;

    public HttpClientRequest(Context context, String url, String method,
                             Handler handler) {
        this.url = url.replaceAll(" ", "%20");
        this.method = method;
        this.handler = handler;
        this.context = context;

    }

    /**
     * Issue a POST request to the server.
     *
     * @param endpoint POST address.
     * @param params   request parameters.
     * @throws java.io.IOException propagated from POST.
     */
    @SuppressLint("NewApi")
    private static void post(String endpoint, Map<String, String> params)
            throws IOException {
        URL url;
        try {
            url = new URL(endpoint);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("invalid url: " + endpoint);
        }
        StringBuilder bodyBuilder = new StringBuilder();
        Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
        // constructs the POST body using the parameters
        while (iterator.hasNext()) {
            Entry<String, String> param = iterator.next();
            bodyBuilder.append(param.getKey()).append('=')
                    .append(param.getValue());
            if (iterator.hasNext()) {
                bodyBuilder.append('&');
            }
        }
        String body = bodyBuilder.toString();

        byte[] bytes = body.getBytes();
        HttpURLConnection conn = null;
        try {

            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setFixedLengthStreamingMode(bytes.length);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded;charset=UTF-8");
            // post the request
            OutputStream out = conn.getOutputStream();
            out.write(bytes);
            out.close();
            // handle the response
            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Post failed with error code " + status);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String doGet() throws Exception, IOException {
        String jsonData = "";
        HttpGet get = new HttpGet(url);
        get.addHeader("Content-Type", "application/json");
        get.addHeader("Authentication", "1111");
        StringBuffer sb = new StringBuffer();
        HttpResponse resp = client.execute(get);

        status = resp.getStatusLine().getStatusCode();
        Log.i("Response_Status", status + "");

        if (status == 200 || status == 500) {
            String line = null;
            BufferedReader r = new BufferedReader(new InputStreamReader(resp
                    .getEntity().getContent()));

            while ((line = r.readLine()) != null) {
                sb.append(line + "\n");
            }
            jsonData = new String(sb.toString().getBytes("UTF-8"));
            // jsonData = sb.toString();

            // if(jsonData.indexOf("header") != -1){
            // jsonData = checkResponseData(jsonData);
            // }

            if (status == 500) {
                jsonData = errorParsersData(jsonData);

            } else {

            }

            // } else if(status == 500){
            // String line = null;
            // BufferedReader r = new BufferedReader(new
            // InputStreamReader(resp.getEntity().getContent()));
            //
            // while ((line = r.readLine()) != null) {
            // sb.append(line + "\n");
            // }
            // jsonData = new String(sb.toString().getBytes("UTF-8"));
            // //jsonData = sb.toString();
            // errorParsersData(jsonData);
            //
        } else if (status == 403 || status == 401 || status == 204) {

        } else {
            Message msg = new Message();
            Bundle data = new Bundle();

            data.putString("ErrorMsg",
                    "ERROR:" + status + "\n" + resp.getStatusLine());
            msg.setData(data);

            errorHandler.sendMessage(msg);

        }

        // Log.i("", jsonData);

        return jsonData;
    }

    private String doPost() throws Exception, IOException {
        String jsonData = "";

        HttpPost post = new HttpPost(url);

        post.addHeader("Content-Type", "application/json");
        post.addHeader("Authentication", "1111");
        // UrlEncodedFormEntity postEntity = new
        // UrlEncodedFormEntity(data,HTTP.UTF_8);

        if (postEntity != null) {
            post.setEntity(postEntity);
            Log.i("postEntity", EntityUtils.toString(postEntity));
        } else {
            Log.i("HttpClientRequest", "HttpEntity postEntity is null!!");
        }

        StringBuffer sb = new StringBuffer();
        HttpResponse resp = client.execute(post);

        status = resp.getStatusLine().getStatusCode();
        Log.i("Response_Status", status + "");

        if (status == 200 || status == 500) {
            String line = null;
            BufferedReader r = new BufferedReader(new InputStreamReader(resp
                    .getEntity().getContent()));

            while ((line = r.readLine()) != null) {
                sb.append(line + "\n");
            }
            jsonData = new String(sb.toString().getBytes("UTF-8"));
            // jsonData = sb.toString();
            if (status == 500) {

                jsonData = errorParsersData(jsonData);
            } else {

            }

            // } else if(status == 500){
            // String line = null;
            // BufferedReader r = new BufferedReader(new
            // InputStreamReader(resp.getEntity().getContent()));
            //
            // while ((line = r.readLine()) != null) {
            // sb.append(line + "\n");
            // }
            // jsonData = new String(sb.toString().getBytes("UTF-8"));
            // //jsonData = sb.toString();
            // errorParsersData(jsonData);

        } else if (status == 403 || status == 401 || status == 204) {

        } else {
            Message msg = new Message();
            Bundle data = new Bundle();

            data.putString("ErrorMsg",
                    "ERROR:" + status + "\n" + resp.getStatusLine());
            msg.setData(data);

            errorHandler.sendMessage(msg);

        }
        Log.i("", jsonData);

        return jsonData;
    }

    private String errorParsersData(String jsonData) throws JSONException {
        Log.e("HttpClientRequest->errorParsersData", "JsonData:" + jsonData);
        JSONObject json = new JSONObject(jsonData);

        if ("exception".equals(json.getString("responseStatusCode"))) {
            Message msg = new Message();
            Bundle data = new Bundle();

            String error_Message = json.getString("responseStatusDesc");
            data.putString("ErrorMsg", error_Message);
            msg.setData(data);
            errorHandler.sendMessage(msg);
            Log.e("HttpClientRequest->errorParsersData", error_Message);
            return "";
        } else {
            return jsonData;
        }

    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

        String jsonData = "";
        Message msg;
        Bundle data = new Bundle();

        try {

            Log.i("HttpClientRequest", "URL->" + url + "\n Method->" + method);

            client = new DefaultHttpClient();
            HttpConnectionParams
                    .setConnectionTimeout(client.getParams(), 10000);

            if (HttpClientRequest.POST.equals(method)) {
                jsonData = doPost();
            } else if (HttpClientRequest.GET.equals(method)) {
                jsonData = doGet();
            } else if (HttpClientRequest.GCM.equals(method)) {
                post(url, params);
            }

        } catch (Exception e) {

            data.putString("ErrorMsg", "ERROR:網路異常\n" + e.getMessage());
            msg = errorHandler.obtainMessage();
            msg.what = 2;
            msg.setData(data);

            errorHandler.sendMessage(msg);

            e.printStackTrace();
            Log.e("", e.toString());

        } finally {
            data.putString("Json_Data", jsonData);
            data.putInt("status", status);
            if (handler != null) {
                msg = handler.obtainMessage();
                msg.what = 3;
                msg.setData(data);
                handler.sendMessage(msg);
                client.getConnectionManager().shutdown();
            }
        }

    }

    public void setHttpEntity(HttpEntity postEntity) {
        this.postEntity = postEntity;
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        // pd = progressDialog;
    }

}
