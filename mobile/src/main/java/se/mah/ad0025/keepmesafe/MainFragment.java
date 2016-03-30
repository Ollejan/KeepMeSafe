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


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private OnHelpClickedListener helpBtnClicked;

    public interface OnHelpClickedListener {
        void onHelpBtnClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = context instanceof Activity ? (Activity) context : null;

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            helpBtnClicked = (OnHelpClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHelpClickedListener");
        }

    }

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Button btn_help = (Button)view.findViewById(R.id.btn_help);
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo Hämta koordinater och skicka iväg SMS.
                helpBtnClicked.onHelpBtnClicked();
                //Snackbar.make(v, "Messages have been sent", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

        return view;
    }

}
