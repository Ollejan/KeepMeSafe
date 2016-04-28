package se.mah.ad0025.keepmesafe;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.util.Log;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

/**
 * Service som tar emot meddelanden från Wearable och skickar broadcast som MainActivity tar emot.
 * Created by Jonas on 2016-04-17.
 */
public class DataLayerListenerService extends WearableListenerService {

    private GPSTracker gps;
    private SharedPreferences prefs;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private DBController dbController;


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if("/KEEPMESAFE".equals(messageEvent.getPath())) {
            initVariables();
            textAllContacts();
            Log.d("myTag", "Message received from Wearable");
        }
    }

    private void initVariables() {
        gps = new GPSTracker(DataLayerListenerService.this);
        prefs = getSharedPreferences("KeepMeSafePrefs", MODE_PRIVATE);
        dbController = new DBController(this);
        getAllContactsFromDB();
    }

    /**
     * Hämtar alla kontakter från databasen och lagrar i ArrayListan "contacts".
     * Används vid programstart och när användaren lagt till en ny kontakt.
     */
    private void getAllContactsFromDB() {
        Contact newContact;
        contacts.clear();
        dbController.open();
        Cursor c = dbController.getContacts();
        if (c.moveToFirst()) {
            do {
                newContact = new Contact(c.getString(1), c.getString(2));
                newContact.setID(c.getInt(0));
                contacts.add(newContact);
            } while (c.moveToNext());
        }
        c.close();
        dbController.close();
    }

    private void textAllContacts() {
        SmsManager smsManager = SmsManager.getDefault();
        String message = prefs.getString(getString(R.string.textMessage), "");
        boolean defaultMessage = false;
        String smsBody;
        if(message.equals(""))
            defaultMessage = true;

        if(gps.canGetLocation()) {
            String coordinatesString = " http://maps.google.com?q=" + gps.getLatitude() + "," + gps.getLongitude();

            if(defaultMessage) {
                smsBody = getString(R.string.defaultMessage) + coordinatesString;
            } else {
                smsBody = message + coordinatesString;
            }

            for(int i = 0; i < contacts.size(); i++) {
                smsManager.sendTextMessage(contacts.get(i).getNumber(), null, smsBody, null, null);
            }

    //       meddela wear att det skickats med coords.
        } else {
            if(defaultMessage) {
                smsBody = getString(R.string.defaultMessage);
            } else {
                smsBody = message;
            }

            for(int i = 0; i < contacts.size(); i++) {
                smsManager.sendTextMessage(contacts.get(i).getNumber(), null, smsBody, null, null);
            }


            //       meddela wear att det skickats utan coords.
        }
    }
}