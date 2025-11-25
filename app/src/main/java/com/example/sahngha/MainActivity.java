package com.example.sahngha;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.Button;
import android.view.View;
import android.widget.AdapterView; // Cần thiết cho setOnItemClickListener

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements HangHoaAdapter.OnItemActionListener {

    ListView lvHangHoa;
    HangHoaAdapter adapter;

    ArrayList<HangHoa> dsHangHoa;
    ArrayList<HangHoa> originalList;

    EditText edtTimHh;
    TextView txtTong;
    Button btn_Them;

    FirebaseDatabase database;
    DatabaseReference hangHoaRef;

    // ĐÃ BỎ: private boolean isSelectorMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ
        txtTong = findViewById(R.id.txtTong);
        lvHangHoa = findViewById(R.id.lvHangHoa);
        btn_Them = findViewById(R.id.btnThem);
        edtTimHh = findViewById(R.id.edt_timhh);

        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        hangHoaRef = database.getReference("hanghoa");

        dsHangHoa = new ArrayList<>();
        originalList = new ArrayList<>();

        adapter = new HangHoaAdapter(this, R.layout.item_hanghoa, dsHangHoa);
        adapter.setOnItemActionListener(this);

        lvHangHoa.setAdapter(adapter);

        // Lấy dữ liệu từ Firebase
        layDuLieuFirebase();

        // === LOGIC CŨ: CHẾ ĐỘ QUẢN LÝ MẶC ĐỊNH ===

        // 1. Nút Thêm Hàng
        btn_Them.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, themhanghoa.class);
            startActivity(intent);
        });

        // 2. Click Item (Xem Chi tiết Hàng hóa)
        lvHangHoa.setOnItemClickListener((parent, view, position, id) -> {
            HangHoa selectedHangHoa = dsHangHoa.get(position);

            // Logic Mở màn hình ChiTietHangHoaActivity
            Intent detailIntent = new Intent(MainActivity.this, ChiTietHangHoaActivity.class);
            detailIntent.putExtra("MA_HANG_HOA", selectedHangHoa.getMaHangHoa());
            startActivity(detailIntent);
        });

        // Thiết lập chức năng tìm kiếm
        setupSearchListener();
    }

    // ĐÃ BỎ: setupSelectorModeListener()

    // HÀM THIẾT LẬP LẮNG NGHE TÌM KIẾM (GIỮ NGUYÊN)
    private void setupSearchListener() {
        edtTimHh.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Gọi hàm lọc mỗi khi văn bản thay đổi
                filterList(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    // HÀM LỌC DỮ LIỆU (GIỮ NGUYÊN)
    private void filterList(String query) {
        String lowerCaseQuery = query.toLowerCase().trim();

        dsHangHoa.clear();

        if (lowerCaseQuery.isEmpty()) {
            // Nếu ô tìm kiếm trống, hiển thị lại toàn bộ danh sách gốc
            dsHangHoa.addAll(originalList);
        } else {
            // Duyệt qua danh sách gốc (originalList)
            for (HangHoa item : originalList) {
                // Kiểm tra Mã hàng hóa và Tên hàng hóa
                boolean isMatch = false;

                // 1. Kiểm tra Mã hàng hóa
                if (item.getMaHangHoa() != null && item.getMaHangHoa().toLowerCase().contains(lowerCaseQuery)) {
                    isMatch = true;
                }

                // 2. Kiểm tra Tên hàng hóa (chỉ kiểm tra nếu chưa khớp Mã)
                if (!isMatch && item.getTen() != null && item.getTen().toLowerCase().contains(lowerCaseQuery)) {
                    isMatch = true;
                }

                if (isMatch) {
                    dsHangHoa.add(item);
                }
            }
        }

        // Cập nhật Adapter và Tổng số
        adapter.notifyDataSetChanged();
        capNhatTong();
    }

    // HÀM layDuLieuFirebase (GIỮ NGUYÊN)
    private void layDuLieuFirebase() {
        hangHoaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsHangHoa.clear();
                originalList.clear();

                for (DataSnapshot item : snapshot.getChildren()) {
                    HangHoa hh = item.getValue(HangHoa.class);
                    originalList.add(hh); // Thêm vào danh sách gốc
                }

                // Hiển thị tất cả dữ liệu gốc lên ListView lần đầu
                dsHangHoa.addAll(originalList);

                adapter.notifyDataSetChanged();
                capNhatTong();

                // Lọc lại dữ liệu ngay sau khi load xong nếu người dùng đã gõ tìm kiếm
                filterList(edtTimHh.getText().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu", error.toException());
            }
        });
    }

    private void capNhatTong() {
        // Cập nhật tổng dựa trên dsHangHoa (danh sách hiển thị)
        txtTong.setText("Tổng: " + dsHangHoa.size() + " sản phẩm");
    }


    @Override
    public void onDeleteClick(String hangHoaId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa hàng hóa có mã " + hangHoaId + " không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    xoaHangHoaTrenFirebase(hangHoaId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void xoaHangHoaTrenFirebase(String hangHoaId) {
        DatabaseReference itemRef = hangHoaRef.child(hangHoaId);

        itemRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Xóa hàng hóa thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Firebase", "Lỗi khi xóa dữ liệu", e);
                });
    }
}