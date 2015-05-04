package com.carpool.dj.carpool.application;

import android.app.Application;
import android.util.Log;

import com.carpool.dj.carpool.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * @author Jay Lee
 *         <p/>
 *         整个应用的上下文对象
 */
public class App extends Application {
    private static final App instance = new App();
    // The following line should be changed to include the correct property id.
    private static final String PROPERTY_ID = "UA-55687910-2";
    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();
    /**
     * 全局变量
     */
    private String name = "Jay Lee";

    //
    public App() {
        super();
    }

    /**
     * 此方法方便在那些没有context对象的类中使用
     *
     * @return MyApp实例
     */
    public static App getApplicationInstance() {
        return instance;
    }

    /* setter/getter 方法 */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public  synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    /*
     * android应用程序真正入口。 此方法在所有activity，servie，receiver组件之前调用
     */
    @Override
    public void onCreate() {
        super.onCreate();// 必须调用父类方法
        Log.i("CREATE", "application created....");
    }

    /**
     * Enum used to identify the tracker that needs to be used for tracking.
     *
     * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
     * storing them all in Application object helps ensure that they are created only once per
     * application instance.
     */
    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }



}