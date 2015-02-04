package com.ist.login.testloginapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;


public class MainActivity extends ActionBarActivity {


    private EditText username = null,password = null;
    private TextView errorMessage = null;
    private Button login;
    public final static String apiURL = Constants.baseUrl+ "/Access";
    private ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


        String user_name = prefs.getString("username", "");
        String pass = prefs.getString("password", "");
        if(user_name != null && !user_name.isEmpty() && pass!= null && !pass.isEmpty())
        {
            Intent intent = new  Intent(MainActivity.this,HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.password);
        errorMessage = (TextView)findViewById(R.id.errorMessage);
        login = (Button)findViewById(R.id.login);
        password.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    login.performClick();
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                            INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
    }

    public void openBrowser(View view){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.forgot_password)));
        startActivity(browserIntent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
        //return super.onTouchEvent(event);
    }




    public static boolean isValidEmail(String target) {
        return !(target == null || target.isEmpty()) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static boolean isValidPassword(String target) {
        return !(target == null || target.isEmpty());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private boolean validateForm(){

        boolean flag = true;
        String u_name = username.getText().toString();
        String pwd = password.getText().toString();
        if (!isValidPassword(pwd))
        {
            password.setError("Password cannot be empty");
            flag = false;
        }
        if (!isValidEmail(u_name))
        {
            flag = false;
            username.setError("Invalid Email");
        }
        return flag;
    }

    public void login(View view) {
        if(validateForm()){
            login.setEnabled(false);
            errorMessage.setVisibility(View.INVISIBLE);
            String u_name = username.getText().toString();
            String pwd = password.getText().toString();
            String urlString = apiURL + "?email=" + u_name+ "&password="+ pwd;
            new CallAPI().execute(urlString);
        }
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

    private class CallAPI extends AsyncTask<String, String, Boolean> {

        ServiceHandler handler = new ServiceHandler();
        @Override
        protected Boolean doInBackground(String... params) {

            String urlString=params[0]; // URL to call

            // HTTP Get
            try {
                String res= handler.GET(urlString);
                JSONObject object = new JSONObject(res);

                boolean isAuthenticated = object.getBoolean("IsAuthenticated");
                if (isAuthenticated)
                {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.putString("username", username.getText().toString());
                    editor.putString("password", password.getText().toString());
                    editor.putString("UserId", object.getString("UserId"));
                    editor.putString("Email", object.getString("Email"));
                    editor.putString("Activities", object.getString("Activities"));
                    editor.apply();
                }
                return isAuthenticated;
            } catch (Exception e ) {
                System.out.println(e.getMessage());
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if ( pDialog.isShowing()){
                pDialog.dismiss();
            }

            if (result){
                Intent intent = new  Intent(MainActivity.this,HomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            errorMessage.setVisibility(View.VISIBLE);
            login.setEnabled(true);

        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
    } // end CallAPI
}
