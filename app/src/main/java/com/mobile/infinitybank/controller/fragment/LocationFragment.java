package com.mobile.infinitybank.controller.fragment;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mobile.infinitybank.databinding.FragmentLocationBinding;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class LocationFragment extends Fragment implements OnMapReadyCallback {

    private FragmentLocationBinding binding;
    private GoogleMap mMap;
    private static final String TAG = "LocationFragment";

    // Danh sách chi nhánh ngân hàng mẫu (latitude, longitude)
    private final List<LatLng> bankBranches = new ArrayList<LatLng>() {{
        add(new LatLng(10.7769, 106.7009)); // Chi nhánh 1 - TP.HCM
        add(new LatLng(10.7869, 106.7109)); // Chi nhánh 2
        add(new LatLng(10.7669, 106.6909)); // Chi nhánh 3
    }};

    // Danh sách các địa điểm Nguyễn Thị Thập (dựa trên dữ liệu thực tế tại TP. HCM)
    private final List<Address> nguyenThiThapLocations = new ArrayList<Address>() {{
        Address address1 = new Address(Locale.getDefault());
        address1.setLatitude(10.735); // 5 Nguyễn Thị Thập, Quận 7 (ước lượng)
        address1.setLongitude(106.716);
        address1.setFeatureName("5 Nguyễn Thị Thập");
        address1.setThoroughfare("Nguyễn Thị Thập");
        address1.setLocality("Quận 7");
        add(address1);

        Address address2 = new Address(Locale.getDefault());
        address2.setLatitude(10.738); // Một địa điểm khác trên Nguyễn Thị Thập
        address2.setLongitude(106.718);
        address2.setFeatureName("10 Nguyễn Thị Thập");
        address2.setThoroughfare("Nguyễn Thị Thập");
        address2.setLocality("Quận 7");
        add(address2);

        Address address3 = new Address(Locale.getDefault());
        address3.setLatitude(10.740); // Địa điểm khác (ví dụ)
        address3.setLongitude(106.720);
        address3.setFeatureName("15 Nguyễn Thị Thập");
        address3.setThoroughfare("Nguyễn Thị Thập");
        address3.setLocality("Quận 7");
        add(address3);
    }};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLocationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tvLocationTitle.setText("Vị trí");

        binding.mapView.onCreate(savedInstanceState);
        binding.mapView.getMapAsync(this);

        // Xử lý khi nhấn nút "Tìm địa chỉ"
        binding.btnSearchAddress.setOnClickListener(v -> {
            String addressInput = binding.etAddress.getText().toString().trim();
            if (!addressInput.isEmpty()) {
                searchAddress(addressInput);
            } else {
                Log.w(TAG, "Address input is empty, using default location");
                binding.tvRouteInfo.setText("Vui lòng nhập địa chỉ!");
                useDefaultLocation();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Map is ready");

        // Hiển thị vị trí mặc định ban đầu
        useDefaultLocation();
    }

    private void useDefaultLocation() {
        LatLng defaultLocation = new LatLng(10.7769, 106.7009); // TP.HCM làm mặc định
        Log.d(TAG, "Using default location: " + defaultLocation.latitude + ", " + defaultLocation.longitude);
        updateMapWithLocation(defaultLocation);
        binding.tvRouteInfo.setText("Sử dụng vị trí mặc định TP.HCM!");
    }

    private void searchAddress(String addressInput) {
        // Chuẩn hóa chuỗi nhập vào thành không dấu
        String normalizedInput = removeDiacritics(addressInput).toLowerCase();
        Log.d(TAG, "Normalized input: " + normalizedInput);

        try {
            // Tìm các địa điểm có chứa "Nguyễn Thị Thập"
            List<Address> matchingAddresses = new ArrayList<>();
            for (Address address : nguyenThiThapLocations) {
                String normalizedThoroughfare = removeDiacritics(address.getThoroughfare() != null ? address.getThoroughfare() : "").toLowerCase();
                if (normalizedThoroughfare.contains("nguyen thi thap")) {
                    matchingAddresses.add(address);
                }
            }

            if (!matchingAddresses.isEmpty()) {
                // Ưu tiên địa điểm gần nhất với "5 Nguyễn Thị Thập, Quận 7"
                Address nearestAddress = null;
                double minDistance = Double.MAX_VALUE;
                LatLng referencePoint = new LatLng(10.735, 106.716); // Tọa độ ước lượng 5 Nguyễn Thị Thập, Quận 7

                for (Address address : matchingAddresses) {
                    LatLng addressLocation = new LatLng(address.getLatitude(), address.getLongitude());
                    double distance = calculateDistance(referencePoint, addressLocation);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestAddress = address;
                    }
                }

                if (nearestAddress != null) {
                    LatLng userLocation = new LatLng(nearestAddress.getLatitude(), nearestAddress.getLongitude());
                    Log.d(TAG, "Found location: " + nearestAddress.getFeatureName() + ", " + nearestAddress.getLatitude() + ", " + nearestAddress.getLongitude());
                    updateMapWithLocation(userLocation);
                    binding.tvRouteInfo.setText("Đã tìm thấy: " + nearestAddress.getFeatureName() + ", Quận 7");
                } else {
                    Log.w(TAG, "No valid location found, using default");
                    useDefaultLocation();
                    binding.tvRouteInfo.setText("Không tìm thấy địa chỉ hợp lệ. Sử dụng vị trí mặc định!");
                }
            } else {
                Log.w(TAG, "No matching addresses found, using default");
                useDefaultLocation();
                binding.tvRouteInfo.setText("Không tìm thấy địa chỉ phù hợp. Sử dụng vị trí mặc định!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Geocoder error: " + e.getMessage());
            useDefaultLocation();
            binding.tvRouteInfo.setText("Lỗi khi tìm địa chỉ. Sử dụng vị trí mặc định!");
        }
    }

    // Hàm chuẩn hóa chuỗi có dấu thành không dấu
    private String removeDiacritics(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    private void updateMapWithLocation(LatLng userLocation) {
        // Thêm marker cho vị trí người dùng
        mMap.clear(); // Xóa marker cũ để tránh chồng chéo
        mMap.addMarker(new MarkerOptions()
                .position(userLocation)
                .title("Vị trí của bạn")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        // Thêm marker cho các chi nhánh ngân hàng
        for (int i = 0; i < bankBranches.size(); i++) {
            LatLng branch = bankBranches.get(i);
            mMap.addMarker(new MarkerOptions()
                    .position(branch)
                    .title("Chi nhánh " + (i + 1)));
        }

        // Tìm chi nhánh gần nhất và đề xuất lộ trình
        LatLng nearestBranch = findNearestBranch(userLocation);
        suggestRoute(userLocation, nearestBranch);

        // Zoom vào vị trí người dùng
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f));
    }

    private LatLng findNearestBranch(LatLng userLocation) {
        double minDistance = Double.MAX_VALUE;
        LatLng nearestBranch = new LatLng(0.0, 0.0);

        for (LatLng branch : bankBranches) {
            double distance = calculateDistance(userLocation, branch);
            if (distance < minDistance) {
                minDistance = distance;
                nearestBranch = branch;
            }
        }
        return nearestBranch;
    }

    private double calculateDistance(LatLng point1, LatLng point2) {
        double lat1 = Math.toRadians(point1.latitude);
        double lon1 = Math.toRadians(point1.longitude);
        double lat2 = Math.toRadians(point2.latitude);
        double lon2 = Math.toRadians(point2.longitude);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;
        double a = Math.pow(Math.sin(dlat / 2), 2.0) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2.0);
        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6371.0; // Bán kính Trái Đất (km)
        return c * r;
    }

    private void suggestRoute(LatLng start, LatLng end) {
        double distance = calculateDistance(start, end);
        String routeInfo = "Đề xuất tuyến đường:\n" +
                "Điểm đến: Chi nhánh gần nhất\n" +
                "Khoảng cách: " + String.format("%.2f", distance) + " km\n" +
                "Thời gian ước tính: " + String.format("%.0f", distance * 15) + " phút (tốc độ trung bình 4km/h)";

        binding.tvRouteInfo.setText(routeInfo);

        // Vẽ đường đi đơn giản bằng Polyline
        mMap.addPolyline(new PolylineOptions()
                .add(start, end)
                .width(5f)
                .color(0xFF0288D1));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.mapView.onDestroy();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        binding.mapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }
} 