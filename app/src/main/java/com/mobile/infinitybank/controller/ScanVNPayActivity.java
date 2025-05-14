package com.mobile.infinitybank.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.mobile.infinitybank.R;
import com.vnpay.authentication.VNP_AuthenticationActivity;
import com.vnpay.authentication.VNP_SdkCompletedCallback;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScanVNPayActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final String TAG = "ScanVNPayActivity";
    private PreviewView viewFinder;
    private ImageButton btnFlash;
    private ImageButton btnBack;
    private ExecutorService cameraExecutor;
    private boolean isFlashOn = false;
    private boolean isProcessingPayment = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_vnpay);

        // Initialize views
        viewFinder = findViewById(R.id.viewFinder);
        btnFlash = findViewById(R.id.btnFlash);
        btnBack = findViewById(R.id.btnBack);

        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Set up click listeners
        btnBack.setOnClickListener(v -> finish());
        btnFlash.setOnClickListener(v -> toggleFlash());

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        // Initialize VNPay SDK callback
        setupVNPayCallback();
    }

    private void setupVNPayCallback() {
        VNP_AuthenticationActivity.setSdkCompletedCallback(new VNP_SdkCompletedCallback() {
            @Override
            public void sdkAction(String action) {
                Log.d(TAG, "VNPay SDK Action: " + action);
                switch (action) {
                    case "AppBackAction":
                        // User pressed back from SDK
                        break;
                    case "CallMobileBankingApp":
                        // User selected payment via banking app
                        // Store PNR for later status check
                        break;
                    case "WebBackAction":
                        // User pressed back from successful payment page
                        finish();
                        break;
                    case "FaildBackAction":
                        // Payment failed
                        Toast.makeText(ScanVNPayActivity.this, 
                            "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case "SuccessBackAction":
                        // Payment successful
                        Toast.makeText(ScanVNPayActivity.this, 
                            "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
            }
        });
    }

    private void startVNPayPayment(String value) {
        if (isProcessingPayment) return;
        isProcessingPayment = true;

        Intent intent = new Intent(this, VNP_AuthenticationActivity.class);
        intent.putExtra("url", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html"); //bắt buộc, VNPAY cung cấp
        intent.putExtra("tmn_code", "7KXSGFFI"); //bắt buộc, VNPAY cung cấp
        intent.putExtra("scheme", "com.mobile.infinitybank.controller.ScanVNPayActivity"); //bắt buộc, scheme để mở lại app khi có kết quả thanh toán từ mobile banking
        intent.putExtra("is_sandbox", true); //bắt buộc, true <=> môi trường test, false <=> môi trường live
        VNP_AuthenticationActivity.setSdkCompletedCallback(new VNP_SdkCompletedCallback() {
            @Override
            public void sdkAction(String action) {
                Log.d(TAG, "VNPay SDK Action: " + action);
                switch (action) {
                    case "AppBackAction":
                        // User pressed back from SDK
                        break;
                    case "CallMobileBankingApp":
                        // User selected payment via banking app
                        // Store PNR for later status check
                        break;
                    case "WebBackAction":
                        // User pressed back from successful payment page
                        finish();
                        break;
                    case "FaildBackAction":
                        // Payment failed
                        Toast.makeText(ScanVNPayActivity.this,
                            "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                    case "SuccessBackAction":
                        // Payment successful
                        Toast.makeText(ScanVNPayActivity.this,
                            "Thanh toán thành công", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
            }
        });
        startActivity(intent);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview use case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                // Image analysis use case
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

                // Camera selector
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Bind use cases to camera
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview, imageAnalysis);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeImage(ImageProxy image) {
        InputImage inputImage = InputImage.fromMediaImage(
                image.getImage(), image.getImageInfo().getRotationDegrees());

        BarcodeScanning.getClient()
                .process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        if (barcode.getValueType() == Barcode.TYPE_TEXT) {
                            String value = barcode.getRawValue();
                            //example value: 00020101021226290010A0000007750111987498237455204481453037045405100005802VN5909CTT VNPAY6005HANOI62610312CTT VNPAY 01051901250513131640191800708CTTVNP010806testtt63041F6E
//                            startVNPayPayment(value);
                        }
                    }
                })
                .addOnCompleteListener(task -> image.close());
    }

    private void toggleFlash() {
        isFlashOn = !isFlashOn;
        btnFlash.setImageResource(isFlashOn ? R.drawable.ic_flash_on : R.drawable.ic_flash_off);
        // TODO: Implement flash control
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Yêu cầu cấp quyền camera",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        isProcessingPayment = false;
    }
} 