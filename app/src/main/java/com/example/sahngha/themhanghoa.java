package com.example.sahngha;

import androidx.activity.result.ActivityResultLauncher; // Mới thêm
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Mới thêm
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.journeyapps.barcodescanner.ScanContract; // Mới thêm
import com.journeyapps.barcodescanner.ScanOptions;   // Mới thêm

import java.util.HashMap;
import java.util.Map;

public class themhanghoa extends AppCompatActivity {

    // Khai báo biến
    EditText edtMaHangHoa, edtTenHangHoa, edtGia, edtSoLuong, edtTenNCC, edtEmailNCC, edtSdtNCC;
    Spinner spnLoaiHangHoa;

    Button btnXacNhan, btnHuy, btnChonAnh;
    ImageButton btnScanBarcode; // Nút quét mã vạch
    ImageView imgHangHoa;
    DatabaseReference hangHoaRef;
    Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    // --- KHỞI TẠO LAUNCHER QUÉT MÃ VẠCH ---
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    // Quét thành công -> Điền mã vào ô nhập
                    edtMaHangHoa.setText(result.getContents());
                    Toast.makeText(this, "Đã quét mã: " + result.getContents(), Toast.LENGTH_SHORT).show();
                }
            });
    // ---------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_themhanghoa);

        // KHỞI TẠO CLOUDINARY
        try {
            if (MediaManager.get() == null) {
                Map config = new HashMap();
                config.put("cloud_name", "dbrussgnn");
                config.put("api_key", "567682869669221");
                config.put("api_secret", "DRa4brtFZuilZvICiZSkjnGfRRY");
                MediaManager.init(this, config);
            }
        } catch (Exception e) {
            Log.e("Cloudinary", "Lỗi khởi tạo: " + e.getMessage());
        }

        // ÁNH XẠ VIEW
        edtMaHangHoa = findViewById(R.id.edtMaHangHoa);
        btnScanBarcode = findViewById(R.id.btnScanBarcode); // Ánh xạ nút quét

        edtTenHangHoa = findViewById(R.id.edtTenHangHoa);
        spnLoaiHangHoa = findViewById(R.id.spnLoaiHangHoa);

        // Cấu hình Spinner
        String[] loaiHangItems = new String[]{"Nước ngọt", "Bánh", "Kẹo", "Đồ ăn vặt"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, loaiHangItems);
        spnLoaiHangHoa.setAdapter(adapter);

        edtGia = findViewById(R.id.edtGia);
        edtSoLuong = findViewById(R.id.edtSoLuong);
        edtTenNCC = findViewById(R.id.edtTenNCC);
        edtEmailNCC = findViewById(R.id.edtEmailNCC);
        edtSdtNCC = findViewById(R.id.edtSdtNCC);

        btnXacNhan = findViewById(R.id.btnXacNhan);
        btnHuy = findViewById(R.id.btnHuy);
        btnChonAnh = findViewById(R.id.btnChonAnh);
        imgHangHoa = findViewById(R.id.imgHangHoa);

        // KHỞI TẠO FIREBASE
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        hangHoaRef = database.getReference("hanghoa");

        // SỰ KIỆN CÁC NÚT
        btnChonAnh.setOnClickListener(v -> openGallery());

        // --- SỰ KIỆN NÚT QUÉT MÃ ---
        btnScanBarcode.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Hướng camera vào mã vạch");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true); // Khóa xoay dọc
            options.setCaptureActivity(ManHinhQuetDoc.class); // Sử dụng màn hình dọc (nếu đã tạo class này)
            barcodeLauncher.launch(options);
        });
        // ---------------------------

        btnXacNhan.setOnClickListener(v -> themHangHoa());
        btnHuy.setOnClickListener(v -> finish());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgHangHoa.setImageURI(imageUri);
        }
    }

    private void themHangHoa() {
        String ma = edtMaHangHoa.getText().toString().trim();
        String ten = edtTenHangHoa.getText().toString().trim();
        String loai = spnLoaiHangHoa.getSelectedItem().toString();
        String giaStr = edtGia.getText().toString().trim();
        String soLuongStr = edtSoLuong.getText().toString().trim();
        String tenNCC = edtTenNCC.getText().toString().trim();
        String email = edtEmailNCC.getText().toString().trim();
        String sdt = edtSdtNCC.getText().toString().trim();

        if (ma.isEmpty() || ten.isEmpty() || giaStr.isEmpty() || soLuongStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        double gia = Double.parseDouble(giaStr);
        int soLuong = Integer.parseInt(soLuongStr);

        hangHoaRef.child(ma).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Toast.makeText(this, "Mã hàng hóa đã tồn tại!", Toast.LENGTH_SHORT).show();
            } else {
                if (imageUri != null) {
                    Toast.makeText(this, "Đang tải ảnh...", Toast.LENGTH_SHORT).show();
                    uploadImageToCloudinary(ma, ten, loai, gia, soLuong, tenNCC, email, sdt);
                } else {
                    themhanghoavaodb(ma, ten, loai, gia, soLuong, "", tenNCC, email, sdt);
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi kiểm tra mã: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    private void uploadImageToCloudinary(String ma, String ten, String loai, double gia, int soLuong,
                                         String tenNCC, String email, String sdt) {
        String PRESET_NAME = "ml_default";
        MediaManager.get().upload(imageUri).unsigned(PRESET_NAME)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { btnXacNhan.setEnabled(false); }
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override
                    public void onSuccess(String requestId, java.util.Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        themhanghoavaodb(ma, ten, loai, gia, soLuong, imageUrl, tenNCC, email, sdt);
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Toast.makeText(themhanghoa.this, "Lỗi tải ảnh", Toast.LENGTH_SHORT).show();
                        btnXacNhan.setEnabled(true);
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    private void themhanghoavaodb(String ma, String ten, String loai, double gia, int soLuong,
                                  String imageUrl, String tenNCC, String email, String sdt) {
        HangHoa hangHoa = new HangHoa(ma, ten, loai, gia, soLuong, imageUrl, tenNCC, email, sdt);
        hangHoaRef.child(ma).setValue(hangHoa)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Thêm thành công!", Toast.LENGTH_SHORT).show();
                    btnXacNhan.setEnabled(true);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu DB", Toast.LENGTH_SHORT).show();
                    btnXacNhan.setEnabled(true);
                });
    }
}