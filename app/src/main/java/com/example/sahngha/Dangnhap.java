package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Dangnhap extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dangnhap);

        EditText edtUsername = findViewById(R.id.edtUsername);
        EditText edtPassword = findViewById(R.id.edtPassword);
        View btnLoginAction = findViewById(R.id.btnLoginAction);

        //Sự kiện bấm nút đăng nhập
        btnLoginAction.setOnClickListener(v -> {
            String usernameInput = edtUsername.getText().toString().trim();
            String passwordInput = edtPassword.getText().toString().trim();

            if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ tên đăng nhập và mật khẩu!", Toast.LENGTH_SHORT).show();
            } else {
                dangNhapVoiFirebase(usernameInput, passwordInput);
            }
        });
    }

    private void dangNhapVoiFirebase(String user, String pass) {
        Toast.makeText(this, "Đang kiểm tra thông tin...", Toast.LENGTH_SHORT).show();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        DatabaseReference myRef = database.getReference("TaiKhoan");

        Query checkUser = myRef.orderByChild("username").equalTo(user);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {

                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {

                        TaiKhoan taiKhoan = userSnapshot.getValue(TaiKhoan.class);

                        if (taiKhoan != null) {

                            if (taiKhoan.getPassword().equals(pass)) {

                                Toast.makeText(Dangnhap.this,
                                        "Xin chào " + taiKhoan.getUsername(),
                                        Toast.LENGTH_SHORT
                                ).show();

                                Intent intent = new Intent(Dangnhap.this, Trangchu.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            } else {
                                Toast.makeText(Dangnhap.this, "Mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } else {
                    Toast.makeText(Dangnhap.this, "Tài khoản không tồn tại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Dangnhap.this, "Lỗi kết nối: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
