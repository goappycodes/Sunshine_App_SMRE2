package com.swatiag1101.sunshine_new_v1.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Created by Swati Agarwal on 05-04-2016.
 */
public class MyInstanceIDListenerService extends InstanceIDListenerService {

    private static final String TAG = "MyInstanceIDLS";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        Intent i = new Intent(this,RegistrationIntentService.class);
        startActivity(i);
    }
}
