package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout; // Mới thêm
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.Button;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements HangHoaAdapter.OnItemActionListener {

    ListView lvHangHoa;
    HangHoaAdapter adapter;

    ArrayList<HangHoa> dsHangHoa;
    ArrayList<HangHoa> originalList;

    EditText edtTimHh;
    TextView txtTong;
    Button btn_Them;

    // === KHAI BÁO CÁC NÚT DANH MỤC (MỚI THÊM) ===
    LinearLayout btnDoAnVat, btnNuocNgot, btnKeo, btnBanh;

    FirebaseDatabase database;
    DatabaseReference hangHoaRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // --- 1. ÁNH XẠ VIEW ---
        txtTong = findViewById(R.id.txtTong);
        lvHangHoa = findViewById(R.id.lvHangHoa);
        btn_Them = findViewById(R.id.btnThem);
        edtTimHh = findViewById(R.id.edt_timhh);

        // Ánh xạ các nút danh mục mới thêm trong layout
        btnDoAnVat = findViewById(R.id.btnDoAnVat);
        btnNuocNgot = findViewById(R.id.btnNuocNgot);
        btnKeo = findViewById(R.id.btnKeo);
        btnBanh = findViewById(R.id.btnBanh);

        // --- 2. KHỞI TẠO FIREBASE ---
        database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        hangHoaRef = database.getReference("hanghoa");

        dsHangHoa = new ArrayList<>();
        originalList = new ArrayList<>();

        adapter = new HangHoaAdapter(this, R.layout.item_hanghoa, dsHangHoa);
        adapter.setOnItemActionListener(this);

        lvHangHoa.setAdapter(adapter);

        // Lấy dữ liệu từ Firebase
        layDuLieuFirebase();

        // --- 3. XỬ LÝ CÁC SỰ KIỆN ---

        // Nút Thêm Hàng
        btn_Them.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, themhanghoa.class);
            startActivity(intent);
        });

        // Click Item (Xem Chi tiết Hàng hóa)
        lvHangHoa.setOnItemClickListener((parent, view, position, id) -> {
            HangHoa selectedHangHoa = dsHangHoa.get(position);
            Intent detailIntent = new Intent(MainActivity.this, ChiTietHangHoaActivity.class);
            detailIntent.putExtra("MA_HANG_HOA", selectedHangHoa.getMaHangHoa());
            startActivity(detailIntent);
        });

        // === XỬ LÝ CLICK DANH MỤC (MỚI THÊM) ===
        // Lưu ý: Các chuỗi text này phải khớp với "Loại hàng hoá" bạn nhập trên Firebase
        btnDoAnVat.setOnClickListener(v -> moManHinhLoc("Đồ ăn vặt"));
        btnNuocNgot.setOnClickListener(v -> moManHinhLoc("Nước ngọt"));
        btnKeo.setOnClickListener(v -> moManHinhLoc("Kẹo"));
        btnBanh.setOnClickListener(v -> moManHinhLoc("Bánh"));

        // Thiết lập chức năng tìm kiếm
        setupSearchListener();
    }

    // === HÀM CHUYỂN MÀN HÌNH LỌC (MỚI THÊM) ===
    private void moManHinhLoc(String loaiHang) {
        Intent intent = new Intent(MainActivity.this, DanhSachTheoLoaiActivity.class);
        intent.putExtra("LOAI_CAN_LOC", loaiHang);
        startActivity(intent);
    }

    // HÀM THIẾT LẬP LẮNG NGHE TÌM KIẾM (GIỮ NGUYÊN)
    private void setupSearchListener() {
        edtTimHh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // HÀM LỌC DỮ LIỆU (GIỮ NGUYÊN)
    private void filterList(String query) {
        String lowerCaseQuery = query.toLowerCase().trim();
        dsHangHoa.clear();

        if (lowerCaseQuery.isEmpty()) {
            dsHangHoa.addAll(originalList);
        } else {
            for (HangHoa item : originalList) {
                boolean isMatch = false;
                if (item.getMaHangHoa() != null && item.getMaHangHoa().toLowerCase().contains(lowerCaseQuery)) {
                    isMatch = true;
                }
                if (!isMatch && item.getTen() != null && item.getTen().toLowerCase().contains(lowerCaseQuery)) {
                    isMatch = true;
                }
                if (isMatch) {
                    dsHangHoa.add(item);
                }
            }
        }
        adapter.notifyDataSetChanged();
        capNhatTong();
    }

    // HÀM layDuLieuFirebase
    private void layDuLieuFirebase() {
        hangHoaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsHangHoa.clear();
                originalList.clear();

                for (DataSnapshot item : snapshot.getChildren()) {
                    HangHoa hh = item.getValue(HangHoa.class);
                    originalList.add(hh);
                }
                dsHangHoa.addAll(originalList);
                adapter.notifyDataSetChanged();
                capNhatTong();
                filterList(edtTimHh.getText().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu", error.toException());
            }
        });
    }

    private void capNhatTong() {
        txtTong.setText("Tổng: " + dsHangHoa.size() + " sản phẩm");
    }

    @Override
    public void onDeleteClick(String hangHoaId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa hàng hóa có mã " + hangHoaId + " không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    xoaHangHoaTrenFirebase(hangHoaId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void xoaHangHoaTrenFirebase(String hangHoaId) {
        DatabaseReference itemRef = hangHoaRef.child(hangHoaId);
        itemRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Xóa hàng hóa thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Firebase", "Lỗi khi xóa dữ liệu", e);
                });
    }
}