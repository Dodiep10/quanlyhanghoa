package com.example.sahngha;

import java.util.Map;

public class PhieuNhap {
    private String maPhieu;                        // Mã phiếu nhập
    private long thoiGianNhap;                     // Thời gian nhập (timestamp)
    private String nguoiNhap;                      // Người tạo phiếu
    private double tongTien;                       // Tổng giá trị phiếu
    private Map<String, ChiTietNhapHang> chiTiet;  // Danh sách chi tiết nhập

    // --- Constructor rỗng (bắt buộc cho Firebase) ---
    public PhieuNhap() { }

    // --- Constructor 4 tham số (tự tính tổng tiền) ---
    public PhieuNhap(String maPhieu, long thoiGianNhap, String nguoiNhap,
                     Map<String, ChiTietNhapHang> chiTiet) {

        this.maPhieu = maPhieu;
        this.thoiGianNhap = thoiGianNhap;
        this.nguoiNhap = nguoiNhap;
        this.chiTiet = chiTiet;

        // TỰ TÍNH TỔNG TIỀN
        this.tongTien = tinhTongTien();
    }

    // --- Constructor 5 tham số (truyền tongTien từ bên ngoài) ---
    public PhieuNhap(String maPhieu, long thoiGianNhap, String nguoiNhap,
                     double tongTien, Map<String, ChiTietNhapHang> chiTiet) {
        this.maPhieu = maPhieu;
        this.thoiGianNhap = thoiGianNhap;
        this.nguoiNhap = nguoiNhap;
        this.tongTien = tongTien;
        this.chiTiet = chiTiet;
    }

    // --- GETTERS & SETTERS ---
    public String getMaPhieu() { return maPhieu; }
    public void setMaPhieu(String maPhieu) { this.maPhieu = maPhieu; }

    public long getThoiGianNhap() { return thoiGianNhap; }
    public void setThoiGianNhap(long thoiGianNhap) { this.thoiGianNhap = thoiGianNhap; }

    public String getNguoiNhap() { return nguoiNhap; }
    public void setNguoiNhap(String nguoiNhap) { this.nguoiNhap = nguoiNhap; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public Map<String, ChiTietNhapHang> getChiTiet() { return chiTiet; }
    public void setChiTiet(Map<String, ChiTietNhapHang> chiTiet) {
        this.chiTiet = chiTiet;
        this.tongTien = tinhTongTien(); // Cập nhật tổng tiền khi đổi danh sách
    }

    // --- HÀM TÍNH TỔNG TIỀN TỪ CHI TIẾT ---
    public double tinhTongTien() {
        double tong = 0;
        if (chiTiet != null) {
            for (ChiTietNhapHang ct : chiTiet.values()) {
                tong += ct.getThanhTien();
            }
        }
        return tong;
    }
}
