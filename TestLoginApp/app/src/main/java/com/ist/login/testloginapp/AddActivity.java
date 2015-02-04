package com.ist.login.testloginapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;


public class AddActivity extends ActionBarActivity {


    public  static String api_URL_POST = Constants.baseUrl + "/UserAct";
    private static final String TAG_ID = "Id";
    private static final String TAG_NAME = "Name";
    private static final String TAG_HAS_FB_POINTS = "IsFbPoints";
    private static final String TAG_POINTS = "Points";
    private static final String TAG_FbPoints = "FbPoints";

    ArrayList<String> activityList;
    private int selectedActivityId = -1;
    private boolean switch_status;
    private int selectedActivityPosition ;
    ArrayList<ActivityModel> activityModels;
    private ProgressDialog pDialog;
    private static final int FILE_SELECT_CODE = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView ivImage = null;
    Switch fb_switch = null;
    private static GregorianCalendar selectedDate;
    private EditText date_picker ;
    private EditText activity_picker;
    private EditText activity_comment;
    private String activities_json;
    private Button cancel_activity, save_activity;
    private TextView errorMessage ;
    public UserActivityModel activityModel;
    private boolean photo_flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        activityModel = new UserActivityModel();
        photo_flag = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addactivity);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        ivImage = (ImageView) findViewById(R.id.ivImage);
        cancel_activity = (Button) findViewById(R.id.cancel_activity);
        save_activity = (Button) findViewById(R.id.create_activity);
        errorMessage = (TextView) findViewById(R.id.errorMessage);
        activities_json = prefs.getString("Activities","");
        selectedDate = new GregorianCalendar();
        date_picker = (EditText) findViewById(R.id.date_picker);
        date_picker.addTextChangedListener(new TextValidator(date_picker) {

            @Override
            public void validate(TextView textView, String text) {
                if (!text.isEmpty()){
                    textView.setError(null);
                }
            }
        });
        activity_picker = (EditText) findViewById(R.id.activity_picker);
        activity_comment = (EditText) findViewById(R.id.activity_comment);
        activity_comment.addTextChangedListener(new TextValidator(activity_comment) {
            @Override
            public void validate(TextView textView, String text) {
                if(!text.isEmpty())
                    textView.setError(null);
            }
        });

        fb_switch = (Switch) findViewById(R.id.fb_switch);
        fb_switch.setEnabled(false);
        fb_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch_status = isChecked;
                activityModel.setIsFbPost(isChecked);
                setUI(isChecked);
            }
        });
        new MyEditTextDatePicker(this, R.id.date_picker, selectedDate);
        setUI(false);
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });
        new CallAPI().execute();
    }


    @Override
    public void onBackPressed() {
       int a = 0;
    }

    public void setUI(boolean isChecked){
        if (isChecked){
            //Show Camera Panel
            ivImage.setVisibility(View.VISIBLE);
            activity_comment.setVisibility(View.VISIBLE);
        }else
        {
            //Hide Camera Panel
            ivImage.setVisibility(View.GONE);
            activity_comment.setVisibility(View.GONE);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_addactivity, menu);
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

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        /*create instance of File with name img.jpg*/
        File file = new File(Environment.getExternalStorageDirectory()+File.separator + "img.jpg");
        /*put uri as extra in intent object*/
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        /*start activity for result pass intent as argument and request code */
        startActivityForResult(intent, 1);

    }
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    dispatchTakePictureIntent();
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"),FILE_SELECT_CODE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        return true;
        //return super.onTouchEvent(event);
    }

    private void selectActivity() {

        AlertDialog.Builder builder = new AlertDialog.Builder(AddActivity.this);
        builder.setTitle("Select Activity!");
        ArrayAdapter<ActivityModel> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, activityModels);

        builder.setSingleChoiceItems(adapter, selectedActivityPosition, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {


                selectedActivityPosition = item;
                // Set the text followed by the position
                ActivityModel model = activityModels.get(item);
                Boolean is_fb = model.getIsFbPost();
                activityModel.setId(model.getId());
                activityModel.setIsFbPost(is_fb);
                activityModel.setPoints(model.getPoints());
                activityModel.setFbPoints(model.getFbPoints());
                activityModel.setName(model.toString());
                selectedActivityId = model.getId();
                activity_picker.setError(null);
                fb_switch.setEnabled(is_fb);
                fb_switch.setChecked(is_fb);
                setUI(is_fb);
                activity_picker.setText(model.toString());
                dialog.cancel();
            }
        });
        //builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String setPhoto(Bitmap bitmapm) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            byte[] byteArrayImage = baos.toByteArray();
            return Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private boolean validateForm(){
        boolean flag = true;
        String errorString = "";
        if (date_picker.getText().toString().isEmpty())
        {
            errorString += "*Activity Date is Required\n";
            date_picker.setError("Required");
            flag = false;
        }
        if (selectedActivityId <= 0){
            errorString += " *Activity is Required\n";
            activity_picker.setError("Required");
            flag = false;
        }
        if (fb_switch.isChecked() && activity_comment.getText().toString().isEmpty()){
            errorString += "*Comment Required for FaceBook\n";
            activity_comment.setError("Required");
            flag = false;
        }
        if (fb_switch.isChecked() && !photo_flag){
            errorString += "*Photo Required for FaceBook\n";
            flag = false;
        }

        if(!flag){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setTitle("Please Correct following error(s)")
                    .setMessage(errorString)
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }
        return flag;
    }
    public void saveActivity(View view) {
        if (validateForm())
            new SaveAPI().execute();
    }

    public void cancelActivity(View view){
        finish();
    }

    public static Bitmap decodeSampledBitmapFromFile(String path,
                                                     int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        //Query bitmap without allocating memory
        options.inJustDecodeBounds = true;
        //decode file from path
        BitmapFactory.decodeFile(path, options);
        // Calculate inSampleSize
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        //decode according to configuration or according best match
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        int inSampleSize = 1;
        if (height > reqHeight) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        }
        int expectedWidth = width / inSampleSize;
        if (expectedWidth > reqWidth) {
            //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
        //if value is greater than 1,sub sample the original image
        options.inSampleSize = inSampleSize;
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
          /*  Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");*/
            //ivImage.setImageBitmap(imageBitmap);
            //create instance of File with same name we created before to get image from storage
            File file = new File(Environment.getExternalStorageDirectory()+ File.separator + "img.jpg");
            //get bitmap from path with size of
            ivImage.setImageBitmap(decodeSampledBitmapFromFile(file.getAbsolutePath(), 600, 450));
        } else if (requestCode == FILE_SELECT_CODE) {
            Uri selectedImageUri = data.getData();

            String tempPath = getPath(selectedImageUri, AddActivity.this);
            Bitmap bm;
            BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
            bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
            ivImage.setImageBitmap(bm);
        }
        photo_flag = true;
    }

    public String getPath(Uri uri, Activity activity) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private class CallAPI extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

           // Locate the Activities
            activityModels = new ArrayList<>();
            // Create an array to populate the spinner
            activityList = new ArrayList<>();
            // HTTP Get
            try {
                JSONArray activitiesJson = new JSONArray(activities_json);
                try {
                    // Locate the NodeList name
                    for (int i = 0; i < activitiesJson.length(); i++) {
                        JSONObject jsonobject = activitiesJson.getJSONObject(i);

                        ActivityModel model = new ActivityModel();

                        model.setId(jsonobject.getInt(TAG_ID));
                        model.setName(jsonobject.optString(TAG_NAME));
                        model.setIsFbPost(jsonobject.getBoolean(TAG_HAS_FB_POINTS));
                        int pt = jsonobject.getInt(TAG_POINTS);
                        int fbpt = jsonobject.getInt(TAG_FbPoints);
                        model.setPoints(pt);
                        model.setFbPoints(fbpt);
                        activityModels.add(model);

                        // Populate spinner with country names
                        activityList.add(model.getName());

                    }
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }


            } catch (Exception e ) {
                System.out.println(e.getMessage());

            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            activity_picker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectActivity();
                }
            });
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }


    } // end CallAPI

    private class SaveAPI extends AsyncTask<Void, Void, Long> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            errorMessage.setVisibility(View.GONE);
            save_activity.setEnabled(false);
            cancel_activity.setEnabled(false);
            // Showing progress dialog
            pDialog = new ProgressDialog(AddActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Long doInBackground(Void... params) {
            // Create a new HttpClient and Post Header
            BitmapDrawable drawable = (BitmapDrawable) ivImage.getDrawable();
            Bitmap bitmap = drawable.getBitmap();
            String file = setPhoto(bitmap);
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(api_URL_POST);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String user_id = prefs.getString("UserId", "");
            String email = prefs.getString("Email", "");

            try {
                InputStream inputStream;

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("UserEmail", email);
                jsonObject.accumulate("UserId",user_id);
                jsonObject.accumulate("ImageBase64", file);
                jsonObject.accumulate("IsFbPost", switch_status);
                jsonObject.accumulate("Comment", switch_status? activity_comment.getText(): "");
                jsonObject.accumulate("ActivityId",selectedActivityId );
                jsonObject.accumulate("PerformanceDay",selectedDate.getInstance().get(Calendar.DATE) );
                jsonObject.accumulate("PerformanceMonth",selectedDate.getInstance().get(Calendar.MONTH) + 1);
                jsonObject.accumulate("PerformanceYear",selectedDate.getInstance().get(Calendar.YEAR) );

                // 4. convert JSONObject to JSON to String
                String json = jsonObject.toString();

                // ** Alternative way to convert Person object to JSON string usin Jackson Lib
                // ObjectMapper mapper = new ObjectMapper();
                // json = mapper.writeValueAsString(person);

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httppost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httppost.setHeader("Accept", "application/json");
                httppost.setHeader("Content-type", "application/json");

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient.execute(httppost);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();
                String str = convertInputStreamToString(inputStream);
                return Long.parseLong(str);
            }catch (Exception e) {
                Log.d("InputStream", e.getLocalizedMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();

            if (result > 0){
                Intent resultIntent = new Intent();
                resultIntent.putExtra("id", result.longValue());
                resultIntent.putExtra("Name", activityModel.getName());
                int totalPoints = activityModel.getPoints()+ activityModel.getFbPoints();
                String formattedPoints = String.valueOf(totalPoints);
                if (activityModel.getIsFbPost()){
                    formattedPoints += "("+ activityModel.getPoints() + " + " + activityModel.getFbPoints()+ " Fb points)" ;
                }
                resultIntent.putExtra("FormattedPoints", formattedPoints);
                resultIntent.putExtra("IsFbPost", activityModel.getIsFbPost());
                resultIntent.putExtra("Date", activityModel.getDate());
                resultIntent.putExtra("Points", activityModel.getPoints());
                resultIntent.putExtra("FbPoints", activityModel.getFbPoints());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                return;
            }

            save_activity.setEnabled(true);
            cancel_activity.setEnabled(true);
            errorMessage.setVisibility(View.VISIBLE);

        }


    } // end CallAPI

    private class MyEditTextDatePicker  implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
        EditText _editText;
        private int _day;
        private int _month;
        private int _birthYear;
        private Context _context;
        private GregorianCalendar date;
        public MyEditTextDatePicker(Context context, int editTextViewID , GregorianCalendar dt)
        {
            Activity act = (Activity)context;
            this._editText = (EditText)act.findViewById(editTextViewID);
            this._editText.setOnClickListener(this);
            this._context = context;
            this.date = dt;
        }


        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            _birthYear = year;
            _month = monthOfYear;
            _day = dayOfMonth;
            this.date.set(_birthYear,_month + 1,_day);
            updateDisplay();
        }
        @Override
        public void onClick(View v) {
            Calendar c = Calendar.getInstance();
            DatePickerDialog dialog =  new DatePickerDialog(_context, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
            dialog.getDatePicker().setMaxDate(c.getTimeInMillis());
            dialog.show();

        }

        // updates the date in the birth date EditText
        private void updateDisplay() {

            _editText.setText(new StringBuilder()
                    // Month is 0 based so add 1
                    .append(_day).append("/").append(_month + 1).append("/").append(_birthYear).append(" "));
            _editText.setError(null);
            activityModel.setDate(_editText.getText().toString());
        }
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
