package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Dangnhap extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Gắn giao diện (Quan trọng nhất)
        setContentView(R.layout.activity_dangnhap);

        // 2. Ánh xạ các View từ XML
        EditText edtUsername = findViewById(R.id.edtUsername);
        EditText edtPassword = findViewById(R.id.edtPassword);
        View btnLoginAction = findViewById(R.id.btnLoginAction);

        // 3. Xử lý sự kiện nút Đăng nhập
        if (btnLoginAction != null) {
            btnLoginAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Lấy dữ liệu người dùng nhập (xóa khoảng trắng thừa bằng trim())
                    String username = edtUsername.getText().toString().trim();
                    String password = edtPassword.getText().toString().trim();

                    // Gọi hàm kiểm tra
                    if (kiemTraTaiKhoan(username, password)) {
                        // --- ĐĂNG NHẬP THÀNH CÔNG ---
                        Toast.makeText(Dangnhap.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                        // Chuyển sang màn hình Trang chủ
                        Intent intent = new Intent(Dangnhap.this, Trangchu.class);
                        startActivity(intent);

                        // Đóng màn hình đăng nhập lại để không quay lại được khi ấn Back
                        finish();
                    } else {
                        // --- ĐĂNG NHẬP THẤT BẠI ---
                        Toast.makeText(Dangnhap.this, "Sai tên đăng nhập hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        // 4. Xử lý nút Back (Nếu có ImageView nút back trong XML)

    }

    /**
     * Hàm kiểm tra thông tin đăng nhập
     * Danh sách hợp lệ: nhanvien1 -> nhanvien5
     * Mật khẩu chung: 123456
     */
    private boolean kiemTraTaiKhoan(String user, String pass) {
        // Kiểm tra mật khẩu trước (cho nhanh)
        if (!pass.equals("123456")) {
            return false;
        }

        // Danh sách các tài khoản nhân viên được phép
        String[] danhSachNhanVien = {
                "nhanvien1",
                "nhanvien2",
                "nhanvien3",
                "nhanvien4",
                "nhanvien5"
        };

        // Duyệt xem tên đăng nhập có nằm trong danh sách không
        for (String nv : danhSachNhanVien) {
            if (nv.equals(user)) {
                return true; // Tìm thấy tài khoản hợp lệ
            }
        }

        return false; // Không tìm thấy
    }
}