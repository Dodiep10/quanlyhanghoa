// File MỚI: app/src/main/java/com/example/sahngha/BaoCaoChiTietActivity.java
package com.example.sahngha;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.view.View;

public class BaoCaoChiTietActivity extends AppCompatActivity {

    private Toolbar toolbarBaoCao;
    private TextView tvTieuDeBaoCao, tvTongSoLieu, tvNgayBatDau, tvNgayKetThuc;
    private Button btnChonNgayBatDau, btnChonNgayKetThuc, btnXuatBaoCao;
    private ListView lvKetQuaBaoCao;
    private String loaiBaoCao;
    private DatabaseReference firebaseRef;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Để lưu trữ ngày đã chọn (Timestamp in milliseconds)
    private long thoiGianBatDau;
    private long thoiGianKetThuc;

    // Danh sách kết quả chung, sẽ được dùng cho Xuất báo cáo
    private ArrayList<HangHoa> dsHangHoaResult = new ArrayList<>();
    private ArrayList<PhieuNhap> dsPhieuNhapResult = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bao_cao_chi_tiet);

        anhXaViews();
        loaiBaoCao = getIntent().getStringExtra(ThongKeActivity.LOAI_BAO_CAO);

        // Cài đặt Toolbar
        setSupportActionBar(toolbarBaoCao);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarBaoCao.setNavigationOnClickListener(v -> finish());

        // Khởi tạo Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");

        // Thiết lập sự kiện cho Date Picker
        setupDatePickers();

        // Xử lý loại báo cáo
        if (loaiBaoCao != null) {
            switch (loaiBaoCao) {
                case ThongKeActivity.TON_KHO:
                    tvTieuDeBaoCao.setText("Báo Cáo Tồn Kho Hiện Tại");
                    getSupportActionBar().setTitle("Báo Cáo Tồn Kho");
                    // Tồn kho không phụ thuộc vào ngày, nên ta ẩn bộ chọn ngày
                    hideDatePickers();
                    firebaseRef = database.getReference("hanghoa");
                    loadBaoCaoTonKho();
                    break;
                case ThongKeActivity.NHAP_HANG:
                    tvTieuDeBaoCao.setText("Thống Kê Nhập Hàng");
                    getSupportActionBar().setTitle("Báo Cáo Nhập Hàng");
                    // Nhập hàng phụ thuộc vào ngày, sẽ tải khi chọn ngày
                    firebaseRef = database.getReference("phieunhap");
                    loadBaoCaoNhapHang();
                    break;
                default:
                    Toast.makeText(this, "Lỗi: Loại báo cáo không xác định!", Toast.LENGTH_SHORT).show();
                    finish();
            }
        }
    }

    // ====================== PHẦN ÁNH XẠ VÀ THIẾT LẬP GIAO DIỆN ======================

    private void anhXaViews() {
        toolbarBaoCao = findViewById(R.id.toolbarBaoCao);
        tvTieuDeBaoCao = findViewById(R.id.tvTieuDeBaoCao);
        tvTongSoLieu = findViewById(R.id.tvTongSoLieu);
        lvKetQuaBaoCao = findViewById(R.id.lvKetQuaBaoCao);

        tvNgayBatDau = findViewById(R.id.tvNgayBatDau);
        tvNgayKetThuc = findViewById(R.id.tvNgayKetThuc);
        btnChonNgayBatDau = findViewById(R.id.btnChonNgayBatDau);
        btnChonNgayKetThuc = findViewById(R.id.btnChonNgayKetThuc);
        btnXuatBaoCao = findViewById(R.id.btnXuatBaoCao);
    }

    private void hideDatePickers() {
        tvNgayBatDau.setVisibility(View.GONE);
        tvNgayKetThuc.setVisibility(View.GONE);
        btnChonNgayBatDau.setVisibility(View.GONE);
        btnChonNgayKetThuc.setVisibility(View.GONE);
        // Hiển thị nút Xuất báo cáo nếu có dữ liệu
        btnXuatBaoCao.setOnClickListener(v -> xuatBaoCao());
    }

    private void setupDatePickers() {
        // Khởi tạo ngày ban đầu (Mặc định: 30 ngày trước đến hôm nay)
        Calendar cal = Calendar.getInstance();
        thoiGianKetThuc = cal.getTimeInMillis(); // Hôm nay
        cal.add(Calendar.DAY_OF_YEAR, -30); // Lùi 30 ngày
        thoiGianBatDau = cal.getTimeInMillis();

        // Hiển thị ngày mặc định
        tvNgayBatDau.setText(sdf.format(new Date(thoiGianBatDau)));
        tvNgayKetThuc.setText(sdf.format(new Date(thoiGianKetThuc)));

        btnChonNgayBatDau.setOnClickListener(v -> showDatePicker(true)); // true = Ngày bắt đầu
        btnChonNgayKetThuc.setOnClickListener(v -> showDatePicker(false)); // false = Ngày kết thúc
        btnXuatBaoCao.setOnClickListener(v -> xuatBaoCao());
    }

    private void showDatePicker(final boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth, 23, 59, 59); // Set giờ cho ngày kết thúc (cuối ngày)
                    long selectedTime = calendar.getTimeInMillis();

                    if (isStartDate) {
                        calendar.set(year, month, dayOfMonth, 0, 0, 0); // Set giờ cho ngày bắt đầu (đầu ngày)
                        thoiGianBatDau = calendar.getTimeInMillis();
                        tvNgayBatDau.setText(sdf.format(new Date(thoiGianBatDau)));
                    } else {
                        // Đảm bảo ngày kết thúc luôn lớn hơn hoặc bằng ngày bắt đầu
                        if (selectedTime < thoiGianBatDau) {
                            Toast.makeText(BaoCaoChiTietActivity.this, "Ngày kết thúc không thể trước ngày bắt đầu!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        thoiGianKetThuc = selectedTime;
                        tvNgayKetThuc.setText(sdf.format(new Date(thoiGianKetThuc)));
                    }

                    // Sau khi chọn ngày, tự động tải lại báo cáo
                    if (ThongKeActivity.NHAP_HANG.equals(loaiBaoCao)) {
                        loadBaoCaoNhapHang();
                    }

                }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ====================== PHẦN TRUY VẤN DỮ LIỆU TỒN KHO ======================

    private void loadBaoCaoTonKho() {
        // Dùng lại Adapter HangHoaAdapter để hiển thị
        dsHangHoaResult.clear();
        HangHoaAdapter adapter = new HangHoaAdapter(this, R.layout.item_hanghoa, dsHangHoaResult);
        lvKetQuaBaoCao.setAdapter(adapter);

        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsHangHoaResult.clear();
                int tongSoLuongTon = 0;

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    HangHoa hh = itemSnapshot.getValue(HangHoa.class);
                    // Chỉ hiển thị hàng hóa có số lượng > 0
                    if (hh != null && hh.getSoLuong() > 0) {
                        dsHangHoaResult.add(hh);
                        tongSoLuongTon += hh.getSoLuong();
                    }
                }

                adapter.notifyDataSetChanged();
                tvTongSoLieu.setText("Tổng Số Lượng Tồn: " + tongSoLuongTon + " (Mặt hàng: " + dsHangHoaResult.size() + ")");

                if (dsHangHoaResult.isEmpty()) {
                    Toast.makeText(BaoCaoChiTietActivity.this, "Kho đang trống!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BaoCaoChiTietActivity.this, "Lỗi tải tồn kho: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ====================== PHẦN TRUY VẤN DỮ LIỆU NHẬP HÀNG (SỬ DỤNG NGÀY) ======================

    // File: app/src/main/java/com/example/sahngha/BaoCaoChiTietActivity.java

// ... (Các hàm khác giữ nguyên) ...

// ====================== PHẦN TRUY VẤN DỮ LIỆU NHẬP HÀNG (SỬ DỤNG NGÀY) ======================

    private void loadBaoCaoNhapHang() {
        // 1. Khởi tạo và Thiết lập Adapter cho ListView
        // Sử dụng PhieuNhapAdapter và item_phieu_nhap.xml (giả định tên file)
        dsPhieuNhapResult.clear();
        // Chú ý: Đảm bảo tên Class PhieuNhapAdapter ở đây khớp với tên Class của bạn mình!
        PhieuNhapAdapter adapter = new PhieuNhapAdapter(this, R.layout.item_phieunhap, dsPhieuNhapResult);
        lvKetQuaBaoCao.setAdapter(adapter);

        // 2. Truy vấn Firebase Realtime Database
        // Sắp xếp theo thoiGianNhap và lọc theo phạm vi ngày đã chọn
        Query query = firebaseRef.orderByChild("thoiGianNhap")
                .startAt(thoiGianBatDau)
                .endAt(thoiGianKetThuc);

        tvTongSoLieu.setText("Đang tải dữ liệu...");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsPhieuNhapResult.clear();
                double tongGiaTriNhap = 0;

                for (DataSnapshot phieuSnapshot : snapshot.getChildren()) {
                    PhieuNhap phieu = phieuSnapshot.getValue(PhieuNhap.class);
                    if (phieu != null) {
                        dsPhieuNhapResult.add(phieu);
                        tongGiaTriNhap += phieu.getTongTien();
                    }
                }

                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
                symbols.setGroupingSeparator('.');
                DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
                String tongTienFormatted = decimalFormat.format(tongGiaTriNhap);

                // Cập nhật TextView tổng số liệu
                tvTongSoLieu.setText("Tổng giá trị nhập hàng (" + sdf.format(new Date(thoiGianBatDau)) + " - " + sdf.format(new Date(thoiGianKetThuc)) + "): " + tongTienFormatted + "đ (Số phiếu: " + dsPhieuNhapResult.size() + ")");

                // Cập nhật ListView
                adapter.notifyDataSetChanged();

                if (dsPhieuNhapResult.isEmpty()) {
                    Toast.makeText(BaoCaoChiTietActivity.this, "Không có phiếu nhập trong phạm vi ngày đã chọn.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(BaoCaoChiTietActivity.this, "Lỗi tải nhập hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ====================== PHẦN XUẤT BÁO CÁO (CHƯA CODE CHI TIẾT) ======================

    private void xuatBaoCao() {
        if (ThongKeActivity.TON_KHO.equals(loaiBaoCao)) {
            if (dsHangHoaResult.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu tồn kho để xuất.", Toast.LENGTH_SHORT).show();
                return;
            }
            // BẮT ĐẦU CODE XUẤT FILE TỪ dsHangHoaResult
            Toast.makeText(this, "Đang xuất Báo Cáo Tồn Kho...", Toast.LENGTH_SHORT).show();
            // Chức năng xuất PDF/Excel sẽ được code ở bước tiếp theo
        } else if (ThongKeActivity.NHAP_HANG.equals(loaiBaoCao)) {
            if (dsPhieuNhapResult.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu nhập hàng để xuất.", Toast.LENGTH_SHORT).show();
                return;
            }
            // BẮT ĐẦU CODE XUẤT FILE TỪ dsPhieuNhapResult
            Toast.makeText(this, "Đang xuất Báo Cáo Nhập Hàng...", Toast.LENGTH_SHORT).show();
            // Chức năng xuất PDF/Excel sẽ được code ở bước tiếp theo
        }
    }
}