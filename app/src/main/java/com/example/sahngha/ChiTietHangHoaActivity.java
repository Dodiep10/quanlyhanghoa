package com.example.sahngha; // Đảm bảo đúng package của bạn

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

// Đây là file Activity đã cập nhật để dùng layout MỚI
public class ChiTietHangHoaActivity extends AppCompatActivity {

    // (Khai báo biến... giữ nguyên)
    TextView tvChiTietMaHang, tvChiTietTenHang, tvChiTietLoaiHang, tvChiTietGia, tvChiTietSoLuong,
            tvChiTietTenNCC, tvChiTietEmailNCC, tvChiTietSdtNCC;
    FloatingActionButton fabSuaHangHoa;
    Toolbar toolbarChiTietMoi;
    DatabaseReference hangHoaRef;
    String maHangHoaKey;
    ImageView imgChiTietHangHoa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_hang_hoa); // Đảm bảo tên layout khớp

        anhXaViews();

        // Cài đặt Toolbar
        setSupportActionBar(toolbarChiTietMoi);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Sự kiện cho nút back (mũi tên) -> finish() là ĐÚNG
        toolbarChiTietMoi.setNavigationOnClickListener(v -> finish());

        // (Code lấy maHangHoaKey... giữ nguyên)
        maHangHoaKey = getIntent().getStringExtra("MA_HANG_HOA");
        if (maHangHoaKey == null || maHangHoaKey.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã hàng hóa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // (Code khởi tạo Firebase... giữ nguyên)
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        hangHoaRef = database.getReference("hanghoa").child(maHangHoaKey);

        // Tải dữ liệu
        loadDataFromFirebase();

        // *** CODE QUAN TRỌNG ĐÂY ***
        // Thêm sự kiện click cho "Nút Cây Bút" (FAB)
        // File cũ của bạn bị thiếu đoạn code này
        fabSuaHangHoa.setOnClickListener(v -> {
            // Tạo Intent để mở màn hình Sửa
            Intent intent = new Intent(ChiTietHangHoaActivity.this, SuaHangHoaActivity.class);

            // Gửi "Mã Hàng Hóa" qua cho màn hình Sửa (để màn hình Sửa biết tải gì)
            intent.putExtra("MA_HANG_HOA", maHangHoaKey);

            // Mở màn hình Sửa
            startActivity(intent);
        });

    } // <-- Dấu } này đóng hàm onCreate


    private void anhXaViews() {
        tvChiTietMaHang = findViewById(R.id.tvChiTietMaHang);
        tvChiTietTenHang = findViewById(R.id.tvChiTietTenHang);
        tvChiTietLoaiHang = findViewById(R.id.tvChiTietLoaiHang);
        tvChiTietGia = findViewById(R.id.tvChiTietGia);
        tvChiTietSoLuong = findViewById(R.id.tvChiTietSoLuong);
        tvChiTietTenNCC = findViewById(R.id.tvChiTietTenNCC);
        tvChiTietEmailNCC = findViewById(R.id.tvChiTietEmailNCC);
        tvChiTietSdtNCC = findViewById(R.id.tvChiTietSdtNCC);
        fabSuaHangHoa = findViewById(R.id.fabSuaHangHoa);
        toolbarChiTietMoi = findViewById(R.id.toolbarChiTietMoi);
        imgChiTietHangHoa = findViewById(R.id.imgChiTietHangHoa);
    }

    private void loadDataFromFirebase() {
        hangHoaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HangHoa hangHoa = snapshot.getValue(HangHoa.class);
                    if (hangHoa != null) {
                        // (Code .setText() của bạn... giữ nguyên)
                        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                        symbols.setGroupingSeparator('.');
                        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);

                        tvChiTietMaHang.setText(hangHoa.getMaHangHoa());
                        tvChiTietTenHang.setText(hangHoa.getTen());
                        tvChiTietLoaiHang.setText(hangHoa.getLoaiHangHoa());
                        tvChiTietGia.setText(decimalFormat.format(hangHoa.getGia()) + "đ");
                        tvChiTietSoLuong.setText(String.valueOf(hangHoa.getSoLuong()));
                        tvChiTietTenNCC.setText(hangHoa.getTenNCC());
                        tvChiTietEmailNCC.setText(hangHoa.getEmailNCC());
                        tvChiTietSdtNCC.setText(hangHoa.getSdtNCC());

                        // Hiển thị ảnh
                        if (hangHoa.getHinhAnh() != null && !hangHoa.getHinhAnh().isEmpty()) {
                            Glide.with(ChiTietHangHoaActivity.this)
                                    .load(hangHoa.getHinhAnh())
                                    .into(imgChiTietHangHoa);
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
