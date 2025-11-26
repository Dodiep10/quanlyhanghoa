package com.example.sahngha;

public class ChiTietNhapHang {
    private HangHoa hangHoa;
    private int soLuong;

    // Constructor rỗng (bắt buộc cho Firebase)
    public ChiTietNhapHang() {
    }

    public ChiTietNhapHang(HangHoa hangHoa, int soLuong) {
        this.hangHoa = hangHoa;
        this.soLuong = soLuong;
    }

    // --- CÁC HÀM GETTER VÀ SETTER (Cần thiết để sửa lỗi) ---

    public HangHoa getHangHoa() {
        return hangHoa;
    }

    public void setHangHoa(HangHoa hangHoa) {
        this.hangHoa = hangHoa;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    // Helper: Lấy mã hàng hóa nhanh
    public String getMaHangHoa() {
        return hangHoa != null ? hangHoa.getMaHangHoa() : "";
    }

    // Helper: Tính thành tiền
    public double getThanhTien() {
        return hangHoa != null ? hangHoa.getGia() * soLuong : 0;
    }
}