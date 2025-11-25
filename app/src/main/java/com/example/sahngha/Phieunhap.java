package com.example.sahngha; // Đảm bảo đúng package của bạn

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PhieuNhap {
    private String maPhieu; // Mã phiếu nhập (ID)
    private long thoiGianNhap; // Timestamp thời gian nhập (dùng long để lưu trữ trên Firebase)
    private String nguoiNhap; // Tên người tạo phiếu
    private double tongTien; // Tổng giá trị phiếu
    // HashMap lưu trữ chi tiết các mặt hàng: Key là MaHangHoa, Value là ChiTietNhapHang
    // Hoặc sử dụng List<ChiTietNhapHang> nếu bạn có Class ChiTietNhapHang riêng
    private Map<String, ChiTietNhapHang> chiTiet;

    // Constructor mặc định (cần thiết cho Firebase)
    public PhieuNhap() {
        // Cần có constructor rỗng cho Firebase Database
    }

    // Constructor có tham số (tùy chọn)
    public PhieuNhap(String maPhieu, long thoiGianNhap, String nguoiNhap, double tongTien, Map<String, ChiTietNhapHang> chiTiet) {
        this.maPhieu = maPhieu;
        this.thoiGianNhap = thoiGianNhap;
        this.nguoiNhap = nguoiNhap;
        this.tongTien = tongTien;
        this.chiTiet = chiTiet;
    }

    // --- GETTERS VÀ SETTERS ---

    public String getMaPhieu() {
        return maPhieu;
    }

    public void setMaPhieu(String maPhieu) {
        this.maPhieu = maPhieu;
    }

    public long getThoiGianNhap() {
        return thoiGianNhap;
    }

    public void setThoiGianNhap(long thoiGianNhap) {
        this.thoiGianNhap = thoiGianNhap;
    }

    public String getNguoiNhap() {
        return nguoiNhap;
    }

    public void setNguoiNhap(String nguoiNhap) {
        this.nguoiNhap = nguoiNhap;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public Map<String, ChiTietNhapHang> getChiTiet() {
        return chiTiet;
    }

    public void setChiTiet(Map<String, ChiTietNhapHang> chiTiet) {
        this.chiTiet = chiTiet;
    }
}