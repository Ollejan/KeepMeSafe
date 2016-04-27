package se.mah.ad0025.keepmesafe;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Service som tar emot meddelanden från Wearable och skickar broadcast som MainActivity tar emot.
 * Created by Jonas on 2016-04-17.
 */
public class DataLayerListenerService extends WearableListenerService {

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if("/KEEPMESAFE".equals(messageEvent.getPath())) {
            // launch some Activity or do anything you like
            Log.d("myTag", "Message received from Wearable");
            Intent intent = new Intent("my-event");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}