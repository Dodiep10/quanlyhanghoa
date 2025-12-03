package com.example.sahngha;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter; // Thêm
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner; // Thêm
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

import java.util.ArrayList; // Thêm
import java.util.HashMap;
import java.util.Map;

public class SuaHangHoaActivity extends AppCompatActivity {

    // View
    private ImageView imgHinhAnh;
    private TextInputEditText edtTen, edtGia, edtSoLuong; // Bỏ edtLoai
    private Spinner spnLoai; // [THAY ĐỔI] Dùng Spinner thay vì EditText
    private EditText edtTenNCC, edtEmailNCC, edtSdtNCC;
    private Button btnLuu, btnHuy;

    // Firebase
    private DatabaseReference mRef;
    private DatabaseReference loaiHangRef; // [MỚI] Ref lấy danh sách loại

    private String maHangHoa;
    private String linkAnhCu = "";
    private Uri uriAnhMoi = null;
    private String loaiHangHienTai = ""; // Biến lưu loại hàng cũ để set selection

    // Spinner Data
    private ArrayList<String> loaiHangList;
    private ArrayAdapter<String> adapterLoai;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ProgressDialog progressDialog;

    // === CẤU HÌNH CLOUDINARY ===
    private static final String CLOUD_NAME = "dbrussgnn";
    private static final String UPLOAD_PRESET = "ml_default";
    private static final String API_KEY = "567682869669221";
    private static final String API_SECRET = "DRa4brtFZuilZvICiZSkjnGfRRY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_hang_hoa);

        initCloudinary();
        initViews();

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false);

        // 1. Nhận mã
        Intent intent = getIntent();
        if (intent != null) maHangHoa = intent.getStringExtra("MA_HANG_HOA");
        if (maHangHoa == null) { finish(); return; }

        // 2. Kết nối Firebase
        FirebaseDatabase db = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        mRef = db.getReference("hanghoa").child(maHangHoa);
        loaiHangRef = db.getReference("loaihanghoa"); // [MỚI]

        // 3. Cấu hình Spinner
        setupSpinner();

        // 4. Load danh sách loại hàng TRƯỚC, sau đó load dữ liệu hàng
        layDanhSachLoaiHang();

        // 5. Setup chọn ảnh và nút bấm
        setupImagePicker();
        imgHinhAnh.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnLuu.setOnClickListener(v -> xuLyLuu());
        btnHuy.setOnClickListener(v -> finish());
    }

    private void initCloudinary() {
        try {
            if (MediaManager.get() == null) {
                Map<String, Object> config = new HashMap<>();
                config.put("cloud_name", CLOUD_NAME);
                config.put("api_key", API_KEY);
                config.put("api_secret", API_SECRET);
                config.put("secure", true);
                MediaManager.init(this, config);
            }
        } catch (Exception e) {
            Log.d("Cloudinary", "Init error: " + e.getMessage());
        }
    }

    private void initViews() {
        imgHinhAnh = findViewById(R.id.imgSuaHinhAnh);
        edtTen = findViewById(R.id.edtSuaTen);
        // edtLoai = findViewById(R.id.edtSuaLoai); // XÓA
        spnLoai = findViewById(R.id.spnSuaLoai); // [MỚI] Ánh xạ Spinner (Nhớ sửa ID trong XML)

        edtGia = findViewById(R.id.edtSuaGia);
        edtSoLuong = findViewById(R.id.edtSuaSoLuong);
        edtTenNCC = findViewById(R.id.edtSuaTenNCC);
        edtEmailNCC = findViewById(R.id.edtSuaEmailNCC);
        edtSdtNCC = findViewById(R.id.edtSuaSdtNCC);
        btnLuu = findViewById(R.id.btnLuuThayDoi);
        btnHuy = findViewById(R.id.btnHuySua);
    }

    private void setupSpinner() {
        loaiHangList = new ArrayList<>();
        adapterLoai = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, loaiHangList);
        spnLoai.setAdapter(adapterLoai);
    }

    // [MỚI] Hàm lấy danh sách loại để đồng bộ
    private void layDanhSachLoaiHang() {
        loaiHangRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                loaiHangList.clear();
                // Thêm mặc định
                loaiHangList.add("Đồ ăn vặt");
                loaiHangList.add("Nước ngọt");
                loaiHangList.add("Bánh");
                loaiHangList.add("Kẹo");

                for (DataSnapshot data : snapshot.getChildren()) {
                    String tenLoai = data.child("tenLoai").getValue(String.class);
                    if (tenLoai != null && !loaiHangList.contains(tenLoai)) {
                        loaiHangList.add(tenLoai);
                    }
                }
                adapterLoai.notifyDataSetChanged();

                // Sau khi có danh sách loại, mới load dữ liệu hàng hóa để set selection đúng
                loadDuLieuCu();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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
                        edtGia.setText(String.valueOf((long)hh.getGia()));
                        edtSoLuong.setText(String.valueOf(hh.getSoLuong()));
                        edtTenNCC.setText(hh.getTenNCC());
                        edtEmailNCC.setText(hh.getEmailNCC());
                        edtSdtNCC.setText(hh.getSdtNCC());

                        linkAnhCu = hh.getHinhAnh();
                        if (linkAnhCu != null && !linkAnhCu.isEmpty()) {
                            Glide.with(SuaHangHoaActivity.this).load(linkAnhCu).into(imgHinhAnh);
                        }

                        // [QUAN TRỌNG] Set loại hàng cũ vào Spinner
                        loaiHangHienTai = hh.getLoaiHangHoa();
                        if (loaiHangHienTai != null) {
                            int viTri = adapterLoai.getPosition(loaiHangHienTai);
                            if (viTri >= 0) {
                                spnLoai.setSelection(viTri);
                            }
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
        btnLuu.setEnabled(false);

        if (uriAnhMoi != null) {
            uploadAnhLenCloudinary();
        } else {
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
                        String urlMoi = (String) resultData.get("secure_url");
                        luuVaoDatabase(urlMoi);
                    }
                    @Override
                    public void onError(String requestId, ErrorInfo errorInfo) {
                        progressDialog.dismiss();
                        btnLuu.setEnabled(true);
                        Toast.makeText(SuaHangHoaActivity.this, "Lỗi upload ảnh", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onReschedule(String requestId, ErrorInfo errorInfo) { }
                })
                .dispatch();
    }

    private void luuVaoDatabase(String urlHinhAnh) {
        String ten = edtTen.getText().toString().trim();

        // [THAY ĐỔI] Lấy dữ liệu từ Spinner
        String loai = "";
        if (spnLoai.getSelectedItem() != null) {
            loai = spnLoai.getSelectedItem().toString();
        }

        double gia = 0;
        int soLuong = 0;
        try {
            gia = Double.parseDouble(edtGia.getText().toString().trim());
            soLuong = Integer.parseInt(edtSoLuong.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Lỗi số liệu", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
            btnLuu.setEnabled(true);
            return;
        }

        String tenNCC = edtTenNCC.getText().toString().trim();
        String emailNCC = edtEmailNCC.getText().toString().trim();
        String sdtNCC = edtSdtNCC.getText().toString().trim();

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("ten", ten);
        updateMap.put("loaiHangHoa", loai); // Lưu loại mới chọn
        updateMap.put("gia", gia);
        updateMap.put("soLuong", soLuong);
        updateMap.put("hinhAnh", urlHinhAnh);
        updateMap.put("tenNCC", tenNCC);
        updateMap.put("email", emailNCC); // Chú ý key cho khớp với Model (email hay emailNCC)
        updateMap.put("sdt", sdtNCC);     // Chú ý key cho khớp (sdt hay sdtNCC)

        mRef.updateChildren(updateMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(SuaHangHoaActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    btnLuu.setEnabled(true);
                    Toast.makeText(SuaHangHoaActivity.this, "Lỗi lưu DB", Toast.LENGTH_SHORT).show();
                });
    }
}