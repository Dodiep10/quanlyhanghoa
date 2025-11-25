package com.example.sahngha;

class ChiTietNhapHang {
    private HangHoa hangHoa;       // Liên kết trực tiếp với hàng hóa
    private int soLuongNhap;

    ChiTietNhapHang() {
    }

    // Constructor mới: chỉ cần truyền vào đối tượng HangHoa và số lượng
    ChiTietNhapHang(HangHoa hangHoa, int soLuongNhap) {
        this.hangHoa = hangHoa;
        this.soLuongNhap = soLuongNhap;
    }

    // --- GETTERS & SETTERS ---
    public HangHoa getHangHoa() {
        return hangHoa;
    }

    public void setHangHoa(HangHoa hangHoa) {
        this.hangHoa = hangHoa;
    }

    public int getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(int soLuongNhap) {
        this.soLuongNhap = soLuongNhap;
    }

    // --- LẤY GIÁ TRỰC TIẾP TỪ HANGHOA ---
    public double getGiaNhap() {
        return hangHoa.getGia();   // LẤY GIÁ TỪ CLASS HANGHOA
    }

    // --- THÀNH TIỀN ---
    public double getThanhTien() {
        return soLuongNhap * getGiaNhap();
    }

    // --- LẤY MÃ VÀ TÊN CHO DỄ DÙNG ---
    public String getMaHangHoa() {
        return hangHoa.getMaHangHoa();
    }

    public String getTenHangHoa() {
        return hangHoa.getTen();
    }
}
