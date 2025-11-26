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

// ĐÃ GỠ BỎ: implements PhieuNhapAdapter.OnItemActionListener (để đơn giản hóa)
public class DanhSachPhieuNhapActivity extends AppCompatActivity {

    ListView lvPhieuNhap;
    PhieuNhapAdapter adapter;
    ArrayList<PhieuNhap> dsPhieuNhap;
    ArrayList<PhieuNhap> originalList;

    EditText edtTimPn;
    TextView txtTongPn;
    Button btnThemPn;

    FirebaseDatabase database;
    DatabaseReference phieuNhapRef;

    // === HÀM HỖ TRỢ ĐỊNH DẠNG NGÀY THÁNG ===
    private String getFormattedDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    // =======================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.danhsachphieunhap);

        // === ÁNH XẠ VIEWs ===
        txtTongPn = findViewById(R.id.txtTongPhieu);
        lvPhieuNhap = findViewById(R.id.lvPhieuNhap);
        btnThemPn = findViewById(R.id.btnThemPhieu);
        edtTimPn = findViewById(R.id.edt_timphieu);

        // === KHỞI TẠO FIREBASE ===
        database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        phieuNhapRef = database.getReference("phieunhap");

        dsPhieuNhap = new ArrayList<>();
        originalList = new ArrayList<>();

        // Khởi tạo Adapter
        adapter = new PhieuNhapAdapter(this, R.layout.item_phieunhap, dsPhieuNhap);
        // BỎ: adapter.setOnItemActionListener(this);

        lvPhieuNhap.setAdapter(adapter);

        layDuLieuFirebase(); // <<< KHÔNG CÒN BÁO LỖI VÌ HÀM NÀY NẰM TRONG CLASS >>>

        // === SỰ KIỆN CLICK ===
        btnThemPn.setOnClickListener(v -> {
            Intent intent = new Intent(DanhSachPhieuNhapActivity.this, ThemPhieuNhap.class);
            startActivity(intent);
        });

        // Click vào item trong ListView (Xem chi tiết)
        lvPhieuNhap.setOnItemClickListener((parent, view, position, id) -> {
            PhieuNhap selectedPn = dsPhieuNhap.get(position);
            Intent intent = new Intent(DanhSachPhieuNhapActivity.this, XemChiTietPhieu.class);
            intent.putExtra("MA_PHIEU", selectedPn.getMaPhieu());
            startActivity(intent);
        });

        setupSearchListener(); // <<< KHÔNG CÒN BÁO LỖI VÌ HÀM NÀY NẰM TRONG CLASS >>>
    }

    // ===================================================================================
    // CÁC HÀM LOGIC PHẢI BẮT ĐẦU TỪ ĐÂY (NGOÀI HÀM ONCREATE)
    // ===================================================================================

    // --- HÀM LỌC DỮ LIỆU (Giữ nguyên logic lọc 3 trường) ---
    private void filterList(String query) {
        String lowerCaseQuery = query.toLowerCase().trim();
        dsPhieuNhap.clear();

        if (lowerCaseQuery.isEmpty()) {
            dsPhieuNhap.addAll(originalList);
        } else {
            for (PhieuNhap item : originalList) {
                boolean isMatch = false;

                // 1. Kiểm tra Mã phiếu (maPhieu)
                if (item.getMaPhieu() != null && item.getMaPhieu().toLowerCase().contains(lowerCaseQuery)) {
                    isMatch = true;
                }

                // 2. Kiểm tra Người nhập (nguoiNhap)
                if (!isMatch && item.getNguoiNhap() != null && item.getNguoiNhap().toLowerCase().contains(lowerCaseQuery)) {
                    isMatch = true;
                }

                // 3. KIỂM TRA NGÀY NHẬP
                String ngayNhapStr = getFormattedDate(item.getThoiGianNhap());

                if (!isMatch && ngayNhapStr.contains(lowerCaseQuery)) {
                    isMatch = true;
                }

                if (isMatch) {
                    dsPhieuNhap.add(item);
                }
            }
        }

        adapter.notifyDataSetChanged();
        capNhatTong();
    }

    // --- HÀM TẢI DỮ LIỆU TỪ FIREBASE ---
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
                // Phải gọi filterList sau khi dữ liệu đã tải
                filterList(edtTimPn.getText().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu Phiếu Nhập", error.toException());
                Toast.makeText(DanhSachPhieuNhapActivity.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void capNhatTong() {
        txtTongPn.setText("Tổng: " + dsPhieuNhap.size() + " phiếu nhập");
    }

    private void setupSearchListener() {
        edtTimPn.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    // === HÀM XÓA (Giữ lại logic xóa) ===
    public void thucHienXoaPhieu(String maPhieu) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa Phiếu Nhập")
                .setMessage("Bạn có chắc chắn muốn xóa Phiếu Nhập có mã " + maPhieu + " không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    phieuNhapRef.child(maPhieu).removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(this, "Xóa Phiếu Nhập thành công!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Log.e("Firebase", "Lỗi xóa PN", e));
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}