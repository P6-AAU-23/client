package com.example.whiteboardclient;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.pedro.encoder.input.video.CameraOpenException;
import com.pedro.rtmp.utils.ConnectCheckerRtmp;
import com.pedro.rtplibrary.rtmp.RtmpCamera1;
import com.pedro.rtplibrary.rtmp.RtmpCamera2;
import com.pedro.rtplibrary.view.OpenGlView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CaptureActivity extends AppCompatActivity
        implements ConnectCheckerRtmp, SurfaceHolder.Callback {

    private Executor executor = Executors.newSingleThreadExecutor();
    private PreviewView previewView;
    private ImageView capturedImageView;
    private SurfaceView surfaceView;
    private RtmpCamera1 rtmpCamera1;
    private OpenGlView openGlView;
    private Button button;
    private EditText url;

    @SuppressLint("WrongViewCast")
    @Override
    @androidx.camera.core.ExperimentalGetImage
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_display);
        Objects.requireNonNull(getSupportActionBar()).hide();

        //previewView = findViewById(R.id.cameraPreview);
        //capturedImageView = findViewById(R.id.capturedImage);

        previewView = findViewById(R.id.cameraPreview1);
        SurfaceView surfaceView = findViewById(R.id.surfaceview);

        button = findViewById(R.id.startCapturingBtn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                connectToServer();
            }
        });


        url = findViewById(R.id.et_rtp_url);
        url.setHint(R.string.hint_rtmp);

        rtmpCamera1 = new RtmpCamera1(surfaceView, this);
        rtmpCamera1.setReTries(10);
        surfaceView.getHolder().addCallback(this);

        //addBtnListeners();
        //startCamera();

    }

   // private void addBtnListeners(){
   //     Button cornerButton = findViewById(R.id.setCornersBtn);
    //    Button capturingButton = findViewById(R.id.startCapturingBtn);
     //   Button changeImageBtn = findViewById(R.id.changeImageBtn);
//
  //      changeImageBtn.setOnClickListener(view -> {
//
  //      });
//
  //      cornerButton.setOnClickListener(view -> {
    //    });
//
  //      capturingButton.setOnClickListener(view -> {
    //    });
    //}

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
        //preview.setSurfaceProvider(surfaceView.getSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);

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
        //if (rtmpCamera2.isRecording()) {
        //    rtmpCamera2.stopRecord();
            //PathUtils.updateGallery(this, folder.getAbsolutePath() + "/" + currentDateAndTime + ".mp4");
            //bRecord.setText(R.string.start_record);
            //Toast.makeText(this,
            //        "file " + currentDateAndTime + ".mp4 saved in " + folder.getAbsolutePath(),
             //       Toast.LENGTH_SHORT).show();
            //currentDateAndTime = "";
        //}
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Wait 5s and retry connect stream
                if (rtmpCamera1.reTry(5000, s, null)) { //string > reason
                    Toast.makeText(CaptureActivity.this, "Retry", Toast.LENGTH_SHORT)
                            .show();
                } else {
                    Toast.makeText(CaptureActivity.this, "Connection failed. " + s, Toast.LENGTH_SHORT).show();
                    rtmpCamera1.stopStream();
                    button.setText(R.string.startCapturingBtn);
                }
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
