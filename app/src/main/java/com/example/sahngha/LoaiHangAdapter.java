package com.example.sahngha;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LoaiHangAdapter extends RecyclerView.Adapter<LoaiHangAdapter.ViewHolder> {

    private Context context;
    private ArrayList<LoaiHang> listLoaiHang;
    private OnLoaiHangClickListener listener;
    private String selectedId = ""; // Lưu ID của loại đang được chọn

    // Interface để gửi sự kiện click về MainActivity
    public interface OnLoaiHangClickListener {
        void onItemClick(LoaiHang loaiHang); // Click vào loại hàng
        void onAddClick(); // Click vào nút Thêm
    }

    public LoaiHangAdapter(Context context, ArrayList<LoaiHang> listLoaiHang, OnLoaiHangClickListener listener) {
        this.context = context;
        this.listLoaiHang = listLoaiHang;
        this.listener = listener;
    }

    public void setSelectedId(String id) {
        this.selectedId = id;
        notifyDataSetChanged(); // Cập nhật lại giao diện để đổi màu
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_loai_hang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Xử lý nút THÊM ở cuối danh sách (Không đổi)
        if (position == listLoaiHang.size()) {
            holder.txtTen.setText("Thêm");
            holder.imgIcon.setImageResource(android.R.drawable.ic_input_add);
            holder.imgIcon.setBackgroundResource(R.drawable.bg_search_rounded);
            holder.itemView.setOnClickListener(v -> listener.onAddClick());
            return;
        }

        LoaiHang item = listLoaiHang.get(position);
        holder.txtTen.setText(item.getTenLoai());

        // [CẬP NHẬT] MAP ICON DÙNG TÀI NGUYÊN CÓ SẴN CỦA ANDROID
        switch (item.getIcon()) {
            // --- Nhóm Ăn Uống / Phổ biến ---
            case "star": holder.imgIcon.setImageResource(android.R.drawable.btn_star_big_on); break;
            case "gallery": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_gallery); break;
            case "place": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_myplaces); break;
            case "compass": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_compass); break;
            case "rotate": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_rotate); break; // Cho nước ngọt cũ

            // --- Nhóm Văn Phòng ---
            case "office_pen": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_edit); break;   // Hình cây bút
            case "office_book": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_agenda); break; // Hình quyển sổ
            case "office_phone": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_call); break;  // Hình điện thoại
            case "office_info": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_info_details); break;

            // --- Nhóm Gia Dụng / Công cụ ---
            case "tool_gear": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_manage); break;   // Hình bánh răng (Gia dụng)
            case "tool_camera": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_camera); break; // Máy ảnh
            case "tool_save": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_save); break;
            case "tool_view": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_view); break;

            // --- Nhóm Khác ---
            case "other_delete": holder.imgIcon.setImageResource(android.R.drawable.ic_delete); break;
            case "other_search": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_search); break;
            case "other_share": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_share); break;
            case "other_upload": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_upload); break;

            // Mặc định
            default: holder.imgIcon.setImageResource(android.R.drawable.btn_star_big_on); break;
        }

        // Xử lý hiệu ứng chọn (Active State)
        if (item.getTenLoai().equals(selectedId)) {
            holder.imgIcon.setBackgroundResource(R.drawable.bg_sticker_selected);
        } else {
            holder.imgIcon.setBackgroundResource(R.drawable.bg_search_rounded);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        // +1 để dành chỗ cho nút "Thêm" ở cuối
        return listLoaiHang.size() + 1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtTen;
        ImageView imgIcon;
        LinearLayout layoutItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTen = itemView.findViewById(R.id.txtTenLoai);
            imgIcon = itemView.findViewById(R.id.imgIcon);
            layoutItem = itemView.findViewById(R.id.layoutItem);
        }
    }
}