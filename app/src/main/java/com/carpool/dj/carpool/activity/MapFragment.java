
package com.carpool.dj.carpool.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import com.carpool.dj.carpool.R;

import com.carpool.dj.carpool.application.App;
import com.carpool.dj.carpool.application.App.TrackerName;
import com.carpool.dj.carpool.model.DirectionsJSONParser;
import com.carpool.dj.carpool.model.GPSTracker;
import com.carpool.dj.carpool.model.HttpClientRequest;

import com.carpool.dj.carpool.model.Utils;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.games.internal.constants.TimeSpan;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This shows how to draw circles on a map.
 */
@SuppressLint("CutPasteId")
public class MapFragment extends Fragment implements
		OnMyLocationButtonClickListener, OnMarkerDragListener,
		OnMarkerClickListener, OnMapLongClickListener, OnMapReadyCallback {

    public static LatLng MyLocation = new LatLng(0, 0);
    public static double DEFAULT_RADIUS = 0;

	private int mColor = 0;
	private int mAlpha = 30;
	private static final  int mWidth = 2;
	private static  final float mZoom = 14.0f;
	public static GPSTracker gps;
	private static GoogleMap mMap;

	private static List<Marker> mMarker = new ArrayList<Marker>();
    public static DraggableCircle mCircles ;
	private static int mStrokeColor;
	private static int mFillColor;
	private Spinner spinner;
	private ArrayAdapter<String> lunchList;
	private static int transport = 1;
	public static int onStatus = 0;
	public static Tracker tracker;
	private ImageButton imageButton1;
	private ImageButton imageButton2;
	private ImageButton imageButton3;


    public static JSONArray markList = new JSONArray();
	private static JSONArray beforeMarkList = new JSONArray();
	private static JSONObject markMap = new JSONObject();
    public static JSONObject markMapbak = new JSONObject();
	public static Timer mTimer;
	private static Boolean moveLocation = false;
	private static Builder myAlertDialog;
	private static String toId = "";
	private static String toName = "";
	private static AlertDialog alertDialog;
	private static String backMsg;
	private static String backMsg2;
	private static Switch mySwitch;
    public static double latitude_m = 0;
    public static double longitude_m = 0;
    private static String address_m ="";
    private ProfilePictureView profilePictureView;

	private static final List<Marker> destinationMarkerOptionsList = new ArrayList<Marker>();
	private static final List<Polyline> PolylineList = new ArrayList<Polyline>();
    private static View view;
    public static Boolean login = false;
    private static ListView listView;

    public static class DraggableCircle {

		private static Marker centerMarker;

		private static Circle circle;

		public  DraggableCircle(LatLng center, double radius) {

			centerMarker = mMap.addMarker(new MarkerOptions()
					.position(center)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.pegman)).draggable(true));

			circle = mMap.addCircle(new CircleOptions().center(center)
					.radius(radius).strokeWidth(mWidth)
					.strokeColor(mStrokeColor).fillColor(mFillColor));
		}

		public static boolean onMarkerMoved(Marker marker) {
			if (marker.equals(centerMarker)) {
				circle.setCenter(marker.getPosition());
				return true;
			}

			return false;
		}

	}

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.i("","MapFragment onActivityCreated");
        try {


            profilePictureView = (ProfilePictureView) this.getView().findViewById(R.id.profilePicture);
            profilePictureView.setProfileId(Utils.UserPic.replace("facebook_",""));

            if(!login) {
                MsgFragment.putList(Utils.UserPic, Utils.UserName,
                        Utils.getSimSerialNumber(Utils.nowActivity), "歡迎使用共乘趣");
            }


            TextView userName = (TextView) this.getView().findViewById(R.id.textView1);
            userName.setText(Utils.UserName);

            if (gps.canGetLocation()) {
                spinner = (Spinner) this.getView().findViewById(R.id.spinner1);
                String[] lunch = { "1KM", "2KM", "3KM", "4KM", "5KM", "6KM",
                        "7KM", "8KM", "9KM", "10KM" };
                lunchList = new ArrayAdapter<String>(Utils.nowActivity,
                        android.R.layout.simple_spinner_item, lunch);
                spinner.setAdapter(lunchList);
                spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {


                        Log.i("","spinner onItemSelected");
                        mCircles.circle.setRadius((position + 1) * 1000);


                        if(DEFAULT_RADIUS !=  (position + 1) * 1000) {
                            Toast.makeText(Utils.nowActivity,
                                    "搜尋範圍為:" + (position + 1) + "公里",
                                    Toast.LENGTH_SHORT).show();
                            DEFAULT_RADIUS = (position + 1) * 1000;
                            MsgFragment.putList(Utils.UserPic, Utils.UserName,
                                    Utils.getSimSerialNumber(Utils.nowActivity), "搜尋範圍為:"
                                            + (position + 1) + "公里");
                        }

                        //MsgFragment.setList();
                        setCarInfo();

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        // TODO Auto-generated method stub

                    }
                });

                if (onStatus == 0 &&  !login ) {
                    MsgFragment.putList(Utils.UserPic, Utils.UserName,
                            Utils.getSimSerialNumber(Utils.nowActivity),
                            "請先上線，才可以搜尋到人喔!");
                }

                imageButton1 = (ImageButton) this.getView().findViewById(R.id.imageButton1);
                imageButton1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        transport = 1;
                        MsgFragment.putList(Utils.UserPic, Utils.UserName,
                                Utils.getSimSerialNumber(Utils.nowActivity),
                                "變更交通工具為=>走路");
                        imageButton1.setBackgroundColor(Color
                                .parseColor("#FF3333"));
                        imageButton2.setBackgroundColor(Color
                                .parseColor("#888888"));
                        imageButton3.setBackgroundColor(Color
                                .parseColor("#888888"));
                        //MsgFragment.setList();
                        setCarInfo();
                    }
                });
                imageButton2 = (ImageButton) this.getView().findViewById(R.id.imageButton2);
                imageButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        transport = 2;
                        MsgFragment.putList(Utils.UserPic, Utils.UserName,
                                Utils.getSimSerialNumber(Utils.nowActivity),
                                "變更交通工具為=>騎車");
                        imageButton2.setBackgroundColor(Color
                                .parseColor("#FF3333"));
                        imageButton1.setBackgroundColor(Color
                                .parseColor("#888888"));
                        imageButton3.setBackgroundColor(Color
                                .parseColor("#888888"));
                        //MsgFragment.setList();
                        setCarInfo();
                    }
                });
                imageButton3 = (ImageButton) this.getView().findViewById(R.id.imageButton3);
                imageButton3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        transport = 3;
                        MsgFragment.putList(Utils.UserPic, Utils.UserName,
                                Utils.getSimSerialNumber(Utils.nowActivity),
                                "變更交通工具為=>開車");
                        imageButton3.setBackgroundColor(Color
                                .parseColor("#FF3333"));
                        imageButton2.setBackgroundColor(Color
                                .parseColor("#888888"));
                        imageButton1.setBackgroundColor(Color
                                .parseColor("#888888"));
                        //MsgFragment.setList();
                        setCarInfo();
                    }
                });
                imageButton1.setBackgroundColor(Color.parseColor("#FF3333"));
                imageButton2.setBackgroundColor(Color.parseColor("#888888"));
                imageButton3.setBackgroundColor(Color.parseColor("#888888"));

                mySwitch = (Switch) this.getView().findViewById(R.id.switch1);

                // attach a listener to check for changes in state
                mySwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if (isChecked) {
                            Toast.makeText(Utils.nowActivity, "您上線了!",
                                    Toast.LENGTH_SHORT).show();
                            MsgFragment.putList(Utils.UserPic, Utils.UserName,
                                    Utils.getSimSerialNumber(Utils.nowActivity), "您上線了!");
                            onStatus = 1;
                            //MsgFragment.setList();
                            setCarInfo();
                            // getNearCar();
                            setTimerTask();

                        } else {
                            Toast.makeText(Utils.nowActivity, "您下線了!",
                                    Toast.LENGTH_SHORT).show();
                            MsgFragment.putList(Utils.UserPic, Utils.UserName,
                                    Utils.getSimSerialNumber(Utils.nowActivity), "您下線了!");
                            onStatus = 0;
                            //MsgFragment.setList();
                            setCarInfo();
                            for (Marker marker : mMarker) {
                                marker.remove();
                            }
                            mMarker.clear();
                        }

                    }
                });

                //MsgFragment.setList();
                mTimer = new Timer();
                login =true;

                // check if GPS enabled

                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();
                latitude_m = latitude;
                longitude_m = longitude;

                MyLocation = new LatLng(latitude, longitude);
                SupportMapFragment mapFragment;
                mapFragment = (SupportMapFragment)  getChildFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);



            } else {
                Toast.makeText(Utils.nowActivity, "can't get location",
                        Toast.LENGTH_LONG).show();
                LoginManager.getInstance().logOut();
                MapFragment.login =false;
                MapFragment.onStatus = 0;
                Utils.circlesBak = null ;
                Utils.nowActivity.finish();
            }



        } catch (Exception e) {
            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
        }


    }
    @Override
    public void onAttach(Activity a) {

        super.onAttach(Utils.nowActivity);

        Log.i("","MapFragment onAttach");
		try {
            MsgFragment.resultList = new JSONArray();

			// Get tracker.
			tracker = ((App) Utils.nowActivity.getApplication()).getTracker(TrackerName.APP_TRACKER);
            tracker.enableAdvertisingIdCollection(true);

			// Set screen name.
			// Where path is a String representing the screen name.
            tracker.setScreenName("MapFragment");
			// Send a screen view.
            tracker.send(new HitBuilders.AppViewBuilder().build());

			// create class object
			gps = new GPSTracker(Utils.nowActivity);


		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}

	}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (ContentActivity.getContentMenu() != null) {
            ContentActivity.getContentMenu().clear();
            Utils.nowActivity.getMenuInflater().inflate(R.menu.car_pool,
                    ContentActivity.getContentMenu());
        }
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException e) {
             /* map is already there, just return view as it is */
        }
        return view;

    }


	public static void setTimerTask() {

        Log.i("","setTimerTask");
        final Handler handler = new Handler();

        TimerTask timertask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            if (onStatus > 0) {
                                Message message = new Message();
                                message.what = 1;
                                doActionHandler.sendMessage(message);
                            }
                        } catch (Exception e) {
                            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
                        }
                    }
                });
            }
        };
        mTimer = new Timer(); //This is new
        mTimer.schedule(timertask, 50, 10000); // execute in every 15sec

	}

	/**
	 * do some action
	 */
	@SuppressLint("HandlerLeak")
	private static Handler doActionHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int msgId = msg.what;
			switch (msgId) {
			case 1:

				try {

                    Log.i("","doActionHandler");

					getNearCar();
					if (!moveLocation) {
						if (gps.canGetLocation()) {

							double latitude = gps.getLatitude();
							double longitude = gps.getLongitude();
							if (latitude == latitude_m
									&& longitude == longitude_m) {
								return;
							}
							latitude_m = latitude;
							longitude_m = longitude;
							MyLocation = new LatLng(latitude, longitude);
                            if(mCircles!=null) {
                                mCircles.centerMarker.setPosition(MyLocation);
                                mCircles.circle.setCenter(MyLocation);
                            }
							mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
									MyLocation, mZoom));
							setCarInfo();

						} else {
							Toast.makeText(Utils.nowActivity, "can't get location",
									Toast.LENGTH_LONG).show();
						}
					}

				} catch (Exception e) {
					Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
				}

				break;
			default:
				break;
			}
		}
	};




	public static void showSussuse() {

		try {
			ImageView imageView = new ImageView(Utils.getNowActivity());
			imageView.setImageResource(Utils.getResourceId(Utils.nowActivity,
					"car_pool_sussuse", "drawable", Utils.nowActivity.getPackageName()));

			new Builder(Utils.getNowActivity()).setTitle("恭喜")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(imageView).setPositiveButton("確定", null).show();

			mySwitch.setChecked(false);

		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}
	}

	public static void replay(final String accountId, final String accountName) {

		try {
			final EditText input = new EditText(Utils.getNowActivity());
			Builder builder = new Builder(Utils.getNowActivity())
					.setTitle("TALK")
					.setIcon(android.R.drawable.ic_dialog_info).setView(input);

			builder.setNeutralButton("TALK",
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							if (!"".equals(input.getText().toString())) {
								setCarEvent(accountId, accountName, 3, input
										.getText().toString());
								return;
							}

						}
					});
			builder.setPositiveButton("取消",
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			builder.show();
		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}

	}

	public static void showEvent(int eventType, final String accountId,
			final String accountName, String accountPic, String msg) {

		try {
			String title = "";

			LayoutInflater inflater = Utils.getNowActivity()
					.getLayoutInflater();
			final View layout = inflater.inflate(R.layout.car_pool_event,
					(ViewGroup) Utils.getNowActivity()
							.findViewById(R.id.dialog));

            ProfilePictureView profilePictureView;
            profilePictureView = (ProfilePictureView) layout.findViewById(R.id.profilePicture);
            profilePictureView.setProfileId(accountPic.replace("facebook_",""));

            if (!"".equals(accountId)) {
                profilePictureView.setTag(accountId);
            }



			if (!"".equals(accountName)) {
				TextView name = (TextView) layout.findViewById(R.id.textView1);
				name.setText(accountName);
			}

			TextView msgView = (TextView) layout.findViewById(R.id.msg);
			msgView.setText(msg);

			myAlertDialog = new Builder(Utils.nowActivity);
			myAlertDialog.setTitle(title);

			myAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
			myAlertDialog.setCancelable(true);
			myAlertDialog.setView(layout);

			if (eventType == 1) {
				title = "邀請您共乘";
				backMsg = "同意您共乘";
				backMsg2 = "不同意您共乘";
			} else if (eventType == 2) {
				title = "對方回覆";
			} else if (eventType == 3) {
				title = "對方TALK";
			} else if (eventType == 5) {
				title = "對方回覆";
			} else if (eventType == 7) {
				title = "對方回覆";
			} else if (eventType == 8) {
				title = "上車囉!";
				title = "同意您共乘";
			} else {
				return;
			}

			if (eventType == 1) {

				myAlertDialog.setNeutralButton("同意",
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setCarEvent(accountId, accountName, 2, backMsg);
							}
						});
				myAlertDialog.setPositiveButton("不同意",
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setCarEvent(accountId, accountName, 2, backMsg2);
							}
						});
			} else if (eventType == 3) {
				myAlertDialog.setNeutralButton("回覆",
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								replay(accountId, accountName);
							}
						});
				myAlertDialog.setPositiveButton("取消",
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
			} else if (eventType == 7) {
				myAlertDialog.setPositiveButton("確定",
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setCarEvent(accountId, accountName, 8,
										"恭喜使用共乘趣成功!");
								showSussuse();
							}
						});
			} else if (eventType == 8) {
				showSussuse();
				return;
			} else {

				myAlertDialog.setPositiveButton("確定",
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});
			}

			alertDialog = myAlertDialog.create();
			alertDialog.show();

		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}

	}

	@SuppressLint("HandlerLeak")
	protected static void setCarInfo() {

		try {
			if (onStatus == 0 && address_m.equals(""))
				return;

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
						+ "\",\"accountName\":\"" + URLEncoder.encode(Utils.UserName, "UTF-8")
                        + "\",\"destination\":\"" + URLEncoder.encode(address_m, "UTF-8")
						+ "\",\"accountPic\":\"" + Utils.UserPic
						+ "\",\"transport\":" + transport + ",\"status\":"
						+ onStatus + ",\"latitude\":\""
						+ mCircles.centerMarker.getPosition().latitude + "\""
						+ ",\"longitude\":\""
						+ mCircles.centerMarker.getPosition().longitude
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

	@SuppressLint("HandlerLeak")
	protected static void getNearCar() {

		try {
			if (onStatus == 0)
				return;


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

                            for (int i = 0; i < json.length(); i++) {
                                if (json.getJSONObject(i).getString("latitude").equals("0.0")
                                        || (Calendar.getInstance().getTimeInMillis() - json.getJSONObject(i).getLong("datetime")
                                ) > 1000*60*60*24) {
                                    remove(i,json);
                                    setCarOffline(json.getJSONObject(i).getString("accountId") ,json.getJSONObject(i).getString("accountName"));
                                }
                            }
							markList = json;
							for (Marker marker : mMarker) {
								marker.remove();
							}
							mMarker.clear();
							markMap = new JSONObject();
                            OnlineFragment.resultList = new JSONArray();
							for (int i = 0; i < json.length(); i++) {
								Marker marker = mMap
										.addMarker(new MarkerOptions()
												.position(
														new LatLng(
																Double.valueOf(json
																		.getJSONObject(
																				i)
																		.getString(
																				"latitude")),
																Double.valueOf(json
																		.getJSONObject(
																				i)
																		.getString(
																				"longitude"))))
												.title(json.getJSONObject(i)
														.getString("accountName"))
												.snippet("")

										);
								if (json.getJSONObject(i).getInt("transport") == 1) {
									marker.setIcon(BitmapDescriptorFactory
											.fromResource(R.drawable.run));
								} else if (json.getJSONObject(i).getInt(
										"transport") == 2) {
									marker.setIcon(BitmapDescriptorFactory
											.fromResource(R.drawable.moto));
								} else {
									marker.setIcon(BitmapDescriptorFactory
											.fromResource(R.drawable.car));
								}

								mMarker.add(marker);
								markMap.put(
										json.getJSONObject(i).getString(
												"accountId"), i);

                                OnlineFragment.putList(json.getJSONObject(i).getString(
                                        "accountPic"), json.getJSONObject(i).getString(
                                        "accountName"), json.getJSONObject(i).getString(
                                        "accountId"));
							}

							markMapbak = markMap;

							for (int i = 0; i < markList.length(); i++) {
								String accountId = markList.getJSONObject(i)
										.getString("accountId");
								Boolean find = false;
								for (int j = 0; j < beforeMarkList.length(); j++) {
									if (accountId.equals(beforeMarkList
											.getJSONObject(j).getString(
													"accountId"))) {
										find = true;
									}
								}
								if (!find) {
									String accountName = markList
											.getJSONObject(i).getString(
													"accountName");
									String accountPic = markList.getJSONObject(
											i).getString("accountPic");

                                    MsgFragment.putList(accountPic, Utils.UserName, Utils.SimSerialNumber,
                                            "發現了" + accountName);
								}
							}
							for (int i = 0; i < beforeMarkList.length(); i++) {
								String accountId = beforeMarkList
										.getJSONObject(i)
										.getString("accountId");
								Boolean find = false;
								for (int j = 0; j < markList.length(); j++) {
									if (accountId.equals(markList
											.getJSONObject(j).getString(
													"accountId"))) {
										find = true;
									}
								}
								if (!find) {
									String accountName = beforeMarkList
											.getJSONObject(i).getString(
													"accountName");
									String accountPic = beforeMarkList
											.getJSONObject(i).getString(
													"accountPic");

                                    MsgFragment.putList(accountPic, accountName, accountId,
                                            accountName + "離線了");
								}
							}
							beforeMarkList = markList;
                            //MsgFragment.setList();
							// Log.e("","mMarker size:"+mMarker.size());
						}

					} catch (Exception e) {
						Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
					}

				}
			};

			HttpClientRequest request = new HttpClientRequest(Utils.nowActivity,
					Utils.APIUrl + "car/getNearCar", HttpClientRequest.POST,
					handler);
			HttpEntity postEntity = null;
			try {


				postEntity = new StringEntity("{\"accountId\":\""
						+ Utils.getSimSerialNumber(Utils.nowActivity)
						+ "\",\"distance\":\"" + DEFAULT_RADIUS
						+ "\",\"latitude\":\""
						+ mCircles.centerMarker.getPosition().latitude + "\""
						+ ",\"longitude\":\""
						+ mCircles.centerMarker.getPosition().longitude
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
    public void onStart() {
		super.onStart();

	}

	@Override
	public void onMapReady(GoogleMap map) {

		try {
			mMap = map;

			// Override the default content description on the view, for
			// accessibility mode.
			// Ideally this string would be localised.
			mMap.setContentDescription("共乘趣");
			mMap.setOnMarkerClickListener(this);
			mMap.setOnMyLocationButtonClickListener(this);
			mMap.setMyLocationEnabled(true);

			mMap.setOnMarkerDragListener(this);
			mMap.setOnMapLongClickListener(this);

			mFillColor = Color.HSVToColor(mAlpha, new float[] { mColor, 1, 1 });
			mStrokeColor = Color.BLACK;

			if (gps.canGetLocation()) {
                if(mCircles == null) {

                    if(Utils.circlesBak == null) {
                        Log.i("",mCircles + "mCircles new DraggableCircle ");
                        mCircles = new DraggableCircle(MyLocation, DEFAULT_RADIUS);
                        Utils.circlesBak = mCircles;
                    }else{
                        mCircles =  Utils.circlesBak ;
                    }
                }
			}

			// Move the map so that it is centered on the initial circle
			mMap.moveCamera(CameraUpdateFactory
					.newLatLngZoom(MyLocation, mZoom));
			setCarInfo();

		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}

	}

	@Override
	public void onMarkerDragStart(Marker marker) {
		onMarkerMoved(marker);
	}

	@Override
	public void onMarkerDragEnd(Marker marker) {

		try {
			onMarkerMoved(marker);

			LatLng position = marker.getPosition();
			String address = getAddressByLocation(position.latitude,
					position.longitude);
            MsgFragment.putList(Utils.UserPic, Utils.UserName,
                    Utils.getSimSerialNumber(Utils.nowActivity),
                    "移動位置到=>"
                            + ("".equals(address) ? marker.getPosition()
                            : address));
			Toast.makeText(Utils.nowActivity,
					"移動位置到=> " +  ("".equals(address) ? marker.getPosition()
                            : address),
					Toast.LENGTH_LONG).show();
            //MsgFragment.setList();
			setCarInfo();
			moveLocation = true;

		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}
	}

	public String getAddressByLocation(Double latitude, Double longitude) {
		String returnAddress = "";
		try {
			if (latitude != null && longitude != null) {
				// Double longitude = location.getLongitude(); //取得經度
				// Double latitude = location.getLatitude(); //取得緯度

				Geocoder gc = new Geocoder(Utils.nowActivity, Locale.TRADITIONAL_CHINESE); // 地區:台灣
				// 自經緯度取得地址
				List<Address> lstAddress = gc.getFromLocation(latitude,
						longitude, 1);

				if (!Geocoder.isPresent()) { // Since: API Level 9
					returnAddress = "";
				}
				returnAddress = lstAddress.get(0).getAddressLine(0);
			}
		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}
		return returnAddress;
	}

	@Override
	public void onMarkerDrag(Marker marker) {
		onMarkerMoved(marker);
	}

	private void onMarkerMoved(Marker marker) {
		mCircles.onMarkerMoved(marker);

	}

	@Override
	public void onMapLongClick(LatLng point) {
		// We know the center, let's place the outline at a point 3/4 along the
		// view.
		// View view = ((SupportMapFragment)
		// getSupportFragmentManager().findFragmentById(R.id.map))
		// .getView();
		// LatLng radiusLatLng = mMap.getProjection().fromScreenLocation(new
		// Point(
		// view.getHeight() * 3 / 4, view.getWidth() * 3 / 4));

		// ok create it
		// DraggableCircle circle = new DraggableCircle(point, radiusLatLng);
		// mCircles.add(circle);
	}

	@Override
	public boolean onMyLocationButtonClick() {

		try {
			if (gps.canGetLocation()) {

				double latitude = gps.getLatitude();
				double longitude = gps.getLongitude();
				latitude_m = latitude;
				longitude_m = longitude;
				MyLocation = new LatLng(latitude, longitude);
				mCircles.centerMarker.setPosition(MyLocation);
				mCircles.circle.setCenter(MyLocation);
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MyLocation,
						mZoom));
				moveLocation = false;

			} else {
				Toast.makeText(Utils.nowActivity, "can't get location",
						Toast.LENGTH_LONG).show();
			}

		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}
		return false;
	}

	@Override
	public boolean onMarkerClick(Marker marker) {

        for (Polyline polyline : PolylineList) {
            polyline.remove();
        }

        PolylineList.clear();

		try {
			if (!"".equals(marker.getTitle())) {

				try {
                    Boolean find =false;
                    int index =-1 ;
                    for(int i = 0 ;i<markList.length();i++){
                        if(markList.getJSONObject(i)
                                .getString("accountName").equals(marker.getTitle())){
                            find =true;
                            index =i;
                        }
                    }
					if (find) {

                        String accountId = markList.getJSONObject(index)
                                .getString("accountId");
						String accountName = markList.getJSONObject(index)
								.getString("accountName");
						String accountPic = markList.getJSONObject(index)
								.getString("accountPic");
                        double latitudeS =  markList.getJSONObject(index)
                                .getDouble("latitude");
                        double longitudeS =  markList.getJSONObject(index)
                                .getDouble("longitude");

                        int near =0 ;
                        JSONArray resultList = new JSONArray();
                        JSONObject json = new JSONObject();

                        for(int i = 0 ;i<markList.length();i++){
                            double n =gps2m(latitudeS,longitudeS,
                                    markList.getJSONObject(i).getDouble("latitude"),
                                    markList.getJSONObject(i).getDouble("longitude")
                            ) ;
                            Log.e("",markList.getJSONObject(i)
                                    .getString("accountName") +":"+n);
                            if(n < 300){
                                json = new JSONObject();
                                try {
                                    json.put("pic", markList.getJSONObject(i)
                                            .getString("accountPic"));
                                    json.put("name", markList.getJSONObject(i)
                                            .getString("accountName"));
                                    json.put("id", markList.getJSONObject(i)
                                            .getString("accountId"));
                                    resultList.put(resultList.length(), json);

                                } catch (JSONException e) {
                                    Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
                                }
                                near++;
                            }
                        }
                        if(near >= 2){
                            showlist(resultList);
                        }else{
                            showSettings(accountPic, accountName,accountId);
                        }

					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}
		return false;
	}

    // 計算兩點距離
    private final double EARTH_RADIUS = 6378137.0;

    private double gps2m(double lat_a, double lng_a, double lat_b, double lng_b) {
        double radLat1 = (lat_a * Math.PI / 180.0);
        double radLat2 = (lat_b * Math.PI / 180.0);
        double a = radLat1 - radLat2;
        double b = (lng_a - lng_b) * Math.PI / 180.0;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }


    private void showlist(JSONArray resultList) {
        LayoutInflater inflater = Utils.nowActivity.getLayoutInflater();
        final View layout = inflater.inflate(R.layout.fragment_online,
                (ViewGroup) this.getView().findViewById(R.id.dialog));

        listView = (ListView) layout.findViewById(R.id.listView);
        listView.setAdapter(null);
        Log.i("","resultList length=>"+resultList.length());
        OnlineListAdapter adapter = new OnlineListAdapter(Utils.nowActivity,
                resultList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                String accountId =  String.valueOf(arg1.findViewById(R.id.name).getTag());
                if (markMap.has(accountId)) {

                    try {
                        int index = markMap.getInt(accountId);
                        String accountName = null;
                        accountName = markList.getJSONObject(index)
                                .getString("accountName");
                        String accountPic = markList.getJSONObject(index)
                                .getString("accountPic");
                        String destination = markList.getJSONObject(index)
                                .getString("destination");

                        Double latitude = markList.getJSONObject(index)
                                .getDouble("latitude");
                        Double longitude = markList.getJSONObject(index)
                                .getDouble("longitude");
                        alertDialog.cancel();
                        showSettings(accountPic, accountName, accountId);
                    } catch (JSONException e) {
                        Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
                    }

                }



            }
        });

        myAlertDialog = new Builder(Utils.nowActivity);
        myAlertDialog.setTitle("選擇相近的人");

        myAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        myAlertDialog.setCancelable(true);
        myAlertDialog.setView(layout);
        // Looper.loop();

        myAlertDialog.setPositiveButton("取消",
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 如果不做任何事情 就會直接關閉 對話方塊
                    }
                });

        alertDialog = myAlertDialog.create();
        alertDialog.show();
    }
	public static void showSettings(String accountPic, String accountName,
			String accountId) {

		try {
			toId = accountId;
			toName = accountName;
			LayoutInflater inflater = Utils.nowActivity.getLayoutInflater();
			final View layout = inflater.inflate(R.layout.car_pool_fun,
                    (ViewGroup) Utils.nowActivity.findViewById(R.id.dialog));



            ProfilePictureView profilePictureView;
            profilePictureView = (ProfilePictureView) layout.findViewById(R.id.profilePicture);
            profilePictureView.setProfileId(accountPic.replace("facebook_",""));

			if (!"".equals(accountId)) {
                profilePictureView.setTag(accountId);
			}

			if (!"".equals(accountName)) {
				TextView name = (TextView) layout.findViewById(R.id.textView1);
				name.setText(accountName);
			}

			Button button1 = (Button) layout.findViewById(R.id.button1);
			button1.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setCarEvent(toId, toName, 1, "邀請您共乘!");
					alertDialog.dismiss();
				}
			});
			Button button2 = (Button) layout.findViewById(R.id.button2);
			button2.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					replay(toId, toName);
					alertDialog.dismiss();
				}
			});
			Button button3 = (Button) layout.findViewById(R.id.button3);
			button3.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setCarEvent(toId, toName, 5, "看到你了!");
					alertDialog.dismiss();
				}
			});
			Button button4 = (Button) layout.findViewById(R.id.button4);
			button4.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					setCarEvent(toId, toName, 7, "上車囉!");
					alertDialog.dismiss();
				}
			});

            Button button5 = (Button) layout.findViewById(R.id.button5);
            button5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    if (markMap.has(toId)) {

                        try {
                            int index = markMap.getInt(toId);
                            String destination = null;
                            destination = markList.getJSONObject(index)
                                    .getString("destination");
                            if(destination != null && !destination.equals("")&& !destination.equals("null")) {
                                Double latitude = markList.getJSONObject(index)
                                        .getDouble("latitude");
                                Double longitude = markList.getJSONObject(index)
                                        .getDouble("longitude");
                                MapFragment.getLocationInfo(destination, latitude, longitude, false);
                            }else{
                                Utils.ShowAlertDialog(Utils.nowActivity,"訊息","此人無目的地");
                            }
                        } catch (JSONException e) {
                            Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
                        }

                    }else{
                        Log.e("",toId+ " markMapbak no has(toId)");
                    }

                    alertDialog.dismiss();
                }
            });
			myAlertDialog = new Builder(Utils.nowActivity);
			myAlertDialog.setTitle("使用共乘趣功能");

			myAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
			myAlertDialog.setCancelable(true);
			myAlertDialog.setView(layout);
			// Looper.loop();

			myAlertDialog.setPositiveButton("取消",
					new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 如果不做任何事情 就會直接關閉 對話方塊
						}
					});

			alertDialog = myAlertDialog.create();
			alertDialog.show();

		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}

	}

	@SuppressLint("HandlerLeak")
	protected static void setCarEvent(String toId, String toName,
			int eventType, String message) {

		try {
			if (onStatus == 0)
				return;

            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    try {
                        Toast.makeText(Utils.nowActivity,
                                "傳送成功!",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
                    }

                }
            };

			HttpClientRequest request = new HttpClientRequest(Utils.nowActivity,
					Utils.APIUrl + "car/setCarEvent", HttpClientRequest.POST,
					handler);
			HttpEntity postEntity = null;
			try {
				postEntity = new StringEntity("{\"accountId\":\""
						+ Utils.getSimSerialNumber(Utils.nowActivity)
						+ "\",\"accountName\":\"" + URLEncoder.encode(Utils.UserName, "UTF-8")
						+ "\",\"accountPic\":\"" + Utils.UserPic
						+ "\",\"eventType\":" + eventType + ",\"message\":\""
						+ URLEncoder.encode(message, "UTF-8") + "\",\"toId\":\"" + toId + "\""
						+ ",\"toName\":\"" + toName + "\" }", "UTF-8");

			} catch (UnsupportedEncodingException e) {
				Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
			}

			request.setHttpEntity(postEntity);
			new Thread(request).start();

		} catch (Exception e) {
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		}

	}

	@SuppressLint("HandlerLeak")
	public static void getLocationInfo(String address ,final  double  latitude_f ,final double longitude_f ,Boolean self) {

        if(self) {
            address_m = address;
            setCarInfo();
        }
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

					JSONObject jsonObject = new JSONObject(jSonData);

					// take the Latitude and Longitude of address by JSONObject

					JSONObject location = jsonObject.getJSONArray("results")

					.getJSONObject(0).getJSONObject("geometry")

					.getJSONObject("location");

					// get the Latitude

					double longitude = location.getDouble("lng");

					// get the Longitude

					double latitude = location.getDouble("lat");

					LatLng origin = new LatLng(latitude_f, longitude_f);
					LatLng dest = new LatLng(latitude, longitude);

					for (Marker marker : destinationMarkerOptionsList) {
						marker.remove();
					}
					destinationMarkerOptionsList.clear();
					destinationMarkerOptionsList.add(mMap.addMarker(new MarkerOptions()
			        .position(new LatLng(latitude, longitude))
			        .title("目的地")));

					String directionsUrl = getDirectionsUrl(origin, dest);
					DownloadTask downloadTask = new DownloadTask();

					// Start downloading json data from Google Directions
					// API
					downloadTask.execute(directionsUrl);

				} catch (Exception e) {
					Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
				}

			}
		};

		String url = "http://maps.google.com/maps/api/geocode/json?address="
				+ address + "&sensor=false&language=zh-TW";
		Log.i("url", url);
		HttpClientRequest request = new HttpClientRequest(Utils.nowActivity, url,
				HttpClientRequest.GET, handler);

		new Thread(request).start();

	}

	private static String getDirectionsUrl(LatLng origin, LatLng dest) {

		// Origin of route
		String str_origin = "origin=" + origin.latitude + ","
				+ origin.longitude;

		// Destination of route
		String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

		// Sensor enabled
		String sensor = "sensor=false";

		// Building the parameters to the web service
		String parameters = str_origin + "&" + str_dest + "&" + sensor;

		// Output format
		String output = "json";

		// Building the url to the web service
		String url = "https://maps.googleapis.com/maps/api/directions/"
				+ output + "?" + parameters;

		return url;
	}

	// Fetches data from url passed
	private static class DownloadTask extends AsyncTask<String, Void, String> {

		// Downloading data in non-ui thread
		@Override
		protected String doInBackground(String... url) {

			// For storing data from web service
			String data = "";

			try {
				// Fetching the data from web service
				data = downloadUrl(url[0]);
				Log.w("DownloadTask",data.toString());
				
			} catch (Exception e) {
				Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
				Log.d("Background Task", e.toString());
			}
			return data;
		}

		// Executes in UI thread, after the execution of
		// doInBackground()
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			ParserTask parserTask = new ParserTask();

			// Invokes the thread for parsing the JSON data
			parserTask.execute(result);

		}
	}

	/** 從URL下載JSON資料的方法 **/
	private static String downloadUrl(String strUrl) throws IOException {
		
		Log.w("downloadUrl",strUrl);
		String data = "";
		InputStream iStream = null;
		HttpURLConnection urlConnection = null;
		try {
			URL url = new URL(strUrl);

			// Creating an http connection to communicate with url
			urlConnection = (HttpURLConnection) url.openConnection();

			// Connecting to url
			urlConnection.connect();

			// Reading data from url
			iStream = urlConnection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(
					iStream));

			StringBuffer sb = new StringBuffer();

			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			data = sb.toString();

			br.close();

		} catch (Exception e) {
			Log.d("Exception", e.toString());
			Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
		} finally {
			iStream.close();
			urlConnection.disconnect();
		}
		return data;
	}

	/** 解析JSON格式 **/
	private static class ParserTask extends
			AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

		// Parsing the data in non-ui thread
		@Override
		protected List<List<HashMap<String, String>>> doInBackground(
				String... jsonData) {

			JSONObject jObject;
			List<List<HashMap<String, String>>> routes = null;

			try {
				jObject = new JSONObject(jsonData[0]);
				DirectionsJSONParser parser = new DirectionsJSONParser();

				// Starts parsing data
				routes = parser.parse(jObject);
			} catch (Exception e) {
				Utils.ExceptionHandler(e, tracker, Utils.nowActivity);
			}
			return routes;
		}
		// Executes in UI thread, after the parsing process
		  @Override
		  protected void onPostExecute(List<List<HashMap<String, String>>> result) {
			  
		   if(result.size() == 0){
			   Utils.ShowAlertDialog(Utils.nowActivity, "路徑規劃", "失敗!無找到相關路線規劃!");
			   return;
		   }
		   
		   ArrayList<LatLng> points = null;
		   
		   PolylineOptions lineOptions = null ;

		   Log.w("onPostExecute",result.toString());
		   
		   // Traversing through all the routes
		   for (int i = 0; i < result.size(); i++) {
		    points = new ArrayList<LatLng>();
		    lineOptions = new PolylineOptions();

		    // Fetching i-th route
		    List<HashMap<String, String>> path = result.get(i);

		    // Fetching all the points in i-th route
		    for (int j = 0; j < path.size(); j++) {
		     HashMap<String, String> point = path.get(j);

		     double lat = Double.parseDouble(point.get("lat"));
		     double lng = Double.parseDouble(point.get("lng"));
		     LatLng position = new LatLng(lat, lng);

		     points.add(position);
		    }

		    // Adding all the points in the route to LineOptions
		    lineOptions.addAll(points);
		    lineOptions.width(5);  //導航路徑寬度
		    lineOptions.color(Color.BLUE); //導航路徑顏色
		    

		   }

		   for (Polyline polyline : PolylineList) {
			   polyline.remove();
			}
		   
		   PolylineList.clear();
		   // Drawing polyline in the Google Map for the i-th route
		   PolylineList.add(mMap.addPolyline(lineOptions));
		   
		  }
		 }

    public static JSONArray remove(final int idx, final JSONArray from) {
        final List<JSONObject> objs = asList(from);
        objs.remove(idx);

        final JSONArray ja = new JSONArray();
        for (final JSONObject obj : objs) {
            ja.put(obj);
        }

        return ja;
    }

    public static List<JSONObject> asList(final JSONArray ja) {
        final int len = ja.length();
        final ArrayList<JSONObject> result = new ArrayList<JSONObject>(len);
        for (int i = 0; i < len; i++) {
            final JSONObject obj = ja.optJSONObject(i);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }


    @SuppressLint("HandlerLeak")
    protected static void setCarOffline(String otherId,String otherName) {

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
                        + otherId
                        + "\",\"accountName\":\"" + URLEncoder.encode(otherName, "UTF-8")
                        + "\",\"accountPic\":\"" + ""
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

}
