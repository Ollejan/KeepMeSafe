package se.mah.ad0025.keepmesafe;


import android.os.Bundle;
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
    private EditText et_detailsContactName, et_detailsContactNumber;
    private Button btn_deleteContact, btn_updateContact;

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
        btn_deleteContact = (Button)view.findViewById(R.id.btn_deleteContact);
        btn_updateContact = (Button)view.findViewById(R.id.btn_updateContact);
        return view;
    }

    public void setNameAndNumber(String name, String number) {
        et_detailsContactName.setText(name);
        et_detailsContactNumber.setText(number);
    }

}
