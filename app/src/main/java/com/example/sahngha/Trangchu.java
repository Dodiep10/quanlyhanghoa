package com.example.sahngha;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView; // Nhớ import thư viện này

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Trangchu extends AppCompatActivity {

    // Khai báo biến
    CardView cvQuanLyHangHoa;
    Button btnLogout;
    TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu); // Liên kết với file XML giao diện

        // ======== ÁNH XẠ VIEW ========
        // Tìm CardView theo ID chúng ta vừa đặt ở Bước 1
        cvQuanLyHangHoa = findViewById(R.id.cvQuanLyHangHoa);

        // Ánh xạ thêm nút đăng xuất và lời chào (nếu cần xử lý sau này)
        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);

        // ======== SỰ KIỆN CLICK ========

        // 1. Sự kiện ấn vào thẻ "Quản lý hàng hoá"
        cvQuanLyHangHoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Tạo Intent để chuyển sang màn hình themhanghoa
                Intent intent = new Intent(Trangchu.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 2. Sự kiện ấn nút Đăng xuất (Ví dụ minh hoạ)
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Trangchu.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                // Code xử lý đăng xuất ở đây (ví dụ quay về màn hình Login)
                finish();
            }
        });
    }
}