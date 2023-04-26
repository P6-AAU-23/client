package com.example.whiteboardclient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;

import java.util.Objects;

public class CaptureActivity extends AppCompatActivity
        implements ConnectCheckerRtmp, SurfaceHolder.Callback {

    private RtmpCamera1 rtmpCamera1;
    private Button button;
    private EditText url;

    @SuppressLint("WrongViewCast")
    @Override
    @androidx.camera.core.ExperimentalGetImage
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_display);
        Objects.requireNonNull(getSupportActionBar()).hide();

        SurfaceView surfaceView = findViewById(R.id.surfaceview);

        button = findViewById(R.id.startCapturingBtn);
        button.setOnClickListener(v -> connectToServer());


        url = findViewById(R.id.et_rtp_url);
        url.setHint(R.string.hint_rtmp);

        rtmpCamera1 = new RtmpCamera1(surfaceView, this);
        rtmpCamera1.setReTries(10);
        surfaceView.getHolder().addCallback(this);

    }

    public void connectToServer() {
        if (!rtmpCamera1.isStreaming()) {
            if (rtmpCamera1.prepareVideo()) {
                rtmpCamera1.startStream(url.getText().toString());
            } else {
                Toast.makeText(this, "other sus error",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            button.setText(R.string.startCapturingBtn);
            rtmpCamera1.stopStream();
        }
    }




    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }
    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        rtmpCamera1.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (rtmpCamera1.isStreaming()) {
            rtmpCamera1.stopStream();
            //button.setText(getResources().getString(R.id.startCapturingBtn));
        }
        rtmpCamera1.stopPreview();
    }

    @Override
    public void onAuthErrorRtmp() {

    }

    @Override
    public void onAuthSuccessRtmp() {

    }

    @Override
    public void onConnectionFailedRtmp(@NonNull String s) {
        runOnUiThread(() -> {
            //Wait 5s and retry connect stream
            if (rtmpCamera1.reTry(5000, s, null)) { //string > reason
                Toast.makeText(CaptureActivity.this, "Retry", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(CaptureActivity.this, "Connection failed. " + s, Toast.LENGTH_SHORT).show();
                rtmpCamera1.stopStream();
                button.setText(R.string.startCapturingBtn);
            }
        });
    }

    @Override
    public void onConnectionStartedRtmp(@NonNull String s) {

    }

    @Override
    public void onConnectionSuccessRtmp() {

    }

    @Override
    public void onDisconnectRtmp() {

    }

    @Override
    public void onNewBitrateRtmp(long l) {

    }
}
