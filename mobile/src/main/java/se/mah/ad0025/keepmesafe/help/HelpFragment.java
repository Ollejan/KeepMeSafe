package se.mah.ad0025.keepmesafe.help;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import se.mah.ad0025.keepmesafe.R;


public class HelpFragment extends Fragment {

    public static final String ARG_PAGE = "page";
    private int mPageNumber;

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
                break;
        }
        return rootView;
    }
}
