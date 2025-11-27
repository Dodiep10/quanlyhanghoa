package com.example.sahngha;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class SuaHangHoaActivity extends AppCompatActivity {

    // View
    private ImageView imgHinhAnh;
    private TextInputEditText edtTen, edtLoai, edtGia, edtSoLuong;
    private EditText edtTenNCC, edtEmailNCC, edtSdtNCC;
    private Button btnLuu, btnHuy;

    // Firebase
    private DatabaseReference mRef;

    private String maHangHoa;
    private String linkAnhCu = "";
    private Uri uriAnhMoi = null;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ProgressDialog progressDialog;

    // === CẤU HÌNH CLOUDINARY (Đã lấy từ file themhanghoa của bạn) ===
    private static final String CLOUD_NAME = "dbrussgnn";
    private static final String UPLOAD_PRESET = "ml_default";
    private static final String API_KEY = "567682869669221";
    private static final String API_SECRET = "DRa4brtFZuilZvICiZSkjnGfRRY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_hang_hoa);

        // Khởi tạo Cloudinary
        initCloudinary();

        initViews();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        // 1. Nhận mã
        Intent intent = getIntent();
        if (intent != null) maHangHoa = intent.getStringExtra("MA_HANG_HOA");

        if (maHangHoa == null) { finish(); return; }

        // 2. Kết nối Firebase Realtime
        mRef = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("hanghoa").child(maHangHoa);

        // 3. Load dữ liệu
        loadDuLieuCu();

        // 4. Chọn ảnh
        setupImagePicker();
        imgHinhAnh.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // 5. Lưu
        btnLuu.setOnClickListener(v -> xuLyLuu());
        btnHuy.setOnClickListener(v -> finish());
    }

    private void initCloudinary() {
        try {
            // Kiểm tra xem đã init chưa để tránh crash
            if (MediaManager.get() == null) {
                Map<String, Object> config = new HashMap<>();
                config.put("cloud_name", CLOUD_NAME);
                config.put("api_key", API_KEY);
                config.put("api_secret", API_SECRET);
                config.put("secure", true);
                MediaManager.init(this, config);
            }
        } catch (Exception e) {
            // Nếu đã init ở màn hình khác rồi thì bỏ qua
            Log.d("Cloudinary", "Đã khởi tạo trước đó hoặc lỗi nhẹ: " + e.getMessage());
        }
    }

    private void initViews() {
        imgHinhAnh = findViewById(R.id.imgSuaHinhAnh);
        edtTen = findViewById(R.id.edtSuaTen);
        edtLoai = findViewById(R.id.edtSuaLoai);
        edtGia = findViewById(R.id.edtSuaGia);
        edtSoLuong = findViewById(R.id.edtSuaSoLuong);

        edtTenNCC = findViewById(R.id.edtSuaTenNCC);
        edtEmailNCC = findViewById(R.id.edtSuaEmailNCC);
        edtSdtNCC = findViewById(R.id.edtSuaSdtNCC);

        btnLuu = findViewById(R.id.btnLuuThayDoi);
        btnHuy = findViewById(R.id.btnHuySua);
    }

    private void setupImagePicker() {
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uriAnhMoi = uri;
                        imgHinhAnh.setImageURI(uri);
                    }
                }
        );
    }

    private void loadDuLieuCu() {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HangHoa hh = snapshot.getValue(HangHoa.class);
                    if (hh != null) {
                        edtTen.setText(hh.getTen());
                        edtLoai.setText(hh.getLoaiHangHoa());
                        edtGia.setText(String.valueOf((long)hh.getGia())); // Ép kiểu về long để bỏ số thập phân thừa
                        edtSoLuong.setText(String.valueOf(hh.getSoLuong()));

                        edtTenNCC.setText(hh.getTenNCC());
                        edtEmailNCC.setText(hh.getEmailNCC());
                        edtSdtNCC.setText(hh.getSdtNCC());

                        linkAnhCu = hh.getHinhAnh();
                        if (linkAnhCu != null && !linkAnhCu.isEmpty()) {
                            Glide.with(SuaHangHoaActivity.this).load(linkAnhCu).into(imgHinhAnh);
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void xuLyLuu() {
        if (edtTen.getText().toString().isEmpty()) {
            Toast.makeText(this, "Chưa nhập tên", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();
        // Vô hiệu hóa nút lưu để tránh ấn nhiều lần
        btnLuu.setEnabled(false);

        if (uriAnhMoi != null) {
            // CÓ ẢNH MỚI -> Upload lên Cloudinary trước
            uploadAnhLenCloudinary();
        } else {
            // KHÔNG CÓ ẢNH MỚI -> Dùng link cũ lưu luôn
            luuVaoDatabase(linkAnhCu);
        }
    }

    private void uploadAnhLenCloudinary() {
        MediaManager.get().upload(uriAnhMoi)
                .unsigned(UPLOAD_PRESET)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) { }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        // Lấy link ảnh từ Cloudinary trả về
                        String urlMoi = (String) resultData.get("secure_url");

                        // Gọi hàm lưu vào Firebase
                        luuVaoDatabase(urlMoi);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo errorInfo) {
                        progressDialog.dismiss();
                        btnLuu.setEnabled(true);
                        Toast.makeText(SuaHangHoaActivity.this, "Lỗi upload ảnh: " + errorInfo.getDescription(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo errorInfo) { }
                })
                .dispatch();
    }

    private void luuVaoDatabase(String urlHinhAnh) {
        // Lấy dữ liệu
        String ten = edtTen.getText().toString().trim();
        String loai = edtLoai.getText().toString().trim();

        double gia = 0;
        int soLuong = 0;
        try {
            gia = Double.parseDouble(edtGia.getText().toString().trim());
            soLuong = Integer.parseInt(edtSoLuong.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá hoặc số lượng không hợp lệ", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            btnLuu.setEnabled(true);
            return;
        }

        String tenNCC = edtTenNCC.getText().toString().trim();
        String emailNCC = edtEmailNCC.getText().toString().trim();
        String sdtNCC = edtSdtNCC.getText().toString().trim();

        // Đóng gói updateMap
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("ten", ten);
        updateMap.put("loaiHangHoa", loai);
        updateMap.put("gia", gia);
        updateMap.put("soLuong", soLuong);
        updateMap.put("hinhAnh", urlHinhAnh); // Link ảnh (Cloudinary hoặc cũ)

        // Cần đảm bảo tên key khớp với Firebase (trong themhanghoa bạn dùng: tenNCC, email, sdt)
        updateMap.put("tenNCC", tenNCC);
        updateMap.put("email", emailNCC);
        updateMap.put("sdt", sdtNCC);

        // Update Firebase
        mRef.updateChildren(updateMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(SuaHangHoaActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    btnLuu.setEnabled(true);
                    Toast.makeText(SuaHangHoaActivity.this, "Lỗi lưu DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}