package com.mobile.infinitybank.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ekycsolutions.ekycid.core.models.FrameStatus;
import com.ekycsolutions.ekycid.livenessdetection.LivenessDetectionResult;
import com.ekycsolutions.ekycid.livenessdetection.cameraview.LivenessDetectionCameraEventListener;
import com.ekycsolutions.ekycid.livenessdetection.cameraview.LivenessDetectionCameraOptions;
import com.ekycsolutions.ekycid.livenessdetection.cameraview.LivenessDetectionCameraView;
import com.ekycsolutions.ekycid.livenessdetection.cameraview.LivenessPromptType;
import com.mobile.infinitybank.databinding.ActivityLivenessDetectionBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LivenessDetectionActivity extends AppCompatActivity {
    public static final String EXTRA_LIVENESS_RESULT = "extra_liveness_result";
    public static final int RESULT_LIVENESS_SUCCESS = 1;
    public static final int RESULT_LIVENESS_FAILED = 2;
    
    private ActivityLivenessDetectionBinding binding;
    private LivenessDetectionCameraView livenessDetectionCameraView;
    private LivenessDetectionCameraEventListener livenessDetectionCameraEventListener;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLivenessDetectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        livenessDetectionCameraView = binding.livenessDetectionCameraView;
        livenessDetectionCameraEventListener = new LivenessDetectionCameraEventListener() {
            @Override
            public void onPromptCompleted(int i, boolean b, float v) {
                // Handle individual prompt completion if needed
            }

            @Override
            public void onAllPromptsCompleted(@NonNull LivenessDetectionResult livenessDetectionResult) {
                // Return result to calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_LIVENESS_RESULT, RESULT_OK);
                setResult(RESULT_LIVENESS_SUCCESS, resultIntent);
                finish();
            }

            @Override
            public void onCountDownChanged(int i, int i1) {
                // Handle countdown changes if needed
            }

            @Override
            public void onFrame(@NonNull FrameStatus frameStatus) {
                // Handle frame status updates if needed
            }

            @Override
            public void onFocusDropped() {
                // Handle focus dropped if needed
            }

            @Override
            public void onInitialize() {
                // Handle initialization if needed
            }

            @Override
            public void onFocus() {
                // Handle focus if needed
            }

            @Override
            public void onActivePromptChanged(@Nullable LivenessPromptType livenessPromptType) {
                // Handle active prompt changes if needed
            }
        };
        livenessDetectionCameraView.addListener(livenessDetectionCameraEventListener);
        livenessDetectionCameraView.start(new LivenessDetectionCameraOptions());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
