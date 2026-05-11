// (1) Semua model di file ini berada dalam package data.
// Package yang sama membuat model ini bisa dipakai oleh AuthViewModel, HomeViewModel, dan MyRecipesViewModel.
package com.kelompoksatu.kafecraft.data

// (2) User adalah model data user dari Firebase.
// Model ini dipakai saat register, login, verifikasi hint, dan reset password.
data class User(
    // (3) Nama user yang disimpan saat registrasi dan dipakai untuk identitas tampilan.
    val name: String = "",

    // (4) Email user dipakai sebagai kunci pencarian saat login dan lupa password.
    val email: String = "",

    // (5) Password user dipakai AuthViewModel untuk membandingkan input login.
    val password: String = "",

    // (6) Hint dipakai pada flow lupa password untuk memverifikasi user.
    val hint: String = "",

    // (7) Counter gagal login dipakai untuk memblokir akun setelah terlalu banyak percobaan salah.
    val failedLoginAttempts: Int = 0
)

// (8) Recipe adalah model data resep dari Firebase.
// Model ini dipakai oleh fitur home, detail resep, dan resep milik user.
data class Recipe(
    // (9) authorId menyimpan id user pembuat resep.
    val authorId: String = "",

    // (10) authorName menyimpan nama pembuat resep agar bisa langsung ditampilkan.
    val authorName: String = "",

    // (11) title adalah judul resep yang tampil di daftar dan detail.
    val title: String = "",

    // (12) description adalah isi/deskripsi resep.
    val description: String = "",

    // (13) imageUrl menyimpan URL gambar resep dari storage/database.
    val imageUrl: String = "",

    // (14) timestamp menyimpan waktu resep dibuat atau diperbarui.
    val timestamp: Long = 0L
)

// (15) Comment adalah model data komentar pada resep.
// Model ini dipakai saat user menambahkan komentar di detail resep.
data class Comment(
    // (16) authorName menyimpan nama user yang menulis komentar.
    val authorName: String = "",

    // (17) text adalah isi komentar.
    val text: String = "",

    // (18) timestamp menyimpan waktu komentar dibuat.
    val timestamp: Long = 0L
)
