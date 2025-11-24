package com.example.sahngha;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageButton; // Cần thêm import này nếu layout item_hanghoa có ImageButton btnXoa

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class HangHoaAdapter extends ArrayAdapter<HangHoa> {

    // THÊM INTERFACE LISTENER CHO HÀNH ĐỘNG XÓA (CONTRACT)
    public interface OnItemActionListener {
        void onDeleteClick(String hangHoaId, int position);
    }

    // KHAI BÁO BIẾN LISTENER VÀ HÀM SETTER
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
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_hanghoa, parent, false);
        }

        TextView txtMa = convertView.findViewById(R.id.tvMaHangHoa);
        TextView txtTen = convertView.findViewById(R.id.txtTenHang);
        TextView txtGia = convertView.findViewById(R.id.txtGia);
        TextView txtSoLuong = convertView.findViewById(R.id.txtSoLuong);

        // Tìm kiếm nút Xóa (nếu bạn có nút riêng)
        ImageButton btnXoa = convertView.findViewById(R.id.btnXoa);

        final HangHoa hangHoa = getItem(position);
        if (hangHoa != null) {
            txtTen.setText(hangHoa.getTen());
            txtMa.setText("Mã: " + hangHoa.getMaHangHoa());
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);

            txtGia.setText("Giá: " + decimalFormat.format(hangHoa.getGia()) + "đ");
            txtSoLuong.setText("Số lượng: " + hangHoa.getSoLuong());


            // GẮN SỰ KIỆN CLICK CHO NÚT XÓA
            if (btnXoa != null) {
                btnXoa.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (listener != null) {
                            // Gọi hàm onDeleteClick trong Activity/Fragment
                            listener.onDeleteClick(hangHoa.getMaHangHoa(), position);
                        }
                    }
                });
            }


            // *** NA CHỈNH SỬA ***
            // Thêm sự kiện click cho toàn bộ item (convertView)
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Lấy mã hàng hóa của item được click
                    String maHangHoa = hangHoa.getMaHangHoa();

                    // Tạo Intent để chuyển sang ChiTietHangHoaActivity
                    Intent intent = new Intent(getContext(), ChiTietHangHoaActivity.class);

                    // Gửi mã hàng hóa qua Intent
                    intent.putExtra("MA_HANG_HOA", maHangHoa);

                    // Khởi động Activity chi tiết
                    getContext().startActivity(intent);
                }
            });
            // *** NA CHỈNH SỬA ***
        }
        return convertView;
    }
}