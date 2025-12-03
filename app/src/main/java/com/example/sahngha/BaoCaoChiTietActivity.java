// File MỚI: app/src/main/java/com.example.sahngha/BaoCaoChiTietActivity.java
package com.example.sahngha;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class BaoCaoChiTietActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private Toolbar toolbarBaoCao;
    private TextView tvTieuDeBaoCao, tvTongSoLieu, tvNgayBatDau, tvNgayKetThuc;
    private Button btnChonNgayBatDau, btnChonNgayKetThuc, btnXuatBaoCao;
    private ListView lvKetQuaBaoCao;
    private String loaiBaoCao;
    private DatabaseReference firebaseRef;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    private long thoiGianBatDau;
    private long thoiGianKetThuc;

    private ArrayList<HangHoa> dsHangHoaResult = new ArrayList<>();
    private ArrayList<PhieuNhap> dsPhieuNhapResult = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bao_cao_chi_tiet);

        anhXaViews();
        loaiBaoCao = getIntent().getStringExtra(ThongKeActivity.LOAI_BAO_CAO);

        setupToolbar();
        setupDatePickers();

        // Khởi tạo Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");

        // XỬ LÝ LOẠI BÁO CÁO (KHỐI SWITCH DUY NHẤT)
        if (loaiBaoCao != null) {
            switch (loaiBaoCao) {
                case ThongKeActivity.TON_KHO:
                    tvTieuDeBaoCao.setText("BÁO CÁO TỒN KHO HIỆN TẠI");
                    getSupportActionBar().setTitle("Báo Cáo Tồn Kho");
                    hideDatePickers();
                    firebaseRef = database.getReference("hanghoa");
                    loadBaoCaoTonKho();
                    break;
                case ThongKeActivity.NHAP_HANG:
                    tvTieuDeBaoCao.setText("THỐNG KÊ NHẬP HÀNG");
                    getSupportActionBar().setTitle("Báo Cáo Nhập Hàng");
                    firebaseRef = database.getReference("phieunhap");
                    loadBaoCaoNhapHang();
                    break;
                default:
                    Toast.makeText(this, "Lỗi: Loại báo cáo không xác định!", Toast.LENGTH_SHORT).show();
                    finish();
            }
        }
    }

    // ====================== PHẦN THIẾT LẬP GIAO DIỆN ======================

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

    private void setupToolbar() {
        setSupportActionBar(toolbarBaoCao);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbarBaoCao.setNavigationOnClickListener(v -> finish());
    }

    private void hideDatePickers() {
        tvNgayBatDau.setVisibility(View.GONE);
        tvNgayKetThuc.setVisibility(View.GONE);
        btnChonNgayBatDau.setVisibility(View.GONE);
        btnChonNgayKetThuc.setVisibility(View.GONE);
    }

    private void setupDatePickers() {
        Calendar cal = Calendar.getInstance();
        thoiGianKetThuc = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -30);
        thoiGianBatDau = cal.getTimeInMillis();

        tvNgayBatDau.setText(sdf.format(new Date(thoiGianBatDau)));
        tvNgayKetThuc.setText(sdf.format(new Date(thoiGianKetThuc)));

        btnChonNgayBatDau.setOnClickListener(v -> showDatePicker(true));
        btnChonNgayKetThuc.setOnClickListener(v -> showDatePicker(false));
        btnXuatBaoCao.setOnClickListener(v -> xuatBaoCao());
    }

    private void showDatePicker(final boolean isStartDate) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth, 23, 59, 59);
                    long selectedTime = calendar.getTimeInMillis();

                    if (isStartDate) {
                        calendar.set(year, month, dayOfMonth, 0, 0, 0);
                        thoiGianBatDau = calendar.getTimeInMillis();
                        tvNgayBatDau.setText(sdf.format(new Date(thoiGianBatDau)));
                    } else {
                        if (selectedTime < thoiGianBatDau) {
                            Toast.makeText(BaoCaoChiTietActivity.this, "Ngày kết thúc không thể trước ngày bắt đầu!", Toast.LENGTH_LONG).show();
                            return;
                        }
                        thoiGianKetThuc = selectedTime;
                        tvNgayKetThuc.setText(sdf.format(new Date(thoiGianKetThuc)));
                    }

                    if (ThongKeActivity.NHAP_HANG.equals(loaiBaoCao)) {
                        loadBaoCaoNhapHang();
                    }

                }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // ====================== PHẦN TRUY VẤN DỮ LIỆU ======================

    private void loadBaoCaoTonKho() {
        dsHangHoaResult.clear();
        HangHoaAdapter adapter = new HangHoaAdapter(this, R.layout.item_hanghoa, dsHangHoaResult);
        lvKetQuaBaoCao.setAdapter(adapter);
        tvTongSoLieu.setText("Đang tải dữ liệu tồn kho...");

        firebaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsHangHoaResult.clear();
                int tongSoLuongTon = 0;

                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    HangHoa hh = itemSnapshot.getValue(HangHoa.class);
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
                Log.e("Firebase", "Lỗi tải tồn kho: " + error.getMessage());
                Toast.makeText(BaoCaoChiTietActivity.this, "Lỗi tải tồn kho: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                tvTongSoLieu.setText("LỖI: Không tải được dữ liệu tồn kho.");
            }
        });
    }

    private void loadBaoCaoNhapHang() {
        dsPhieuNhapResult.clear();
        PhieuNhapAdapter adapter = new PhieuNhapAdapter(this, R.layout.item_phieunhap, dsPhieuNhapResult);
        lvKetQuaBaoCao.setAdapter(adapter);

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
                    PhieuNhap phieu = phieuSnapshot.getValue(PhieuNhap.class); // SỬ DỤNG Phieunhap
                    if (phieu != null) {
                        dsPhieuNhapResult.add(phieu);
                        tongGiaTriNhap += phieu.getTongTien();
                    }
                }

                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
                symbols.setGroupingSeparator('.');
                DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
                String tongTienFormatted = decimalFormat.format(tongGiaTriNhap);

                tvTongSoLieu.setText("Tổng giá trị nhập hàng (" + sdf.format(new Date(thoiGianBatDau)) + " - " + sdf.format(new Date(thoiGianKetThuc)) + "): " + tongTienFormatted + "đ (Số phiếu: " + dsPhieuNhapResult.size() + ")");

                adapter.notifyDataSetChanged();

                if (dsPhieuNhapResult.isEmpty()) {
                    Toast.makeText(BaoCaoChiTietActivity.this, "Không có phiếu nhập trong phạm vi ngày đã chọn.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải nhập hàng: " + error.getMessage());
                Toast.makeText(BaoCaoChiTietActivity.this, "Lỗi tải nhập hàng: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                tvTongSoLieu.setText("LỖI: Không tải được dữ liệu nhập hàng.");
            }
        });
    }

    // ====================== PHẦN XUẤT BÁO CÁO VÀ QUYỀN ======================

    private void xuatBaoCaoImplementation() {
        String pdfContent;
        String pdfTitle;

        if (ThongKeActivity.TON_KHO.equals(loaiBaoCao)) {
            if (dsHangHoaResult.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu tồn kho để xuất.", Toast.LENGTH_SHORT).show();
                return;
            }
            pdfTitle = "Báo Cáo Tồn Kho";
            pdfContent = formatDataForPdf();

        } else if (ThongKeActivity.NHAP_HANG.equals(loaiBaoCao)) {
            if (dsPhieuNhapResult.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu nhập hàng để xuất.", Toast.LENGTH_SHORT).show();
                return;
            }
            pdfTitle = "Báo Cáo Nhập Hàng";
            pdfContent = formatDataForPdf();

        } else {
            return;
        }

        createPdf(pdfTitle, pdfContent);
    }

    // HÀM KIỂM TRA VÀ YÊU CẦU QUYỀN
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PERMISSION_GRANTED) {

            xuatBaoCaoImplementation();

        } else {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    // HÀM GỌI KHI NHẤN NÚT (CHỈ KIỂM TRA QUYỀN)
    private void xuatBaoCao() {
        checkAndRequestPermissions();
    }

    // XỬ LÝ KẾT QUẢ YÊU CẦU QUYỀN
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED) {
                xuatBaoCaoImplementation();
            } else {
                Toast.makeText(this, "Không thể xuất báo cáo do thiếu quyền truy cập bộ nhớ.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Định dạng dữ liệu thành chuỗi string
    private String formatDataForPdf() {
        StringBuilder sb = new StringBuilder();
        DecimalFormat decimalFormat = new DecimalFormat("#,###");

        if (ThongKeActivity.TON_KHO.equals(loaiBaoCao)) {
            sb.append("Tình trạng Tồn Kho Hiện Tại:\n");
            sb.append("Mã hàng | Tên hàng | Số lượng tồn | Giá bán\n");
            sb.append("------------------------------------------\n");
            for (HangHoa hh : dsHangHoaResult) {
                sb.append(hh.getMaHangHoa()).append(" | ")
                        .append(hh.getTen()).append(" | ")
                        .append(hh.getSoLuong()).append(" | ")
                        .append(decimalFormat.format(hh.getGia())).append("đ\n");
            }
        } else if (ThongKeActivity.NHAP_HANG.equals(loaiBaoCao)) {
            sb.append("Báo Cáo Nhập Hàng: ").append(sdf.format(new Date(thoiGianBatDau)))
                    .append(" đến ").append(sdf.format(new Date(thoiGianKetThuc))).append("\n\n");
            sb.append("Mã Phiếu | Thời gian nhập | Người nhập | Tổng tiền\n");
            sb.append("---------------------------------------------------\n");
            for (PhieuNhap pn : dsPhieuNhapResult) { // SỬ DỤNG Phieunhap
                String time = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(new Date(pn.getThoiGianNhap()));
                sb.append(pn.getMaPhieu()).append(" | ")
                        .append(time).append(" | ")
                        .append(pn.getNguoiNhap()).append(" | ")
                        .append(decimalFormat.format(pn.getTongTien())).append("đ\n");
            }
        }
        return sb.toString();
    }

    // Tạo nội dung và xuất PDF
    private void createPdf(String title, String content) {
        File pdfFile;
        String fileName = "BAOCAO_" + title.replace(" ", "_").toUpperCase() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        File reportsDir = new File(getExternalFilesDir(null), "Reports");
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
        pdfFile = new File(reportsDir, fileName);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);

        int x = 40, y = 50;

        // Vẽ Tiêu đề
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(title, pageInfo.getPageWidth() / 2, y, paint);

        y += 40;
        paint.setTextSize(12);
        paint.setTextAlign(Paint.Align.LEFT);

        String[] lines = content.split("\n");
        for (String line : lines) {
            canvas.drawText(line, x, y, paint);
            y += 20;

            if (y > 800) {
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
        }

        document.finishPage(page);

        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            Toast.makeText(this, "Xuất PDF thành công! Lưu tại thư mục Reports của ứng dụng.", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Log.e("PDF_EXPORT", "Lỗi khi lưu file: " + e.getMessage());
            Toast.makeText(this, "Lỗi khi lưu file PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}