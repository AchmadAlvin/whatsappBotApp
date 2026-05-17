// 1. [Models.kt] 'package' mendeklarasikan namespace (ruang lingkup nama) untuk file ini, menghindari bentrok nama kelas dengan library lain.
package com.kelompoksatu.kafecraft.data

// 2. 'data class' adalah fitur khusus Kotlin. Ini meng-generate otomatis method seperti equals(), hashCode(), toString(), componentN() (untuk destructuring declaration), dan copy().
// Istilah: Boilerplate Code Reduction (pengurangan kode berulang) & Immutability.
data class User(
    // 3. Konstruktor utama (Primary Constructor) berada di dalam tanda kurung ().
    // 'val' (Value) membuat properti ini read-only (immutable) setelah objek diinisialisasi.
    // '= ""' adalah Default Argument. Jika pemanggil tidak memberikan nilai, otomatis diisi string kosong, menghindari NullPointerException.
    val name: String = "",
    // 4. Properti 'email' juga bersifat read-only.
    val email: String = ""
)

// 5. 'data class Recipe' memodelkan state data resep. Sangat disarankan (Best Practice) menggunakan data class untuk Model di arsitektur MVC/MVVM.
data class Recipe(
    // 6. 'authorId' menyimpan ID unik. Properti pada data class langsung diubah menjadi 'Backing Field' dengan 'Getter' otomatis di bawah layar oleh kompilator Kotlin.
    val authorId: String = "",
    // 7. 'authorName' untuk nama pembuat resep.
    val authorName: String = "",
    // 8. 'title' untuk judul resep.
    val title: String = "",
    // 9. 'description' untuk deskripsi resep.
    val description: String = "",
    // 11. 'timestamp' bertipe primitif Long (64-bit integer) untuk menyimpan waktu UNIX (Epoch time).
    val timestamp: Long = 0L
)

// 12. 'data class Comment' memodelkan data komentar.
data class Comment(
    // 13. 'authorName' properti immutable.
    val authorName: String = "",
    // 14. 'text' isi komentar.
    val text: String = "",
    // 15. 'timestamp' penanda waktu komentar dibuat.
    val timestamp: Long = 0L
)
