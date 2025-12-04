package com.example.sahngha;

public class LoaiHang {
    private String id;
    private String tenLoai;
    private String icon;

    // 1. Constructor rỗng (Bắt buộc để Firebase đọc dữ liệu)
    public LoaiHang() {
    }

    // 2. Constructor đầy đủ tham số (Để dùng trong MainActivity)
    public LoaiHang(String id, String tenLoai, String icon) {
        this.id = id;
        this.tenLoai = tenLoai;
        this.icon = icon;
    }

    // Getter và Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTenLoai() {
        return tenLoai;
    }

    public void setTenLoai(String tenLoai) {
        this.tenLoai = tenLoai;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}