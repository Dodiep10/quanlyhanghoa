package com.example.sahngha;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Trangchu extends AppCompatActivity {

    // Khai báo biến
    CardView cvQuanLyHangHoa;
    CardView cvQuanLyPhieuNhap; // <-- KHAI BÁO THÊM CARDVIEW MỚI
    Button btnLogout;
    TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu);

        // ======== ÁNH XẠ VIEW ========
        // Tìm CardView theo id
        cvQuanLyHangHoa = findViewById(R.id.cvQuanLyHangHoa);
        cvQuanLyPhieuNhap = findViewById(R.id.cvQuanLyPhieuNhap); // <-- ÁNH XẠ CARDVIEW PHIẾU NHẬP

        // Ánh xạ thêm nút đăng xuất và lời chào (nếu cần xử lý sau này)
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);

        // ======== SỰ KIỆN CLICK ========

        // 1. Sự kiện ấn vào thẻ "Quản lý hàng hoá"
        cvQuanLyHangHoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trangchu.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 2. Sự kiện ấn vào thẻ "Quản lý phiếu nhập" <--- THÊM CODE NÀY
        cvQuanLyPhieuNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển sang màn hình Danhsachphieunhap
                Intent intent = new Intent(Trangchu.this, DanhSachPhieuNhapActivity.class);
                startActivity(intent);
            }
        });

        // 3. Sự kiện ấn nút Đăng xuất
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Trangchu.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}