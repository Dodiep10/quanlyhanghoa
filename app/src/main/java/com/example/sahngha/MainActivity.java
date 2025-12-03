package com.example.sahngha;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout; // [QUAN TRỌNG] Thêm thư viện này
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements HangHoaAdapter.OnItemActionListener {

    // --- CÁC BIẾN GIAO DIỆN ---
    ListView lvHangHoa;
    HangHoaAdapter adapter;
    EditText edtTimHh;
    TextView txtTong;
    Button btn_Them;

    RecyclerView rvLoaiHang;
    LoaiHangAdapter loaiHangAdapter;
    ArrayList<LoaiHang> dsLoaiHang;

    // --- BIẾN DỮ LIỆU ---
    ArrayList<HangHoa> dsHangHoa;       // Danh sách hiển thị
    ArrayList<HangHoa> originalList;    // Danh sách gốc

    FirebaseDatabase database;
    DatabaseReference hangHoaRef;
    DatabaseReference loaiHangRef;

    // Biến lưu trạng thái lọc
    String currentCategoryFilter = "";

    // Biến lưu sticker trong Dialog thêm mới (Mặc định là ngôi sao)
    private String selectedSticker = "star";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // --- 1. ÁNH XẠ VIEW ---
        txtTong = findViewById(R.id.txtTong);
        lvHangHoa = findViewById(R.id.lvHangHoa);
        btn_Them = findViewById(R.id.btnThem);
        edtTimHh = findViewById(R.id.edt_timhh);
        rvLoaiHang = findViewById(R.id.rvLoaiHang);

        // --- 2. KHỞI TẠO FIREBASE ---
        database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        hangHoaRef = database.getReference("hanghoa");
        loaiHangRef = database.getReference("loaihanghoa");

        // --- 3. CẤU HÌNH DANH SÁCH HÀNG HÓA (LISTVIEW) ---
        dsHangHoa = new ArrayList<>();
        originalList = new ArrayList<>();
        adapter = new HangHoaAdapter(this, R.layout.item_hanghoa, dsHangHoa);
        adapter.setOnItemActionListener(this);
        lvHangHoa.setAdapter(adapter);

        // --- 4. CẤU HÌNH DANH MỤC LOẠI HÀNG (RECYCLERVIEW) ---
        dsLoaiHang = new ArrayList<>();

        // Thiết lập dạng danh sách nằm ngang (Horizontal)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvLoaiHang.setLayoutManager(layoutManager);

        // Khởi tạo Adapter cho loại hàng
        loaiHangAdapter = new LoaiHangAdapter(this, dsLoaiHang, new LoaiHangAdapter.OnLoaiHangClickListener() {
            @Override
            public void onItemClick(LoaiHang loaiHang) {
                // Khi bấm vào một loại hàng -> Thực hiện lọc
                apDungBoLocLoai(loaiHang.getTenLoai());
            }

            @Override
            public void onAddClick() {
                // Khi bấm vào nút "Thêm" -> Hiện Dialog
                hienThiDialogThemLoaiHang();
            }
        });
        rvLoaiHang.setAdapter(loaiHangAdapter);

        // --- 5. LẤY DỮ LIỆU TỪ FIREBASE ---
        layDuLieuFirebase();       // Lấy danh sách hàng hóa
        layDanhSachLoaiHang();     // Lấy danh sách loại hàng

        // --- 6. XỬ LÝ CÁC SỰ KIỆN KHÁC ---
        btn_Them.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, themhanghoa.class);
            startActivity(intent);
        });

        lvHangHoa.setOnItemClickListener((parent, view, position, id) -> {
            HangHoa selectedHangHoa = dsHangHoa.get(position);
            Intent detailIntent = new Intent(MainActivity.this, ChiTietHangHoaActivity.class);
            detailIntent.putExtra("MA_HANG_HOA", selectedHangHoa.getMaHangHoa());
            startActivity(detailIntent);
        });

        edtTimHh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterData(s.toString(), currentCategoryFilter);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // === [CẬP NHẬT] HÀM LẤY DANH SÁCH LOẠI ===
    private void layDanhSachLoaiHang() {
        loaiHangRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsLoaiHang.clear();
                dsLoaiHang.add(new LoaiHang("def1", "Đồ ăn vặt", "star"));
                dsLoaiHang.add(new LoaiHang("def2", "Nước ngọt", "start"));
                dsLoaiHang.add(new LoaiHang("def3", "Bánh", "book"));
                dsLoaiHang.add(new LoaiHang("def4", "Kẹo", "home"));

                // 2. Thêm các danh mục từ Firebase
                for (DataSnapshot data : snapshot.getChildren()) {
                    String ten = data.child("tenLoai").getValue(String.class);
                    String icon = data.child("icon").getValue(String.class);
                    String id = data.getKey();

                    // Kiểm tra để tránh trùng lặp với danh mục mặc định
                    if (ten != null && !isDuplicated(ten)) {
                        dsLoaiHang.add(new LoaiHang(id, ten, icon));
                    }
                }
                loaiHangAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải loại hàng: " + error.getMessage());
            }
        });
    }

    // Hàm kiểm tra trùng lặp tên loại
    private boolean isDuplicated(String ten) {
        for (LoaiHang item : dsLoaiHang) {
            if (item.getTenLoai().equalsIgnoreCase(ten)) return true;
        }
        return false;
    }

    private void apDungBoLocLoai(String loaiCanLoc) {
        if (currentCategoryFilter.equals(loaiCanLoc)) {
            currentCategoryFilter = "";
        } else {
            currentCategoryFilter = loaiCanLoc;
        }
        filterData(edtTimHh.getText().toString(), currentCategoryFilter);
        loaiHangAdapter.setSelectedId(currentCategoryFilter);
    }

    // === [CẬP NHẬT] HÀM HIỂN THỊ DIALOG VỚI GRIDLAYOUT ===
    private void hienThiDialogThemLoaiHang() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_them_loai_custom);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        EditText edtTen = dialog.findViewById(R.id.edtTenLoai);
        Button btnHuy = dialog.findViewById(R.id.btnHuy);
        Button btnThem = dialog.findViewById(R.id.btnXacNhanThem);

        // Lấy Grid chứa các sticker (Thay vì ánh xạ từng ảnh)
        GridLayout gridStickers = dialog.findViewById(R.id.gridStickers);

        // Sự kiện khi bấm vào 1 sticker bất kỳ
        View.OnClickListener stickerClickListener = v -> {
            // 1. Xóa nền chọn của tất cả sticker
            for (int i = 0; i < gridStickers.getChildCount(); i++) {
                View child = gridStickers.getChildAt(i);
                child.setBackgroundResource(0);
            }

            // 2. Đánh dấu sticker vừa chọn
            v.setBackgroundResource(R.drawable.bg_sticker_selected);

            // 3. Lấy tag để lưu (star, rotate, book, home...)
            if (v.getTag() != null) {
                selectedSticker = v.getTag().toString();
            }
        };

        // Gán sự kiện click cho tất cả con trong Grid
        for (int i = 0; i < gridStickers.getChildCount(); i++) {
            View child = gridStickers.getChildAt(i);
            child.setOnClickListener(stickerClickListener);
        }

        // Mặc định chọn cái đầu tiên (Ngôi sao)
        if (gridStickers.getChildCount() > 0) {
            gridStickers.getChildAt(0).performClick();
        }

        btnHuy.setOnClickListener(v -> dialog.dismiss());

        btnThem.setOnClickListener(v -> {
            String tenLoai = edtTen.getText().toString().trim();
            if (!tenLoai.isEmpty()) {
                luuLoaiHangLenFirebase(tenLoai, selectedSticker);
                dialog.dismiss();
            } else {
                Toast.makeText(MainActivity.this, "Vui lòng nhập tên loại!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void luuLoaiHangLenFirebase(String tenLoai, String iconTag) {
        String key = loaiHangRef.push().getKey();
        if (key != null) {
            DatabaseReference newItem = loaiHangRef.child(key);
            newItem.child("tenLoai").setValue(tenLoai);
            newItem.child("icon").setValue(iconTag)
                    .addOnSuccessListener(aVoid -> Toast.makeText(MainActivity.this, "Đã thêm: " + tenLoai, Toast.LENGTH_SHORT).show());
        }
    }

    private void filterData(String keyword, String category) {
        String lowerKey = keyword.toLowerCase().trim();
        dsHangHoa.clear();

        for (HangHoa item : originalList) {
            boolean matchCategory = category.isEmpty() ||
                    (item.getLoaiHangHoa() != null && item.getLoaiHangHoa().equals(category));

            boolean matchKeyword = lowerKey.isEmpty() ||
                    (item.getTen() != null && item.getTen().toLowerCase().contains(lowerKey)) ||
                    (item.getMaHangHoa() != null && item.getMaHangHoa().toLowerCase().contains(lowerKey));

            if (matchCategory && matchKeyword) {
                dsHangHoa.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        capNhatTong();
    }

    private void layDuLieuFirebase() {
        hangHoaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsHangHoa.clear();
                originalList.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    HangHoa hh = item.getValue(HangHoa.class);
                    if (hh != null) originalList.add(hh);
                }
                filterData(edtTimHh.getText().toString(), currentCategoryFilter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void capNhatTong() {
        txtTong.setText("Tổng: " + dsHangHoa.size() + " sản phẩm");
    }

    @Override
    public void onDeleteClick(String hangHoaId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa hàng hóa")
                .setMessage("Bạn có chắc muốn xóa?")
                .setPositiveButton("Xóa", (dialog, which) -> hangHoaRef.child(hangHoaId).removeValue())
                .setNegativeButton("Hủy", null)
                .show();
    }
}