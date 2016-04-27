package com.swatiag1101.sunshine_new_v1;

/**
 * Created by Swati Agarwal on 18-01-2016.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.text.format.Time;

import com.swatiag1101.sunshine_new_v1.sync.SunshineSyncAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utility {
    public static String getPreferredLocation(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.location),
                "734001");
    }

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.temp),
                context.getString(R.string.temp_default))
                .equals(context.getString(R.string.temp_default));
    }

    public static String formatTemperature(Context context, double temperature) {
        // Data stored in Celsius by default.  If user prefers to see in Fahrenheit, convert
        // the values here.
        String suffix = "\u00B0";
        if (!isMetric(context)) {
            temperature = (temperature * 1.8) + 32;
        }

        // For presentation, assume the user doesn't care about tenths of a degree.
        return String.format(context.getString(R.string.format_temperature), temperature);
    }

    static String formatDate(long dateInMillis) {
        Date date = new Date(dateInMillis);
        return DateFormat.getDateInstance().format(date);
    }

    // Format used for storing dates in the database.  ALso used for converting those strings
    // back into date objects for comparison/processing.
    public static final String DATE_FORMAT = "yyyyMMdd";

    /**
     * Helper method to convert the database representation of the date into something to display
     * to users.  As classy and polished a user experience as "20140102" is, we can do better.
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return a user-friendly representation of the date.
     */
    public static String getFriendlyDayString(Context context, long dateInMillis) {
        // The day string for forecast uses the following logic:
        // For today: "Today, June 8"
        // For tomorrow:  "Tomorrow"
        // For the next 5 days: "Wednesday" (just the day name)
        // For all days after that: "Mon Jun 8"

        Time time = new Time();
        time.setToNow();
        long currentTime = System.currentTimeMillis();
        int julianDay = Time.getJulianDay(dateInMillis, time.gmtoff);
        int currentJulianDay = Time.getJulianDay(currentTime, time.gmtoff);

        // If the date we're building the String for is today's date, the format
        // is "Today, June 24"
        if (julianDay == currentJulianDay) {
            String today = context.getString(R.string.today);
            int formatId = R.string.format_full_friendly_date;
            return String.format(context.getString(
                    formatId,
                    today,
                    getFormattedMonthDay(context, dateInMillis)));
        } else if ( julianDay < currentJulianDay + 7 ) {
            // If the input date is less than a week in the future, just return the day name.
            return getDayName(context, dateInMillis);
        } else {
            // Otherwise, use the form "Mon Jun 3"
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(dateInMillis);
        }
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "wednesday".
     *
     * @param context Context to use for resource localization
     * @param dateInMillis The date in milliseconds
     * @return
     */
    public static String getDayName(Context context, long dateInMillis) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.

        Time t = new Time();
        t.setToNow();
        int julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if ( julianDay == currentJulianDay +1 ) {
            return context.getString(R.string.tomorrow);
        } else {
            Time time = new Time();
            time.setToNow();
            // Otherwise, the format is just the day of the week (e.g "Wednesday".
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
            return dayFormat.format(dateInMillis);
        }
    }

    /**
     * Converts db date format to the format "Month day", e.g "June 24".
     * @param context Context to use for resource localization
     * @param dateInMillis The db formatted date string, expected to be of the form specified
     *                in Utility.DATE_FORMAT
     * @return The day in the form of a string formatted "December 6"
     */
    public static String getFormattedMonthDay(Context context, long dateInMillis ) {
        Time time = new Time();
        time.setToNow();
        SimpleDateFormat dbDateFormat = new SimpleDateFormat(Utility.DATE_FORMAT);
        SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMMM dd");
        String monthDayString = monthDayFormat.format(dateInMillis);
        return monthDayString;
    }

    public static String getFormattedWind(Context context, float windSpeed, float degrees) {
             int windFormat;
            if (Utility.isMetric(context)) {
                    windFormat = R.string.format_wind_kmh;
               } else {
               windFormat = R.string.format_wind_mph;
            windSpeed = .621371192237334f * windSpeed;
              }

                     // From wind direction in degrees, determine compass direction as a string (e.g NW)
                             // You know what's fun, writing really long if/else statements with tons of possible
                               // conditions.  Seriously, try it!
          String direction = "Unknown";
        if (degrees >= 337.5 || degrees < 22.5) {
              direction = "N";
            } else if (degrees >= 22.5 && degrees < 67.5) {
             direction = "NE";
            } else if (degrees >= 67.5 && degrees < 112.5) {
             direction = "E";
            } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE";
            } else if (degrees >= 157.5 && degrees < 202.5) {
             direction = "S";
            } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW";
            } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W";
            } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW";
            }
            return String.format(context.getString(windFormat), windSpeed, direction);
        }

    public static int getIconResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
         // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
         if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.ic_storm;
         } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.ic_light_rain;
         } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.ic_rain;
         } else if (weatherId == 511) {
             return R.drawable.ic_snow;
         } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.ic_rain;
         } else if (weatherId >= 600 && weatherId <= 622) {
             return R.drawable.ic_snow;
         } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.ic_fog;
          } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.ic_storm;
         } else if (weatherId == 800) {
            return R.drawable.ic_clear;
         } else if (weatherId == 801) {
            return R.drawable.ic_light_clouds;
         } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.ic_cloudy;
         }
       return -1;
    }

    public static int getArtResourceForWeatherCondition(int weatherId) {
        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return R.drawable.art_storm;
        } else if (weatherId >= 300 && weatherId <= 321) {
            return R.drawable.art_light_rain;
        } else if (weatherId >= 500 && weatherId <= 504) {
            return R.drawable.art_rain;
        } else if (weatherId == 511) {
            return R.drawable.art_snow;
        } else if (weatherId >= 520 && weatherId <= 531) {
            return R.drawable.art_rain;
        } else if (weatherId >= 600 && weatherId <= 622) {
            return R.drawable.art_snow;
        } else if (weatherId >= 701 && weatherId <= 761) {
            return R.drawable.art_fog;
        } else if (weatherId == 761 || weatherId == 781) {
            return R.drawable.art_storm;
        } else if (weatherId == 800) {
            return R.drawable.art_clear;
        } else if (weatherId == 801) {
            return R.drawable.art_light_clouds;
        } else if (weatherId >= 802 && weatherId <= 804) {
            return R.drawable.art_clouds;
        }
        return -1;
    }

    public static String getTalkbackWind(Context context, boolean isMetric, float windSpeed, float degrees) {
        int windFormat;
        if (Utility.isMetric(context)) {
            windFormat = R.string.format_compass_kmh_talkback;
        } else {
            windFormat = R.string.format_compass_mph_talkback;
            windSpeed = .621371192237334f * windSpeed;
        }
        String speed = Integer.toString(Math.round(windSpeed));

        String direction = ""; // Don't want to say anything for an unknown direction.
        if(348.75 <= degrees || degrees <= 11.25)       direction = "north";
        else if(11.25 < degrees && degrees < 33.75)     direction = "north-northeast";
        else if(33.75 <= degrees && degrees <= 56.25)   direction = "northeast";
        else if(56.25 < degrees && degrees < 78.75)     direction = "east-northeast";
        else if(78.75 <= degrees && degrees <= 101.25)  direction = "east";
        else if(101.25 < degrees && degrees < 123.75)   direction = "east-southeast";
        else if(123.75 <= degrees && degrees <= 146.25) direction = "southeast";
        else if(146.25 < degrees && degrees < 168.75)   direction = "south-southeast";
        else if(168.75 <= degrees && degrees <= 191.25) direction = "south";
        else if(191.25 < degrees && degrees < 213.75)   direction = "south-southwest";
        else if(213.75 <= degrees && degrees <= 236.25) direction = "southwest";
        else if(236.25 < degrees && degrees < 258.75)   direction = "west-southwest";
        else if(258.75 <= degrees && degrees <= 281.25) direction = "west";
        else if(281.25 < degrees && degrees < 303.75)   direction = "west-northwest";
        else if(303.75 <= degrees && degrees <= 326.25) direction = "northwest";
        else if(326.25 < degrees && degrees < 348.75)   direction = "north-northwest";

        return String.format(context.getString(windFormat), speed, direction);
    }

    public static boolean isNetworkAvailable(Context c){
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni !=null && ni.isConnectedOrConnecting();
    }

    /**
     *
     * @param c Context used to get the SharedPreferences
     * @return the location status integer type
     */
    @SuppressWarnings("ResourceType")
    static public @SunshineSyncAdapter.LocationStatus
    int getLocationStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_string_location_status), SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
    }

    public static void setCurrentLocation(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(c.getString(R.string.pref_string_location_status),SunshineSyncAdapter.LOCATION_STATUS_UNKNOWN);
        editor.apply();
    }

    /**
     * Helper method to provide the art urls according to the weather condition id returned
     * by the OpenWeatherMap call.
     *
     * @param context Context to use for retrieving the URL format
     * @param weatherId from OpenWeatherMap API response
     * @return url for the corresponding weather artwork. null if no relation is found.
     */
    public static String getArtUrlForWeatherCondition(Context context, int weatherId) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String formatUrl = sharedPreferences.getString(context.getString(R.string.pref_art_pack_key),context.getString(R.string.pref_art_pack_sunshine));

        // Based on weather code data found at:
        // http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
        if (weatherId >= 200 && weatherId <= 232) {
            return String.format(Locale.US,formatUrl, "storm");
        } else if (weatherId >= 300 && weatherId <= 321) {
            return String.format(Locale.US, formatUrl, "light_rain");
        } else if (weatherId >= 500 && weatherId <= 504) {
            return String.format(Locale.US, formatUrl, "rain");
        } else if (weatherId == 511) {
            return String.format(Locale.US, formatUrl, "snow");
        } else if (weatherId >= 520 && weatherId <= 531) {
            return String.format(Locale.US, formatUrl, "rain");
        } else if (weatherId >= 600 && weatherId <= 622) {
            return String.format(Locale.US,formatUrl, "snow");
        } else if (weatherId >= 701 && weatherId <= 761) {
            return String.format(Locale.US, formatUrl, "fog");
        } else if (weatherId == 761 || weatherId == 781) {
            return String.format(Locale.US, formatUrl, "storm");
        } else if (weatherId == 800) {
            return String.format(Locale.US, formatUrl, "clear");
        } else if (weatherId == 801) {
            return String.format(Locale.US, formatUrl, "light_clouds");
        } else if (weatherId >= 802 && weatherId <= 804) {
            return String.format(Locale.US, formatUrl, "clouds");
        }
        return null;
    }
}