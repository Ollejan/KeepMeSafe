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
public class EditMessageFragment extends Fragment {
    private OnSaveMessageClickedListener saveMessageBtnClicked;
    private EditText et_message;
    private String message;

    public interface OnSaveMessageClickedListener {
        void onSaveMessageBtnClicked(String message);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            saveMessageBtnClicked = (OnSaveMessageClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnSaveMessageClickedListener");
        }

    }

    public EditMessageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_message, container, false);
        et_message = (EditText)view.findViewById(R.id.et_message);
        Button btn_SaveMessage = (Button)view.findViewById(R.id.btn_saveMessage);

        btn_SaveMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageBtnClicked.onSaveMessageBtnClicked(et_message.getText().toString());
                Snackbar.make(v, "Message have been saved", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        et_message.setText(message);
        super.onResume();
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
