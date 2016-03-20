package se.mah.ad0025.keepmesafe;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddContactFragment extends Fragment {
    private OnImportClickedListener importBtnClicked;
    private OnAddContactClickedListener addContactBtnClicked;
    private EditText et_contactName, et_contactNumber;
    private String name, number;

    public interface OnImportClickedListener {
        void onImportBtnClicked();
    }

    public interface OnAddContactClickedListener {
        void onAddContactBtnClicked(String name, String number);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            importBtnClicked = (OnImportClickedListener) activity;
            addContactBtnClicked = (OnAddContactClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnImportClickedListener and OnAddContactClickedListener");
        }

    }

    public AddContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_contact, container, false);

        et_contactName = (EditText)view.findViewById(R.id.et_contactName);
        et_contactNumber = (EditText)view.findViewById(R.id.et_contactNumber);
        Button btn_openContacts = (Button)view.findViewById(R.id.btn_openContacts);
        Button btn_addContact = (Button)view.findViewById(R.id.btn_addContact);

        btn_openContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                importBtnClicked.onImportBtnClicked();
            }
        });

        btn_addContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_contactName.getText().toString().trim().length() == 0 || et_contactNumber.getText().toString().trim().length() == 0) {
                    Snackbar.make(v, "Please enter name and number", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                } else {
                    addContactBtnClicked.onAddContactBtnClicked(et_contactName.getText().toString().trim(), et_contactNumber.getText().toString().replace(" ", "").replace("-", ""));
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        et_contactName.setText(name);
        et_contactNumber.setText(number);
        super.onResume();
    }

    public void setNameAndNumber(String name, String number) {
        this.name = name;
        this.number = number;
    }
}
