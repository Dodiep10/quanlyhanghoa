package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // Hoặc ImageButton tuỳ code XML của bạn
import androidx.appcompat.widget.AppCompatButton; // Nếu dùng AppCompatButton

import androidx.appcompat.app.AppCompatActivity;

public class Welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Ánh xạ nút bấm (Đảm bảo ID trong XML là btnLogin)
        View btnLogin = findViewById(R.id.btnLogin);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Chuyển sang màn hình Dangnhap
                    Intent intent = new Intent(Welcome.this, Dangnhap.class);
                    startActivity(intent);
                }
            });
        }
    }
}