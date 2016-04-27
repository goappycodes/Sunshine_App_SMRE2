package com.swatiag1101.sunshine_new_v1.gcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import com.swatiag1101.sunshine_new_v1.MainActivity;
import com.swatiag1101.sunshine_new_v1.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Swati Agarwal on 05-04-2016.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static final String EXTRA_DATA = "data";
    private static final String EXTRA_WEATHER = "weather";
    private static final String EXTRA_LOCATION = "location";
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onMessageReceived(String from, Bundle data) {

        if (!data.isEmpty()) {
            if (!data.isEmpty()) {
                // TODO: gcm_default sender ID comes from the API console
                String senderId = getString(R.string.gcm_defaultSenderId);
                if (senderId.length() == 0) {
                    Toast.makeText(this, "SenderID string needs to be set", Toast.LENGTH_LONG).show();
                }
                // Not a bad idea to check that the message is coming from your server.
                if ((senderId).equals(from)) {
                    // Process message and then post a notification of the received message.
                    try {
                        JSONObject jsonObject = new JSONObject(data.getString(EXTRA_DATA));
                        String weather = jsonObject.getString(EXTRA_WEATHER);
                        String location = jsonObject.getString(EXTRA_LOCATION);
                        String alert = String.format(getString(R.string.gcm_weather_alert), weather, location);
                        sendNotification(alert);
                    } catch (JSONException e) {
                        // JSON parsing failed, so we just let this message go, since GCM is not one
                        // of our critical features.
                    }
                }
                Log.i(TAG, "Received: " + data.toString());
            }
        }
    }
            /**
                 +     *  Put the message into a notification and post it.
                 +     *  This is just one simple example of what you might choose to do with a GCM message.
                 +     *
                 +     * @param message The alert message to be posted.
                 +     */
       private void sendNotification(String message) {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent contentIntent =
                    PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

            // Notifications using both a large and a small icon (which yours should!) need the large
            // icon as a bitmap. So we need to create that here from the resource ID, and pass the
            // object along in our notification builder. Generally, you want to use the app icon as the
            // small icon, so that users understand what app is triggering this notification.
            Bitmap largeIcon = BitmapFactory.decodeResource(this.getResources(), R.drawable.art_storm);
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.art_clear)
                    .setLargeIcon(largeIcon)
                    .setContentTitle("Weather Alert!")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            mBuilder.setContentIntent(contentIntent);
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
       }
}
