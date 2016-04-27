package com.swatiag1101.sunshine_new_v1;

/**
 * Created by Swati Agarwal on 18-01-2016.
 */
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import com.bumptech.glide.Glide;
import com.swatiag1101.sunshine_new_v1.data.WeatherContract;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
public class ForecastAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private static final boolean mUseTodayLayout = true;
    /*public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /**
     * Prepare the weather high/lows for presentation.

    private String formatHighLows(double high, double low) {
        boolean isMetric = Utility.isMetric(mContext);
        String highLowStr = Utility.formatTemperature(high, isMetric) + "/" + Utility.formatTemperature(low, isMetric);
        return highLowStr;
    }*/

    public void setUseTodayLayout(boolean mUseTodayLayout) {
        mUseTodayLayout = this.mUseTodayLayout;
    }
    @Override
    public int getItemViewType(int position) {

        return ((position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY);
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
    /*
        This is ported from FetchWeatherTask --- but now we go straight from the cursor to the
        string.

    private String convertCursorRowToUXFormat(Cursor cursor) {
        // get row indices for our cursor
        int idx_max_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MAX_TEMP);
        int idx_min_temp = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_MIN_TEMP);
        int idx_date = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_DATE);
        int idx_short_desc = cursor.getColumnIndex(WeatherContract.WeatherEntry.COLUMN_SHORT_DESC);

        String highAndLow = formatHighLows(
                cursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP),
                cursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP));

        return Utility.formatDate(MainActivityFragment.COL_WEATHER_DATE) +
                " - " + cursor.getString(MainActivityFragment.COL_WEATHER_DESC) +
                " - " + highAndLow;
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());

        int layoutId = -1;
        // TODO: Determine layoutId from viewType
        if(viewType==0){
            Log.v("viewType-if",viewType+"");
            layoutId = R.layout.list_item_forecast_today;
        }else{
            Log.v("viewType-else",viewType+"");
            layoutId = R.layout.list_item_forecast;
        }
        Log.v("layoutId",layoutId+"");
        View v = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        v.setTag(viewHolder);
        return v;
    }

    /*
        This is where we fill-in the views with the contents of the cursor.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(MainActivityFragment.COL_WEATHER_ID);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        // Use placeholder image for now
        int viewType = getItemViewType(cursor.getPosition());
        int fallbackIconId=0;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
             // Get weather icon
                fallbackIconId = Utility.getArtResourceForWeatherCondition(
                cursor.getInt(MainActivityFragment.COL_WEATHER_CONDITION_ID));
                break;
                }
            case VIEW_TYPE_FUTURE_DAY: {
                // Get weather icon
                fallbackIconId = Utility.getIconResourceForWeatherCondition(
                cursor.getInt(MainActivityFragment.COL_WEATHER_CONDITION_ID));
                break;
            }
        }

        Glide.with(mContext)
                .load(Utility.getArtUrlForWeatherCondition(mContext, weatherId))
                .error(fallbackIconId)
                .crossFade()
                .into(viewHolder.imageView);
        // TODO Read date from cursor
        long date = cursor.getLong(MainActivityFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context,date));

        // TODO Read weather forecast from cursor
        String forecast = cursor.getString(MainActivityFragment.COL_WEATHER_DESC);
        viewHolder.desc.setText(forecast);
        viewHolder.imageView.setContentDescription(forecast);
        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(MainActivityFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.high.setText(Utility.formatTemperature(context,high));

        // Read low temperature from cursor
        double low = cursor.getDouble(MainActivityFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.low.setText(Utility.formatTemperature(context,low));


    }
    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }
    public static class ViewHolder{
        public final ImageView imageView;
        public final TextView dateView;
        public final TextView desc;
        public final TextView high;
        public final TextView low;

        public ViewHolder(View v){
            imageView = (ImageView) v.findViewById(R.id.list_item_icon);
            dateView = (TextView) v.findViewById(R.id.list_item_date_textview);
            desc = (TextView) v.findViewById(R.id.list_item_forecast_textview);
            high = (TextView) v.findViewById(R.id.list_item_high_textview);
            low = (TextView) v.findViewById(R.id.list_item_low_textview);
        }
    }
}