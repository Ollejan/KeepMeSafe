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
 * A fragment used to edit the text sent to the contacts.
 */
public class EditMessageFragment extends Fragment {
    private EditMessageListener editMessageListener;
    private EditText et_message;
    private String message;

    /**
     * Interface to message parent activity
     */
    public interface EditMessageListener {
        void onSaveMessageBtnClicked(String message);
    }

    /**
     * Attach fragment to parent activity.
     *
     * @param context parent activity.
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            editMessageListener = (EditMessageListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + activity.getString(R.string.implementEditMessageListener));
        }
    }

    /**
     * Default constructor.
     */
    public EditMessageFragment() {
        // Required empty public constructor
    }

    /**
     * Prepare all components and attach listeners.
     *
     * @param inflater           default inflater
     * @param container          default viewGroup
     * @param savedInstanceState default Bundle
     * @return inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_message, container, false);
        et_message = (EditText) view.findViewById(R.id.et_message);
        Button btn_SaveMessage = (Button) view.findViewById(R.id.btn_saveMessage);

        btn_SaveMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMessageListener.onSaveMessageBtnClicked(et_message.getText().toString());
                Snackbar.make(v, R.string.msgSaved, Snackbar.LENGTH_LONG).setAction(R.string.Action, null).show();
            }
        });
        return view;
    }

    /**
     * Set the text to the current text for easier editing.
     */
    @Override
    public void onResume() {
        et_message.setText(message);
        super.onResume();
    }

    /**
     * Setter for the message.
     *
     * @param message new message.
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
