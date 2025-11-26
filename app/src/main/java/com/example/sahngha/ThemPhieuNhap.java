package com.example.sahngha;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemPhieuNhap extends AppCompatActivity {

    // Khai báo View
    private EditText edtNguoiNhap, edtSoLuongNhap;
    private TextView tvHangHoaDaChon, tvTongTien;
    private Button btnThemHang, btnLuuPhieuNhap;
    private RecyclerView rvChiTietNhap;

    // Biến xử lý dữ liệu
    private HangHoa selectedHangHoa; // Hàng hoá đang chọn tạm thời
    private List<ChiTietNhapHang> listChiTiet; // Danh sách các hàng sẽ nhập
    private ChiTietPhieuNhapAdapter adapter;
    private List<HangHoa> listHangHoaFirebase; // Danh sách hàng hoá lấy về để chọn

    // Firebase
    private FirebaseDatabase database;
    private DatabaseReference hangHoaRef;
    private DatabaseReference phieuNhapRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.themphieunhap);

        // 1. Ánh xạ View
        initViews();

        // 2. Khởi tạo Firebase
        database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        hangHoaRef = database.getReference("hanghoa");
        phieuNhapRef = database.getReference("phieunhap");

        listHangHoaFirebase = new ArrayList<>();
        listChiTiet = new ArrayList<>();

        // 3. Cấu hình RecyclerView
        setupRecyclerView();

        // 4. Tải danh sách hàng hoá từ Firebase (để hiện dialog chọn)
        layDuLieuHangHoa();

        // --- SỰ KIỆN CLICK ---

        // Bấm vào TextView để chọn hàng hoá
        tvHangHoaDaChon.setOnClickListener(v -> hienThiDialogChonHangHoa());

        // Bấm nút "Thêm hàng vào danh sách"
        btnThemHang.setOnClickListener(v -> xuLyThemHangVaoList());

        // Bấm nút "Lưu phiếu nhập" (QUAN TRỌNG: Sẽ cập nhật kho ở đây)
        btnLuuPhieuNhap.setOnClickListener(v -> xuLyLuuPhieuNhap());
    }

    private void initViews() {
        edtNguoiNhap = findViewById(R.id.edtNguoiNhap);
        tvHangHoaDaChon = findViewById(R.id.tvHangHoaDaChon);
        edtSoLuongNhap = findViewById(R.id.edtSoLuongNhap);
        btnThemHang = findViewById(R.id.btnThemHang);
        btnLuuPhieuNhap = findViewById(R.id.btnLuuPhieuNhap);
        rvChiTietNhap = findViewById(R.id.rvChiTietNhap);
        tvTongTien = findViewById(R.id.tvTongTien);
    }

    private void setupRecyclerView() {
        adapter = new ChiTietPhieuNhapAdapter(listChiTiet);
        rvChiTietNhap.setLayoutManager(new LinearLayoutManager(this));
        rvChiTietNhap.setAdapter(adapter);

        // Xử lý sự kiện xóa item trong danh sách tạm
        adapter.setOnDeleteListener(position -> {
            listChiTiet.remove(position);
            adapter.notifyItemRemoved(position);
            adapter.notifyItemRangeChanged(position, listChiTiet.size());
            updateTongTien();
            Toast.makeText(ThemPhieuNhap.this, "Đã xóa hàng hóa khỏi phiếu", Toast.LENGTH_SHORT).show();
        });
    }

    // --- HÀM 1: Lấy danh sách hàng hoá để chọn ---
    private void layDuLieuHangHoa() {
        hangHoaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listHangHoaFirebase.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    HangHoa hh = item.getValue(HangHoa.class);
                    if (hh != null) {
                        listHangHoaFirebase.add(hh);
                    }
                }
                if (listHangHoaFirebase.isEmpty()) {
                    tvHangHoaDaChon.setText("Không có hàng hóa nào trong kho!");
                } else {
                    tvHangHoaDaChon.setHint("--- Bấm vào đây để chọn hàng hóa ---");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThemPhieuNhap.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- HÀM 2: Hiển thị Dialog chọn hàng ---
    private void hienThiDialogChonHangHoa() {
        if (listHangHoaFirebase.isEmpty()) {
            Toast.makeText(this, "Chưa có hàng hóa nào để chọn.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] tenHangArray = new String[listHangHoaFirebase.size()];
        DecimalFormat df = new DecimalFormat("#,###");

        for (int i = 0; i < listHangHoaFirebase.size(); i++) {
            HangHoa hh = listHangHoaFirebase.get(i);
            tenHangArray[i] = hh.getTen() + " - " + df.format(hh.getGia()) + "đ";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn Hàng Hóa");
        builder.setItems(tenHangArray, (dialog, which) -> {
            selectedHangHoa = listHangHoaFirebase.get(which);
            String info = selectedHangHoa.getTen() + " (" + selectedHangHoa.getMaHangHoa() + ")";
            tvHangHoaDaChon.setText(info);
            tvHangHoaDaChon.setError(null);
        });
        builder.show();
    }

    // --- HÀM 3: Xử lý nút Thêm (Add vào RecyclerView) ---
    private void xuLyThemHangVaoList() {
        if (selectedHangHoa == null) {
            tvHangHoaDaChon.setError("Chưa chọn hàng!");
            Toast.makeText(this, "Vui lòng chọn hàng hóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        String soLuongStr = edtSoLuongNhap.getText().toString().trim();
        if (soLuongStr.isEmpty() || soLuongStr.equals("0")) {
            Toast.makeText(this, "Nhập số lượng > 0", Toast.LENGTH_SHORT).show();
            return;
        }

        int soLuong = Integer.parseInt(soLuongStr);

        // Kiểm tra xem hàng này đã có trong phiếu chưa để cộng dồn
        boolean daCo = false;
        for (ChiTietNhapHang existing : listChiTiet) {
            if (existing.getMaHangHoa().equals(selectedHangHoa.getMaHangHoa())) {
                existing.setSoLuong(existing.getSoLuong() + soLuong);
                daCo = true;
                break;
            }
        }

        if (!daCo) {
            // Tạo chi tiết nhập mới
            ChiTietNhapHang ct = new ChiTietNhapHang(selectedHangHoa, soLuong);
            listChiTiet.add(ct);
        }

        adapter.notifyDataSetChanged();
        updateTongTien();

        // Reset ô nhập
        edtSoLuongNhap.setText("");
        selectedHangHoa = null;
        tvHangHoaDaChon.setText("--- Bấm vào đây để chọn hàng hóa ---");
    }

    private void updateTongTien() {
        double tong = 0;
        for (ChiTietNhapHang ct : listChiTiet) tong += ct.getThanhTien();
        DecimalFormat df = new DecimalFormat("#,###");
        tvTongTien.setText("Tổng tiền: " + df.format(tong) + "đ");
    }

    // --- HÀM 4: Xử lý nút LƯU PHIẾU NHẬP (Có cập nhật kho) ---
    private void xuLyLuuPhieuNhap() {
        if (listChiTiet.isEmpty()) {
            Toast.makeText(this, "Danh sách trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        String nguoiNhap = edtNguoiNhap.getText().toString().trim();
        if (nguoiNhap.isEmpty()) nguoiNhap = "Admin";

        // Tạo ID Phiếu nhập
        String firebaseKey = phieuNhapRef.push().getKey();
        String randomPart = (firebaseKey != null && firebaseKey.length() >= 6) ? firebaseKey.substring(0, 6) : "123456";
        String newPhieuNhapId = "PN-" + randomPart.toUpperCase();

        // Tính tổng tiền & Map chi tiết
        double tongTien = 0;
        Map<String, ChiTietNhapHang> chiTietMap = new HashMap<>();

        for (ChiTietNhapHang ct : listChiTiet) {
            chiTietMap.put(ct.getMaHangHoa(), ct);
            tongTien += ct.getThanhTien();
        }

        // Tạo object Phiếu Nhập
        PhieuNhap phieuNhap = new PhieuNhap(
                newPhieuNhapId,
                System.currentTimeMillis(),
                nguoiNhap,
                tongTien,
                chiTietMap
        );

        // === BƯỚC 1: LƯU PHIẾU NHẬP VÀO FIREBASE ===
        phieuNhapRef.child(newPhieuNhapId).setValue(phieuNhap)
                .addOnSuccessListener(aVoid -> {
                    DecimalFormat df = new DecimalFormat("#,###");
                    Toast.makeText(ThemPhieuNhap.this,
                            "Đã lưu phiếu! Đang cập nhật kho...", Toast.LENGTH_SHORT).show();

                    // === BƯỚC 2: TỰ ĐỘNG CẬP NHẬT SỐ LƯỢNG TỒN KHO ===
                    capNhatSoLuongTonKho(listChiTiet);

                    // Reset giao diện
                    listChiTiet.clear();
                    adapter.notifyDataSetChanged();
                    updateTongTien();
                    edtNguoiNhap.setText("Admin");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ThemPhieuNhap.this, "Lỗi lưu phiếu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // --- HÀM 5: Logic cộng dồn số lượng vào kho ---
    private void capNhatSoLuongTonKho(List<ChiTietNhapHang> danhSachNhap) {
        // Duyệt qua từng sản phẩm trong phiếu nhập
        for (ChiTietNhapHang chiTiet : danhSachNhap) {
            String maHang = chiTiet.getMaHangHoa();
            int soLuongNhap = chiTiet.getSoLuong();

            // Tìm đến đúng sản phẩm đó trong node "hanghoa"
            DatabaseReference itemRef = hangHoaRef.child(maHang);

            // Lấy số lượng hiện tại -> Cộng thêm -> Lưu lại
            itemRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        HangHoa hh = snapshot.getValue(HangHoa.class);
                        if (hh != null) {
                            int soLuongCu = hh.getSoLuong();
                            int soLuongMoi = soLuongCu + soLuongNhap;

                            // Cập nhật số lượng mới lên Firebase
                            itemRef.child("soLuong").setValue(soLuongMoi);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("UpdateStock", "Lỗi update kho: " + maHang);
                }
            });
        }

        // Thông báo hoàn tất
        Toast.makeText(this, "Đã cập nhật số lượng tồn kho!", Toast.LENGTH_SHORT).show();
    }
}