package com.example.sahngha;

// 1. LOẠI BỎ TỪ KHÓA 'public'
// Nếu class KHÔNG phải là public, nó không cần khớp với tên file.
// Tuy nhiên, việc loại bỏ public có thể gây ra lỗi khi truy cập từ package khác.
class chitietnhaphang {
    private String maHangHoa;
    private String tenHangHoa;
    private int soLuongNhap;
    private double giaNhap;

    // 2. Tên Constructor phải khớp với tên Class
    chitietnhaphang() {
    }

    // 3. Tên Constructor có tham số phải khớp với tên Class
    chitietnhaphang(String maHangHoa, String tenHangHoa, int soLuongNhap, double giaNhap) {
        this.maHangHoa = maHangHoa;
        this.tenHangHoa = tenHangHoa;
        this.soLuongNhap = soLuongNhap;
        this.giaNhap = giaNhap;
    }

    // --- PHƯƠNG THỨC TÍNH TOÁN (Thành tiền của riêng mặt hàng này) ---

    public double getThanhTien() {
        return this.soLuongNhap * this.giaNhap;
    }

    // --- GETTERS VÀ SETTERS (Giữ nguyên) ---

    // Lưu ý: Các phương thức này vẫn phải là public để được truy cập
    public String getMaHangHoa() { return maHangHoa; }
    public void setMaHangHoa(String maHangHoa) { this.maHangHoa = maHangHoa; }

    public String getTenHangHoa() { return tenHangHoa; }
    public void setTenHangHoa(String tenHangHoa) { this.tenHangHoa = tenHangHoa; }

    public int getSoLuongNhap() { return soLuongNhap; }
    public void setSoLuongNhap(int soLuongNhap) { this.soLuongNhap = soLuongNhap; }

    public double getGiaNhap() { return giaNhap; }
    public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }
}