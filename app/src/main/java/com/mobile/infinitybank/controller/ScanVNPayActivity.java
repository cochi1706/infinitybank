package com.mobile.infinitybank.controller;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebSettings;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.View;

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

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONException;
import org.json.JSONObject;

public class ScanVNPayActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final String TAG = "ScanVNPayActivity";
    private PreviewView viewFinder;
    private ImageButton btnFlash;
    private ImageButton btnBack;
    private WebView webView;
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
        webView = findViewById(R.id.webView);

        // Configure WebView
        setupWebView();

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

    private String doPost(String orderInfo, String amount) throws IOException, JSONException {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TxnRef = Config.getRandomNumber(8);
        String vnp_IpAddr = "127.0.0.1"; // Default IP for mobile app
        String vnp_TmnCode = Config.vnp_TmnCode;

        int amountInt = Integer.parseInt(amount) * 100;
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amountInt));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");

        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", "260000");
        vnp_Params.put("vnp_Locale", "vn");
        
        vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Build data to hash and querystring
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (int i = 0; i < fieldNames.size(); i++) {
            String fieldName = fieldNames.get(i);
            String fieldValue = vnp_Params.get(fieldName);
            if (fieldValue != null && fieldValue.length() > 0) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (i < fieldNames.size() - 1) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;

        // Create JSON response
        JSONObject job = new JSONObject();
        job.put("code", "00");
        job.put("message", "success");
        job.put("data", paymentUrl);
        
        // Return the payment URL
        return paymentUrl;
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

    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("vnpay_return")) {
                    // Handle return URL
                    handlePaymentResult(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                Toast.makeText(ScanVNPayActivity.this, 
                    "Lỗi tải trang thanh toán", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handlePaymentResult(String url) {
        if (url.contains("vnp_ResponseCode=00")) {
            Toast.makeText(this, "Thanh toán thành công", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Thanh toán thất bại", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void loadPaymentUrl(String paymentUrl) {
        String card_number_mask = "9704198526191432198";
        String cardHolder = "NGUYEN VAN A";
        String cardDate = "07/15";
        runOnUiThread(() -> {
            webView.setVisibility(View.VISIBLE);
            viewFinder.setVisibility(View.GONE);
            btnFlash.setVisibility(View.GONE);
            webView.loadUrl(paymentUrl);

            // Inject JavaScript after page load
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    // Auto-fill card information
                    String js = "javascript:(function() {" +
                            "document.getElementById('card_number_mask').value = '" + card_number_mask + "';" +
                            "document.getElementById('cardHolder').value = '" + cardHolder + "';" +
                            "document.getElementById('cardDate').value = '" + cardDate + "';" +
                            "})()";
                    webView.evaluateJavascript(js, null);
                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    String url = request.getUrl().toString();
                    if (url.contains("vnpay_return")) {
                        handlePaymentResult(url);
                        return true;
                    }
                    return false;
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    super.onReceivedError(view, request, error);
                    Toast.makeText(ScanVNPayActivity.this, 
                        "Lỗi tải trang thanh toán", Toast.LENGTH_SHORT).show();
                }
            });
        });
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
                            String[] valueSplit = value.split(" ", 4);
                            String orderInfo = valueSplit[3].substring(41, valueSplit[3].length() - 8);
                            String amount = valueSplit[0].substring(64, valueSplit[0].length() - 13);
                            try {
                                String paymentUrl = doPost(orderInfo, amount);
                                Log.d(TAG, "Payment URL: " + paymentUrl);
                                loadPaymentUrl(paymentUrl);
                            } catch (IOException | JSONException e) {
                                Log.e(TAG, "Error creating payment URL", e);
                                Toast.makeText(ScanVNPayActivity.this,
                                    "Lỗi tạo URL thanh toán", Toast.LENGTH_SHORT).show();
                            }
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