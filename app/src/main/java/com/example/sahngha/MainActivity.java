package com.example.sahngha;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText; // Đã đổi tên biến sang edtTimHh
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HangHoaAdapter.OnItemActionListener {

    ListView lvHangHoa;
    HangHoaAdapter adapter;

    // dsHangHoa sẽ đóng vai trò là danh sách hiển thị (displayList)
    ArrayList<HangHoa> dsHangHoa;
    // originalList dùng để lưu trữ dữ liệu gốc, không bị thay đổi khi tìm kiếm
    ArrayList<HangHoa> originalList;

    // Đã đổi tên biến sang edtTimHh, ánh xạ tới R.id.edt_timhh
    EditText edtTimHh;

    TextView txtTong;
    Button btn_Them;

    // Firebase database
    FirebaseDatabase database;
    DatabaseReference hangHoaRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ánh xạ
        txtTong = findViewById(R.id.txtTong);
        lvHangHoa = findViewById(R.id.lvHangHoa);
        btn_Them = findViewById(R.id.btnThem);
        // Ánh xạ EditText tìm kiếm với ID mới: R.id.edt_timhh
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

        btn_Them.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, themhanghoa.class);
            startActivity(intent);
        });

        // Thiết lập chức năng tìm kiếm
        setupSearchListener();
    }

    // HÀM THIẾT LẬP LẮNG NGHE TÌM KIẾM
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

    // HÀM LỌC DỮ LIỆU
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

    // CHÚ Ý: CHỈNH SỬA HÀM layDuLieuFirebase()
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