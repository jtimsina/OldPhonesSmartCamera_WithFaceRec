package com.hqcd.smartsecuritycamera;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pedro.rtplibrary.rtsp.RtspCamera1;
import com.pedro.rtsp.utils.ConnectCheckerRtsp;

public class StreamingActivity extends AppCompatActivity
        implements ConnectCheckerRtsp, SurfaceHolder.Callback {

    private static final String TAG = "StreamingActivity";
    RtspCamera1 rtspCamera1;
    Button streamButton;
    SharedPreferences sharedPreferences;
    FirebaseAuth firebaseAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streaming);
        //Keep Device from Sleeping
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Set preview of camera
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.streaming_surfaceview);
        //Start/stop streaming
        streamButton = (Button)findViewById(R.id.streaming_start_button);
        //Initialize rtsp camera and connect to camera preview
        rtspCamera1 = new RtspCamera1(surfaceView, this);
        surfaceView.getHolder().addCallback(this);

        //Obtain preferences and user
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();


    }

    @Override
    public void onConnectionSuccessRtsp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamingActivity.this, "Connection Successful", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onConnectionFailedRtsp(String reason) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamingActivity.this, "Connection Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDisconnectRtsp() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(StreamingActivity.this, "Disconnected ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthErrorRtsp() {

    }

    @Override
    public void onAuthSuccessRtsp() {

    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rtspCamera1.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        rtspCamera1.stopPreview();
    }

    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.streaming_start_button:
                if (!rtspCamera1.isStreaming()) {
                    if (rtspCamera1.isRecording()
                            || rtspCamera1.prepareAudio() && rtspCamera1.prepareVideo()) {
                        streamButton.setText("Stop Streaming");
                        String url = "rtsp://" + sharedPreferences.getString("pref_ip_address", "")
                                + ":" + sharedPreferences.getString("pref_port","" ) + "/" +
                                user.getUid() + "/" + sharedPreferences.getString("pref_device_name","" );
                        Log.d(TAG, "Streaming to : " + url);
                        rtspCamera1.startStream(url);
                    } else {
                        Toast.makeText(this, "Error preparing stream, This device cant do it",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    streamButton.setText("Start Stream");
                    rtspCamera1.stopStream();
                }
        }
    }
}
