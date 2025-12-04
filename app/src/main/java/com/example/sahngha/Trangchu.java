package com.example.sahngha;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class Trangchu extends AppCompatActivity {

    // Khai báo các biến cũ
    CardView cvQuanLyHangHoa;
    CardView cvQuanLyPhieuNhap;
    CardView cvThongKe;
    Button btnLogout;
    TextView tvWelcome;

    // Khai báo thêm biến cho Drawer
    CardView cvAvatar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu);

        // --- ÁNH XẠ VIEW CŨ ---
        cvQuanLyHangHoa = findViewById(R.id.cvQuanLyHangHoa);
        cvQuanLyPhieuNhap = findViewById(R.id.cvQuanLyPhieuNhap);
        cvThongKe = findViewById(R.id.cvThongKe);
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);

        // --- ÁNH XẠ VIEW MỚI CHO DRAWER ---
        cvAvatar = findViewById(R.id.cvAvatar);
        drawerLayout = findViewById(R.id.drawer_layout_trangchu);
        navigationView = findViewById(R.id.nav_view_trangchu);

        // 1. XỬ LÝ SỰ KIỆN MỞ MENU KHI BẤM VÀO AVATAR
        cvAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mở menu từ bên PHẢI
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // 2. XỬ LÝ SỰ KIỆN KHI BẤM VÀO CÁC MỤC TRONG MENU TRƯỢT
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                // --- [CẬP NHẬT] LOGIC CHUYỂN MÀN HÌNH TẠI ĐÂY ---

                if (id == R.id.nav_info) {
                    // Chuyển sang màn hình Thông tin tài khoản
                    Intent intent = new Intent(Trangchu.this, ThongTinTaiKhoanActivity.class);
                    startActivity(intent);
                }

                else if (id == R.id.nav_logout) {
                    // Xử lý đăng xuất & quay về màn Đăng nhập
                    Intent intent = new Intent(Trangchu.this, Dangnhap.class);
                    // Xóa cờ activity để không back lại được trang chủ
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                // Đóng menu lại sau khi chọn xong
                drawerLayout.closeDrawer(GravityCompat.END);
                return true;
            }
        });

        // --- CÁC SỰ KIỆN CŨ CỦA TRANG CHỦ ---

        // Nút Quản lý hàng hoá
        cvQuanLyHangHoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trangchu.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Nút Quản lý phiếu nhập
        cvQuanLyPhieuNhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trangchu.this, DanhSachPhieuNhapActivity.class);
                startActivity(intent);
            }
        });

        // Nút Đăng xuất (Nút to ở giữa màn hình - Logic giống menu logout)
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trangchu.this, Dangnhap.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // Nút Báo cáo thống kê
        cvThongKe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trangchu.this, BaoCaoChiTietActivity.class);
                startActivity(intent);
            }
        });
    }
}