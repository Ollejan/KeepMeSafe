package se.mah.ad0025.keepmesafe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Controller-class for the database to store contacts.
 */
public class DBController extends SQLiteOpenHelper {

    private static final String DB_NAME = "contactDB";
    private static final int DB_VERSION = 1;

    private SQLiteDatabase database;

    /**
     * Default constructor.
     * @param context
     *              the activity.
     */
    public DBController(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Prepare the database for action.
     */
    public void open(){
        database = getWritableDatabase();
    }

    /**
     * Close the database to save CPU.
     */
    public void close(){
        database.close();
    }

    /**
     * A method to get all the contacts.
     * @return
     *          A Cursor with all the active contacts.
     */
    public Cursor getContacts(){
        return database.rawQuery("SELECT _id, name, number from contacts", new String[]{});
    }

    /**
     * A method to get the ID of a specific contact.
     * @return
     *          A Cursor with a specific ID.
     */
    public Cursor getSpecificContactID(String name, String number){
        return database.rawQuery("SELECT _id from contacts where name='" + name + "' and number='" + number + "'", new String[]{});
    }

    /**
     * A method to add a contact to the database.
     * @param name
     *              the name we want to store.
     * @param number
     *              the number to the contact.
     */
    public void addContact(String name, String number){
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);

        //insert into table contacts
        database.insert("contacts", null, values);
    }

    /**
     * A method used to delete a contact from the database.
     * @param ID
     *              the ID of the contact we want to delete.
     */
    public void deleteContact(int ID) {
        database.delete("contacts", "_id='" + ID + "'", null);
    }

    /**
     * A method used to update a contact in the database.
     * @param ID
     *              the ID of the contact we want to update.
     * @param name
     *              the new name of the contact.
     * @param number
     *              the new number of the contact.
     */
    public void updateContact(int ID, String name, String number) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("number", number);

        database.update("contacts", values, "_id="+ID, null);
    }

    /**
     * The method that creates the table. Only used once.
     * @param db
     *          The database we want the tables to be in.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE table contacts (_id INTEGER PRIMARY KEY, name VARCHAR(255), number VARCHAR(30));");
    }

    /**
     * Unused inherited method.
     * @param db
     *           the database.
     * @param oldVersion
     *                  old version number.
     * @param newVersion
     *                  new version number.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
