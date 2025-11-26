package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide; // Thư viện load ảnh (cần thêm vào build.gradle)
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class ChiTietHangHoaActivity extends AppCompatActivity {

    // Khai báo các biến View
    private Toolbar toolbar;
    private ImageView imgHinhAnh;
    private TextView tvTen, tvMa, tvLoai, tvGia, tvSoLuong;
    private TextView tvTenNCC, tvEmailNCC, tvSdtNCC;
    private FloatingActionButton fabSua;

    // Firebase
    private DatabaseReference mRef;
    private String receivedMaHangHoa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_hang_hoa); // Đảm bảo tên layout XML đúng

        // 1. Ánh xạ View
        initViews();

        // 2. Thiết lập Toolbar (Nút Back)
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish()); // Quay lại màn hình trước khi ấn nút Back

        // 3. Nhận dữ liệu từ Intent (MainActivity gửi sang)
        Intent intent = getIntent();
        if (intent != null) {
            receivedMaHangHoa = intent.getStringExtra("MA_HANG_HOA");
        }

        // 4. Load dữ liệu từ Firebase
        if (receivedMaHangHoa != null && !receivedMaHangHoa.isEmpty()) {
            loadDataFromFirebase(receivedMaHangHoa);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã hàng hoá", Toast.LENGTH_SHORT).show();
        }

        fabSua.setOnClickListener(v -> {
            // Chuyển sang màn hình Sửa và gửi kèm Mã Hàng Hoá
            Intent intentSua = new Intent(ChiTietHangHoaActivity.this, SuaHangHoaActivity.class);
            intentSua.putExtra("MA_HANG_HOA", receivedMaHangHoa);
            startActivity(intentSua);
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarChiTietMoi);
        imgHinhAnh = findViewById(R.id.imgChiTietHangHoa);

        tvTen = findViewById(R.id.tvChiTietTenHang);
        tvMa = findViewById(R.id.tvChiTietMaHang);
        tvLoai = findViewById(R.id.tvChiTietLoaiHang);

        tvGia = findViewById(R.id.tvChiTietGia);
        tvSoLuong = findViewById(R.id.tvChiTietSoLuong);

        tvTenNCC = findViewById(R.id.tvChiTietTenNCC);
        tvEmailNCC = findViewById(R.id.tvChiTietEmailNCC);
        tvSdtNCC = findViewById(R.id.tvChiTietSdtNCC);

        fabSua = findViewById(R.id.fabSuaHangHoa);
    }

    private void loadDataFromFirebase(String maHangHoa) {
        // Đường dẫn tới node hanghoa -> maHangHoa cụ thể
        mRef = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("hanghoa").child(maHangHoa);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Lấy dữ liệu về object HangHoa
                    HangHoa hh = snapshot.getValue(HangHoa.class);

                    if (hh != null) {
                        // Hiển thị thông tin cơ bản
                        tvTen.setText(hh.getTen());
                        tvMa.setText(hh.getMaHangHoa());
                        tvLoai.setText(hh.getLoaiHangHoa()); // Giả sử model HangHoa có getLoai()
                        tvSoLuong.setText(String.valueOf(hh.getSoLuong()));

                        // Format giá tiền (Ví dụ: 100,000 VND)
                        DecimalFormat formatter = new DecimalFormat("###,###,###");
                        String giaDaFormat = formatter.format(hh.getGia()) + " VND";
                        tvGia.setText(giaDaFormat);

                        // Hiển thị thông tin NCC (Giả sử model HangHoa có các getter này)
                        // Nếu model của bạn lưu NCC dưới dạng object con, hãy sửa lại hh.getNhaCungCap().getTen()...
                        // Dưới đây là ví dụ giả định các trường nằm thẳng trong HangHoa
                        /* Lưu ý: Bạn cần kiểm tra Class HangHoa của bạn có các trường nhaCungCap, emailNCC, sdtNCC không.
                           Nếu không có, hãy thêm vào hoặc comment lại đoạn code dưới đây.
                        */
                        // tvTenNCC.setText(hh.getTenNhaCungCap());
                        // tvEmailNCC.setText(hh.getEmailNhaCungCap());
                        // tvSdtNCC.setText(hh.getSdtNhaCungCap());

                        // Load ảnh bằng Glide
                        if (hh.getHinhAnh() != null && !hh.getHinhAnh().isEmpty()) {
                            Glide.with(ChiTietHangHoaActivity.this)
                                    .load(hh.getHinhAnh())
                                    .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ (tạo trong drawable nếu chưa có)
                                    .error(R.drawable.ic_launcher_background)       // Ảnh lỗi
                                    .into(imgHinhAnh);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChiTietHangHoaActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}