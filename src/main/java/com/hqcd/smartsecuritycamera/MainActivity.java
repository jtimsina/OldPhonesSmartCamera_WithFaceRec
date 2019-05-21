package com.hqcd.smartsecuritycamera;

import android.Manifest;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hqcd.smartsecuritycamera.adapter.RecyclerViewAdapter;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private FirebaseAuth mAuth;
    private Button imageButton;
    private TextView welcomeText, allImageText;
    private FirebaseUser user;
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private RecyclerView recyclerView;
    private FloatingActionButton fab;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Obtain Current User if Logged In
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Initialize Default Settings
        PreferenceManager.setDefaultValues(this,R.xml.preferences ,false );

        //Initialize views;
        welcomeText = (TextView)findViewById(R.id.welcomeText);
        allImageText = (TextView)findViewById(R.id.all_images);
        recyclerView = (RecyclerView)findViewById(R.id.recentActivityRecyclerView);
        fab = (FloatingActionButton)findViewById(R.id.floatingActionButton);


        //Request Permissions
        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO}, 1);

        //Obtain Prefs
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        if(user==null)
        {
            menu.findItem(R.id.item_logout).setEnabled(false);
            menu.findItem(R.id.item_login).setEnabled(true);
        }
        else
        {
            menu.findItem(R.id.item_logout).setEnabled(true);
            menu.findItem(R.id.item_login).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.item_login:
                startActivity(new Intent(this, LogInActivity.class));
                break;
            case R.id.item_logout:
                logOut();
                break;
            case R.id.item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.item_stream:
                startActivity(new Intent(this, StreamingActivity.class));
                break;
            case R.id.item_recordings:
                startActivity(new Intent(this, RecordingListActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        updateUI(user);
    }

    public void logOut()
    {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getApplicationContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
        user = FirebaseAuth.getInstance().getCurrentUser();
        invalidateOptionsMenu();
        updateUI(user);
        mNames.clear();
        mImageUrls.clear();
    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {

            case R.id.floatingActionButton:
                viewLiveStream();
                break;
            case R.id.all_images:
                startActivity(new Intent(this, ImageListActivity.class));
                break;
        }
    }

    public void viewLiveStream()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Type the device name of the stream you want to view");
        builder.setTitle("View Stream");
        final EditText deviceET = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        deviceET.setLayoutParams(lp);
        builder.setView(deviceET);
        builder.setPositiveButton("View", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(deviceET.getText().toString() == sharedPreferences.getString("pref_device_name",""))
                {
                    AlertDialog.Builder invalid = new AlertDialog.Builder(MainActivity.this);
                    invalid.setMessage("Choose a different device");
                    invalid.setTitle("Already viewing this device");
                    builder.setPositiveButton("Ok", null);
                    AlertDialog invalidDialog = invalid.create();
                    invalidDialog.show();
                }
                else
                {
                    int vlcRequestCode = 42;
                    Uri uri = Uri.parse("rtsp://" + sharedPreferences.getString("pref_ip_address", "") + ":80/" + user.getUid() + "/" +
                            deviceET.getText().toString());
                    Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
                    vlcIntent.setPackage("org.videolan.vlc");
                    vlcIntent.setData(uri);
                    vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
                    startActivityForResult(vlcIntent, vlcRequestCode);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void updateUI(FirebaseUser user)
    {
        if(user == null)
        {
            welcomeText.setText("Sign In to Continue");
            recyclerView.setVisibility(View.INVISIBLE);
        }
        else
        {
            welcomeText.setText("Welcome " + user.getDisplayName());
            recyclerView.setVisibility(View.VISIBLE);
            initImageBitmaps();
        }
    }

    private void initImageBitmaps()
    {
        mNames.clear();
        mImageUrls.clear();

        String url = "http://" + sharedPreferences.getString("pref_ip_address", "") + ":5000/images";
        Ion.with(this).load(url)
                .setBodyParameter("user", user.getUid())
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        JsonArray jsonArray = result.getAsJsonArray("files");
                        for(int i = jsonArray.size() -1 ; i > jsonArray.size() - 4;i--)
                        {
                            //Remove quotes from the image names
                            String temp = jsonArray.get(i).toString();
                            temp = temp.replace("\"", "");

                            //Add the image paths to the arraylist
                            mNames.add(temp);

                        }
                        initRecyclerView();
                    }
                });
    }

    private void initRecyclerView(){
        Log.d(TAG, "initRecyclerView: init recycler view");

        for(int i = 0; i < mNames.size(); i++)
        {
            mImageUrls.add("http://" + sharedPreferences.getString("pref_ip_address", "")
                    + ":5000/users/" + user.getUid() + "/images/" + mNames.get(i));
        }


        RecyclerView recyclerView = findViewById(R.id.recentActivityRecyclerView);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(mNames, mImageUrls, this);

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

}