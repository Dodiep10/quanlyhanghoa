package com.example.sahngha;

public class ChiTietNhapHang {
    private String maHangHoa;
    private String tenHangHoa;
    private int soLuongNhap;      // Số lượng nhập (Yêu cầu số lượng)
    private double giaNhap;       // Giá nhập của một đơn vị sản phẩm (Yêu cầu giá thành)

    // Constructor mặc định (QUAN TRỌNG cho Firebase)
    public ChiTietNhapHang() {
    }

    // Constructor có tham số
    public ChiTietNhapHang(String maHangHoa, String tenHangHoa, int soLuongNhap, double giaNhap) {
        this.maHangHoa = maHangHoa;
        this.tenHangHoa = tenHangHoa;
        this.soLuongNhap = soLuongNhap;
        this.giaNhap = giaNhap;
    }

    // --- PHƯƠNG THỨC TÍNH TOÁN (Thành tiền của riêng mặt hàng này) ---

    /**
     * Tính toán Thành tiền/Giá thành của riêng mặt hàng này.
     */
    public double getThanhTien() {
        return this.soLuongNhap * this.giaNhap;
    }

    // --- GETTERS VÀ SETTERS (Đã đầy đủ) ---

    public String getMaHangHoa() { return maHangHoa; }
    public void setMaHangHoa(String maHangHoa) { this.maHangHoa = maHangHoa; }

    public String getTenHangHoa() { return tenHangHoa; }
    public void setTenHangHoa(String tenHangHoa) { this.tenHangHoa = tenHangHoa; }

    public int getSoLuongNhap() { return soLuongNhap; }
    public void setSoLuongNhap(int soLuongNhap) { this.soLuongNhap = soLuongNhap; }

    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }
}