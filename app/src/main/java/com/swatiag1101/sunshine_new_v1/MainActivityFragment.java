package com.swatiag1101.sunshine_new_v1;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.widget.TextView;

import com.swatiag1101.sunshine_new_v1.data.WeatherContract;
import com.swatiag1101.sunshine_new_v1.sync.SunshineSyncAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG
    };
    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private int mPosition = ListView.INVALID_POSITION;
    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    private boolean mUseTodayLayout;

    ListView lv;
    private ForecastAdapter ad;
    URL url1;
    private static String url = "http://api.openweathermap.org/data/2.5/forecast/daily";
    private static String q = "q";
    private static String q_val = "734001";
    private static String mode = "mode";
    private static String mode_val = "json";
    private static String units = "units";
    private static String units_val = "metric";
    private static String cnt = "cnt";
    private static String cnt_val = "7";
    private static String appid = "appid";
    private static String appid_val = "65b305ff41d4562e0ba4344316d8a1b6";
    private static final int FORECAST_LOADER = 0;
    Uri builtUri;
    int mposition;
    private String SELECTED_KEY="key";
    public MainActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ad = new ForecastAdapter(getActivity(), null, 0);
        lv = (ListView) rootView.findViewById(R.id.listView);
        TextView empty_view = (TextView) rootView.findViewById(R.id.empty_string);
        lv.setEmptyView(empty_view);
       // ad = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.textView,al);

        lv.setAdapter(ad);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                            locationSetting, cursor.getLong(COL_WEATHER_DATE)
                    ));
                    mposition = position;
                }

            }
        });

        if(savedInstanceState!=null && savedInstanceState.containsKey(SELECTED_KEY)){
            mposition = savedInstanceState.getInt(SELECTED_KEY);
        }
        ad.setUseTodayLayout(mUseTodayLayout);
        //c.close();
        return rootView;


    }
    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mposition!=ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY,mposition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_string_location_status)) ) {
            updateEmptyView();
        }
    }

    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
    @Override
      public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.pref_loc) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshWeather(){

    }

    private void updateWeather(){
        String location = Utility.getPreferredLocation(getActivity());
        //Intent i = new Intent(getActivity(), SunshineService.class);
        //i.putExtra("location", location);
       // SunshineService ss = new SunshineService("SUnshine");
       // getActivity().startService(i);

        //Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
       // alarmIntent.putExtra(SunshineService.LOCATION_QUERY_EXTRA, Utility.getPreferredLocation(getActivity()));
        //Wrap in a pending intent which only fires once.
       // PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0,alarmIntent,PendingIntent.FLAG_ONE_SHOT);
        //getBroadcast(context, 0, i, 0);
      //  AlarmManager am=(AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        //Set the AlarmManager to wake up the system.
      //  am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pi);
        //weatherTask.execute(location);

        SunshineSyncAdapter.syncImmediately(getActivity());
    }
    Uri weatherForLocationUri;
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v("Inside onCreateLoader ","onCreateLoader");
        String locationSetting = Utility.getPreferredLocation(getActivity());

      // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                      locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),weatherForLocationUri,FORECAST_COLUMNS,null,
                        null,sortOrder);
    }

 @Override
   public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
     ad.swapCursor(cursor);
     if(mposition!= ListView.INVALID_POSITION){
         lv.smoothScrollToPosition(mposition);
     }
     updateEmptyView();
   }

   @Override
   public void onLoaderReset(Loader<Cursor> loader) {
       Log.v("Inside onLoaderReset ", "onLoaderReset");
        ad.swapCursor(null);
   }
    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (ad != null) {
            ad.setUseTodayLayout(mUseTodayLayout);
        }
    }
    public void onLocationChanged(){
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    private void openPreferredLocationInMap() {
        // Using the URI scheme for showing a location found on a map.  This super-handy
                // intent can is detailed in the "Common Intents" page of Android's developer site:
        // http://developer.android.com/guide/components/intents-common.html#Maps
        if ( null != ad ) {
            Cursor c = ad.getCursor();
            if ( null != c ) {
                c.moveToPosition(0);
                String posLat = c.getString(COL_COORD_LAT);
                String posLong = c.getString(COL_COORD_LONG);
                Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(geoLocation);

                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                     Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                }
            }
        }
    }

    public void updateEmptyView(){
        if ( ad.getCount() == 0 ) {
            TextView tv = (TextView) getView().findViewById(R.id.empty_string);
            if ( null != tv ) {
                // if cursor is empty, why? do we have an invalid location
                int message = R.string.empty_list_forecast;
                @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());
                switch (location) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.empty_forecast_list_server_down;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.empty_forecast_list_server_error;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.empty_forecast_list_invalid_location;
                        break;
                    default:
                        if (!Utility.isNetworkAvailable(getActivity()) ) {
                            message = R.string.empty_list_forecast_no_network;
                        }
                }
                tv.setText(message);
            }
        }
    }
}
