package com.example.sahngha;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
// ... existing imports ...

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ThemPhieuNhap extends AppCompatActivity {

    private EditText edtNguoiNhap, edtSoLuongNhap;
    private TextView tvHangHoaDaChon;
    private Button btnThemHang, btnLuuPhieuNhap;
    private RecyclerView rvChiTietNhap;
    private TextView tvTongTien;

    private HangHoa selectedHangHoa;
    private List<ChiTietNhapHang> listChiTiet;
    private ChiTietPhieuNhapAdapter adapter;

    private FirebaseDatabase database;
    private DatabaseReference hangHoaRef;
    private DatabaseReference phieuNhapRef;
    private List<HangHoa> listHangHoaFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.themphieunhap);

        // --- Khởi tạo view ---
        edtNguoiNhap = findViewById(R.id.edtNguoiNhap);
        tvHangHoaDaChon = findViewById(R.id.tvHangHoaDaChon);
        edtSoLuongNhap = findViewById(R.id.edtSoLuongNhap);
        btnThemHang = findViewById(R.id.btnThemHang);
        btnLuuPhieuNhap = findViewById(R.id.btnLuuPhieuNhap);
        rvChiTietNhap = findViewById(R.id.rvChiTietNhap);
        tvTongTien = findViewById(R.id.tvTongTien);

        // --- KHỞI TẠO FIREBASE ---
        database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        hangHoaRef = database.getReference("hanghoa");
        phieuNhapRef = database.getReference("phieunhap");
        listHangHoaFirebase = new ArrayList<>();

        layDuLieuHangHoa();

        tvHangHoaDaChon.setOnClickListener(v -> {
            hienThiDialogChonHangHoa();
        });

        // --- RecyclerView ---
        listChiTiet = new ArrayList<>();
        adapter = new ChiTietPhieuNhapAdapter(listChiTiet);

        // --- QUAN TRỌNG: CÀI ĐẶT SỰ KIỆN XÓA ---
        adapter.setOnDeleteListener(position -> {
            // 1. Xóa phần tử khỏi danh sách
            listChiTiet.remove(position);
            // 2. Cập nhật giao diện RecyclerView
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, listChiTiet.size());
            // 3. Tính lại tổng tiền
            updateTongTien();
            Toast.makeText(ThemPhieuNhap.this, "Đã xóa hàng hóa khỏi phiếu", Toast.LENGTH_SHORT).show();
        });
        // -----------------------------------------

        rvChiTietNhap.setLayoutManager(new LinearLayoutManager(this));
        rvChiTietNhap.setAdapter(adapter);

        // --- Logic Thêm hàng vào chi tiết ---
        btnThemHang.setOnClickListener(v -> {
            if(selectedHangHoa == null){
                tvHangHoaDaChon.setError("Vui lòng chọn hàng hóa!");
                Toast.makeText(this,"Vui lòng chọn hàng hóa!",Toast.LENGTH_SHORT).show();
                return;
            }

            String soLuongStr = edtSoLuongNhap.getText().toString().trim();
            if(soLuongStr.isEmpty() || soLuongStr.equals("0")){
                Toast.makeText(this,"Số lượng phải lớn hơn 0!",Toast.LENGTH_SHORT).show();
                return;
            }

            int soLuong = Integer.parseInt(soLuongStr);

            // Kiểm tra xem hàng hóa đã có trong list chưa để cộng dồn (Tuỳ chọn nâng cao)
            boolean daCo = false;
            for(ChiTietNhapHang existing : listChiTiet) {
                if(existing.getMaHangHoa().equals(selectedHangHoa.getMaHangHoa())) {
                    existing.setSoLuong(existing.getSoLuong() + soLuong);
                    daCo = true;
                    break;
                }
            }

            if(!daCo) {
                ChiTietNhapHang ct = new ChiTietNhapHang(selectedHangHoa, soLuong);
                listChiTiet.add(ct);
            }

            adapter.notifyDataSetChanged();
            updateTongTien();

            edtSoLuongNhap.setText("");
            // Không nhất thiết phải null selectedHangHoa nếu muốn nhập tiếp, nhưng reset cho an toàn
            selectedHangHoa = null;
            tvHangHoaDaChon.setText("--- Bấm vào đây để chọn hàng hóa ---");
            tvHangHoaDaChon.setError(null);
        });

        // ... existing code (Logic Lưu phiếu nhập) ...
        btnLuuPhieuNhap.setOnClickListener(v -> {
            if(listChiTiet.isEmpty()){
                Toast.makeText(this,"Danh sách chi tiết trống!",Toast.LENGTH_SHORT).show();
                return;
            }

            String nguoiNhap = edtNguoiNhap.getText().toString().trim();
            if(nguoiNhap.isEmpty()) nguoiNhap = "Admin";

            String firebaseKey = phieuNhapRef.push().getKey();
            String randomPart = (firebaseKey.length() >= 6) ? firebaseKey.substring(0, 6) : firebaseKey;
            String newPhieuNhapId = "PN-" + randomPart.toUpperCase();

            double tongTien = 0;
            Map<String, ChiTietNhapHang> chiTietMap = new HashMap<>();

            for(ChiTietNhapHang ct : listChiTiet){
                chiTietMap.put(ct.getMaHangHoa(), ct);
                tongTien += ct.getThanhTien();
            }

            PhieuNhap phieuNhap = new PhieuNhap(
                    newPhieuNhapId,
                    System.currentTimeMillis(),
                    nguoiNhap,
                    tongTien,
                    chiTietMap
            );

            phieuNhapRef.child(newPhieuNhapId).setValue(phieuNhap)
                    .addOnSuccessListener(aVoid -> {
                        DecimalFormat df = new DecimalFormat("#,###");
                        Toast.makeText(ThemPhieuNhap.this,
                                "Đã lưu phiếu " + newPhieuNhapId + ", tổng tiền: "+df.format(phieuNhap.getTongTien())+"đ",
                                Toast.LENGTH_LONG).show();

                        listChiTiet.clear();
                        adapter.notifyDataSetChanged();
                        updateTongTien();
                        edtNguoiNhap.setText("Admin");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ThemPhieuNhap.this, "Lỗi lưu phiếu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e("FirebaseSave", "Lỗi lưu phiếu", e);
                    });
        });
    }

    private void layDuLieuHangHoa() {
        // ... existing code ...
        hangHoaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listHangHoaFirebase.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    HangHoa hh = item.getValue(HangHoa.class);
                    listHangHoaFirebase.add(hh);
                }
                if (listHangHoaFirebase.isEmpty()) {
                    tvHangHoaDaChon.setText("Không có hàng hóa nào để chọn!");
                } else {
                    tvHangHoaDaChon.setHint("--- Bấm vào đây để chọn hàng hóa ---");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu hàng hóa", error.toException());
                Toast.makeText(ThemPhieuNhap.this, "Lỗi tải dữ liệu hàng hóa!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void hienThiDialogChonHangHoa() {
        // ... existing code ...
        if (listHangHoaFirebase.isEmpty()) {
            Toast.makeText(this, "Chưa có hàng hóa nào được tải.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] tenHangArray = new String[listHangHoaFirebase.size()];
        for (int i = 0; i < listHangHoaFirebase.size(); i++) {
            HangHoa hh = listHangHoaFirebase.get(i);
            DecimalFormat df = new DecimalFormat("#,###");
            tenHangArray[i] = hh.getTen() + " (Giá: " + df.format(hh.getGia()) + "đ)";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Hàng Hóa Cần Nhập");

        builder.setItems(tenHangArray, (dialog, which) -> {
            selectedHangHoa = listHangHoaFirebase.get(which);

            DecimalFormat df = new DecimalFormat("#,###");
            String info = selectedHangHoa.getTen() + " (" + selectedHangHoa.getMaHangHoa() + ") - Giá nhập: " + df.format(selectedHangHoa.getGia()) + "đ";
            tvHangHoaDaChon.setText(info);
            tvHangHoaDaChon.setError(null);
        });

        builder.show();
    }

    private void updateTongTien(){
        double tong = 0;
        for(ChiTietNhapHang ct : listChiTiet) tong += ct.getThanhTien();

        DecimalFormat df = new DecimalFormat("#,###");
        tvTongTien.setText("Tổng tiền: " + df.format(tong) + "đ");
    }
}