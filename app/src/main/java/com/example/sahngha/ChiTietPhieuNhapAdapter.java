package com.example.sahngha;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class ChiTietPhieuNhapAdapter extends RecyclerView.Adapter<ChiTietPhieuNhapAdapter.ViewHolder> {

    private List<ChiTietNhapHang> listChiTiet;

    // 1. Interface để xử lý sự kiện bấm nút Xóa
    public interface OnItemDeleteListener {
        void onDeleteClick(int position);
    }

    private OnItemDeleteListener deleteListener;

    // Constructor
    public ChiTietPhieuNhapAdapter(List<ChiTietNhapHang> listChiTiet) {
        this.listChiTiet = listChiTiet;
    }

    // Hàm set sự kiện từ bên ngoài (Activity)
    public void setOnDeleteListener(OnItemDeleteListener listener) {
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chitiet_nhap, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietNhapHang item = listChiTiet.get(position);
        DecimalFormat df = new DecimalFormat("#,###");


        // Kiểm tra null
        if (item.getHangHoa() != null) {
            holder.tvTenHang.setText(item.getHangHoa().getTen());
            holder.tvDonGia.setText(df.format(item.getHangHoa().getGia()));
        } else {
            holder.tvTenHang.setText("Hàng hóa lỗi");
            holder.tvDonGia.setText("0");
        }

        holder.tvSoLuong.setText(String.valueOf(item.getSoLuong()));

        // Thành tiền
        holder.tvThanhTien.setText(df.format(item.getThanhTien()) + "đ");

        // 2. Xử lý ẩn/hiện và click nút Xóa
        if (deleteListener != null) {
            holder.btnXoa.setVisibility(View.VISIBLE);
            holder.btnXoa.setOnClickListener(v -> {
                deleteListener.onDeleteClick(holder.getAdapterPosition());
            });
        } else {
            holder.btnXoa.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return listChiTiet != null ? listChiTiet.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenHang, tvDonGia, tvSoLuong, tvThanhTien;
        ImageButton btnXoa;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenHang = itemView.findViewById(R.id.tvTenHang);
            tvDonGia = itemView.findViewById(R.id.tvDonGia);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvThanhTien = itemView.findViewById(R.id.tvThanhTien);
            btnXoa = itemView.findViewById(R.id.btnXoaItem);
        }
    }
}