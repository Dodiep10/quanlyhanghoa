package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Để ghi log lỗi
import android.view.View;
// Dùng đúng loại Button bạn khai báo trong XML
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.app.AppCompatActivity;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_welcome);

            // Ánh xạ nút bấm
            // Ép kiểu về View cho an toàn, hoặc dùng AppCompatButton
            View btnLogin = findViewById(R.id.btnLogin);

            if (btnLogin != null) {
                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            // Chuyển sang màn hình Dangnhap
                            Intent intent = new Intent(Welcome.this, Dangnhap.class);
                            startActivity(intent);
                        } catch (Exception e) {
                            // Ghi log lỗi nếu không chuyển được trang
                            Log.e("WelcomeActivity", "Lỗi chuyển trang: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            } else {
                Log.e("WelcomeActivity", "Không tìm thấy nút btnLogin!");
            }
        } catch (Exception e) {
            Log.e("WelcomeActivity", "Lỗi khởi tạo màn hình: " + e.getMessage());
        }
    }
}