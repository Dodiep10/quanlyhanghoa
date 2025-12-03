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

    CardView cvQuanLyHangHoa;
    CardView cvQuanLyPhieuNhap;
    CardView cvThongKe;
    Button btnLogout;
    TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu);

        // ÁNH XẠ VIEW
        cvQuanLyHangHoa = findViewById(R.id.cvQuanLyHangHoa);
        cvQuanLyPhieuNhap = findViewById(R.id.cvQuanLyPhieuNhap);
        cvThongKe = findViewById(R.id.cvThongKe);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);

        // 1. Quản lý hàng hoá
        cvQuanLyHangHoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trangchu.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 2. Quản lý phiếu nhập
        cvQuanLyPhieuNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trangchu.this, DanhSachPhieuNhapActivity.class);
                startActivity(intent);
            }
        });

        // 3. Đăng xuất
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Trangchu.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // 4. Báo cáo thống kê
        cvThongKe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trangchu.this, BaoCaoChiTietActivity.class);
                startActivity(intent);
            }
        });
    }
}