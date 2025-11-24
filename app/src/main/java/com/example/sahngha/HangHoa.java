package com.example.sahngha;

import java.text.DecimalFormat;

public class HangHoa {
    private String maHangHoa;
    private String ten;
    private int soLuong;
    private double gia;
    private String loaiHangHoa;
    private String hinhAnh;
    private String tenNCC;
    private String emailNCC;
    private String sdtNCC;

    public HangHoa(String maHangHoa, String ten, String loaiHangHoa, double gia, int soLuong, String hinhAnh, String tenNCC, String emailNCC, String sdtNCC) {
        this.maHangHoa = maHangHoa;
        this.ten = ten;
        this.loaiHangHoa = loaiHangHoa;
        this.gia = gia;
        this.soLuong = soLuong;
        this.hinhAnh = hinhAnh;
        this.tenNCC = tenNCC;
        this.emailNCC = emailNCC;
        this.sdtNCC = sdtNCC;
    }
    public HangHoa() {
    }
    public String getTen() {
        return ten;
    }
    public void setTen(String ten) {
        this.ten = ten;
    }
    public int getSoLuong() {
        return soLuong;
    }
    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }
    public double getGia() {
        return gia;
    }
    public void setGia(double gia) {
        this.gia = gia;
    }
    public String getGiaFormatted() {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(gia) + "đ";
    }
    public String getMaHangHoa() {
        return maHangHoa;
    }
    public void setMaHangHoa(String maHangHoa) {
        this.maHangHoa = maHangHoa;
    }
    public String getLoaiHangHoa() {
        return loaiHangHoa;
    }
    public void setLoaiHangHoa(String loaiHangHoa) {
        this.loaiHangHoa = loaiHangHoa;
    }
    public String getHinhAnh() {
        return hinhAnh;
    }
    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }
    public String getTenNCC() {
        return tenNCC;
    }
    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
    }
    public String getEmailNCC() {
        return emailNCC;
    }
    public void setEmailNCC(String emailNCC) {
        this.emailNCC = emailNCC;
    }
    public String getSdtNCC() {
        return sdtNCC;
    }
    public void setSdtNCC(String sdtNCC) {
        this.sdtNCC = sdtNCC;
    }
}