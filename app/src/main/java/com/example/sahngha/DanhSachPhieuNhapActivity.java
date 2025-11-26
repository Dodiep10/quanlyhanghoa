package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

// Implement Interface từ Adapter
public class DanhSachPhieuNhapActivity extends AppCompatActivity implements PhieuNhapAdapter.OnItemActionListener {

    ListView lvPhieuNhap;
    PhieuNhapAdapter adapter;
    ArrayList<PhieuNhap> dsPhieuNhap;
    ArrayList<PhieuNhap> originalList;

    EditText edtTimPn;
    TextView txtTongPn;
    Button btnThemPn;

    FirebaseDatabase database;
    DatabaseReference phieuNhapRef;

    private String getFormattedDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.danhsachphieunhap);

        // Ánh xạ
        txtTongPn = findViewById(R.id.txtTongPhieu);
        lvPhieuNhap = findViewById(R.id.lvPhieuNhap);
        btnThemPn = findViewById(R.id.btnThemPhieu);
        edtTimPn = findViewById(R.id.edt_timphieu);

        // Firebase
        database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        phieuNhapRef = database.getReference("phieunhap");

        dsPhieuNhap = new ArrayList<>();
        originalList = new ArrayList<>();

        adapter = new PhieuNhapAdapter(this, R.layout.item_phieunhap, dsPhieuNhap);

        // Gán listener để nhận sự kiện xóa
        adapter.setOnItemActionListener(this);

        lvPhieuNhap.setAdapter(adapter);

        layDuLieuFirebase();

        // Sự kiện click thêm phiếu
        btnThemPn.setOnClickListener(v -> {
            Intent intent = new Intent(DanhSachPhieuNhapActivity.this, ThemPhieuNhap.class);
            startActivity(intent);
        });

        // Sự kiện tìm kiếm
        setupSearchListener();
    }

    // === ĐÃ SỬA LẠI HÀM NÀY CHO KHỚP VỚI ADAPTER ===
    @Override
    public void onDeleteClick(String maPhieu, int position) {
        // Khi bấm nút xóa ở Adapter, nó sẽ chạy vào đây
        thucHienXoaPhieu(maPhieu);
    }
    // ===============================================

    // --- HÀM XÓA ---
    public void thucHienXoaPhieu(String maPhieu) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa Phiếu Nhập " + maPhieu + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    phieuNhapRef.child(maPhieu).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Xóa thành công!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Log.e("Firebase", "Lỗi xóa PN", e));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // --- CÁC HÀM KHÁC GIỮ NGUYÊN ---
    private void filterList(String query) {
        String lowerCaseQuery = query.toLowerCase().trim();
        dsPhieuNhap.clear();

        if (lowerCaseQuery.isEmpty()) {
            dsPhieuNhap.addAll(originalList);
        } else {
            for (PhieuNhap item : originalList) {
                boolean isMatch = false;
                if (item.getMaPhieu() != null && item.getMaPhieu().toLowerCase().contains(lowerCaseQuery)) isMatch = true;
                if (!isMatch && item.getNguoiNhap() != null && item.getNguoiNhap().toLowerCase().contains(lowerCaseQuery)) isMatch = true;
                String ngayNhapStr = getFormattedDate(item.getThoiGianNhap());
                if (!isMatch && ngayNhapStr.contains(lowerCaseQuery)) isMatch = true;

                if (isMatch) dsPhieuNhap.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        capNhatTong();
    }

    private void layDuLieuFirebase() {
        phieuNhapRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsPhieuNhap.clear();
                originalList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    PhieuNhap pn = item.getValue(PhieuNhap.class);
                    originalList.add(pn);
                }
                dsPhieuNhap.addAll(originalList);
                adapter.notifyDataSetChanged();
                capNhatTong();
                filterList(edtTimPn.getText().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải dữ liệu", error.toException());
            }
        });
    }

    private void capNhatTong() {
        txtTongPn.setText("Tổng: " + dsPhieuNhap.size() + " phiếu nhập");
    }

    private void setupSearchListener() {
        edtTimPn.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { filterList(s.toString()); }
            public void afterTextChanged(Editable s) {}
        });
    }
}