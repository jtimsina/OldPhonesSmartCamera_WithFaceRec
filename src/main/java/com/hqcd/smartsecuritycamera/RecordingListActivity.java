package com.hqcd.smartsecuritycamera;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hqcd.smartsecuritycamera.adapter.RecordingListAdapter;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

public class RecordingListActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ArrayList<String> list;
    private SharedPreferences sharedPreferences;
    private TextView load;
    private RecyclerView recyclerView;
    private String ip, user, device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording_list);

        //Initialize
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        load = (TextView)findViewById(R.id.load_recordings_tv);
        recyclerView = (RecyclerView)findViewById(R.id.recording_list_recycler_view);
        list = new ArrayList<>();

        //Obtain Strings for prefs
        ip = sharedPreferences.getString("pref_ip_address","" );
        user = firebaseUser.getUid();
        device = sharedPreferences.getString("pref_device_name", "");

        //Obtain recordings list from json
        fetchRecordings();
    }

    public void fetchRecordings()
    {
        list.clear();
        String url = "http://" + sharedPreferences.getString("pref_ip_address", "") + ":5000/videos";
        Ion.with(this).load(url)
                .setBodyParameter("user", user)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        JsonArray jsonArray = result.getAsJsonArray("files");
                        for(int i = 0; i < jsonArray.size();i++)
                        {
                            //Remove quotes from the image names
                            String temp = jsonArray.get(i).toString();
                            temp = temp.replace("\"", "");

                            //Add the image paths to the arraylist
                            list.add(temp);
                        }
                        initRecyclerView();
                    }
                });
    }

    public void initRecyclerView()
    {
        load.setVisibility(View.INVISIBLE);
        RecordingListAdapter adapter = new RecordingListAdapter(this, list);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.recording_name:
                TextView tv = (TextView)view;
                String rec = tv.getText().toString();
                Toast.makeText(this, rec, Toast.LENGTH_SHORT).show();
                launchVideo(rec);
                break;
        }
    }

    public void launchVideo(String name)
    {
        int vlcRequestCode = 42;
        Uri uri = Uri.parse("http://" + ip + ":5000/users/" + user + "/videos/" + name);
        Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
        vlcIntent.setPackage("org.videolan.vlc");
        vlcIntent.setDataAndTypeAndNormalize(uri, "video/*");
        vlcIntent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
        startActivityForResult(vlcIntent, vlcRequestCode);
    }


}
