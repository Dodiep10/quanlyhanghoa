// File: PhieuNhapActivity.java (Phiên bản HOÀN CHỈNH)

package com.example.sahngha;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import com.example.sahngha.PhieuNhapAdapter;

// THÊM INTERFACE ĐỂ KẾT NỐI VỚI ADAPTER
public class PhieuNhapActivity extends AppCompatActivity implements PhieuNhapAdapter.OnItemActionListener {

    private Toolbar toolbarPhieuNhap;
    private ListView lvPhieuNhap;
    private TextView txtTongPhieuNhap;
    private FloatingActionButton fabThemPhieuNhap;

    private ArrayList<PhieuNhap> dsPhieuNhap;
    private PhieuNhapAdapter adapter;
    private DatabaseReference phieuNhapRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phieu_nhap);

        anhXaViews();
        setupToolbar();

        // Khởi tạo Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        phieuNhapRef = database.getReference("phieunhap");

        dsPhieuNhap = new ArrayList<>();
        adapter = new PhieuNhapAdapter(this, R.layout.item_phieunhap, dsPhieuNhap);
        lvPhieuNhap.setAdapter(adapter);

        // GẮN LISTENER CHO ADAPTER (QUAN TRỌNG ĐỂ XÓA)
        adapter.setOnItemActionListener(this);

        layDuLieuPhieuNhapFirebase();

        // Sự kiện click cho nút Thêm Phiếu Nhập (FAB)
        fabThemPhieuNhap.setOnClickListener(v -> {
            // Liên kết với Activity ThemPhieuNhap đã có
            Intent intent = new Intent(PhieuNhapActivity.this, ThemPhieuNhap.class);
            startActivity(intent);
        });
    }

    private void anhXaViews() {
        toolbarPhieuNhap = findViewById(R.id.toolbarPhieuNhap);
        lvPhieuNhap = findViewById(R.id.lvPhieuNhap);
        txtTongPhieuNhap = findViewById(R.id.txtTongPhieuNhap);
        fabThemPhieuNhap = findViewById(R.id.fabThemPhieuNhap);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarPhieuNhap);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbarPhieuNhap.setNavigationOnClickListener(v -> finish());
    }

    private void layDuLieuPhieuNhapFirebase() {
        // ... (Giữ nguyên logic tải dữ liệu) ...
        phieuNhapRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsPhieuNhap.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    PhieuNhap pn = item.getValue(PhieuNhap.class);
                    if (pn != null) {
                        // GÁN MÃ PHIẾU NHẬP (LÀ KEY CỦA FIREBASE)
                        pn.setMaPhieu(item.getKey());
                        dsPhieuNhap.add(pn);
                    }
                }
                adapter.notifyDataSetChanged();
                txtTongPhieuNhap.setText("Tổng: " + dsPhieuNhap.size() + " phiếu nhập");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu phiếu nhập", error.toException());
                Toast.makeText(PhieuNhapActivity.this, "Lỗi tải dữ liệu phiếu nhập!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ========================================================
    // PHẦN XỬ LÝ SỰ KIỆN XÓA (Từ Adapter gửi về)
    // ========================================================
    @Override
    public void onDeleteClick(String maPhieu, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa phiếu")
                .setMessage("Bạn có chắc chắn muốn xóa Phiếu Nhập " + maPhieu + " không? Việc này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi hàm xóa trên Firebase
                    xoaPhieuNhapTrenFirebase(maPhieu);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void xoaPhieuNhapTrenFirebase(String maPhieu) {
        // Tham chiếu trực tiếp đến key (MaPhieu) trong node phieunhap
        phieuNhapRef.child(maPhieu).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PhieuNhapActivity.this, "Đã xóa phiếu nhập thành công!", Toast.LENGTH_SHORT).show();
                    // Lưu ý: Dữ liệu trên ListView sẽ tự cập nhật nhờ addValueEventListener
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Lỗi xóa phiếu nhập: " + e.getMessage());
                    Toast.makeText(PhieuNhapActivity.this, "Lỗi xóa phiếu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}