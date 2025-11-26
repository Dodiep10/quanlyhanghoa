package com.example.sahngha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class HangHoaAdapter extends ArrayAdapter<HangHoa> {

    // 1. INTERFACE LISTENER (ĐỂ BẮT SỰ KIỆN XÓA)
    public interface OnItemActionListener {
        void onDeleteClick(String hangHoaId, int position);
        // Có thể thêm onViewDetailClick nếu cần
    }

    private OnItemActionListener listener;

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public HangHoaAdapter(@NonNull Context context, int resource, @NonNull ArrayList<HangHoa> dsHangHoa) {
        super(context, resource, dsHangHoa);
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Tái sử dụng View
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            // Giả định layout item cho hàng hóa là item_hanghoa
            convertView = inflater.inflate(R.layout.item_hanghoa, parent, false);
        }

        // Ánh xạ Views
        TextView txtMa = convertView.findViewById(R.id.tvMaHangHoa);
        TextView txtTen = convertView.findViewById(R.id.txtTenHang);
        TextView txtGia = convertView.findViewById(R.id.txtGia);
        TextView txtSoLuong = convertView.findViewById(R.id.txtSoLuong);
        ImageButton btnXoa = convertView.findViewById(R.id.btnXoa);

        final HangHoa hangHoa = getItem(position);
        if (hangHoa != null) {

            // Định dạng tiền tệ
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);

            // Đổ dữ liệu
            txtTen.setText(hangHoa.getTen());
            txtMa.setText("Mã: " + hangHoa.getMaHangHoa());
            txtGia.setText("Giá: " + decimalFormat.format(hangHoa.getGia()) + "đ");
            txtSoLuong.setText("Số lượng: " + hangHoa.getSoLuong());


            // 2. GẮN SỰ KIỆN CLICK CHO NÚT XÓA (Chỉ xử lý nút Xóa)
            if (btnXoa != null) {
                btnXoa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            listener.onDeleteClick(hangHoa.getMaHangHoa(), position);
                        }
                    }
                });
            }

            // LƯU Ý QUAN TRỌNG:
            // KHÔNG ĐẶT convertView.setOnClickListener ở đây.
            // Logic Xem Chi Tiết/Trả Về Dữ Liệu đã được chuyển về MainActivity.java.
        }
        return convertView;
    }
}