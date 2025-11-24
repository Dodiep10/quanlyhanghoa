package com.example.sahngha;

import android.content.DialogInterface; // Import mới
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast; // Import mới

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog; // Import mới
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener; // Import mới
import com.google.android.gms.tasks.OnSuccessListener; // Import mới
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.widget.Button;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements HangHoaAdapter.OnItemActionListener {

    ListView lvHangHoa;
    HangHoaAdapter adapter;
    ArrayList<HangHoa> dsHangHoa;
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
        // Khởi tạo Firebase
        database = FirebaseDatabase.getInstance("https://quanlyhanghoa-e4135-default-rtdb.asia-southeast1.firebasedatabase.app/");
        hangHoaRef = database.getReference("hanghoa"); // trỏ đến nhánh lưu trữ dữ liệu hàng hóa

        dsHangHoa = new ArrayList<>();
        adapter = new HangHoaAdapter(this, R.layout.item_hanghoa, dsHangHoa);
        adapter.setOnItemActionListener(this);

        lvHangHoa.setAdapter(adapter);
        // Lấy dữ liệu từ Firebase
        layDuLieuFirebase();
        btn_Them.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, themhanghoa.class); // tạo intent để chuyển từ main qua thêm hàng hóa
            startActivity(intent);
        });
    }
    private void layDuLieuFirebase() {
        hangHoaRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dsHangHoa.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    HangHoa hh = item.getValue(HangHoa.class);
                    dsHangHoa.add(hh);
                }
                adapter.notifyDataSetChanged();
                capNhatTong();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi đọc dữ liệu", error.toException());
            }
        });
    }
    private void capNhatTong() {
        txtTong.setText("Tổng: " + dsHangHoa.size() + " sản phẩm");
    }


    @Override
    public void onDeleteClick(String hangHoaId, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa hàng hóa có mã " + hangHoaId + " không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi hàm xóa Firebase
                    xoaHangHoaTrenFirebase(hangHoaId);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // 4. HÀM THỰC HIỆN XÓA TRÊN FIREBASE
    private void xoaHangHoaTrenFirebase(String hangHoaId) {
        // Tham chiếu đến chính node hàng hóa cần xóa trong Firebase
        DatabaseReference itemRef = hangHoaRef.child(hangHoaId);

        itemRef.removeValue() // Lệnh xóa
                .addOnSuccessListener(aVoid -> {
                    // Xóa thành công trên Firebase
                    Toast.makeText(MainActivity.this, "Xóa hàng hóa thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("Firebase", "Lỗi khi xóa dữ liệu", e);
                });
    }
}