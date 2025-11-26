package com.example.sahngha;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView; // Mới thêm
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide; // Mới thêm (Đảm bảo đã thêm thư viện trong build.gradle)

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;

public class HangHoaAdapter extends ArrayAdapter<HangHoa> {

    // 1. INTERFACE LISTENER (ĐỂ BẮT SỰ KIỆN XÓA)
    public interface OnItemActionListener {
        void onDeleteClick(String hangHoaId, int position);
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
            convertView = inflater.inflate(R.layout.item_hanghoa, parent, false);
        }

        // Ánh xạ Views
        TextView txtMa = convertView.findViewById(R.id.tvMaHangHoa);
        TextView txtTen = convertView.findViewById(R.id.txtTenHang);
        TextView txtGia = convertView.findViewById(R.id.txtGia);
        TextView txtSoLuong = convertView.findViewById(R.id.txtSoLuong);
        ImageButton btnXoa = convertView.findViewById(R.id.btnXoa);

        // --- MỚI THÊM: Ánh xạ ImageView ảnh đại diện ---
        ImageView imgAvatar = convertView.findViewById(R.id.imgHangHoaAvt);

        final HangHoa hangHoa = getItem(position);
        if (hangHoa != null) {

            // Định dạng tiền tệ
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.');
            DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);

            // Đổ dữ liệu chữ
            txtTen.setText(hangHoa.getTen());
            txtMa.setText("Mã: " + hangHoa.getMaHangHoa());
            txtGia.setText("Giá: " + decimalFormat.format(hangHoa.getGia()) + "đ");
            txtSoLuong.setText("Số lượng: " + hangHoa.getSoLuong());

            // --- MỚI THÊM: LOAD ẢNH BẰNG GLIDE ---
            // Kiểm tra xem hàng hoá có link ảnh không
            if (hangHoa.getHinhAnh() != null && !hangHoa.getHinhAnh().isEmpty()) {
                Glide.with(getContext())
                        .load(hangHoa.getHinhAnh()) // Link ảnh từ Firebase (Cloudinary)
                        .centerCrop()               // Tự động cắt ảnh cho vừa khung tròn
                        .placeholder(R.drawable.ic_launcher_background) // Ảnh chờ khi đang tải
                        .error(R.drawable.ic_launcher_background)       // Ảnh lỗi nếu link hỏng
                        .into(imgAvatar);
            } else {
                // Nếu không có ảnh thì hiện ảnh mặc định
                imgAvatar.setImageResource(R.drawable.ic_launcher_background);
            }
            // ---------------------------------------

            // GẮN SỰ KIỆN CLICK CHO NÚT XÓA
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
        }
        return convertView;
    }
}