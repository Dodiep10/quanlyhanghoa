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

    // Biến lưu tên loại đang chọn để đổi màu nền
    private String selectedName = "";

    // --- INTERFACE (Quan trọng để sửa lỗi bên MainActivity) ---
    public interface OnLoaiHangClickListener {
        void onItemClick(LoaiHang loaiHang); // Click vào item
        void onAddClick();                   // Click vào nút Thêm
    }

    public LoaiHangAdapter(Context context, ArrayList<LoaiHang> listLoaiHang, OnLoaiHangClickListener listener) {
        this.context = context;
        this.listLoaiHang = listLoaiHang;
        this.listener = listener;
    }

    // Hàm nhận tên loại đang lọc từ MainActivity để tô màu
    public void setSelectedId(String name) {
        this.selectedName = name;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Đảm bảo bạn có file item_loai_hang.xml
        View view = LayoutInflater.from(context).inflate(R.layout.item_loai_hang, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // --- XỬ LÝ NÚT THÊM (+) Ở CUỐI DANH SÁCH ---
        if (position == listLoaiHang.size()) {
            holder.txtTen.setText("Thêm");
            holder.imgIcon.setImageResource(android.R.drawable.ic_input_add);
            // Reset background về mặc định
            holder.layoutItem.setBackgroundResource(R.drawable.bg_search_rounded);

            holder.itemView.setOnClickListener(v -> listener.onAddClick());
            return;
        }

        // --- XỬ LÝ ITEM BÌNH THƯỜNG ---
        LoaiHang item = listLoaiHang.get(position);
        holder.txtTen.setText(item.getTenLoai());

        // Map Icon từ dữ liệu sang hình ảnh Android có sẵn
        String iconKey = (item.getIcon() == null) ? "star" : item.getIcon();
        switch (iconKey) {
            case "star":    holder.imgIcon.setImageResource(android.R.drawable.btn_star_big_on); break;
            case "book":    holder.imgIcon.setImageResource(android.R.drawable.ic_menu_agenda); break;
            case "home":    holder.imgIcon.setImageResource(android.R.drawable.ic_dialog_map); break;
            case "camera":  holder.imgIcon.setImageResource(android.R.drawable.ic_menu_camera); break;
            case "gallery": holder.imgIcon.setImageResource(android.R.drawable.ic_menu_gallery); break;
            default:        holder.imgIcon.setImageResource(android.R.drawable.star_on); break;
        }

        // --- XỬ LÝ HIỆU ỨNG ĐANG CHỌN (HIGHLIGHT) ---
        // Nếu tên loại trùng với loại đang lọc -> Đổi màu nền xanh
        if (item.getTenLoai().equals(selectedName)) {
            holder.layoutItem.setBackgroundResource(R.drawable.bg_sticker_selected);
            holder.txtTen.setTextColor(Color.parseColor("#006064")); // Màu xanh đậm
        } else {
            holder.layoutItem.setBackgroundResource(R.drawable.bg_search_rounded);
            holder.txtTen.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return listLoaiHang.size() + 1; // +1 cho nút thêm
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