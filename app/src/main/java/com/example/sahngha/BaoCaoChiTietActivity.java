package com.example.sahngha;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
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

public class BaoCaoChiTietActivity extends AppCompatActivity {

    // View (Đã xóa nút Xuất báo cáo)
    private Toolbar toolbarBaoCao;
    private TextView tvTieuDeBaoCao, tvTongSoLieu, tvNgayBatDau, tvNgayKetThuc;
    private Button btnChonNgayBatDau, btnChonNgayKetThuc;
    private ListView lvKetQuaBaoCao;

    // Firebase & Data
    private DatabaseReference firebaseRef;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private long thoiGianBatDau;
    private long thoiGianKetThuc;
    private ArrayList<PhieuNhap> dsPhieuNhapResult = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bao_cao_chi_tiet);

        anhXaViews();
        setupToolbar();
        setupDatePickers();

        tvTieuDeBaoCao.setText("BÁO CÁO THỐNG KÊ");
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("Báo Cáo Thống Kê");

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        firebaseRef = database.getReference("phieunhap");

        loadBaoCaoNhapHang();
    }

    private void anhXaViews() {
        toolbarBaoCao = findViewById(R.id.toolbarBaoCao);
        tvTieuDeBaoCao = findViewById(R.id.tvTieuDeBaoCao);
        tvTongSoLieu = findViewById(R.id.tvTongSoLieu);
        lvKetQuaBaoCao = findViewById(R.id.lvKetQuaBaoCao);
        tvNgayBatDau = findViewById(R.id.tvNgayBatDau);
        tvNgayKetThuc = findViewById(R.id.tvNgayKetThuc);
        btnChonNgayBatDau = findViewById(R.id.btnChonNgayBatDau);
        btnChonNgayKetThuc = findViewById(R.id.btnChonNgayKetThuc);
        // Đã xóa btnXuatBaoCao
    }

    private void setupToolbar() {
        setSupportActionBar(toolbarBaoCao);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbarBaoCao.setNavigationOnClickListener(v -> finish());
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
                    loadBaoCaoNhapHang();

                }, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadBaoCaoNhapHang() {
        dsPhieuNhapResult.clear();
        PhieuNhapAdapter adapter = new PhieuNhapAdapter(this, R.layout.item_phieunhap, dsPhieuNhapResult);
        lvKetQuaBaoCao.setAdapter(adapter);

        Query query = firebaseRef.orderByChild("thoiGianNhap").startAt(thoiGianBatDau).endAt(thoiGianKetThuc);
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

                String prefix = "Tổng giá trị (" + sdf.format(new Date(thoiGianBatDau)) + " - " + sdf.format(new Date(thoiGianKetThuc)) + "): ";
                String moneyPart = tongTienFormatted + "đ";
                String suffix = " (SL: " + dsPhieuNhapResult.size() + ")";

                String fullText = prefix + moneyPart + suffix;
                SpannableString spannable = new SpannableString(fullText);

                int start = prefix.length();
                int end = start + moneyPart.length();

                spannable.setSpan(new ForegroundColorSpan(Color.RED), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tvTongSoLieu.setText(spannable);

                adapter.notifyDataSetChanged();

                if (dsPhieuNhapResult.isEmpty()) {
                    Toast.makeText(BaoCaoChiTietActivity.this, "Không có dữ liệu trong khoảng thời gian này!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}