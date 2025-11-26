package com.example.sahngha;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class XemChiTietPhieu extends AppCompatActivity {

    // Khai báo các biến View
    private TextView tvMaPhieu, tvNguoiNhap, tvThoiGianNhap, tvTongTienChiTiet;
    private RecyclerView rvChiTietHangHoa;
    private Toolbar toolbar;

    // Biến cho RecyclerView
    private ChiTietPhieuNhapAdapter adapter;
    private List<ChiTietNhapHang> listChiTiet;

    // Biến Firebase và dữ liệu
    private String maPhieuCanXem;
    private DatabaseReference phieuNhapRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Quan trọng: Phải đúng tên file layout xml của bạn
        setContentView(R.layout.xemchitietphieu);

        // 1. Ánh xạ các View từ layout
        initViews();

        // 2. Thiết lập Toolbar (Nút quay lại)
        setupToolbar();

        // 3. Lấy Mã Phiếu được gửi từ màn hình danh sách
        if (getIntent().hasExtra("MA_PHIEU")) {
            maPhieuCanXem = getIntent().getStringExtra("MA_PHIEU");
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã phiếu!", Toast.LENGTH_SHORT).show();
            finish(); // Đóng màn hình nếu không có mã
            return;
        }

        // 4. Cấu hình RecyclerView để hiển thị danh sách hàng hóa
        setupRecyclerView();

        // 5. Khởi tạo Firebase
        // Lưu ý: URL database phải khớp với cấu hình trong ThemPhieuNhap
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        phieuNhapRef = database.getReference("phieunhap");

        // 6. Tải dữ liệu từ Firebase
        loadThongTinPhieu();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbarChiTietPhieu);
        tvMaPhieu = findViewById(R.id.tvMaPhieu);
        tvNguoiNhap = findViewById(R.id.tvNguoiNhap);
        tvThoiGianNhap = findViewById(R.id.tvThoiGianNhap);
        tvTongTienChiTiet = findViewById(R.id.tvTongTienChiTiet);
        rvChiTietHangHoa = findViewById(R.id.rvChiTietHangHoa);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi Tiết Phiếu Nhập");
        }
        // Xử lý sự kiện khi bấm nút mũi tên quay lại trên toolbar
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        listChiTiet = new ArrayList<>();
        // Tái sử dụng Adapter mà bạn đã dùng ở màn hình Thêm Phiếu Nhập
        // Vì cấu trúc hiển thị 1 dòng chi tiết hàng hóa là giống nhau
        adapter = new ChiTietPhieuNhapAdapter(listChiTiet);

        rvChiTietHangHoa.setLayoutManager(new LinearLayoutManager(this));
        rvChiTietHangHoa.setAdapter(adapter);
    }

    private void loadThongTinPhieu() {
        // Truy vấn trực tiếp vào node con có key là maPhieuCanXem
        phieuNhapRef.child(maPhieuCanXem).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Map dữ liệu từ Firebase vào object PhieuNhap
                    PhieuNhap phieuNhap = snapshot.getValue(PhieuNhap.class);

                    if (phieuNhap != null) {
                        hienThiThongTin(phieuNhap);
                    }
                } else {
                    Toast.makeText(XemChiTietPhieu.this, "Phiếu nhập không tồn tại hoặc đã bị xóa.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(XemChiTietPhieu.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", "Lỗi load chi tiết phiếu", error.toException());
            }
        });
    }

    private void hienThiThongTin(PhieuNhap phieuNhap) {
        // --- 1. Hiển thị thông tin chung ---
        tvMaPhieu.setText("Mã Phiếu: " + phieuNhap.getMaPhieu());
        tvNguoiNhap.setText("Người Nhập: " + phieuNhap.getNguoiNhap());

        // Format thời gian (từ mili-giây sang ngày/tháng/năm giờ:phút)
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String strDate = sdf.format(new Date(phieuNhap.getThoiGianNhap()));
        tvThoiGianNhap.setText("Thời Gian: " + strDate);

        // Format tổng tiền
        DecimalFormat df = new DecimalFormat("#,###");
        tvTongTienChiTiet.setText("Tổng Tiền: " + df.format(phieuNhap.getTongTien()) + "đ");

        // --- 2. Hiển thị danh sách chi tiết hàng hóa ---
        listChiTiet.clear();

        // Trong ThemPhieuNhap, bạn lưu chiTiet dưới dạng Map<String, ChiTietNhapHang>
        // Cần chuyển đổi Map này thành List để đưa vào RecyclerView
        if (phieuNhap.getChiTiet() != null) {
            listChiTiet.addAll(phieuNhap.getChiTiet().values());
        }

        // Cập nhật giao diện list
        adapter.notifyDataSetChanged();
    }
}