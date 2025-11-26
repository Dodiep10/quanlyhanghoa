package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Nếu muốn dùng toolbar

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DanhSachTheoLoaiActivity extends AppCompatActivity {

    ListView lvLoc;
    TextView tvTieuDe;
    ArrayList<HangHoa> dsLoc;
    HangHoaAdapter adapterLoc;
    String loaiCanTim = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Đảm bảo tên file XML của bạn chính xác là activity_danh_sach_theo_loai.xml
        setContentView(R.layout.activity_danh_sach_theo_loai);

        // Ánh xạ View
        lvLoc = findViewById(R.id.lvLocHangHoa);
        tvTieuDe = findViewById(R.id.tvTieuDeLoc);

        // 1. NHẬN DỮ LIỆU AN TOÀN (Tránh lỗi Null)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("LOAI_CAN_LOC")) {
            loaiCanTim = intent.getStringExtra("LOAI_CAN_LOC");
        }

        // Nếu không nhận được loại (null), gán về rỗng để không bị crash khi so sánh
        if (loaiCanTim == null) {
            loaiCanTim = "";
        }

        tvTieuDe.setText("Danh mục: " + loaiCanTim);

        // 2. CẤU HÌNH LISTVIEW
        dsLoc = new ArrayList<>();
        // Sử dụng HangHoaAdapter (đảm bảo Adapter này đã code đúng như bài trước)
        adapterLoc = new HangHoaAdapter(this, R.layout.item_hanghoa, dsLoc);

        // Cần setAdapter cho ListView thì nó mới hiện
        if (lvLoc != null) {
            lvLoc.setAdapter(adapterLoc);
        }

        // 3. TẢI DỮ LIỆU
        loadDuLieuTheoLoai();

        // 4. SỰ KIỆN CLICK VÀO ITEM (Chuyển sang xem chi tiết)
        if (lvLoc != null) {
            lvLoc.setOnItemClickListener((parent, view, position, id) -> {
                if (position >= 0 && position < dsLoc.size()) {
                    HangHoa selected = dsLoc.get(position);
                    Intent detailIntent = new Intent(DanhSachTheoLoaiActivity.this, ChiTietHangHoaActivity.class);
                    detailIntent.putExtra("MA_HANG_HOA", selected.getMaHangHoa());
                    startActivity(detailIntent);
                }
            });
        }
    }

    private void loadDuLieuTheoLoai() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("hanghoa");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsLoc.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    HangHoa hh = item.getValue(HangHoa.class);

                    if (hh != null) {
                        // --- LOGIC SO SÁNH AN TOÀN (CHỐNG CRASH) ---
                        String loaiTrenFirebase = hh.getLoaiHangHoa();

                        // Kiểm tra null trước khi so sánh
                        if (loaiTrenFirebase != null && !loaiCanTim.isEmpty()) {
                            // Chuyển hết về chữ thường và xóa khoảng trắng thừa để so sánh chính xác
                            if (loaiTrenFirebase.trim().equalsIgnoreCase(loaiCanTim.trim())) {
                                dsLoc.add(hh);
                            }
                        }
                    }
                }

                // Cập nhật giao diện
                if (adapterLoc != null) {
                    adapterLoc.notifyDataSetChanged();
                }

                if (dsLoc.isEmpty()) {
                    Toast.makeText(DanhSachTheoLoaiActivity.this, "Không tìm thấy hàng hoá nào thuộc loại: " + loaiCanTim, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DanhSachTheoLoaiActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}