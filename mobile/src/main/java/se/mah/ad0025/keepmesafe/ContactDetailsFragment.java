package se.mah.ad0025.keepmesafe;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A fragment that allows the user to edit or delete a contact.
 */
public class ContactDetailsFragment extends Fragment {
    private ContactDetailsListener contactDetailsListener;
    private EditText et_detailsContactName, et_detailsContactNumber;
    private String name, number;
    private int ID;

    public interface ContactDetailsListener {
        void onUpdateContactClicked(int ID, String name, String number);

        void onDeleteContactClicked(int ID);
    }

    /**
     * Bind fragment to the parent activity
     *
     * @param context parent activity
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            contactDetailsListener = (ContactDetailsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + activity.getString(R.string.implementContactDetailsListener));
        }
    }

    /**
     * Necessary empty constructor.
     */
    public ContactDetailsFragment() {
        // Required empty public constructor
    }

    /**
     * Init all components of the interface and set listeners.
     *
     * @param inflater           default inflater
     * @param container          default viewGroup
     * @param savedInstanceState default Bundle
     * @return inflated view.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_details, container, false);
        et_detailsContactName = (EditText) view.findViewById(R.id.et_detailsContactName);
        et_detailsContactNumber = (EditText) view.findViewById(R.id.et_detailsContactNumber);
        FloatingActionButton fab_deleteContact = (FloatingActionButton) view.findViewById(R.id.fab);
        Button btn_updateContact = (Button) view.findViewById(R.id.btn_updateContact);

        fab_deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactDetailsListener.onDeleteContactClicked(ID);
            }
        });

        btn_updateContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = et_detailsContactName.getText().toString();
                number = et_detailsContactNumber.getText().toString();
                contactDetailsListener.onUpdateContactClicked(ID, name, number);
            }
        });

        return view;
    }

    /**
     * Populate the edit text using data from an imported contact.
     */
    @Override
    public void onResume() {
        et_detailsContactName.setText(name);
        et_detailsContactNumber.setText(number);
        super.onResume();
    }

    /**
     * Method used to update the variables regarding the contact
     *
     * @param name   name of the contact
     * @param number the phone number of the contact
     * @param ID     the contacts ID
     */
    public void setNameAndNumber(String name, String number, int ID) {
        this.name = name;
        this.number = number;
        this.ID = ID;
    }

}
