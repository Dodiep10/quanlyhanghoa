package com.example.sahngha;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull; // Thêm
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DataSnapshot; // Thêm
import com.google.firebase.database.DatabaseError; // Thêm
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener; // Thêm
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList; // Thêm
import java.util.HashMap;
import java.util.Map;

public class themhanghoa extends AppCompatActivity {

    // Khai báo biến
    EditText edtMaHangHoa, edtTenHangHoa, edtGia, edtSoLuong, edtTenNCC, edtEmailNCC, edtSdtNCC;
    Spinner spnLoaiHangHoa;

    Button btnXacNhan, btnHuy, btnChonAnh;
    ImageButton btnScanBarcode;
    ImageView imgHangHoa;

    DatabaseReference hangHoaRef;
    DatabaseReference loaiHangRef;

    Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    // [MỚI] Danh sách động cho Spinner
    ArrayList<String> loaiHangList;
    ArrayAdapter<String> adapterLoaiHang;

    // --- KHỞI TẠO LAUNCHER QUÉT MÃ VẠCH ---
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
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
        btnScanBarcode = findViewById(R.id.btnScanBarcode);

        edtTenHangHoa = findViewById(R.id.edtTenHangHoa);
        spnLoaiHangHoa = findViewById(R.id.spnLoaiHangHoa);

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
        loaiHangRef = database.getReference("loaihanghoa"); // [MỚI]

        // [CẬP NHẬT] CẤU HÌNH SPINNER ĐỘNG
        loaiHangList = new ArrayList<>();
        adapterLoaiHang = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, loaiHangList);
        spnLoaiHangHoa.setAdapter(adapterLoaiHang);

        // Gọi hàm lấy dữ liệu loại hàng
        layDanhSachLoaiHangTuFirebase();

        // SỰ KIỆN CÁC NÚT
        btnChonAnh.setOnClickListener(v -> openGallery());

        btnScanBarcode.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setPrompt("Hướng camera vào mã vạch");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(ManHinhQuetDoc.class);
            barcodeLauncher.launch(options);
        });

        btnXacNhan.setOnClickListener(v -> themHangHoa());
        btnHuy.setOnClickListener(v -> finish());
    }

    // [MỚI] HÀM LẤY DANH SÁCH LOẠI TỪ FIREBASE VÀ ĐỒNG BỘ VÀO SPINNER
    private void layDanhSachLoaiHangTuFirebase() {
        loaiHangRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loaiHangList.clear();

                // 1. Thêm các loại mặc định (Giống bên MainActivity để đồng bộ)
                loaiHangList.add("Đồ ăn vặt");
                loaiHangList.add("Nước ngọt");
                loaiHangList.add("Bánh");
                loaiHangList.add("Kẹo");
                // Bạn có thể thêm "Sách", "Home" vào đây nếu muốn mặc định có sẵn

                // 2. Thêm các loại mới từ Firebase
                for (DataSnapshot data : snapshot.getChildren()) {
                    String tenLoai = data.child("tenLoai").getValue(String.class);
                    // Kiểm tra null và tránh trùng lặp với danh sách mặc định
                    if (tenLoai != null && !loaiHangList.contains(tenLoai)) {
                        loaiHangList.add(tenLoai);
                    }
                }
                // Cập nhật giao diện Spinner
                adapterLoaiHang.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(themhanghoa.this, "Lỗi tải danh mục", Toast.LENGTH_SHORT).show();
            }
        });
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

        // Lấy loại từ Spinner (Lúc này Spinner đã có dữ liệu từ Firebase)
        String loai = "";
        if (spnLoaiHangHoa.getSelectedItem() != null) {
            loai = spnLoaiHangHoa.getSelectedItem().toString();
        }

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

        String finalLoai = loai;
        hangHoaRef.child(ma).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Toast.makeText(this, "Mã hàng hóa đã tồn tại!", Toast.LENGTH_SHORT).show();
            } else {
                if (imageUri != null) {
                    Toast.makeText(this, "Đang tải ảnh...", Toast.LENGTH_SHORT).show();
                    uploadImageToCloudinary(ma, ten, finalLoai, gia, soLuong, tenNCC, email, sdt);
                } else {
                    themhanghoavaodb(ma, ten, finalLoai, gia, soLuong, "", tenNCC, email, sdt);
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