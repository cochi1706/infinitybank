package com.mobile.infinitybank.controller;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ekycsolutions.ekycid.core.models.FrameStatus;
import com.ekycsolutions.ekycid.livenessdetection.LivenessDetectionFailureResetType;
import com.ekycsolutions.ekycid.livenessdetection.LivenessDetectionResult;
import com.ekycsolutions.ekycid.livenessdetection.cameraview.LivenessDetectionCameraEventListener;
import com.ekycsolutions.ekycid.livenessdetection.cameraview.LivenessDetectionCameraOptions;
import com.ekycsolutions.ekycid.livenessdetection.cameraview.LivenessDetectionCameraView;
import com.ekycsolutions.ekycid.livenessdetection.cameraview.LivenessPromptType;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.mobile.infinitybank.databinding.ActivityLivenessDetectionBinding;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LivenessDetectionActivity extends AppCompatActivity {
    private static final String TAG = "LivenessDetection";
    public static final String EXTRA_LIVENESS_RESULT = "extra_liveness_result";
    public static final int RESULT_LIVENESS_SUCCESS = 1;
    public static final int RESULT_LIVENESS_FAILED = 2;
    
    private ActivityLivenessDetectionBinding binding;
    private LivenessDetectionCameraView livenessDetectionCameraView;
    private LivenessDetectionCameraEventListener livenessDetectionCameraEventListener;
    private FaceDetector faceDetector;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLivenessDetectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            // Initialize face detector
            FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                    .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                    .setMinFaceSize(0.15f)
                    .enableTracking()
                    .build();
            
            faceDetector = FaceDetection.getClient(options);
            setupLivenessDetection();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing face detection", e);
            Toast.makeText(this, "Không thể khởi tạo tính năng xác thực khuôn mặt", Toast.LENGTH_LONG).show();
            setResult(RESULT_LIVENESS_FAILED);
            finish();
        }
    }

    private void setupLivenessDetection() {
        livenessDetectionCameraView = binding.livenessDetectionCameraView;
        livenessDetectionCameraEventListener = new LivenessDetectionCameraEventListener() {
            @Override
            public void onPromptCompleted(int i, boolean b, float v) {
                // Handle individual prompt completion if needed
                if (!b) {
                    // Prompt failed
                    binding.tvScanningStep.setText("Thất bại bước " + i);
                    binding.tvStepDescription.setText("Vui lòng thử lại");
                }
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
                binding.tvScanningStep.setText("Vui lòng giữ khuôn mặt trong khung");
                binding.tvStepDescription.setText("Điều chỉnh vị trí để khuôn mặt nằm trong khung");
            }

            @Override
            public void onInitialize() {
                binding.tvScanningStep.setText("Đang khởi tạo camera...");
                binding.tvStepDescription.setText("Vui lòng chờ trong giây lát");
            }

            @Override
            public void onFocus() {
                binding.tvScanningStep.setText("Đã nhận diện khuôn mặt");
                binding.tvStepDescription.setText("Vui lòng thực hiện theo hướng dẫn");
            }

            @Override
            public void onActivePromptChanged(@Nullable LivenessPromptType livenessPromptType) {
                if (livenessPromptType != null) {
                    switch (livenessPromptType) {
                        case BLINKING:
                            binding.tvScanningStep.setText("Vui lòng nháy mắt");
                            binding.tvStepDescription.setText("Nhìn thẳng và nháy mắt tự nhiên");
                            break;
                        case LOOK_LEFT:
                            binding.tvScanningStep.setText("Vui lòng quay đầu sang trái");
                            binding.tvStepDescription.setText("Quay đầu từ từ sang trái");
                            break;
                        case LOOK_RIGHT:
                            binding.tvScanningStep.setText("Vui lòng quay đầu sang phải");
                            binding.tvStepDescription.setText("Quay đầu từ từ sang phải");
                            break;
                        default:
                            binding.tvScanningStep.setText("Vui lòng nhìn thẳng vào camera");
                            binding.tvStepDescription.setText("Giữ nguyên vị trí cho đến khi hoàn thành");
                            break;
                    }
                }
            }
        };
        livenessDetectionCameraView.addListener(livenessDetectionCameraEventListener);
        
        // Configure liveness detection options
        ArrayList<LivenessPromptType> promptTypes = new ArrayList<>();
        promptTypes.add(LivenessPromptType.BLINKING);
        promptTypes.add(LivenessPromptType.LOOK_LEFT);
        promptTypes.add(LivenessPromptType.LOOK_RIGHT);
        
        LivenessDetectionCameraOptions options = new LivenessDetectionCameraOptions(
            promptTypes,                    // prompts
            5,                             // promptTimerCountDownSec - 5 seconds for each prompt
            LivenessDetectionFailureResetType.MARK_AS_FAILURE,  // Mark as failure on failure
            false                          // Don't record video
        );
        
        livenessDetectionCameraView.start(options);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (livenessDetectionCameraView != null) {
            livenessDetectionCameraView.stop();
        }
        if (faceDetector != null) {
            faceDetector.close();
        }
    }
}
