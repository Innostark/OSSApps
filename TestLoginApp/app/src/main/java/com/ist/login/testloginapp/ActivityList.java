package com.ist.login.testloginapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class ActivityList extends Fragment implements AdapterView.OnItemClickListener {

    private ProgressDialog pDialog;
    private static final String TAG_DATA = "data";
    private boolean isLoaded;
    private static final String TAG_NAME = "Name";
    private static final String TAG_DATE = "Date";
    private static final String TAG_UTILIZABLE_POINTS = "totalRemainingPoints";
    private static final String TAG_FB_POST = "FbPost";
    private static final String TAG_FB_FORMATTED = "FbPointsFormatted";
    private String url = Constants.baseUrl + "/UserActivity?UserId=";
    ListView lv;
    public ArrayList<Item> items = new ArrayList<>();
    // contacts JSONArray
    JSONArray activities = null;
    int utilizable_points;
    SwipeRefreshLayout swipeLayout;
    TextView header;
    public CustomAdapter adapter;
    // Hashmap for ListView
    SharedPreferences prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLoaded = false;
        // Get back arguments
        //int SomeInt = getArguments().getInt("someInt", 0);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_activity_list, container, false);

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
                        new GetActivities().execute(url);
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

        new GetActivities().execute(this.url);
        return v;
    }

    public static ActivityList newInstance(String text) {

        ActivityList f = new ActivityList();
        Bundle b = new Bundle();
        b.putString("msg", text);

        f.setArguments(b);

        return f;
    }

    @Override
    public void onItemClick(AdapterView arg0, View arg1, int position, long arg3) {

        ActivityModel item = (ActivityModel)items.get(position);
        // Starting single contact activity
        StartWebActivity(item);
    }

    private void StartWebActivity(ActivityModel item){
        // Starting single contact activity
        Intent intent = new  Intent(getActivity(),WebViewActivity.class);
        if (item.getIsFbPost()){
            intent.putExtra("fb_url",item.getFbPost());
            startActivity(intent);
        }
    }


   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_list, menu);
        return true;
    }*/




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetActivities extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            if (!isLoaded) {
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
                    utilizable_points = Integer.valueOf(jsonObj.getString(TAG_UTILIZABLE_POINTS));

                    // looping through All Activities
                    for (int i = 0; i < activities.length(); i++) {
                        JSONObject c = activities.getJSONObject(i);
                        ActivityModel model = new ActivityModel();
                        String name = c.getString(TAG_NAME);
                        String fb_post = c.getString(TAG_FB_POST);
                        String points = c.getString(TAG_FB_FORMATTED);

                        String date = c.getString(TAG_DATE);

                        model.setFormattedPoints(points);
                        model.setName(name);
                        model.setDate(date);
                        model.setFbPost(fb_post);
                        model.setIsFbPost((!(fb_post == null || fb_post.isEmpty())));
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

            adapter = new CustomAdapter(getActivity(), items);

            lv.setOnItemClickListener(ActivityList.this);
            header.setText("Usable Points: " + utilizable_points);
            lv.setAdapter(adapter);

        }

    }
}
