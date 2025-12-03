package com.example.sahngha;

public class LoaiHang {
    private String id;
    private String tenLoai;
    private String icon; // Lưu tên icon (vd: "star", "camera")

    public LoaiHang() { }

    public LoaiHang(String id, String tenLoai, String icon) {
        this.id = id;
        this.tenLoai = tenLoai;
        this.icon = icon;
    }

    public String getId() { return id; }
    public String getTenLoai() { return tenLoai; }
    public String getIcon() { return icon; }
}