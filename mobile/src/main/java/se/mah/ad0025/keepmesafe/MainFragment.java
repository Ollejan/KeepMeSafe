package se.mah.ad0025.keepmesafe;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * The main fragment containing a big button.
 */
public class MainFragment extends Fragment {
    private OnHelpClickedListener helpBtnClicked;

    public interface OnHelpClickedListener {
        void onHelpBtnClicked();
    }

    /**
     * Bind fragment to parent activity
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
            helpBtnClicked = (OnHelpClickedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + activity.getString(R.string.implementMainFragmentListener));
        }
    }

    /**
     * Default constructor
     */
    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Find button and set its listener
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
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        Button btn_help = (Button) view.findViewById(R.id.btn_help);
        btn_help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helpBtnClicked.onHelpBtnClicked();
            }
        });
        return view;
    }
}
