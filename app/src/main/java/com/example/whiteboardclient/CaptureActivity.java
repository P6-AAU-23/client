package com.example.whiteboardclient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CaptureActivity extends AppCompatActivity {
    private Executor executor = Executors.newSingleThreadExecutor();
    private PreviewView previewView;
    private ImageView capturedImageView;


    @SuppressLint("WrongViewCast")
    @Override
    @androidx.camera.core.ExperimentalGetImage
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capturescreen);
        Objects.requireNonNull(getSupportActionBar()).hide();

        previewView = findViewById(R.id.cameraPreview);
        capturedImageView = findViewById(R.id.capturedImage);

        addBtnListeners();
        startCamera();

    }

    private void addBtnListeners(){
        Button cornerButton = findViewById(R.id.setCornersBtn);
        Button capturingButton = findViewById(R.id.startCapturingBtn);
        Button changeImageBtn = findViewById(R.id.changeImageBtn);

        changeImageBtn.setOnClickListener(view -> {

        });

        cornerButton.setOnClickListener(view -> {
        });

        capturingButton.setOnClickListener(view -> {
        });
    }

    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {

                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);

            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture.Builder builder = new ImageCapture.Builder();

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getDisplay().getRotation())
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);

    }
}
