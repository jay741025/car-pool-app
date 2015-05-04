package com.carpool.dj.carpool.model;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.carpool.dj.carpool.R;
import com.carpool.dj.carpool.activity.MainActivity;
import com.carpool.dj.carpool.activity.MapFragment;
import com.carpool.dj.carpool.application.App;
import com.carpool.dj.carpool.application.App.TrackerName;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@SuppressLint("NewApi")
public class Utils {

    public static String APIUrl = "http://52.68.57.8:10080/carpool/api/v1/";
    public static String API_IP = "http://52.68.57.8:10080/";
    public static String EMAIL_ADDRESS = "kuochelee@fareastone.com.tw";
    public static JSONObject FriendPic = new JSONObject();
    public static Activity nowActivity = null;

    public static String SimSerialNumber = "";
    public static String UserName = "";
    public static String UserPic = "";

    public static MapFragment.DraggableCircle circlesBak =null;

    @SuppressLint("LongLogTag")
    public static boolean checkNetworkConnected(Context context) {
        String result = "";
        ConnectivityManager CM = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (CM == null) {
            result = "ConnectivityManager not support";
        } else {
            NetworkInfo info = CM.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (!info.isAvailable()) {
                    result = "網路無法使用";
                } else {

                    URL url = null;
                    try {
                        url = new URL(APIUrl);
                    } catch (MalformedURLException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        // result = "MalformedURLException:" + e.getMessage();
                        result = "系統維護中，抱歉!請稍後在試。";
                    }

                    HttpURLConnection con = null;
                    try {
                        con = (HttpURLConnection) url.openConnection();
                        con.setConnectTimeout(5000);
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        // result = "IOException:" + e.getMessage();
                        result = "系統維護中，抱歉!請稍後在試。";
                    }
                    try {
                        if (con.getResponseCode() == 200)// 200為正常連線404為無法連線
                            result = "OK";
                        else
                            result = "系統維護中，抱歉!請稍後在試。";
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        // result = "IOException:" + e.getMessage();
                        result = "系統維護中，抱歉!請稍後在試。";
                        Log.d("", result);
                    }

                }
                Log.d("Utils_>checkNetworkConnected()",
                        "[目前連線方式]" + info.getTypeName());
                Log.d("Utils_>checkNetworkConnected()",
                        "[目前連線狀態]" + info.getState());
                Log.d("Utils_>checkNetworkConnected()",
                        "[目前網路是否可使用]" + info.isAvailable());
                Log.d("Utils_>checkNetworkConnected()",
                        "[網路是否已連接]" + info.isConnected());
                Log.d("Utils_>checkNetworkConnected()", "[網路是否已連接 或 連線中]"
                        + info.isConnectedOrConnecting());
                Log.d("Utils_>checkNetworkConnected()",
                        "[網路目前是否有問題 ]" + info.isFailover());
                /*
				 * Log.d("Utils_>checkNetworkConnected()", "[網路目前是否在漫遊中]" +
				 * info.isRoaming());
				 */
            }
        }

        Log.d("result=>", result);

        if ("OK".equals(result)) {
            return true;
        } else {

            ShowErrorAlertDialog(context, result);
            Tracker t = ((App) context.getApplicationContext())
                    .getTracker(TrackerName.APP_TRACKER);
            t.enableAdvertisingIdCollection(true);
            t.send(new HitBuilders.EventBuilder().setCategory("Check")
                    .setAction("Check Network Connected").setLabel(result)
                    .build());
            return false;
        }

    }

    /*
	public static String downloadImage(Context context, Uri imageUri) {

		File cacheDir;
		// if the device has an SD card
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					".OCFL311");
		} else {
			// it does not have an SD card
			cacheDir = context.getCacheDir();
		}

		if (!cacheDir.exists())
			cacheDir.mkdirs();
		File f = new File(cacheDir, "gallery3d");

		try {

			InputStream is = null;
			if (imageUri.toString().startsWith(
					"content://com.google.android.gallery3d")) {
				is = context.getContentResolver().openInputStream(imageUri);
			} else {
				is = new URL(imageUri.toString()).openStream();
			}

			OutputStream os = new FileOutputStream(f);
			ImageUtils.CopyStream(is, os);

			return f.getAbsolutePath();
		} catch (Exception ex) {
			Log.d(context.getClass().getName(), "Exception: " + ex.getMessage());
			// something went wrong
			ex.printStackTrace();
			return null;
		}
	}
        */
    public static void ExceptionHandler(Exception e, Tracker t, Context context) {


        e.printStackTrace();
        Utils.ShowErrorAlertDialog(context, e.getMessage());

        if(t == null)
            return;

        t.send(new HitBuilders.ExceptionBuilder()
                .setDescription(
                        new StandardExceptionParser(context, null)
                                .getDescription(Thread.currentThread()
                                        .getName(), e)).setFatal(false).build());
    }

    public static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        return "Android SDK: " + sdkVersion + " (" + release + ")";
    }

    public static String getMacAddress(Context context) {
        WifiManager wimanager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        String macAddress = wimanager.getConnectionInfo().getMacAddress();
        if (macAddress == null) {
            macAddress = "Device don't have mac address or wi-fi is disabled";
        }
        return macAddress;
    }

    public static Activity getNowActivity() {
        return nowActivity;
    }

    public static void setNowActivity(Activity nowActivity) {
        Utils.nowActivity = nowActivity;
    }

    public static int getResourceId(Context context, String pVariableName,
                                    String pResourcename, String pPackageName) {
        try {
            return context.getResources().getIdentifier(pVariableName,
                    pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Alert 錯誤訊息
    public static String getSimSerialNumber(Context context) {

        if ("".equals(SimSerialNumber) || SimSerialNumber == null) {
            TelephonyManager telemamanger = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            SimSerialNumber = telemamanger.getSimSerialNumber();
            Log.i("", "SimSerialNumber=" + SimSerialNumber);

            TelephonyManager mTelephonyMgr = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if ("".equals(SimSerialNumber) || SimSerialNumber == null) {
                SimSerialNumber = mTelephonyMgr.getSubscriberId();
            }
            if ("".equals(SimSerialNumber) || SimSerialNumber == null) {
                SimSerialNumber = mTelephonyMgr.getDeviceId();
            }
            Log.i("", "SimSerialNumber=" + SimSerialNumber);

            return SimSerialNumber;
        } else {
            return SimSerialNumber;
        }

    }

    public static String getUsername(Context context) {

        if (!"".equals(UserName)) {
            return UserName;
        }
        AccountManager manager = AccountManager.get(context);
        Account[] accounts = manager.getAccountsByType("com.google");
        List<String> possibleEmails = new LinkedList<String>();

        for (Account account : accounts) {
            // TODO: Check possibleEmail against an email regex or treat
            // account.name as an email address only for certain account.type
            // values.
            possibleEmails.add(account.name);
        }

        if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null) {
            String email = possibleEmails.get(0);
            String[] parts = email.split("@");
            if (parts.length > 0 && parts[0] != null) {
                UserName = parts[0];
                return parts[0];
            } else
                return "";
        } else
            return "";
    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * Showing google speech input dialog
     */


    public static void promptSpeechInput(Activity activity, String speech_prompt) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                Locale.TAIWAN.toString());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                Locale.ENGLISH.toString());

        if ("".equals(speech_prompt)) {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    activity.getString(R.string.speech_prompt));
        } else {
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    speech_prompt);
        }
        try {
            activity.startActivityForResult(intent, 3);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(activity.getApplicationContext(),
                    activity.getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public static void sendEmail(Context context, String emailAddress,
                                 String emailSubject, String emailBody) {

        Intent emailIntent = new Intent();
        emailIntent.setAction(Intent.ACTION_SEND);
        emailIntent.setType("plain/html");
        emailIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{emailAddress});
        emailIntent
                .putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody);
        context.startActivity(Intent.createChooser(emailIntent,
                "Exception Sending email..."));
    }

    public static void setUserPic(Activity activity, ImageView imageView,
                                  String pic) {

        if ("".equals(pic) || activity == null || imageView == null) {
            return;
        }
        if (pic.indexOf("portrait") > -1 || pic.indexOf("group") > -1
                || pic.indexOf("boy") > -1) {
            imageView.setImageResource(Utils.getResourceId(activity, pic,
                    "drawable", activity.getPackageName()));
        } else {
            ImageLoader imgLoader = new ImageLoader(activity);
            String strURL = Utils.APIUrl
                    + "vcard/servlet/test/ShowUserPic?seq=" + pic;
            imgLoader.DisplayImage(strURL, imageView);
        }
    }

    // Alert Message
    public static void ShowAlertDialog(Context context, String title, String msg) {
        ShowAlertDialog(context, title, msg, null, "確認");
    }

    // Alert Message add OnClickListener and botton text
    public static void ShowAlertDialog(Context context, String title,
                                       String msg, OnClickListener click, String btn_text) {

        // Looper.prepare();
        Builder MyAlertDialog = new Builder(context);
        MyAlertDialog.setTitle(title);
        MyAlertDialog.setMessage(msg);
        MyAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        MyAlertDialog.setCancelable(false);
        // Looper.loop();
        if (click == null) {
            click = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 如果不做任何事情 就會直接關閉 對話方塊
                }
            };
        }
        MyAlertDialog.setNeutralButton(btn_text, click);
        MyAlertDialog.show();

    }

    // Alert Message add OnClickListener and botton text
    public static void ShowAlertDialog(Context context, String title,
                                       String msg, OnClickListener click1, String btn_text1,
                                       OnClickListener click2, String btn_text2, View layout) {
        // Looper.prepare();
        Builder MyAlertDialog = new Builder(context);
        MyAlertDialog.setTitle(title);
        MyAlertDialog.setMessage(msg);
        MyAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        if (click1 == null) {
            click1 = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 如果不做任何事情 就會直接關閉 對話方塊
                }
            };

        }
        if (click2 == null) {
            click2 = new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 如果不做任何事情 就會直接關閉 對話方塊
                }
            };

        }

        if (layout != null) {
            MyAlertDialog.setView(layout);
        }

        MyAlertDialog.setCancelable(false);
        MyAlertDialog.setNeutralButton(btn_text1, click1);
        MyAlertDialog.setPositiveButton(btn_text2, click2);
        MyAlertDialog.show();

    }

    // Alert 錯誤訊息
    public static void ShowErrorAlertDialog(Context context, String msg) {
        ShowAlertDialog(context, "錯誤訊息", msg, null, "確認");
    }


}