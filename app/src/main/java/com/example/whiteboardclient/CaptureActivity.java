package com.example.whiteboardclient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
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
    private EditText url;
    private Button streamButton;
    final int CONNECT_RE_TRIES = 10;

    @SuppressLint("WrongViewCast")
    @Override
    @androidx.camera.core.ExperimentalGetImage
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_display);
        Objects.requireNonNull(getSupportActionBar()).hide();
        SurfaceView surfaceView = findViewById(R.id.surfaceview);

        streamButton = findViewById(R.id.streamButton);
        streamButton.setOnClickListener(view -> {
            if (!rtmpCamera1.isStreaming()) {
                if (rtmpCamera1.prepareVideo()) {
                    rtmpCamera1.startStream(url.getText().toString());
                }
            } else {
                rtmpCamera1.stopStream();
            }
        });

        url = findViewById(R.id.et_rtp_url);
        url.setHint(R.string.hint_rtmp);

        rtmpCamera1 = new RtmpCamera1(surfaceView, this);
        rtmpCamera1.setReTries(CONNECT_RE_TRIES);
        surfaceView.getHolder().addCallback(this);
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
    public void onConnectionFailedRtmp(@NonNull String reason) {
        runOnUiThread(() -> {
            streamButton.setEnabled(false);
            long waitTime = 5000;
            Log.d("debug", reason);
            if (rtmpCamera1.reTry(waitTime, reason)) {
                Toast.makeText(CaptureActivity.this, "Retry", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(CaptureActivity.this, "Connection failed: " + reason, Toast.LENGTH_SHORT).show();
                rtmpCamera1.stopStream();
                streamButton.setText(R.string.start_streaming);
                streamButton.setEnabled(true);
            }
        });
    }

    @Override
    public void onConnectionStartedRtmp(@NonNull String s) {
        streamButton.setEnabled(false);
    }

    @Override
    public void onConnectionSuccessRtmp() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
            streamButton.setText(R.string.stop_streaming);
            streamButton.setEnabled(true);
        });
    }

    @Override
    public void onDisconnectRtmp() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
            streamButton.setText(R.string.start_streaming);
            streamButton.setEnabled(true);
        });
    }

    @Override
    public void onNewBitrateRtmp(long l) {
    }
}
