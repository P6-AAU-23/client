package com.example.whiteboardclient;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Start extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        addCaptureBtnListener();
    }

    private void addCaptureBtnListener(){
        Button openCaptureBtn = findViewById(R.id.openCaptureBtn);
        openCaptureBtn.setOnClickListener(view -> {
            if (hasCameraPermission()) {
                showCameraPreview();
            } else {
                requestCameraPermission();
            }
        });
    }

    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 0;

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    // Starts the capture  activity
    private void showCameraPreview() {
        Intent intent = new Intent(this, CaptureActivity.class);
        startActivity(intent);
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            CAMERA_PERMISSION,
            CAMERA_REQUEST_CODE
        );
    }
}
