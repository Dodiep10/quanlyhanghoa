package com.example.sahngha;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar; // Import Toolbar

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

    // Khai báo Views
    private TextView tvMaPhieu, tvNguoiNhap, tvThoiGianNhap, tvTongTienChiTiet;
    private RecyclerView rvChiTietHangHoa;
    private Toolbar toolbarChiTietPhieu; // Khai báo Toolbar

    private ChiTietPhieuNhapAdapter adapter;
    private List<ChiTietNhapHang> listChiTiet;

    private FirebaseDatabase database;
    private DatabaseReference phieuNhapRef;

    // Định dạng tiền tệ
    private DecimalFormat df = new DecimalFormat("#,###");
    // Định dạng thời gian
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());

    private String maPhieuKey; // Biến lưu Mã Phiếu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.xemchitietphieu); // Layout Chi tiết Phiếu Nhập

        // 1. Ánh xạ View và Toolbar
        anhXaViews();

        // 2. Cài đặt Toolbar (Giống như ChiTietHangHoaActivity)
        setSupportActionBar(toolbarChiTietPhieu);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi Tiết Phiếu Nhập");
        }
        // Sự kiện cho nút back (mũi tên) -> finish()
        toolbarChiTietPhieu.setNavigationOnClickListener(v -> finish());

        // 3. Lấy Mã Phiếu từ Intent
        maPhieuKey = getIntent().getStringExtra("MA_PHIEU");

        if (maPhieuKey == null || maPhieuKey.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy Mã Phiếu Nhập", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 4. Khởi tạo Firebase
        database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        phieuNhapRef = database.getReference("phieunhap");

        // 5. Thiết lập RecyclerView
        listChiTiet = new ArrayList<>();
        adapter = new ChiTietPhieuNhapAdapter(listChiTiet);
        rvChiTietHangHoa.setLayoutManager(new LinearLayoutManager(this));
        rvChiTietHangHoa.setAdapter(adapter);

        // 6. Tải dữ liệu
        layChiTietPhieuNhap(maPhieuKey);
    }

    private void anhXaViews() {
        tvMaPhieu = findViewById(R.id.tvMaPhieu);
        tvNguoiNhap = findViewById(R.id.tvNguoiNhap);
        tvThoiGianNhap = findViewById(R.id.tvThoiGianNhap);
        tvTongTienChiTiet = findViewById(R.id.tvTongTienChiTiet);
        rvChiTietHangHoa = findViewById(R.id.rvChiTietHangHoa);
        toolbarChiTietPhieu = findViewById(R.id.toolbarChiTietPhieu); // Cần đảm bảo có ID này trong layout
    }

    private void layChiTietPhieuNhap(String maPhieu) {
        // Hiển thị Mã phiếu ngay lập tức
        tvMaPhieu.setText("Mã Phiếu: " + maPhieu);

        phieuNhapRef.child(maPhieu).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    PhieuNhap phieuNhap = snapshot.getValue(PhieuNhap.class);
                    if (phieuNhap != null) {
                        hienThiDuLieu(phieuNhap);
                    } else {
                        Toast.makeText(XemChiTietPhieu.this, "Lỗi đọc dữ liệu phiếu nhập.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(XemChiTietPhieu.this, "Không tìm thấy phiếu nhập có mã " + maPhieu, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải chi tiết phiếu nhập: " + error.getMessage());
                Toast.makeText(XemChiTietPhieu.this, "Lỗi kết nối Firebase.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void hienThiDuLieu(PhieuNhap phieuNhap) {
        // Hiển thị thông tin chung
        tvNguoiNhap.setText("Người Nhập: " + phieuNhap.getNguoiNhap());
        String thoiGian = sdf.format(new Date(phieuNhap.getThoiGianNhap()));
        tvThoiGianNhap.setText("Thời Gian: " + thoiGian);
        tvTongTienChiTiet.setText("Tổng Tiền: " + df.format(phieuNhap.getTongTien()) + "đ");

        // Hiển thị chi tiết hàng hóa
        listChiTiet.clear();
        if (phieuNhap.getChiTiet() != null) {
            // Chuyển Map ChiTietNhapHang về List để Adapter hiển thị
            listChiTiet.addAll(phieuNhap.getChiTiet().values());
        }
        adapter.notifyDataSetChanged();
    }
}