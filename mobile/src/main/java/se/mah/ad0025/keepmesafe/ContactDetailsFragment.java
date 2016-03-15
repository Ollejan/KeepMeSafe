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
 * A simple {@link Fragment} subclass.
 */
public class ContactDetailsFragment extends Fragment {
    private OnDeleteContactClickedListener deleteContactBtnClicked;
    private OnUpdateContactClickedListener updateContactBtnClicked;
    private EditText et_detailsContactName, et_detailsContactNumber;
    private String name, number;
    private int ID;

    public interface OnDeleteContactClickedListener {
        void onDeleteContactClicked(int ID);
    }

    public interface OnUpdateContactClickedListener {
        void onUpdateContactClicked(int ID, String name, String number);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            deleteContactBtnClicked = (OnDeleteContactClickedListener) activity;
            updateContactBtnClicked = (OnUpdateContactClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnDeleteContactClickedListener and OnUpdateContactClickedListener");
        }

    }

    public ContactDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contact_details, container, false);
        et_detailsContactName = (EditText)view.findViewById(R.id.et_detailsContactName);
        et_detailsContactNumber = (EditText)view.findViewById(R.id.et_detailsContactNumber);
        FloatingActionButton fab_deleteContact = (FloatingActionButton)view.findViewById(R.id.fab);
        Button btn_updateContact = (Button)view.findViewById(R.id.btn_updateContact);

        fab_deleteContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteContactBtnClicked.onDeleteContactClicked(ID);
            }
        });

        btn_updateContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = et_detailsContactName.getText().toString();
                number = et_detailsContactNumber.getText().toString();
                updateContactBtnClicked.onUpdateContactClicked(ID, name, number);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        et_detailsContactName.setText(name);
        et_detailsContactNumber.setText(number);
        super.onResume();
    }

    /**
     * Metod som uppdaterar instansvariablerna.
     * @param name
     *          Namnet på kontakten.
     * @param number
     *          Numret till kontakten.
     * @param ID
     *          Kontaktens ID.
     */
    public void setNameAndNumber(String name, String number, int ID) {
        this.name = name;
        this.number = number;
        this.ID = ID;
    }

}
