// File MỚI: app/src/main/java/com.example.sahngha/ThongKeActivity.java
package com.example.sahngha;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

// Đây là lớp trung gian chứa logic chọn loại báo cáo
public class ThongKeActivity extends AppCompatActivity {

    CardView cvBaoCaoNhapHang;
    CardView cvBaoCaoTonKho;
    Toolbar toolbarThongKe;

    // Các Hằng số để gửi thông tin loại báo cáo
    public static final String LOAI_BAO_CAO = "LOAI_BAO_CAO";
    public static final String NHAP_HANG = "NHAP_HANG";
    public static final String TON_KHO = "TON_KHO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);

        // Ánh xạ
        cvBaoCaoNhapHang = findViewById(R.id.cvBaoCaoNhapHang);
        cvBaoCaoTonKho = findViewById(R.id.cvBaoCaoTonKho);
        toolbarThongKe = findViewById(R.id.toolbarThongKe);

        // Thiết lập Toolbar (Có nút quay lại)
        setSupportActionBar(toolbarThongKe);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbarThongKe.setNavigationOnClickListener(v -> finish());

        // 1. Sự kiện click Báo Cáo Nhập Hàng
        cvBaoCaoNhapHang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThongKeActivity.this, BaoCaoChiTietActivity.class);
                intent.putExtra(LOAI_BAO_CAO, NHAP_HANG);
                startActivity(intent);
            }
        });

        // 2. Sự kiện click Báo Cáo Tồn Kho
        cvBaoCaoTonKho.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ThongKeActivity.this, BaoCaoChiTietActivity.class);
                intent.putExtra(LOAI_BAO_CAO, TON_KHO);
                startActivity(intent);
            }
        });
    }
}