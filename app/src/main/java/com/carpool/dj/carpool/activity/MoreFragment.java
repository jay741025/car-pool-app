
package com.carpool.dj.carpool.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.carpool.dj.carpool.R;
import com.carpool.dj.carpool.application.App;
import com.carpool.dj.carpool.application.App.TrackerName;
import com.carpool.dj.carpool.model.Utils;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * This shows how to draw circles on a map.
 */
@SuppressLint("CutPasteId")
public class MoreFragment extends Fragment  {


    public static Tracker tracker;
    private ProfilePictureView profilePictureView;
    private Button logout ;
    private Button contactMe ;
    private Button termsOfService ;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i("","MoreFragment onActivityCreated");

        try {
            profilePictureView = (ProfilePictureView) this.getView().findViewById(R.id.profilePicture);
            profilePictureView.setProfileId(Utils.UserPic.replace("facebook_",""));

            TextView userName = (TextView) this.getView().findViewById(R.id.textView1);
            userName.setText(Utils.UserName);

            logout = (Button) this.getView().findViewById(R.id.logout);
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LoginManager.getInstance().logOut();
                    MapFragment.login =false;
                    MapFragment.onStatus = 0;
                    Utils.circlesBak = null ;
                    Utils.nowActivity.finish();
                }
            });

            contactMe = (Button) this.getView().findViewById(R.id.contactMe);
            contactMe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailIntent = new Intent(
                            android.content.Intent.ACTION_SEND);
                    emailIntent.setType("plain/text");
                    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                            new String[]{"jay741025@gmail.com"});
                    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                            getString(R.string.app_name));
                    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                            "");
                    Utils.nowActivity.startActivity(Intent.createChooser(emailIntent, ""));
                }
            });

            termsOfService = (Button) this.getView().findViewById(R.id.termsOfService);
            termsOfService.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.ShowAlertDialog(Utils.nowActivity, "隱私權條款", "共乘趣APP 隱私權條款\n" +
                            "歡迎您使用共乘趣應用程式(以下簡稱App)，請您開始使用前，務必詳讀下列條款內容。若您點擊「同意」，即表示您已閱讀、了解並同意遵守下列條款，此條款同步顯示於共乘趣App開啟時:\n" +
                            "1.行車安全\n" +
                            "為確保您的安全，在步行或開車期間請不要操作App，以免發生意外事故。\n" +
                            "1.GPS定位\n" +
                            "App除GPS定位外，另使用行動通訊定位技術，當您透過網路連線或簡訊取得座標時，此座標會依定位來源不同而與您實際位置有一定範圍的誤差；GPS定位雖較為精準，但可能仍因天候或使用地點（如：室內大多無法使用GPS定位）而有所影響，故App提供之座標數據僅供參考，無法確保其正確性。\n" +
                            "1.免責條款\n" +
                            "共乘趣公司擁有隨時更改、提升、中斷App各項功能或服務之權力，本公司不需先行通知任何使用者及單位。\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "\n" +
                            "COPYRIGHT © 共乘趣 CO.,LTD. ALL RIGHT RESERVED");
                }
            });

        } catch (Exception e) {
            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
        }
    }
    @Override
    public void onAttach(Activity a) {

        super.onAttach(Utils.nowActivity);

        Log.i("","MoreFragment onAttach");
		try {

			// Get tracker.
			tracker = ((App) Utils.nowActivity.getApplication()).getTracker(TrackerName.APP_TRACKER);
            tracker.enableAdvertisingIdCollection(true);

			// Set screen name.
			// Where path is a String representing the screen name.
            tracker.setScreenName("MoreFragment");
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
        return inflater.inflate(R.layout.fragment_more, container, false);
    }



	@Override
    public void onStart() {
		super.onStart();

	}

		
}
