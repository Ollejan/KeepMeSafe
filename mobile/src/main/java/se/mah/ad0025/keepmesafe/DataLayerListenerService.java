package se.mah.ad0025.keepmesafe;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.telephony.SmsManager;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;

/**
 * Service that receives messages from the wearable and sends broadcast to parent activity.
 */
public class DataLayerListenerService extends WearableListenerService {

    private GPSTracker gps;
    private SharedPreferences prefs;
    private final ArrayList<Contact> contacts = new ArrayList<>();
    private DBController dbController;

    /**
     * If we receive a message message all contacts.
     */
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if ("/KEEPMESAFE".equals(messageEvent.getPath())) {
            initVariables();
            textAllContacts();
        }
    }

    /**
     * Init variables so they are ready.
     */
    private void initVariables() {
        gps = new GPSTracker(DataLayerListenerService.this);
        prefs = getSharedPreferences("KeepMeSafePrefs", MODE_PRIVATE);
        dbController = new DBController(this);
        getAllContactsFromDB();
    }

    /**
     * Fetch all contacts from the database and store them in an array.
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

    /**
     * Method that sends a text to each added contact.
     */
    private void textAllContacts() {
        SmsManager smsManager = SmsManager.getDefault();
        String message = prefs.getString(getString(R.string.textMessage), "");
        boolean defaultMessage = false;
        String smsBody;
        if (message.equals(""))
            defaultMessage = true;

        if (gps.canGetLocation()) {
            String coordinatesString = " http://maps.google.com?q=" + gps.getLatitude() + "," + gps.getLongitude();

            if (defaultMessage) {
                smsBody = getString(R.string.defaultMessage) + coordinatesString;
            } else {
                smsBody = message + coordinatesString;
            }

            for (int i = 0; i < contacts.size(); i++) {
                smsManager.sendTextMessage(contacts.get(i).getNumber(), null, smsBody, null, null);
            }

        } else {
            if (defaultMessage) {
                smsBody = getString(R.string.defaultMessage);
            } else {
                smsBody = message;
            }

            for (int i = 0; i < contacts.size(); i++) {
                smsManager.sendTextMessage(contacts.get(i).getNumber(), null, smsBody, null, null);
            }
        }
    }
}