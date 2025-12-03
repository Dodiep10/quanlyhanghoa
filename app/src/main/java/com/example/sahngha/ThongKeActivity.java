// File MỚI: app/src/main/java/com.example.sahngha/ThongKeActivity.java
package com.example.sahngha;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

// Đây là lớp trung gian, nhiệm vụ chính là chứa các hằng số cần thiết
// và sẽ được dùng để khởi tạo giao diện chọn loại báo cáo.

public class ThongKeActivity extends AppCompatActivity {

    // Các Hằng số để gửi thông tin loại báo cáo
    public static final String LOAI_BAO_CAO = "LOAI_BAO_CAO";
    public static final String NHAP_HANG = "NHAP_HANG";
    public static final String TON_KHO = "TON_KHO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Do màn hình này cũng cần hiển thị, ta cần thiết lập layout
        // Bạn sẽ cần tạo activity_thong_ke.xml sau, nhưng tạm thời comment để tránh lỗi
        // setContentView(R.layout.activity_thong_ke); 
    }
}