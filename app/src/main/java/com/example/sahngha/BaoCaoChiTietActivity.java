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

// Thêm các import này vào đầu file BaoCaoChiTietActivity.java
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

        // Báo cáo chi tiết
        if (loaiBaoCao != null) {
            switch (loaiBaoCao) {
                case ThongKeActivity.TON_KHO:
                    // Đặt tiêu đề cho TextView lớn
                    tvTieuDeBaoCao.setText("BÁO CÁO TỒN KHO HIỆN TẠI");
                    // Đặt tiêu đề cho Toolbar
                    getSupportActionBar().setTitle("Báo Cáo Tồn Kho");
                    // ... các logic khác ...
                    break;
                case ThongKeActivity.NHAP_HANG:
                    tvTieuDeBaoCao.setText("THỐNG KÊ NHẬP HÀNG"); // Đặt tiêu đề cho TextView lớn
                    getSupportActionBar().setTitle("Báo Cáo Nhập Hàng"); // Đặt tiêu đề cho Toolbar
                    // ... các logic khác ...
                    break;
                // ...
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

        // Đặt trạng thái đang tải
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

    // Sửa hàm: private void xuatBaoCao()

    private void xuatBaoCao() {
        String pdfContent;
        String pdfTitle;

        if (ThongKeActivity.TON_KHO.equals(loaiBaoCao)) {
            if (dsHangHoaResult.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu tồn kho để xuất.", Toast.LENGTH_SHORT).show();
                return;
            }
            pdfTitle = "Báo Cáo Tồn Kho";
            pdfContent = formatDataForPdf(); // Gọi hàm định dạng

        } else if (ThongKeActivity.NHAP_HANG.equals(loaiBaoCao)) {
            if (dsPhieuNhapResult.isEmpty()) {
                Toast.makeText(this, "Không có dữ liệu nhập hàng để xuất.", Toast.LENGTH_SHORT).show();
                return;
            }
            pdfTitle = "Báo Cáo Nhập Hàng";
            pdfContent = formatDataForPdf(); // Gọi hàm định dạng

        } else {
            return;
        }

        // GỌI HÀM TẠO PDF
        createPdf(pdfTitle, pdfContent);
    }

    // Tạo nội dung và xuất PDF
    private void createPdf(String title, String content) {
        // 1. Chuẩn bị File và Đường dẫn
        File pdfFile;
        // Tên file: BAOCAO_TEN_NGAYHIEN_TAI.pdf
        String fileName = "BAOCAO_" + title.replace(" ", "_").toUpperCase() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".pdf";

        // Lưu file vào thư mục Download (Dễ tìm nhất)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            // Đối với Android 10 trở lên, ta dùng MediaStore
            pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
        } else {
            // Đối với các phiên bản cũ hơn, dùng External Storage
            pdfFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        }

        // 2. Tạo đối tượng PdfDocument
        PdfDocument document = new PdfDocument();

        // 3. Thiết lập thuộc tính cho trang (Page)
        // Kích thước trang A4 (595x842 points)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();

        // 4. Mở trang và vẽ nội dung
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Thiết lập bút vẽ (Paint)
        Paint paint = new Paint();
        paint.setTextSize(12);

        // Định dạng nội dung
        int x = 40, y = 50;

        // Vẽ Tiêu đề (In đậm và lớn hơn)
        paint.setTextSize(18);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(title, pageInfo.getPageWidth() / 2, y, paint);

        y += 40;
        paint.setTextSize(12);
        paint.setTextAlign(Paint.Align.LEFT);

        // Vẽ nội dung (Chia nhỏ nội dung theo dòng)
        String[] lines = content.split("\n");
        for (String line : lines) {
            canvas.drawText(line, x, y, paint);
            y += 20; // Xuống dòng

            // Nếu hết trang, ta cần thêm trang mới (logic đơn giản)
            if (y > 800) {
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
        }

        // 5. Kết thúc trang
        document.finishPage(page);

        // 6. Lưu file
        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            Toast.makeText(this, "Xuất PDF thành công! Lưu tại: Downloads/" + fileName, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Lỗi khi lưu file PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
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
            for (PhieuNhap pn : dsPhieuNhapResult) {
                String time = new SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(new Date(pn.getThoiGianNhap()));
                sb.append(pn.getMaPhieu()).append(" | ")
                        .append(time).append(" | ")
                        .append(pn.getNguoiNhap()).append(" | ")
                        .append(decimalFormat.format(pn.getTongTien())).append("đ\n");

                // Nếu muốn thêm chi tiết từng món hàng nhập trong phiếu, logic sẽ phức tạp hơn ở đây.
            }
        }
        return sb.toString();
    }
}