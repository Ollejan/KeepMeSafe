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
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;

import se.mah.ad0025.keepmesafe.help.HelpActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AddContactFragment.OnImportClickedListener, AddContactFragment.OnAddContactClickedListener,
        ManageContactsFragment.OnManageAddContactClickedListener, ManageContactsFragment.OnManageListItemClickedListener, ContactDetailsFragment.OnDeleteContactClickedListener,
        ContactDetailsFragment.OnUpdateContactClickedListener, EditMessageFragment.OnSaveMessageClickedListener, MainFragment.OnHelpClickedListener {

    private static final int PICK_CONTACT = 123;
    private static final int HELP_CLOSED = 666;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 64;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 11;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 25;
    private NavigationView navigationView;
    private FragmentManager fm;
    private SharedPreferences prefs;
    private ArrayList<Contact> contacts = new ArrayList<>();    //Används för att lagra alla kontakter man har sparat i appen.
    private DBController dbController;
    private MainFragment mainFragment;
    private AddContactFragment addContactFragment;
    private ManageContactsFragment manageContactsFragment;
    private ContactDetailsFragment contactDetailsFragment;
    private EditMessageFragment editMessageFragment;
    private LocationManager locationManager;
    private GPSTracker gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("KeepMeSafePrefs", MODE_PRIVATE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        gps = new GPSTracker(MainActivity.this);

        dbController = new DBController(this);
        mainFragment = new MainFragment();
        manageContactsFragment = new ManageContactsFragment();
        contactDetailsFragment = new ContactDetailsFragment();
        editMessageFragment = new EditMessageFragment();


        //---------- DETTA KAN VI NOG ÄNDRA OM EN DEL OM VI TAR BORT LANDSKAPSLÄGE -----------------

        if (findViewById(R.id.container) != null) {
//Här läggs det som alltid ska ske.

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();
            navigationView = (NavigationView) findViewById(R.id.nav_view); //Drawer-menyn. Används bl.a. för att avmarkera i menyn vid bakåtklick.
            navigationView.setNavigationItemSelectedListener(this);
            /*
            if (savedInstanceState != null) {
//Här läggs det som bara ska ske vid rotation men inte första gången. Tex hämta värden via savedInstanceState.
                addContactFragment = (AddContactFragment) fm.findFragmentByTag("contacts");
                manageContactsFragment = (ManageContactsFragment)fm.findFragmentByTag("manage");
                return;
            }
            */
//Här läggs det som ska ske första gången men inte efter rotation.
            fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.container, mainFragment).commit();
        }

        //------------------------------------------------------------------------------------------

        //Kollar om det är första gången användaren kör appen. Då ska tutorial visas.
        if(prefs.getBoolean(getString(R.string.firstTime), true)) {
            prefs.edit().putBoolean(getString(R.string.firstTime), false).apply();
            Intent intent = new Intent(this, HelpActivity.class);
            startActivityForResult(intent, HELP_CLOSED);
        }

        manageContactsFragment.setAdapter(new ContactListAdapter(this, contacts));
        getAllContactsFromDB();
        enableLocationPermission();
        enableGPSDialog();
    }

    @Override
    protected void onPause() {
        gps.stopUsingGPS();
        //Avregistrerar receiver för Wear
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onResume() {
        gps = new GPSTracker(MainActivity.this);

        //Register mMessageReceiver to receive messages.
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("my-event"));

        super.onResume();
    }

    //Handler for received Intents for the "my-event" event
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onHelpBtnClicked();
        }
    };

    /**
     * Metod som körs vid programstart som kollar om GPS är aktiverat eller ej.
     * Om GPS är inaktiverat så visas en dialogruta som tar en till inställningar för GPS.
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        clearBackStack();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Manage) {
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

        item.setChecked(true);  //Markerar vald item i drawern.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Metod som tömmer backstacken. Sker när användaren klickar på något i drawern.
     */
    private void clearBackStack() {
        if (fm.getBackStackEntryCount() > 0) {
            FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
            fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    openContacts();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    getCurrentLocation();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    sendSMSMessages();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onImportBtnClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == -1) {
// Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_CONTACTS)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.


                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getString(R.string.PermissionInfoReadContacts)).setPositiveButton(getString(R.string.Yes), dialogClickListener)
                            .setNegativeButton(getString(R.string.No), dialogClickListener).show();

                } else {

                    // No explanation needed, we can request the permission.
                    requestPermission();

                }
            } else {
                openContacts();
            }
        } else {
            openContacts();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    private void openContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(intent, PICK_CONTACT);
    }

    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    requestPermission();
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
        }
    };

    private void enableLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == -1) {
// Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {

                    // Show an expanation to the user *asynchronously* -- don't block
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

    private void requestPermissionLocation() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_FINE_LOCATION);
    }

    private void getCurrentLocation() {
        gps = new GPSTracker(MainActivity.this);
//        if (gps.canGetLocation()) {
//            Snackbar.make(findViewById(R.id.container), "Lat: " + gps.getLatitude() + ", Long: " + gps.getLongitude(), Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
//        } else {
//            Snackbar.make(findViewById(R.id.container), "Failed to get coordinates, please try again.", Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
//        }
    }

    DialogInterface.OnClickListener dialogClickListenerLocation = new DialogInterface.OnClickListener() {
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

    public void enableSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == -1) {
// Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.SEND_SMS)) {

                    // Show an expanation to the user *asynchronously* -- don't block
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

    DialogInterface.OnClickListener dialogClickListenerSms = new DialogInterface.OnClickListener() {
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

    private void requestSmsPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.SEND_SMS},
                MY_PERMISSIONS_REQUEST_SEND_SMS);
    }

    public void sendSMSMessages() {
        if(contacts.isEmpty()) {
            Snackbar.make(findViewById(R.id.container), getString(R.string.contactListEmpty), Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
            return;
        }
        if(!gps.canGetLocation()) {
            sendMessages(false);
        } else {
            sendMessages(true);
        }
    }

    private void sendMessages(boolean includeCoordinates) {
        SmsManager smsManager = SmsManager.getDefault();
        String message = prefs.getString(getString(R.string.textMessage), "");
        boolean defaultMessage = false;

        if(message.equals(""))
            defaultMessage = true;

        if(includeCoordinates) {
            String coordinatesString = " http://maps.google.com?q=" + gps.getLatitude() + "," + gps.getLongitude();
            String smsBody;
            if(defaultMessage) {
                smsBody = getString(R.string.defaultMessage) + coordinatesString;
            } else {
                smsBody = message + coordinatesString;
            }

            for(int i = 0; i < contacts.size(); i++) {
                smsManager.sendTextMessage(contacts.get(i).getNumber(), null, smsBody, null, null);
            }

            Snackbar.make(findViewById(R.id.container), getString(R.string.smsSentSuccess), Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
        } else {
            String smsBody;
            if(defaultMessage) {
                smsBody = getString(R.string.defaultMessage);
            } else {
                smsBody = message;
            }

            for(int i = 0; i < contacts.size(); i++) {
                smsManager.sendTextMessage(contacts.get(i).getNumber(), null, smsBody, null, null);
            }


            Snackbar.make(findViewById(R.id.container), getString(R.string.smsSentWithoutCoords), Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
        }
    }

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

                int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                people.moveToFirst();

                String name = people.getString(indexName);
                String number = people.getString(indexNumber);
                number = number.replace("-", "");
                addContactFragment.setNameAndNumber(name, number);

                people.close();
            }
            //Avmarkera meny när hjälpen stängs.
        } else if(requestCode == HELP_CLOSED) {
            unCheckDrawer();
        }

    }

    /**
     * Metod som körs när användaren klickar på knappen som tar en till sidan där man lägger till
     * en ny kontakt.
     */
    @Override
    public void onManageAddContactBtnClicked() {
        if (addContactFragment == null)
            addContactFragment = new AddContactFragment();
        fm.beginTransaction().replace(R.id.container, addContactFragment, getString(R.string.contacts)).addToBackStack(null).commit();
        addContactFragment.setNameAndNumber("", "");
    }

    /**
     * Metod som körs när användaren klickar på knappen som lägger till ny kontakt.
     * Lägger till kontakten i databasen och uppdaterar ArrayListan med hjälp av metoden
     * "getAllContactsFromDB".
     *
     * @param name   Namnet på kontakten.
     * @param number Numret till kontakten.
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

    @Override
    public void onManageListItemClicked(int position) {
        fm.beginTransaction().replace(R.id.container, contactDetailsFragment).addToBackStack(null).commit();
        contactDetailsFragment.setNameAndNumber(contacts.get(position).getName(), contacts.get(position).getNumber(), contacts.get(position).getID());
    }

    /**
     * Metod som raderar en kontakt från databasen och uppdaterar kontaktlistan.
     *
     * @param ID Unikt ID till den kontakt som ska raderas från databasen.
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
     * Metod som uppdaterar en kontakt i databasen med nytt namn/nummer.
     *
     * @param ID     Unikt ID till den kontakt som ska uppdateras.
     * @param name   Det namn det ska uppdateras till.
     * @param number Det nummer det ska uppdateras till.
     */
    @Override
    public void onUpdateContactClicked(int ID, String name, String number) {
        for (int i = 0; i < contacts.size(); i++) {
            if (contacts.get(i).getNumber().equals(number)) {
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
    }

    /**
     * Metod som sparar användarens textmeddelande i SharedPreferences.
     *
     * @param message Textmeddelandet som användaren vill spara.
     */
    @Override
    public void onSaveMessageBtnClicked(String message) {
        prefs.edit().putString(getString(R.string.textMessage), message).apply();
        fm.beginTransaction().replace(R.id.container, mainFragment).commit();
        unCheckDrawer();
    }

    public void unCheckDrawer() {
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.getMenu().getItem(1).setChecked(false);
        navigationView.getMenu().getItem(2).setChecked(false);
    }

    @Override
    public void onHelpBtnClicked() {
        enableLocationPermission();
        enableSmsPermission();
    }
}
