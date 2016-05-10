package se.mah.ad0025.keepmesafe.help;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import se.mah.ad0025.keepmesafe.R;

/**
 * A fragment for the help slideshow.
 * This is used to handle the logic of what is seen on the screen.
 */
public class HelpFragment extends Fragment {

    private static final String ARG_PAGE = "page";
    private int mPageNumber;
    private helpListener mCallback;

    /**
     * The create method. It prepares some data before the onCreate method is called.
     *
     * @param pageNumber This parameter is used to help decide which page we are on.
     * @return a HelpFragment with some arguments in the form of a bundle bound to it.
     */
    public static HelpFragment create(int pageNumber) {
        HelpFragment fragment = new HelpFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Default constructor.
     */
    public HelpFragment() {
        // Required empty public constructor
    }

    /**
     * The onCreate method. We set the page number variable to what was in the bundle.
     *
     * @param savedInstanceState We simply pass this along using super.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    /**
     * When the view is created it will use a different layout depending on the page we are on.
     *
     * @param inflater           default LayoutInflater.
     * @param container          default ViewGroup.
     * @param savedInstanceState default Bundle.
     * @return the View.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = null;
        //Depending on the page we load in a different layout.
        switch (mPageNumber) {

            case 0:
                rootView = (ViewGroup) inflater.inflate(R.layout.help1, container, false);
                break;

            case 1:
                rootView = (ViewGroup) inflater.inflate(R.layout.help2, container, false);
                break;
            //The 3rd layout contains a button and therefore requires a listener.
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

    /**
     * Interface used by the 3rd layout to message the parent activity.
     */
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
