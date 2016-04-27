package se.mah.ad0025.keepmesafe.help;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import se.mah.ad0025.keepmesafe.R;


public class HelpFragment extends Fragment {

    private static final String ARG_PAGE = "page";
    private int mPageNumber;
    private helpListener mCallback;

    public static HelpFragment create(int pageNumber) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public HelpFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = null;
        switch (mPageNumber){

            case 0:
                rootView = (ViewGroup) inflater.inflate(R.layout.help1, container, false);
                break;

            case 1:
                rootView = (ViewGroup) inflater.inflate(R.layout.help2, container, false);
                break;

            case 2:
                rootView = (ViewGroup) inflater.inflate(R.layout.help3, container, false);
                Button btnTutorial = (Button) rootView.findViewById(R.id.btn_tutorial);
                btnTutorial.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCallback.btnCloseHelpClicked();
                    }
                });
                break;
        }
        return rootView;
    }

    public interface helpListener {
        void btnCloseHelpClicked();
    }

    /**
     * A method that is called when the fragment is first attached to the activity
     *
     * @param activity The activity it becomes attached to.
     */
    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (helpListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement helpListener");
        }
    }
}
