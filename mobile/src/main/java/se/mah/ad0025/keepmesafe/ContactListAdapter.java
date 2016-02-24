package se.mah.ad0025.keepmesafe;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Jonas on 2016-02-24.
 */
public class ContactListAdapter extends ArrayAdapter<Contact> {
    private ArrayList<Contact> contacts = new ArrayList<>();
    private Context context;

    /**
     * Constructor
     * @param context
     *      The main activity.
     * @param contacts
     *      An array with all the contacts.
     */
    public ContactListAdapter(Activity context, ArrayList<Contact> contacts) {
        super(context, R.layout.row, contacts);
        this.contacts = contacts;
        this.context = context;
    }

    /**
     * A method that puts views into the list with the proper information.
     * @param position
     *      position the view has in the list.
     * @param view
     *      the view.
     * @param parent
     *      ViewGroup for inflating.
     * @return
     *      the view.
     */
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        if(view == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            view = inflater.inflate(R.layout.row, parent, false);
            holder.contactName = (TextView) view.findViewById(R.id.LVTextViewName);
            holder.contactNumber = (TextView) view.findViewById(R.id.LVTextViewNumber);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.contactName.setText(contacts.get(position).getName());
        holder.contactNumber.setText(contacts.get(position).getNumber());

        return view;
    }

    /**
     * ViewHolder for increased performance.
     */
    private static class ViewHolder {
        TextView contactName;
        TextView contactNumber;
    }
}
