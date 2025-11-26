// Sửa file: app/src/main/java/com/example/sahngha/Trangchu.java

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
    CardView cvThongKe; // BIẾN MỚI
    Button btnLogout;
    TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trangchu);

        // ======== ÁNH XẠ VIEW ========
        cvQuanLyHangHoa = findViewById(R.id.cvQuanLyHangHoa);
        cvThongKe = findViewById(R.id.cvThongKe); // ÁNH XẠ MỚI

        btnLogout = findViewById(R.id.btnLogout);
        tvWelcome = findViewById(R.id.tvWelcome);

        // ======== SỰ KIỆN CLICK ========

        // 1. Sự kiện ấn vào thẻ "Quản lý hàng hoá" (GIỮ NGUYÊN)
        cvQuanLyHangHoa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Trangchu.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 2. Sự kiện ấn vào thẻ "Báo cáo thống kê" (MỚI)
        cvThongKe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Chuyển sang màn hình thống kê mới
                Intent intent = new Intent(Trangchu.this, ThongKeActivity.class);
                startActivity(intent);
            }
        });

        // 3. Sự kiện ấn nút Đăng xuất (GIỮ NGUYÊN)
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Trangchu.this, "Đã đăng xuất!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}