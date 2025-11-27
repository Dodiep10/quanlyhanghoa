package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide; // Thư viện dùng để tải và hiển thị ảnh từ URL
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;

public class ChiTietHangHoaActivity extends AppCompatActivity {

    // === 1. KHAI BÁO BIẾN ===
    // Các biến đại diện cho các thành phần giao diện (UI)
    private Toolbar toolbar;
    private ImageView imgHinhAnh;
    private TextView tvTen, tvMa, tvLoai, tvGia, tvSoLuong;
    private TextView tvTenNCC, tvEmailNCC, tvSdtNCC;
    private FloatingActionButton fabSua; // Nút tròn nổi để sửa hàng hóa

    // Các biến để làm việc với Firebase
    private DatabaseReference mRef;
    private String receivedMaHangHoa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_hang_hoa); // Liên kết file code này với file giao diện XML

        // === 2. KHỞI TẠO GIAO DIỆN ===
        // Gọi hàm ánh xạ để kết nối các biến Java với ID trong XML
        initViews();

        // Cấu hình Toolbar (thanh tiêu đề) có nút Back (mũi tên quay lại)
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Xử lý sự kiện khi ấn nút Back trên toolbar -> Đóng màn hình hiện tại
        toolbar.setNavigationOnClickListener(v -> finish());

        // === 3. NHẬN DỮ LIỆU TỪ MÀN HÌNH TRƯỚC ===
        // Lấy Intent được gửi từ MainActivity
        Intent intent = getIntent();
        if (intent != null) {
            // Lấy chuỗi Mã hàng hóa (Key: "MA_HANG_HOA")
            receivedMaHangHoa = intent.getStringExtra("MA_HANG_HOA");
        }

        // === 4. TẢI DỮ LIỆU TỪ FIREBASE ===
        // Nếu mã hàng hóa hợp lệ thì gọi hàm tải dữ liệu
        if (receivedMaHangHoa != null && !receivedMaHangHoa.isEmpty()) {
            loadDataFromFirebase(receivedMaHangHoa);
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy mã hàng hoá", Toast.LENGTH_SHORT).show();
        }

        // === 5. XỬ LÝ SỰ KIỆN NÚT SỬA ===
        fabSua.setOnClickListener(v -> {
            // Tạo Intent để chuyển sang màn hình SuaHangHoaActivity
            Intent intentSua = new Intent(ChiTietHangHoaActivity.this, SuaHangHoaActivity.class);
            // Gửi kèm mã hàng hóa sang màn hình sửa để biết cần sửa sản phẩm nào
            intentSua.putExtra("MA_HANG_HOA", receivedMaHangHoa);
            startActivity(intentSua);
        });
    }

    // Hàm ánh xạ: Tìm View theo ID
    private void initViews() {
        toolbar = findViewById(R.id.toolbarChiTietMoi);
        imgHinhAnh = findViewById(R.id.imgChiTietHangHoa);

        tvTen = findViewById(R.id.tvChiTietTenHang);
        tvMa = findViewById(R.id.tvChiTietMaHang);
        tvLoai = findViewById(R.id.tvChiTietLoaiHang);

        tvGia = findViewById(R.id.tvChiTietGia);
        tvSoLuong = findViewById(R.id.tvChiTietSoLuong);

        tvTenNCC = findViewById(R.id.tvChiTietTenNCC);
        tvEmailNCC = findViewById(R.id.tvChiTietEmailNCC);
        tvSdtNCC = findViewById(R.id.tvChiTietSdtNCC);

        fabSua = findViewById(R.id.fabSuaHangHoa);
    }

    // Hàm kết nối Firebase và lấy dữ liệu chi tiết
    private void loadDataFromFirebase(String maHangHoa) {
        // Tạo đường dẫn đến node: hanghoa -> [Mã cụ thể]
        mRef = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("hanghoa").child(maHangHoa);

        // Lắng nghe sự thay đổi dữ liệu (Realtime)
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Chuyển đổi dữ liệu JSON từ Firebase thành đối tượng Java (HangHoa)
                    HangHoa hh = snapshot.getValue(HangHoa.class);

                    if (hh != null) {
                        // === HIỂN THỊ DỮ LIỆU LÊN GIAO DIỆN ===

                        // Hiển thị tên, mã, loại, số lượng
                        tvTen.setText(hh.getTen());
                        tvMa.setText(hh.getMaHangHoa());
                        tvLoai.setText(hh.getLoaiHangHoa());
                        tvSoLuong.setText(String.valueOf(hh.getSoLuong()));

                        // Định dạng giá tiền
                        DecimalFormat formatter = new DecimalFormat("###,###,###");
                        String giaDaFormat = formatter.format(hh.getGia()) + " VND";
                        tvGia.setText(giaDaFormat);


                        // === TẢI ẢNH ===
                        // Kiểm tra nếu có link ảnh thì dùng Glide để tải
                        if (hh.getHinhAnh() != null && !hh.getHinhAnh().isEmpty()) {
                            Glide.with(ChiTietHangHoaActivity.this)
                                    .load(hh.getHinhAnh())                          // Link ảnh
                                    .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ khi đang tải
                                    .error(R.drawable.ic_launcher_background)       // Ảnh hiển thị nếu lỗi
                                    .into(imgHinhAnh);                              // View để hiển thị ảnh
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Thông báo nếu có lỗi kết nối Firebase
                Toast.makeText(ChiTietHangHoaActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}