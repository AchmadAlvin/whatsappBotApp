// 16. [BookmarkEntity.kt] Mendeklarasikan package.
package com.kelompoksatu.kafecraft.data

// 17. Mengimpor kelas anotasi dari Jetpack Room (ORM - Object Relational Mapping untuk SQLite).
import androidx.room.Entity
// 18. Mengimpor anotasi penanda Primary Key.
import androidx.room.PrimaryKey

// 19. Anotasi kelas (Class Annotation) '@Entity'. Pada tingkat kompilasi (Compile-time), Room compiler (KSP/KAPT) membaca anotasi ini untuk menghasilkan kode (Code Generation) SQL 'CREATE TABLE'. Parameter 'tableName' mengubah nama tabel menjadi "bookmarks" alih-alih menggunakan nama kelas.
@Entity(tableName = "bookmarks")
// 20. 'data class' kembali digunakan. Ini mendefinisikan skema baris tabel.
data class BookmarkEntity(
    // 21. Anotasi properti '@PrimaryKey'. Ini adalah Constraint Database yang memaksa kolom 'recipeId' harus Unik dan menjadi indeks pencarian utama.
    // Karena ini ditaruh di Primary Constructor, ini bertindak sekaligus sebagai parameter konstruktor dan properti kelas.
    @PrimaryKey val recipeId: String,
    // 22. Kolom 'title' bertipe teks (TEXT di SQLite).
    val title: String,
    // 23. Kolom 'description' bertipe teks.
    val description: String,
    // 25. Kolom 'authorName' bertipe teks.
    val authorName: String
)
