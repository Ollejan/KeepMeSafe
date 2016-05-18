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
 * Fragment that shows a list of contacts that the user can either click on or add more to.
 */
public class ManageContactsFragment extends Fragment {
    private ManageContactsListener manageContactsListener;
    private ContactListAdapter adapter;
    private TextView tvListInfo;

    /**
     * Interface to message parent activity
     */
    public interface ManageContactsListener {
        void onManageListItemClicked(int position);

        void onManageAddContactBtnClicked();
    }

    /**
     * Attach fragment to parent activity
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
            manageContactsListener = (ManageContactsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + activity.getString(R.string.implementManageContactsListener));
        }
    }

    /**
     * Default constructor
     */
    public ManageContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Prepare all components and bind listeners
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
        View view = inflater.inflate(R.layout.fragment_manage_contacts, container, false);
        ListView lvContacts = (ListView) view.findViewById(R.id.lvContacts);
        tvListInfo = (TextView) view.findViewById(R.id.tvListInfo);
        lvContacts.setAdapter(adapter);
        Button btnManageAddContact = (Button) view.findViewById(R.id.btn_manageAddContact);

        btnManageAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageContactsListener.onManageAddContactBtnClicked();
            }
        });

        lvContacts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                manageContactsListener.onManageListItemClicked(position);
            }
        });
        return view;
    }

    /**
     * If the list is empty prompt the user to add contacts, otherwise display a title.
     */
    @Override
    public void onResume() {
        if (adapter.isEmpty())
            tvListInfo.setText(R.string.listEmptyPrompt);
        else
            tvListInfo.setText(R.string.contactsColon);
        super.onResume();
    }

    /**
     * Setter for the lists adapter
     *
     * @param adapter the adapter for the list
     */
    public void setAdapter(ContactListAdapter adapter) {
        this.adapter = adapter;
    }
}
