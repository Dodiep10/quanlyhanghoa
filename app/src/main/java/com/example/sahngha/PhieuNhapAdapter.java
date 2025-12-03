package com.example.sahngha;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhieuNhapAdapter extends ArrayAdapter<PhieuNhap> {

    // Interface để giao tiếp với Activity khi bấm nút Xóa
    public interface OnItemActionListener {
        void onDeleteClick(String maPhieu, int position);
    }

    private OnItemActionListener mListener;
    private Context mContext;
    private int mResource;

    // Constructor chuẩn cho ArrayAdapter
    public PhieuNhapAdapter(@NonNull Context context, int resource, @NonNull List<PhieuNhap> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    // Hàm set sự kiện xóa
    public void setOnItemActionListener(OnItemActionListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            holder = new ViewHolder();
            // Ánh xạ các view khớp với layout item_phieunhap.xml
            holder.tvMaPhieu = convertView.findViewById(R.id.tvMaPhieu);
            holder.tvNgayNhap = convertView.findViewById(R.id.tvNgayNhap);
            holder.tvNguoiTao = convertView.findViewById(R.id.tvNguoiTao);
            holder.tvTongTien = convertView.findViewById(R.id.tvTongTien);
            holder.btnXoa = convertView.findViewById(R.id.btnXoa);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Lấy dữ liệu phiếu nhập tại vị trí position
        PhieuNhap phieuNhap = getItem(position);

        if (phieuNhap != null) {
            // SAU KHI SỬA
            // 1. Format ngày tháng (HH:mm dd/MM/yyyy) - Bổ sung Giờ và Phút
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
            String ngayNhapStr = sdf.format(new Date(phieuNhap.getThoiGianNhap()));

            // 2. Format tiền tệ chuẩn Việt Nam (1.000.000đ)
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setGroupingSeparator('.'); // Dùng dấu chấm phân cách hàng nghìn
            DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
            String tongTienStr = "Tổng tiền: " + decimalFormat.format(phieuNhap.getTongTien()) + "đ";

            // 3. Hiển thị lên giao diện
            holder.tvMaPhieu.setText("Mã Phiếu: " + phieuNhap.getMaPhieu());
            holder.tvNgayNhap.setText(ngayNhapStr);
            holder.tvNguoiTao.setText("(" + phieuNhap.getNguoiNhap() + ")");
            holder.tvTongTien.setText(tongTienStr);

            // 4. Xử lý sự kiện nút Xóa
            if (holder.btnXoa != null) {
                // Quan trọng: Chặn nút xóa chiếm quyền click của cả dòng
                holder.btnXoa.setFocusable(false);
                holder.btnXoa.setFocusableInTouchMode(false);

                holder.btnXoa.setOnClickListener(v -> {
                    if (mListener != null) {
                        mListener.onDeleteClick(phieuNhap.getMaPhieu(), position);
                    }
                });
            }

            // 5. Bắt sự kiện click vào cả dòng để xem chi tiết
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, XemChiTietPhieu.class);
                intent.putExtra("MA_PHIEU", phieuNhap.getMaPhieu());
                mContext.startActivity(intent);
            });
        }

        return convertView;
    }

    // ViewHolder lưu trữ các view để tối ưu hiệu năng
    static class ViewHolder {
        TextView tvMaPhieu;
        TextView tvNgayNhap;
        TextView tvNguoiTao;
        TextView tvTongTien;
        ImageButton btnXoa;
    }
}