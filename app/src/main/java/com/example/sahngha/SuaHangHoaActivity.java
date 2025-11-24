package com.example.sahngha;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SuaHangHoaActivity extends AppCompatActivity {

    // Khai báo biến (Dùng TextInputEditText cho layout mới)
    TextInputEditText edtSuaTenHangHoa, edtSuaLoaiHangHoa, edtSuaGia, edtSuaSoLuong, edtSuaTenNCC, edtSuaEmailNCC, edtSuaSdtNCC;
    TextView tvSuaMaHangHoa;
    Button btnLuuThayDoi;
    Toolbar toolbarSua;

    DatabaseReference hangHoaRef;
    String maHangHoaKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sua_hang_hoa); // Dùng layout mới

        // Ánh xạ
        anhXaViews();

        // Cài đặt Toolbar
        setSupportActionBar(toolbarSua);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarSua.setNavigationOnClickListener(v -> finish()); // Nút back

        // Lấy mã hàng hóa từ Intent (được gửi từ ChiTietHangHoaActivity)
        maHangHoaKey = getIntent().getStringExtra("MA_HANG_HOA");

        if (maHangHoaKey == null || maHangHoaKey.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã hàng hóa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Khởi tạo Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        hangHoaRef = database.getReference("hanghoa").child(maHangHoaKey); // Trỏ thẳng đến hàng hóa cần sửa

        // Tải dữ liệu cũ lên
        loadDataFromFirebase();

        // Sự kiện nút Lưu
        btnLuuThayDoi.setOnClickListener(v -> luuThayDoi());
    }

    private void anhXaViews() {
        edtSuaTenHangHoa = findViewById(R.id.edtSuaTenHangHoa);
        edtSuaLoaiHangHoa = findViewById(R.id.edtSuaLoaiHangHoa);
        edtSuaGia = findViewById(R.id.edtSuaGia);
        edtSuaSoLuong = findViewById(R.id.edtSuaSoLuong);
        edtSuaTenNCC = findViewById(R.id.edtSuaTenNCC);
        edtSuaEmailNCC = findViewById(R.id.edtSuaEmailNCC);
        edtSuaSdtNCC = findViewById(R.id.edtSuaSdtNCC);
        tvSuaMaHangHoa = findViewById(R.id.tvSuaMaHangHoa);
        btnLuuThayDoi = findViewById(R.id.btnLuuThayDoi);
        toolbarSua = findViewById(R.id.toolbarSua);
    }

    // Tải dữ liệu cũ của hàng hóa lên các EditText
    private void loadDataFromFirebase() {
        hangHoaRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    HangHoa hangHoa = snapshot.getValue(HangHoa.class);
                    if (hangHoa != null) {
                        // Gán dữ liệu cũ vào các ô
                        tvSuaMaHangHoa.setText(hangHoa.getMaHangHoa());
                        edtSuaTenHangHoa.setText(hangHoa.getTen());
                        edtSuaLoaiHangHoa.setText(hangHoa.getLoaiHangHoa());
                        edtSuaGia.setText(String.valueOf(hangHoa.getGia())); // Chuyển double sang String
                        edtSuaSoLuong.setText(String.valueOf(hangHoa.getSoLuong())); // Chuyển int sang String
                        edtSuaTenNCC.setText(hangHoa.getTenNCC());
                        edtSuaEmailNCC.setText(hangHoa.getEmailNCC());
                        edtSuaSdtNCC.setText(hangHoa.getSdtNCC());
                    }
                } else {
                    Toast.makeText(SuaHangHoaActivity.this, "Không tìm thấy dữ liệu", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SuaHangHoaActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    // Xử lý lưu các thay đổi
    private void luuThayDoi() {
        // Lấy dữ liệu mới từ các EditText
        String ten = edtSuaTenHangHoa.getText().toString().trim();
        String loai = edtSuaLoaiHangHoa.getText().toString().trim();
        String giaStr = edtSuaGia.getText().toString().trim();
        String soLuongStr = edtSuaSoLuong.getText().toString().trim();
        String tenNCC = edtSuaTenNCC.getText().toString().trim();
        String email = edtSuaEmailNCC.getText().toString().trim();
        String sdt = edtSuaSdtNCC.getText().toString().trim();

        // Kiểm tra
        if (ten.isEmpty() || giaStr.isEmpty() || soLuongStr.isEmpty()) {
            Toast.makeText(this, "Tên, Giá, Số Lượng không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        double gia = Double.parseDouble(giaStr);
        int soLuong = Integer.parseInt(soLuongStr);

        // Cập nhật dữ liệu lên Firebase
        // Chúng ta có thể dùng updateChildren để cập nhật nhiều trường cùng lúc
        // Hoặc ghi đè từng trường
        hangHoaRef.child("ten").setValue(ten);
        hangHoaRef.child("loaiHangHoa").setValue(loai);
        hangHoaRef.child("gia").setValue(gia);
        hangHoaRef.child("soLuong").setValue(soLuong);
        hangHoaRef.child("tenNCC").setValue(tenNCC);
        hangHoaRef.child("emailNCC").setValue(email);
        hangHoaRef.child("sdtNCC").setValue(sdt)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SuaHangHoaActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    finish(); // Đóng màn hình Sửa, quay về màn hình Chi Tiết
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SuaHangHoaActivity.this, "Lỗi khi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
