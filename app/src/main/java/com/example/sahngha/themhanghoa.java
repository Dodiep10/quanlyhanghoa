package com.example.sahngha;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class themhanghoa extends AppCompatActivity {

    // Khai báo biến
    EditText edtMaHangHoa, edtTenHangHoa, edtLoaiHangHoa, edtGia, edtSoLuong, edtTenNCC, edtEmailNCC, edtSdtNCC;
    Button btnXacNhan, btnHuy, btnChonAnh;
    ImageView imgHangHoa;
    DatabaseReference hangHoaRef;
    Uri imageUri;
    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_themhanghoa);

        // ======== KHỞI TẠO CLOUDINARY (BẮT BUỘC) ========
        try {
            if (MediaManager.get() == null) {
                Map config = new HashMap();
                config.put("cloud_name", "dbrussgnn");
                config.put("api_key", "567682869669221");
                config.put("api_secret", "DRa4brtFZuilZvICiZSkjnGfRRY");
                MediaManager.init(this, config);
                Log.d("Cloudinary", "Đã khởi tạo MediaManager thành công!");
            }
        } catch (Exception e) {
            Log.e("Cloudinary", "Lỗi khởi tạo: " + e.getMessage());
        }

        // ======== ÁNH XẠ VIEW ========
        edtMaHangHoa = findViewById(R.id.edtMaHangHoa);
        edtTenHangHoa = findViewById(R.id.edtTenHangHoa);
        edtLoaiHangHoa = findViewById(R.id.edtLoaiHangHoa);
        edtGia = findViewById(R.id.edtGia);
        edtSoLuong = findViewById(R.id.edtSoLuong);
        edtTenNCC = findViewById(R.id.edtTenNCC);
        edtEmailNCC = findViewById(R.id.edtEmailNCC);
        edtSdtNCC = findViewById(R.id.edtSdtNCC);

        btnXacNhan = findViewById(R.id.btnXacNhan);
        btnHuy = findViewById(R.id.btnHuy);
        btnChonAnh = findViewById(R.id.btnChonAnh);
        imgHangHoa = findViewById(R.id.imgHangHoa);

        // ======== KHỞI TẠO FIREBASE ========
        FirebaseDatabase database = FirebaseDatabase.getInstance(
                "https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/"
        );
        hangHoaRef = database.getReference("hanghoa");


        // ======== SỰ KIỆN NÚT ========
        btnChonAnh.setOnClickListener(v -> openGallery());
        btnXacNhan.setOnClickListener(v -> themHangHoa());
        btnHuy.setOnClickListener(v -> finish());
    }

    // ======== MỞ THƯ VIỆN CHỌN ẢNH ========
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // ======== NHẬN KẾT QUẢ ẢNH ========
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imgHangHoa.setImageURI(imageUri);
        }
    }

    // ======== XỬ LÝ THÊM HÀNG HÓA ========
    private void themHangHoa() {
        String ma = edtMaHangHoa.getText().toString().trim();
        String ten = edtTenHangHoa.getText().toString().trim();
        String loai = edtLoaiHangHoa.getText().toString().trim();
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

        // ======== KIỂM TRA MÃ TRÙNG ========
        hangHoaRef.child(ma).get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                Toast.makeText(this, "Mã hàng hóa đã tồn tại!", Toast.LENGTH_SHORT).show();
            } else {
                if (imageUri != null) {
                    Toast.makeText(this, "Đang tải ảnh lên Cloudinary...", Toast.LENGTH_SHORT).show();
                    uploadImageToCloudinary(ma, ten, loai, gia, soLuong, tenNCC, email, sdt);
                } else {
                    themhanghoavaodb(ma, ten, loai, gia, soLuong, "", tenNCC, email, sdt);
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Lỗi khi kiểm tra mã: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }

    // ======== TẢI ẢNH LÊN CLOUDINARY ========
    private void uploadImageToCloudinary(String ma, String ten, String loai, double gia, int soLuong,
                                         String tenNCC, String email, String sdt) {

        String PRESET_NAME = "ml_default";

        MediaManager.get().upload(imageUri).unsigned(PRESET_NAME)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Bắt đầu tải lên...");
                        btnXacNhan.setEnabled(false);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {}

                    @Override
                    public void onSuccess(String requestId, java.util.Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Log.d("Cloudinary", "Tải ảnh thành công: " + imageUrl);
                        themhanghoavaodb(ma, ten, loai, gia, soLuong, imageUrl, tenNCC, email, sdt);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "Lỗi tải ảnh: " + error.getDescription());
                        Toast.makeText(themhanghoa.this, "Lỗi tải ảnh: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        btnXacNhan.setEnabled(true);
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}
                })
                .dispatch();
    }

    // ======== LƯU DỮ LIỆU VÀO FIREBASE ========
    private void themhanghoavaodb(String ma, String ten, String loai, double gia, int soLuong,
                                  String imageUrl, String tenNCC, String email, String sdt) {

        HangHoa hangHoa = new HangHoa(ma, ten, loai, gia, soLuong, imageUrl, tenNCC, email, sdt);

        hangHoaRef.child(ma).setValue(hangHoa)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Thêm hàng hóa thành công!", Toast.LENGTH_SHORT).show();
                    btnXacNhan.setEnabled(true);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi lưu DB: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnXacNhan.setEnabled(true);
                });
    }
}
