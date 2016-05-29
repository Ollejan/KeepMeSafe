package se.mah.ad0025.keepmesafe;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;

import se.mah.ad0025.keepmesafe.help.HelpActivity;

/**
 * This is the main activity. This activity is the container for all fragments and also handles all logic.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AddContactFragment.AddContactListener,
        ManageContactsFragment.ManageContactsListener, ContactDetailsFragment.ContactDetailsListener,
        EditMessageFragment.EditMessageListener, MainFragment.OnHelpClickedListener {

    private static final int PICK_CONTACT = 123;
    private static final int HELP_CLOSED = 666;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 64;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 11;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 25;
    private NavigationView navigationView;
    private FragmentManager fm;
    private SharedPreferences prefs;
    private final ArrayList<Contact> contacts = new ArrayList<>();
    private DBController dbController;
    private MainFragment mainFragment;
    private AddContactFragment addContactFragment;
    private ManageContactsFragment manageContactsFragment;
    private ContactDetailsFragment contactDetailsFragment;
    private EditMessageFragment editMessageFragment;
    private LocationManager locationManager;
    private GPSTracker gps;

    /**
     * The onCreate method that is called upon the start of the application.
     *
     * @param savedInstanceState default Bundle when application is started, not modified since orientation is locked.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Initialize necessary variables.
        prefs = getSharedPreferences(getString(R.string.prefs), MODE_PRIVATE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        gps = new GPSTracker(MainActivity.this);

        dbController = new DBController(this);
        mainFragment = new MainFragment();
        manageContactsFragment = new ManageContactsFragment();
        contactDetailsFragment = new ContactDetailsFragment();
        editMessageFragment = new EditMessageFragment();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.container, mainFragment).commit();

        //If the app is running for the first time we show the tutorial.
        if (prefs.getBoolean(getString(R.string.firstTime), true)) {
            prefs.edit().putBoolean(getString(R.string.firstTime), false).apply();
            Intent intent = new Intent(this, HelpActivity.class);
            startActivityForResult(intent, HELP_CLOSED);
        }

        manageContactsFragment.setAdapter(new ContactListAdapter(this, contacts));
        getAllContactsFromDB();
        enableLocationPermission();
        enableGPSDialog();
    }

    /**
     * onPause where we unregister the receiver for the Wear.
     */
    @Override
    protected void onPause() {
        gps.stopUsingGPS();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    /**
     * onResume where register the receiver.
     */
    @Override
    protected void onResume() {
        gps = new GPSTracker(MainActivity.this);
        //Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));

        super.onResume();
    }

    /**
     * Handler for received Intents for the "my-event" event.
     */
    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onHelpBtnClicked();
        }
    };

    /**
     * .
     * Method that is called from onCreate. It checks if GPS is activated on the phone.
     * If it is not active a dialog will ask the user if the want to activate it, if they click yes the settings are opened.
     */
    private void enableGPSDialog() {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.gps_disabled_message)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(getString(R.string.No), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * Method for handling back being pressed.
     */
    @Override
    public void onBackPressed() {
        Fragment currentFragment = fm.findFragmentById(R.id.container);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
        } else if (!(currentFragment instanceof MainFragment)) {
            fm.beginTransaction().replace(R.id.container, mainFragment).commit();
            unCheckDrawer();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Method for handling clicks in the menu.
     *
     * @param item the item that was clicked on.
     * @return True if the event was handled, false otherwise.
     */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        clearBackStack();
        int id = item.getItemId();

        if (id == R.id.nav_Main) {
            if (mainFragment == null)
                mainFragment = new MainFragment();
            fm.beginTransaction().replace(R.id.container, mainFragment, getString(R.string.MainPage)).commit();
        } else if (id == R.id.nav_Manage) {
            if (manageContactsFragment == null)
                manageContactsFragment = new ManageContactsFragment();
            fm.beginTransaction().replace(R.id.container, manageContactsFragment, getString(R.string.manage)).commit();
        } else if (id == R.id.nav_Edit) {
            if (editMessageFragment == null)
                editMessageFragment = new EditMessageFragment();
            fm.beginTransaction().replace(R.id.container, editMessageFragment).commit();
            editMessageFragment.setMessage(prefs.getString(getString(R.string.textMessage), ""));
        } else if (id == R.id.nav_What) {
            Intent intent = new Intent(this, HelpActivity.class);
            startActivityForResult(intent, HELP_CLOSED);
        }
        //Mark the selected item so the user knows where they are in the application.
        item.setChecked(true);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Method that empties the backstack. Used when a menu item is clicked.
     */
    private void clearBackStack() {
        if (fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /**
     * Method for doing things we are only allowed to do with the users permission.
     * If permission is granted this method is invoked.
     *
     * @param requestCode  int code of the permission.
     * @param permissions  array of permissions.
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            //Permission for contacts.
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    openContacts();
                }
                return;
            }
            //Permission for location.
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getCurrentLocation();
                }
                return;
            }
            //Permission for sending SMS.
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    sendSMSMessages();
                }
            }
        }
    }

    /**
     * Method invoked when the user imports a contact from their contact book in their phone.
     */
    public void onImportBtnClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == -1) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.PermissionInfoReadContacts)).setPositiveButton(getString(R.string.Yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.No), dialogClickListener).show();
                } else {
                    // No explanation needed, we can request the permission.
                    requestPermissionContacts();
                }
            } else {
                openContacts();
            }
        } else {
            openContacts();
        }
    }

    /**
     * Request permission to read contacts.
     */
    private void requestPermissionContacts() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    /**
     * Open the contact book of the phone.
     */
    private void openContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }

    private final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    requestPermissionContacts();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    /**
     * Method invoked when we wish to know the users location.
     */
    private void enableLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == -1) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.PermissionInfoGetLocation)).setPositiveButton(getString(R.string.Yes), dialogClickListenerLocation)
                            .setNegativeButton(getString(R.string.No), dialogClickListenerLocation).show();
                } else {
                    // No explanation needed, we can request the permission.
                    requestPermissionLocation();
                }
            } else {
                getCurrentLocation();
            }
        } else {
            getCurrentLocation();
        }
    }

    /**
     * Request permission for location.
     */
    private void requestPermissionLocation() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_FINE_LOCATION);
    }

    /**
     * Method for fetching current location.
     */
    private void getCurrentLocation() {
        gps = new GPSTracker(MainActivity.this);
    }

    private final DialogInterface.OnClickListener dialogClickListenerLocation = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    requestPermissionLocation();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    /**
     * Method invoked when we wish to send SMS.
     */
    private void enableSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == -1) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.PermissionInfoSendSms)).setPositiveButton(getString(R.string.Yes), dialogClickListenerSms)
                            .setNegativeButton(getString(R.string.No), dialogClickListenerSms).show();
                } else {
                    // No explanation needed, we can request the permission.
                    requestSmsPermission();
                }
            } else {
                sendSMSMessages();
            }
        } else {
            sendSMSMessages();
        }
    }

    private final DialogInterface.OnClickListener dialogClickListenerSms = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    requestSmsPermission();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    /**
     * Method invoked when we wish to know if we have permission to send SMS.
     */
    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS},
                MY_PERMISSIONS_REQUEST_SEND_SMS);
    }

    /**
     * Method that checks so messages have contacts the can arrive to before sending them.
     */
    private void sendSMSMessages() {
        if (contacts.isEmpty()) {
            Snackbar.make(findViewById(R.id.container), getString(R.string.contactListEmpty), Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
            return;
        }
        sendMessages(gps.canGetLocation());
    }

    /**
     * Method that sends a text to all contacts
     *
     * @param includeCoordinates include GPS coordinates if true.
     */
    private void sendMessages(boolean includeCoordinates) {
        SmsManager smsManager = SmsManager.getDefault();
        String message = prefs.getString(getString(R.string.textMessage), "");
        boolean defaultMessage = false;
        String smsBody;
        if (message.equals(""))
            defaultMessage = true;

        if (includeCoordinates) {
            String coordinatesString = " http://maps.google.com?q=" + gps.getLatitude() + "," + gps.getLongitude();

            if (defaultMessage) {
                smsBody = getString(R.string.defaultMessage) + coordinatesString;
            } else {
                smsBody = message + coordinatesString;
            }
            for (int i = 0; i < contacts.size(); i++) {
                smsManager.sendTextMessage(contacts.get(i).getNumber(), null, smsBody, null, null);
            }
            Snackbar.make(findViewById(R.id.container), getString(R.string.smsSentSuccess), Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
        } else {

            if (defaultMessage) {
                smsBody = getString(R.string.defaultMessage);
            } else {
                smsBody = message;
            }
            for (int i = 0; i < contacts.size(); i++) {
                smsManager.sendTextMessage(contacts.get(i).getNumber(), null, smsBody, null, null);
            }
            Snackbar.make(findViewById(R.id.container), getString(R.string.smsSentWithoutCoords), Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
        }
    }

    /**
     * Method invoked when a child activity id finished.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult().
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == PICK_CONTACT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                // Get the URI that points to the selected contact
                Uri uri = data.getData();

                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER};

                Cursor people = getContentResolver().query(uri, projection, null, null, null);
                if (people != null) {
                    int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    people.moveToFirst();

                    String name = people.getString(indexName);
                    String number = people.getString(indexNumber);
                    number = number.replace("-", "");
                    addContactFragment.setNameAndNumber(name, number);

                    people.close();
                } else {
                    Snackbar.make(findViewById(R.id.container), R.string.databaseError, Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
                }
            }
            //If the help activity is closed deselect it in the menu.
        } else if (requestCode == HELP_CLOSED) {
            unCheckDrawer();
        }
    }

    /**
     * Method invoked when the user clicks on the button that opens the addContactFragment.
     */
    @Override
    public void onManageAddContactBtnClicked() {
        if (addContactFragment == null)
            addContactFragment = new AddContactFragment();
        fm.beginTransaction().replace(R.id.container, addContactFragment, getString(R.string.contacts)).addToBackStack(null).commit();
        addContactFragment.setNameAndNumber("", "");
    }

    /**
     * Method invoked when the user clicks on the add contact button.
     * This adds the contact in the database and updates the arrayList.
     *
     * @param name   Name of the contact.
     * @param number The number of the contact.
     */
    public void onAddContactBtnClicked(String name, String number) {
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getNumber().equals(number)) {
                Snackbar.make(findViewById(R.id.container), R.string.ContactAlreadyAdded, Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
                return;
            }
        }
        dbController.open();
        dbController.addContact(name, number);
        dbController.close();
        getAllContactsFromDB();
        fm.popBackStack();
        Snackbar.make(findViewById(R.id.container), R.string.ContactAddSuccess, Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
    }

    /**
     * Fetch all contacts from the database and store them in an ArrayList.
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
     * Method invoked when a contact is clicked.
     *
     * @param position the index of the clicked contact.
     */
    @Override
    public void onManageListItemClicked(int position) {
        fm.beginTransaction().replace(R.id.container, contactDetailsFragment).addToBackStack(null).commit();
        contactDetailsFragment.setNameAndNumber(contacts.get(position).getName(), contacts.get(position).getNumber(), contacts.get(position).getID());
    }

    /**
     * Method that deletes a contact from the database and updates the contacts
     *
     * @param ID The ID of the contact that will be deleted from the database.
     */
    @Override
    public void onDeleteContactClicked(int ID) {
        final int finalID = ID;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.Confirm));
        builder.setMessage(getString(R.string.InquireDeleteContact));

        builder.setPositiveButton(getString(R.string.YES), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dbController.open();
                dbController.deleteContact(finalID);
                dbController.close();
                getAllContactsFromDB();
                fm.popBackStack();
                dialog.dismiss();
                Snackbar.make(findViewById(R.id.container), R.string.contactDeleted, Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
            }

        });

        builder.setNegativeButton(getString(R.string.NO), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Method that updates a contact in the database.
     *
     * @param ID     ID of the contact.
     * @param name   The new name of the contact.
     * @param number The new number of the contact.
     */
    @Override
    public void onUpdateContactClicked(int ID, String name, String number) {
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getNumber().equals(number) && contacts.get(i).getName().equals(name)) {
                Snackbar.make(findViewById(R.id.container), R.string.NbrAlreadyAdded, Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
                return;
            }
        }

        if (name.trim().length() == 0 || number.trim().length() == 0) {
            Snackbar.make(findViewById(R.id.container), R.string.RequestNameAndNbr, Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
        } else {
            dbController.open();
            dbController.updateContact(ID, name.trim(), number.replace(" ", ""));
            dbController.close();
            getAllContactsFromDB();
            fm.popBackStack();
            Snackbar.make(findViewById(R.id.container), R.string.contactSaved, Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
        }
        //Close the keyboard.
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Method that saves the users text message in SharedPreferences.
     *
     * @param message The message the user typed that will be stored.
     */
    @Override
    public void onSaveMessageBtnClicked(String message) {
        prefs.edit().putString(getString(R.string.textMessage), message).apply();
        fm.beginTransaction().replace(R.id.container, mainFragment).commit();
        unCheckDrawer();
    }

    /**
     * Method for deselecting the entire drawer.
     */
    private void unCheckDrawer() {
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.getMenu().getItem(1).setChecked(false);
        navigationView.getMenu().getItem(2).setChecked(false);
        navigationView.getMenu().getItem(3).setChecked(false);
    }

    /**
     * Method invoked when the user clicks on the big red button. This sends a text to the contacts.
     */
    @Override
    public void onHelpBtnClicked() {
        enableLocationPermission();
        enableSmsPermission();
    }
}
