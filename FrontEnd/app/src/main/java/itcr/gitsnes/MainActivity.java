/*
 * Copyright 2014 GitSNES Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package itcr.gitsnes;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.facebook.FacebookException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.SessionState;
import com.facebook.Session;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.youtube.player.YouTubeIntents;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

/**
 * Main class, contains unique mainactivity of
 * the app where contains all fragments activities
 */
public class MainActivity extends FragmentActivity {

    private static final String TAG = "log_tag";

    /* Key Store contains all app keys */
    private KeyStore KS = new KeyStore();

    /* Result response for image intent */
    private static final int RESULT_LOAD_IMAGE = 1;
    boolean flag = true;

    /*FB Main Login Button*/
    private LoginButton authButton;
    private UiLifecycleHelper uiHelper;

    /* Accelerometer params */
    private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    /* REST-CLIENT/JSON Params */
    private JSONArray json_arr;
    private File s3game,s3image;

    /* KEYS from AWS bucket*/
    private String file_key = "none";
    private String image_key = "none";


    /**
     * OnCreate methods do that things:
     *      - Inflate Login formulary frame (hide action bar)
     *      - Init Accelerometer intent (onShake)
     *      - Create login method (auth with FB)
     *      - Handle exceptions of previously methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* -- Shows action bar and inflate main Listview from JSON (see BackendHandler) --- */
        getActionBar().hide();
        setContentView(R.layout.activity_main);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();

        /* Add transaction and login FB intent to mainActivity*/
        Login login = new Login();
        transaction.add(R.id.placeholder, login).commit();

        /* Init shake sensor and create event to get random game from DB (Backend on app-engine)*/
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                /* Inflate random-game detail */
                Toast.makeText(getApplicationContext(), "Buscando juego aleatorio!!", Toast.LENGTH_SHORT).show();
                MasterGames new_fragment = new MasterGames(json_arr);
                new_fragment.setRandomgame("random");
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.placeholder, new_fragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);


        /*Initialize FB API helper methods and listen callback from fb_loginbutton*/

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        authButton = (LoginButton) findViewById(R.id.authButton);
        authButton.setOnErrorListener(new LoginButton.OnErrorListener() {
            @Override
            public void onError(FacebookException error) {
                Log.i(TAG, "Error " + error.getMessage());
            }
        });


        /*Obtaining data from successfully FB API callback using GraphUser*/
        authButton.setReadPermissions(Arrays.asList("email"));
        authButton.setSessionStatusCallback(new Session.StatusCallback() {

            @Override
            public void call(Session session, SessionState state, Exception exception) {
                Log.i(TAG, "Accesssss Token");
                if (session.isOpened()) {
                    Log.i(TAG, "Access Token" + session.getAccessToken());
                    Request.executeMeRequestAsync(session,
                            new Request.GraphUserCallback() {
                                @Override
                                public void onCompleted(GraphUser user, Response response) {
                                    if (user != null) {
                                        Log.i(TAG, "User ID " + user.getId());
                                        Log.i(TAG, "Email " + user.asMap().get("email"));
                                        /* Inflate main-Listview from JSON (see BackendHandler) and saving current user*/
                                        back_stage();
                                        getActionBar().show();
                                        KS.setCurrent_user(user.asMap().get("email").toString());
                                        Toast.makeText(getApplicationContext(),"Welcome!! " + KS.getCurrent_user(), Toast.LENGTH_SHORT).show();
                                        new BackendHandler().sendUser( KS.getCurrent_user(),user.asMap().get("email").toString());
                                        //lblEmail.setText(user.asMap().get("email").toString());
                                    }
                                }
                            });
                } else
                    Log.i(TAG, "Nopes Token");
            }
        });
    }



    /**
     *  Method to change user session (Log-in/out)
     *  Params:
     *      - [current session][state of session][session exception (in case of error)]
     */
    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            Log.i(TAG, "Logged in...");
        } else if (state.isClosed()) {
            Log.i(TAG, "Logged out...");
        }
    }

    /**
     *  Method to handle FB callback
     *  Params:
     *      - [current session][state of session][session exception (in case of error)]
     */
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };




    /**
     *  Methods inflate menu options panel
     *  Params:
     *      - [menu]
     *  Returns:
     *      - State of building [true]
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Inflate the menu; this adds items to the action bar if it is present. */
        getMenuInflater().inflate(R.menu.main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search)
                .getActionView();
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                // this is your adapter that will be filtered
                // Log.i("log_tag",newText);

                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                Log.i("log_tag",query);

                MasterGames new_fragment = new MasterGames(json_arr);
                new_fragment.setQname(query);

                RelativeLayout rl = (RelativeLayout) findViewById(R.id.mainback);
                rl.setBackgroundColor(Color.parseColor("#009f28"));
                authButton.setVisibility(View.INVISIBLE);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.placeholder, new_fragment);
                transaction.addToBackStack(null);
                transaction.commit();

                return true;

            }
        };


        searchView.setOnQueryTextListener(queryTextListener);

        return super.onCreateOptionsMenu(menu);
    }


    /**
     *  Method handle action bar item clicks here. The action bar will
     *  automatically handle clicks on the Home/Up button, so long
     *  as you specify a parent activity in AndroidManifest.xml.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.add_game) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     *  Method to go mainFrame
     */
    public void back_stage(){
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String input = new BackendHandler().readJSON();
        try {
            json_arr = new JSONArray(input);
        } catch (JSONException e) {
            Log.i(TAG, e.toString());
            Log.i(TAG, e.toString());
        }

        RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.mainback);
        rl.setBackgroundColor(Color.parseColor("#009f28"));
        authButton.setVisibility(View.INVISIBLE);
        MasterGames new_fragment = new MasterGames(json_arr);
        new_fragment.setQtype("all");

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, new_fragment);
        transaction.addToBackStack(null);
        transaction.commit();



    }

    /* Handle back button event */
    @Override
    public void onBackPressed(){
        back_stage();
    }

    /* Inflate listview mainframe on login panel and show action bar */
    public void goMainFrame(View view) {
        EditText name = (EditText) this.findViewById(R.id.txt_username);
        if(!name.getText().toString().equals("")){
            back_stage();
            getActionBar().show();
            KS.setCurrent_user(name.getText().toString());
        }else
            Toast.makeText(this,"Insert a username", Toast.LENGTH_LONG);

    }

    /* Inflate add game frame on placeholder layout */
    public void add_games(MenuItem item) {
        AddGame new_fragment = new AddGame();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, new_fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.mainback);
        rl.setBackgroundColor(Color.parseColor("#0099cc"));
        authButton.setVisibility(View.INVISIBLE);

    }

    /* Inflates layout to advanced_search layout */
    public void advanced_query(MenuItem item) {

        SearchFrame new_fragment = new SearchFrame();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, new_fragment);
        transaction.addToBackStack(null);
        transaction.commit();

        RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.mainback);
        rl.setBackgroundColor(Color.parseColor("#ff8800"));
        authButton.setVisibility(View.INVISIBLE);

    }


    /** Inflates mainFrame/ListView with the next params from advanced_search layout:
     *      - name: if name isn't null
     *      - category: if category is different to empty
     */
    public void searching(View view) {
        EditText name = (EditText) this.findViewById(R.id.src_name);
        EditText category = (EditText) this.findViewById(R.id.src_category);


        MasterGames new_fragment = new MasterGames(json_arr);
        new_fragment.setQname(name.getText().toString());
        new_fragment.setQcat(category.getText().toString());

        RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.mainback);
        rl.setBackgroundColor(Color.parseColor("#009f28"));
        authButton.setVisibility(View.INVISIBLE);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, new_fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    /** The method sendGame makes many functions:
     *      - Get all data from text boxes on layout add_game
     *      - Makes a random bucket key for a photo/file and put the object into the bucket
     *      - Wait for the success signal and send the JSON build from BackendHandler
     *        to app-engine (Google)
     */
    public void sendGame(View view) throws IOException {

        Toast.makeText(this, "Wait, we are uploading your game =) ",Toast.LENGTH_LONG);
        /* GET DATA FROM INTERFACE*/
        EditText name= (EditText) this.findViewById(R.id.txt_name);
        EditText description = (EditText) this.findViewById(R.id.txt_desc);
        EditText category = (EditText) this.findViewById(R.id.txt_cat);

        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /* GENERATE RANDOM KEYS FOR PHOTO AND FILE*/
        this.file_key = ""+UUID.randomUUID().toString().replace("-","");
        this.image_key = ""+UUID.randomUUID().toString().replace("-","");

        /* SAVING GAME FILE/PHOTO ON AWS BUCKET*/
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials( KS.MY_ACCESS_KEY_ID, KS.MY_SECRET_KEY ));

        PutObjectRequest putObjectRequestnew = new PutObjectRequest(KS.BUCKET_NAME, this.file_key, this.s3game);
        putObjectRequestnew.setCannedAcl(CannedAccessControlList.PublicRead);
        s3Client.putObject(putObjectRequestnew);

        PutObjectRequest putObjectImagenew = new PutObjectRequest(KS.BUCKET_IMG, this.image_key, this.s3image);
        putObjectImagenew.setCannedAcl(CannedAccessControlList.PublicRead);
        s3Client.putObject(putObjectImagenew);

        String actual_key = "none";
        String actual_image = "none";

        if(this.file_key != "none")
            actual_key = this.file_key;

        if(this.image_key != "none")
            actual_image = this.image_key;

        /* SEND JSON*/
        new BackendHandler().sendJSON( KS.getCurrent_user(),
                name.getText().toString(),
                category.getText().toString(),
                description.getText().toString() ,actual_image,actual_key);
                Log.i(TAG, "Successful JSON send");
        Toast.makeText(this, "Congratulations your game has been sent",Toast.LENGTH_LONG);

    }


    /** On function onActivityResult is handle the results to load file from directories
     * and load image from galery, next the results the objects are save on file of bytes
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){

        uiHelper.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && flag ){
            if (resultCode == RESULT_OK){
                Uri uri = data.getData();
                String type = data.getType();
                if (uri != null) {
                    String path = uri.toString();
                    if (path.toLowerCase().startsWith("file://")){
                        this.s3game = new File(URI.create(path));
                        Log.i(TAG, "archivo cargado");
                    }
                }
            }
        }

        /* Handle file/image pick intent and convert that into a media storaged data*/
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data && !flag) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            this.s3image = new File(picturePath);
            Log.i(TAG, "imagen cargado");
        }

    }


    /*Call gallery android intent*/
    public void uploadfile(View view) {
        flag = true;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }


    /** Function to call DownloadFileAsync and
     * download the game file from AWS S3, on click call*/
    public void download(View view) {
        TextView name= (TextView) this.findViewById(R.id.Uni_name);
        TextView _key= (TextView) this.findViewById(R.id.fileurl);

        final String file_key =  _key.getText().toString();
        final String  file_name =  name.getText().toString();

        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toast.makeText(getApplicationContext(), "Starting download...", Toast.LENGTH_SHORT).show();
        String url = KS.MAIN_PATH + file_key;
        new DownloadFileAsync().execute(url,file_name);
        Toast.makeText(getApplicationContext(), "The file was downloaded", Toast.LENGTH_SHORT).show();
    }

    /* Intent to select a simple image */
    public void select_image(View view) {
        flag = false;
        Intent i = new Intent(
                Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_LOAD_IMAGE);
    }


    /* Get a avanced search using YouTube API intent*/
    public void goVideo(View view) {
        TextView name= (TextView) this.findViewById(R.id.Uni_name);
        Intent intent = YouTubeIntents.createSearchIntent(this,"gameplay " + name.getText().toString() + " snes") ;
        startActivity(intent);
    }


    /* Inflate listview of games from JSON
     * through backend handler -> gitsnes.appspot.com/games
     */
    public void goList(MenuItem item) {
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String input = new BackendHandler().readJSON();
        try {
            json_arr = new JSONArray(input);
        } catch (JSONException e) {
            Log.i(TAG, e.toString());
        }

        RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.mainback);
        rl.setBackgroundColor(Color.parseColor("#009f28"));
        authButton.setVisibility(View.INVISIBLE);
        MasterGames new_fragment = new MasterGames(json_arr);
        new_fragment.setQtype("all");

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, new_fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    /*Inflate admin listview menu*/
    public void adminMenu(MenuItem item) {

        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        String input = new BackendHandler().readJSON();
        try {
            json_arr = new JSONArray(input);
        } catch (JSONException e) {
            Log.i(TAG, e.toString());
        }

        RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.mainback);
        rl.setBackgroundColor(Color.parseColor("#d0ff00"));
        authButton.setVisibility(View.INVISIBLE);


        AdminFrame new_fragment = new AdminFrame(json_arr);
        new_fragment.setQtype("all");



        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, new_fragment);
        transaction.addToBackStack(null);
        transaction.commit();


    }



    /** Called after onRestoreInstanceState(Bundle),
     * onRestart(), or onPause(), for your activity
     * to start interacting with the user */
    @Override
    public void onResume() {
        super.onResume();
        // uiHelper.onResume();
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }


    /** That use when an activity is going into the background,
     *  but has not (yet) been killed */
    @Override
    public void onPause() {
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
        // uiHelper.onPause();
    }

    /* Release all remaining resources in onDestroy() */
    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    /** Called to retrieve per-instance state from an
     * activity before being killed so that the state
     * can be restored in onCreate(Bundle) */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    /** Like a game using backendHandler class
     * call like POST method */
    public void like_Game(View view) {
        /* Async mode*/
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /* Change button like image*/
        TextView name= (TextView) this.findViewById(R.id.txtgameid);
        findViewById(R.id.btn_liker).setBackgroundResource(R.drawable.heart2);
        Log.i(TAG,name.getText().toString());
         /* Commit like transaction*/
        new BackendHandler().liked_Game(name.getText().toString());

        Toast.makeText(this ,"Thanks for your opinion about the games !!", Toast.LENGTH_LONG).show();

    }

    /** Create a frame with the favorites games of the user
     * call recomended GET method */
    public void getRecomended(MenuItem item) {
        /* Async mode*/
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /* Build json*/
        String input = new BackendHandler().get_Recommended(new KeyStore().getCurrent_user());
        try {
            json_arr = new JSONArray(input);
        } catch (JSONException e) {
            Log.i(TAG, e.toString());
        }
        /* Commit fragment transaction (master view)*/
        RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.mainback);
        rl.setBackgroundColor(Color.parseColor("#e7df00"));
        authButton.setVisibility(View.INVISIBLE);
        MasterGames new_fragment = new MasterGames(json_arr);
        new_fragment.setQtype("all");

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, new_fragment);
        transaction.addToBackStack(null);
        transaction.commit();

    }

    /** Create a frame with the favorites games of the user
     * call moreliked GET method */
    public void getFavorites(MenuItem item) {
        /* Async mode*/
        StrictMode.ThreadPolicy policy = new StrictMode.
        ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        /* Build json*/
        String input = new BackendHandler().getFavorites();
        try {
            json_arr = new JSONArray(input);
        } catch (JSONException e) {
            Log.i(TAG, e.toString());
        }
        /* Commit fragment transaction (master view)*/
        RelativeLayout rl = (RelativeLayout) this.findViewById(R.id.mainback);
        rl.setBackgroundColor(Color.parseColor("#70d4b0"));
        authButton.setVisibility(View.INVISIBLE);
        MasterGames new_fragment = new MasterGames(json_arr);
        new_fragment.setQtype("all");

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.placeholder, new_fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}


