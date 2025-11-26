package com.example.sahngha;

import android.content.Context;
import android.content.Intent; // Nhớ import Intent
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PhieuNhapAdapter extends ArrayAdapter<PhieuNhap> {

    // 1. Interface để giao tiếp với Activity (CHỈ GIỮ LẠI DELETE)
    public interface OnItemActionListener {
        void onDeleteClick(String maPhieu, int position);
    }

    private OnItemActionListener mListener;
    private Context mContext;
    private int mResource;

    public PhieuNhapAdapter(@NonNull Context context, int resource, @NonNull List<PhieuNhap> objects) {
        super(context, resource, objects);
        this.mContext = context;
        this.mResource = resource;
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.mListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        PhieuNhap phieuNhap = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);

            holder = new ViewHolder();
            holder.tvMaPhieu = convertView.findViewById(R.id.tvMaPhieu);
            holder.tvNgayNhap = convertView.findViewById(R.id.tvNgayNhap);
            holder.tvNguoiTao = convertView.findViewById(R.id.tvNguoiTao);
            holder.tvTongTien = convertView.findViewById(R.id.tvTongTien);
            holder.btnXoa = convertView.findViewById(R.id.btnXoa);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Đổ dữ liệu vào View
        if (phieuNhap != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String ngayNhapStr = sdf.format(new Date(phieuNhap.getThoiGianNhap()));
            String tongTienStr = String.format(Locale.getDefault(), "Tổng tiền: %,.0fđ", phieuNhap.getTongTien());

            holder.tvMaPhieu.setText("Mã Phiếu: " + phieuNhap.getMaPhieu());
            holder.tvNgayNhap.setText(ngayNhapStr);
            holder.tvNguoiTao.setText("(" + phieuNhap.getNguoiNhap() + ")");
            holder.tvTongTien.setText(tongTienStr);

            // Xử lý sự kiện nút Xóa
            if (holder.btnXoa != null) {
                holder.btnXoa.setOnClickListener(v -> {
                    if (mListener != null) {
                        mListener.onDeleteClick(phieuNhap.getMaPhieu(), position);
                    }
                });
            }

            // --- ĐÂY LÀ PHẦN THÊM VÀO ---
            // Bắt sự kiện click vào toàn bộ dòng (convertView) để chuyển màn hình
            convertView.setOnClickListener(v -> {
                Intent intent = new Intent(mContext, XemChiTietPhieu.class);
                intent.putExtra("MA_PHIEU", phieuNhap.getMaPhieu());
                mContext.startActivity(intent);
            });
            // -----------------------------
        }

        return convertView;
    }

    // Class ViewHolder
    static class ViewHolder {
        TextView tvMaPhieu;
        TextView tvNgayNhap;
        TextView tvNguoiTao;
        TextView tvTongTien;
        ImageButton btnXoa;
    }
}