package com.example.sahngha;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton; // Mới thêm
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher; // Mới thêm
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
import com.journeyapps.barcodescanner.ScanContract; // Mới thêm
import com.journeyapps.barcodescanner.ScanOptions;   // Mới thêm

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
    private ImageButton btnScanNhapHang; // Nút quét mã mới
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

    // --- BỘ QUÉT MÃ VẠCH ---
    // Khi quét xong, kết quả trả về đây
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
            result -> {
                if(result.getContents() != null) {
                    String scannedCode = result.getContents();
                    // Gọi hàm tìm kiếm hàng hoá theo mã vừa quét
                    timVaChonHangHoaTheoMa(scannedCode);
                }
            });
    // -----------------------

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

        // 4. Tải danh sách hàng hoá từ Firebase
        layDuLieuHangHoa();

        // --- SỰ KIỆN CLICK ---

        // Bấm vào TextView để chọn hàng hoá thủ công
        tvHangHoaDaChon.setOnClickListener(v -> hienThiDialogChonHangHoa());

        // --- SỰ KIỆN NÚT QUÉT MÃ ---
        btnScanNhapHang.setOnClickListener(v -> {
            // Kiểm tra xem dữ liệu đã tải xong chưa
            if (listHangHoaFirebase.isEmpty()) {
                Toast.makeText(this, "Đang tải dữ liệu hàng hoá, vui lòng đợi...", Toast.LENGTH_SHORT).show();
                return;
            }
            // Cấu hình quét
            ScanOptions options = new ScanOptions();
            options.setPrompt("Quét mã sản phẩm cần nhập");
            options.setBeepEnabled(true);
            options.setOrientationLocked(true);
            options.setCaptureActivity(ManHinhQuetDoc.class); // Dùng màn hình dọc
            barcodeLauncher.launch(options);
        });
        // ---------------------------

        // Bấm nút "Thêm hàng vào danh sách"
        btnThemHang.setOnClickListener(v -> xuLyThemHangVaoList());

        // Bấm nút "Lưu phiếu nhập"
        btnLuuPhieuNhap.setOnClickListener(v -> xuLyLuuPhieuNhap());
    }

    private void initViews() {
        edtNguoiNhap = findViewById(R.id.edtNguoiNhap);
        tvHangHoaDaChon = findViewById(R.id.tvHangHoaDaChon);
        btnScanNhapHang = findViewById(R.id.btnScanNhapHang); // Ánh xạ nút quét
        edtSoLuongNhap = findViewById(R.id.edtSoLuongNhap);
        btnThemHang = findViewById(R.id.btnThemHang);
        btnLuuPhieuNhap = findViewById(R.id.btnLuuPhieuNhap);
        rvChiTietNhap = findViewById(R.id.rvChiTietNhap);
        tvTongTien = findViewById(R.id.tvTongTien);
    }

    // --- HÀM MỚI: TÌM HÀNG HOÁ KHI QUÉT XONG ---
    private void timVaChonHangHoaTheoMa(String maVach) {
        boolean found = false;
        // Duyệt qua danh sách hàng hoá đã tải về từ Firebase
        for (HangHoa hh : listHangHoaFirebase) {
            // So sánh mã quét được với mã hàng hoá (không phân biệt hoa thường)
            if (hh.getMaHangHoa() != null && hh.getMaHangHoa().equalsIgnoreCase(maVach)) {
                selectedHangHoa = hh;

                // Hiển thị thông tin lên giao diện
                DecimalFormat df = new DecimalFormat("#,###");
                String info = selectedHangHoa.getTen() + " (" + selectedHangHoa.getMaHangHoa() + ")";
                tvHangHoaDaChon.setText(info);
                tvHangHoaDaChon.setError(null); // Xóa lỗi nếu có

                // Thông báo và đưa con trỏ chuột vào ô số lượng
                Toast.makeText(this, "Đã chọn: " + hh.getTen(), Toast.LENGTH_SHORT).show();
                edtSoLuongNhap.requestFocus();
                found = true;
                break;
            }
        }

        if (!found) {
            Toast.makeText(this, "Không tìm thấy sản phẩm có mã: " + maVach, Toast.LENGTH_LONG).show();
        }
    }
    // -------------------------------------------

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
                    tvHangHoaDaChon.setHint("--- Bấm chọn hoặc Quét mã ---");
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
        tvHangHoaDaChon.setText("");
        tvHangHoaDaChon.setHint("--- Bấm chọn hoặc Quét mã ---");
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