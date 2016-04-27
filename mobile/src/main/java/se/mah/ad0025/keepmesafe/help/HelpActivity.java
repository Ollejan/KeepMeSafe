package se.mah.ad0025.keepmesafe.help;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import se.mah.ad0025.keepmesafe.R;

/**
 * An activity used to display a slideshow.
 */
public class HelpActivity extends FragmentActivity implements HelpFragment.helpListener {

    //number of pages in the slideshow.
    private static final int NUM_PAGES = 3;
    private ViewPager mPager;

    /**
     * OnCreate method that initializes all buttons, fragment and listeners.
     *
     * @param savedInstanceState unused default parameter.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageTransformer(true, new ZoomOutPageTransformer());
        PagerAdapter mPagerAdapter = new HelpAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        final RadioButton rb1 = (RadioButton) findViewById(R.id.rbHelp1);
        final RadioButton rb2 = (RadioButton) findViewById(R.id.rbHelp2);
        final RadioButton rb3 = (RadioButton) findViewById(R.id.rbHelp3);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int i, float v, int i1) {
                //Unused implemented method from listener.
            }

            /**
             * This method checks the radio buttons depending on which slide is currently being displayed.
             * @param i the index of the page we are on.
             */
            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        radioGroup.check(R.id.rbHelp1);
                        break;

                    case 1:
                        radioGroup.check(R.id.rbHelp2);
                        break;

                    case 2:
                        radioGroup.check(R.id.rbHelp3);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                //Unused implemented method from listener.
            }
        });

        /**
         * Listener for radio button 1.
         */
        rb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbClicked(0);
            }
        });

        /**
         * Listener for radio button 2.
         */
        rb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbClicked(1);
            }
        });

        /**
         * Listener for radio button 3.
         */
        rb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbClicked(2);
            }
        });
    }

    /**
     * If the user presses the back button go back a slide. If there is no slide to return to then close the activity.
     */
    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    /**
     * When a radio button is clicked switch to the slide with the same index as that button.
     *
     * @param position the position of the button.
     */
    private void rbClicked(int position) {
        if (mPager.getCurrentItem() != position)
            mPager.setCurrentItem(position);
    }

    /**
     * When the close button is clicked close the activity.
     */
    @Override
    public void btnCloseHelpClicked() {
        finish();
    }

    /**
     * Inner class for the adapter.
     */
    private class HelpAdapter extends FragmentStatePagerAdapter {
        public HelpAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return HelpFragment.create(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }
}