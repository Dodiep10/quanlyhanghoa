package com.example.sahngha;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

public class ChiTietPhieuNhapAdapter extends RecyclerView.Adapter<ChiTietPhieuNhapAdapter.ViewHolder> {

    private List<ChiTietNhapHang> chiTietList;

    // Constructor
    public ChiTietPhieuNhapAdapter(List<ChiTietNhapHang> chiTietList) {
        this.chiTietList = chiTietList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chitietphieunhap, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChiTietNhapHang ct = chiTietList.get(position);
        DecimalFormat df = new DecimalFormat("#,###");

        holder.tvTenHang.setText(ct.getTenHangHoa());
        holder.tvSoLuong.setText(String.valueOf(ct.getSoLuongNhap()));
        holder.tvGiaNhap.setText(df.format(ct.getGiaNhap()) + "đ");
        holder.tvThanhTien.setText(df.format(ct.getThanhTien()) + "đ");
    }

    @Override
    public int getItemCount() {
        return chiTietList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTenHang, tvSoLuong, tvGiaNhap, tvThanhTien;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTenHang = itemView.findViewById(R.id.tvTenHang);
            tvSoLuong = itemView.findViewById(R.id.tvSoLuong);
            tvGiaNhap = itemView.findViewById(R.id.tvGiaNhap);
            tvThanhTien = itemView.findViewById(R.id.tvThanhTien);
        }
    }
}
