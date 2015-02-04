package com.ist.login.testloginapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class OfferedRewardActivity extends Fragment {

    private ProgressDialog pDialog;
    private static final String TAG_DATA = "RewardPercentage";
    private static final String TAG_NAME = "RewardName";
    private String url = Constants.baseUrl + "/Reward?userId=";
    ListView lv;
    SwipeRefreshLayout swipeLayout;

    ArrayList<Item> items = new ArrayList<>();
    private CustomAdapter mAdapter;
    // contacts JSONArray
    private boolean isLoaded;
    SharedPreferences prefs;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_offered_reward, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isLoaded = false;
        String user_name = prefs.getString("UserId", "");
        String email = prefs.getString("Email", "");
        this.url+= user_name + "&UserEmail="+ email;


        lv = (ListView) v.findViewById(R.id.lv_activities);

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
                        new GetRewards().execute(url);
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


        new GetRewards().execute(this.url);
        return v;
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

    public static OfferedRewardActivity newInstance(String text) {

        OfferedRewardActivity f = new OfferedRewardActivity();
        Bundle b = new Bundle();
        b.putString("msg", text);
        f.setArguments(b);
        return f;
    }

    private class GetRewards extends AsyncTask<String, Void, Void> {

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
                    JSONArray activities = new JSONArray(jsonStr);
                    items.clear();

                    // looping through All Activities
                    for (int i = 0; i < activities.length(); i++) {
                        JSONObject c = activities.getJSONObject(i);
                        RewardModel model = new RewardModel();
                        String name = c.getString(TAG_NAME);
                        String percentage = c.getString(TAG_DATA);

                        model.setRewardName(name);
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
                isLoaded = true;
                pDialog.dismiss();
            }

            RewardsAdapter adapter = new RewardsAdapter(getActivity(), items);
            lv.setAdapter(adapter);

        }

        public class RewardsAdapter extends com.ist.login.testloginapp.CustomAdapter {

            public RewardsAdapter(Context context,ArrayList<Item> items) {
                super(getActivity(), items);
            }


            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;

                final Item i = items.get(position);
                if (i != null) {
                    RewardModel ei = (RewardModel)i;
                    v = vi.inflate(R.layout.list_reward_item, null);
                    final TextView title = (TextView)v.findViewById(R.id.activity_name);
                    final TextView subtitle = (TextView)v.findViewById(R.id.activity_points);

                    if (title != null)
                        title.setText(ei.getRewardName());
                    if(subtitle != null)
                        subtitle.setText(ei.getRewardPercentage());
                }
                return v;
            }
    }
    }
}
