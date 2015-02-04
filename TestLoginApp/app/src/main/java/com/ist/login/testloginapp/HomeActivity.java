package com.ist.login.testloginapp;

import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class HomeActivity extends FragmentActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    static final int REQUEST_ADD_NEW = 1;
    SharedPreferences prefs;
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setOffscreenPageLimit(2);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),
                HomeActivity.this);
        viewPager.setAdapter(mSectionsPagerAdapter);

        // Give the SlidingTabLayout the ViewPager
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        // Center the tabs in the layout
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void createNewActivity(View view){
        Intent intent = new  Intent(HomeActivity.this,AddActivity.class);
        startActivityForResult(intent, REQUEST_ADD_NEW);
    }

    private void StartWebActivity(ActivityModel item){
        // Starting single contact activity
        Intent intent = new  Intent(HomeActivity.this,WebViewActivity.class);
        if (item.getIsFbPost()){
            intent.putExtra("fb_url",item.getFbPost());
            startActivity(intent);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ADD_NEW && resultCode == Activity.RESULT_OK){
            long newlyInsertedId = data.getLongExtra("id", 0);
            String link = "https://graph.facebook.com/oauth/authorize?client_id=" + Constants.AppId + "&redirect_uri=" + Constants.HostUrl + Constants.RedirectUrl + newlyInsertedId + "_" + prefs.getString("Email", "").replaceAll("@","00000") + "&scope=publish_actions";

            ActivityModel model = new ActivityModel();

            model.setId((int)newlyInsertedId);
            model.setIsFbPost(data.getBooleanExtra("IsFbPost",false));
            model.setName(data.getStringExtra("Name"));
            model.setFbPost(model.getIsFbPost() ? link:"");
            model.setFormattedPoints(data.getStringExtra("FormattedPoints"));
            model.setDate(data.getStringExtra("Date"));
            model.setPoints(data.getIntExtra("Points",0));
            model.setFbPoints(data.getIntExtra("FbPoints",0));
            ActivityList fragment = (ActivityList) mSectionsPagerAdapter.getItem(0);
            fragment.items.add(0, model);
            fragment.utilizable_points += model.getFbPoints() + model.getPoints();
            fragment.header.setText("Usable Points: "+ fragment.utilizable_points);
            fragment.adapter.notifyDataSetChanged();
            UserReward fragmentUserRewards = (UserReward) mSectionsPagerAdapter.getItem(2);
            fragmentUserRewards.utilizable_points += model.getFbPoints() + model.getPoints();
            fragmentUserRewards.header.setText("Usable Points:" + fragmentUserRewards.utilizable_points);

            if(model.getIsFbPost())
                StartWebActivity(model);
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        final int PAGE_COUNT = 3;
        private String tabTitles[] = new String[] { "History", "Rewards", "Earned" };
        private Context context;
        ArrayList<Fragment> fragments = new ArrayList<>();

        public SectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
            this.fragments.add(ActivityList.newInstance(""));
            this.fragments.add(OfferedRewardActivity.newInstance(""));
            this.fragments.add(UserReward.newInstance(""));
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return tabTitles[position];
        }

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home, container, false);
            return rootView;
        }
    }

}
