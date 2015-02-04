package com.ist.login.testloginapp;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class UserReward extends Fragment  {

    private ProgressDialog pDialog;
    private static final String TAG_DATA = "UserRewardList";
    static final int REQUEST_ADD_NEW = 1;
    private static final String TAG_NAME = "RewardName";
    private static final String TAG_DATE = "ReceivedDateMobile";
    private static final String TAG_REWARDS_POINTS = "totalRemainingPoints";
    private static final String TAG_PERCENTAGE = "RewardPercentageMobile";
    private static final String TAG_COMMENT = "Comment";
    private static final String TAG_IS_FB_POST = "IsFbPost";
    private String url = Constants.baseUrl + "/UserReward?UserId=";
    ListView lv;
    ArrayList<Item> items = new ArrayList<>();
    private boolean isLoaded;
    private CustomAdapter mAdapter;
    // contacts JSONArray
    JSONArray activities = null;
    public int utilizable_points;
    SwipeRefreshLayout swipeLayout;
    TextView header;
    // Hashmap for ListView
    SharedPreferences prefs;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_user_reward, container, false);
        isLoaded = false;
        //v.setContentView(R.layout.activity_activity_list);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String user_name = prefs.getString("UserId", "");
        String email = prefs.getString("Email", "");
        this.url+= user_name + "&UserEmail="+ email;

        //ListView lv = getListView();
        lv = (ListView) v.findViewById(R.id.lv_activities);
        header = (TextView) v.findViewById(R.id.list_title);

        swipeLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(true);
                Log.d("Swipe", "Refreshing Number");
                ( new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeLayout.setRefreshing(false);
                        new GetUserRewards().execute(url);
                    }
                }, 3000);
            }
        });
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (lv == null || lv.getChildCount() == 0) ?
                                0 : lv.getChildAt(0).getTop();
                swipeLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        new GetUserRewards().execute(this.url);
        return v;
    }

    public static UserReward newInstance(String text) {

        UserReward f = new UserReward();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_new) {

        }*/

        return super.onOptionsItemSelected(item);
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetUserRewards extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (!isLoaded)
            {
            // Showing progress dialog
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
            }

        }

        @Override
        protected Void doInBackground(String... params) {
            String urlString = params[0]; // URL to call
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = sh.GET(urlString);

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    items.clear();
                    // Getting JSON Array node
                    activities = jsonObj.getJSONArray(TAG_DATA);
                    utilizable_points = Integer.valueOf(jsonObj.getString(TAG_REWARDS_POINTS));

                    // looping through All Activities
                    for (int i = 0; i < activities.length(); i++) {
                        JSONObject c = activities.getJSONObject(i);
                        UserRewardModel model = new UserRewardModel();
                        String name = c.getString(TAG_NAME);
                        String date = c.getString(TAG_DATE);
                        String comment = c.getString(TAG_COMMENT);
                        String percentage = c.getString(TAG_PERCENTAGE);
                        model.setRewardName(name);
                        model.setReceivedDateMobile(date);
                        model.setComment(comment);
                        model.setRewardPercentage(percentage);
                        // adding contact to contact list
                        items.add(model);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (!isLoaded && pDialog.isShowing()){
                pDialog.dismiss();
                isLoaded = true;
            }

            UserRewardsAdapter adapter = new UserRewardsAdapter(getActivity(), items);
            header.setText("Usable Points: " + utilizable_points);
            lv.setAdapter(adapter);

        }
        public class UserRewardsAdapter extends com.ist.login.testloginapp.CustomAdapter {

            public UserRewardsAdapter(Context context,ArrayList<Item> items) {
                super(getActivity(), items);
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;

                final Item i = items.get(position);
                if (i != null) {
                    UserRewardModel ei = (UserRewardModel)i;
                    v = vi.inflate(R.layout.list_userreward_item, null);
                    final TextView title = (TextView)v.findViewById(R.id.activity_name);
                    final TextView subtitle = (TextView)v.findViewById(R.id.activity_points);

                    if (title != null)
                        title.setText(ei.getRewardName());
                    if(subtitle != null)
                        subtitle.setText(ei.getRewardPercentage() + " - " + ei.getReceivedDateMobile());
                }
                return v;
            }
        }

    }
}

