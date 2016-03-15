package se.mah.ad0025.keepmesafe;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;


/**
 * Fragment som visar en lista på kontakter som användaren lagt till samt en knapp för att lägga
 * till ny kontakt.
 */
public class ManageContactsFragment extends Fragment {
    private OnManageAddContactClickedListener manageAddContactBtnClicked;   //Används för att mainactivity ska veta när klick sker.
    private OnManageListItemClickedListener manageListItemClicked;
    private ContactListAdapter adapter;
    private TextView tvListInfo;

    public interface OnManageAddContactClickedListener {
        void onManageAddContactBtnClicked();
    }

    public interface OnManageListItemClickedListener {
        void onManageListItemClicked(int position);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            manageAddContactBtnClicked = (OnManageAddContactClickedListener) activity;
            manageListItemClicked = (OnManageListItemClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnManageAddContactClickedListener and OnManageListItemClickedListener");
        }

    }

    public ManageContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_manage_contacts, container, false);
        ListView lvContacts = (ListView)view.findViewById(R.id.lvContacts);
        tvListInfo = (TextView)view.findViewById(R.id.tvListInfo);
        lvContacts.setAdapter(adapter);
        Button btnManageAddContact = (Button)view.findViewById(R.id.btn_manageAddContact);

        btnManageAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageAddContactBtnClicked.onManageAddContactBtnClicked();
            }
        });

        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                manageListItemClicked.onManageListItemClicked(position);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        if(adapter.isEmpty())
            tvListInfo.setText("List is empty, please add contacts...");
        else
            tvListInfo.setText("Contacts:");
        super.onResume();
    }

    /**
     * Sätter adaptern på ListView.
     * @param adapter
     *              Adaptern som ska användas till ListView.
     */
    public void setAdapter(ContactListAdapter adapter) {
        this.adapter = adapter;
    }

}
