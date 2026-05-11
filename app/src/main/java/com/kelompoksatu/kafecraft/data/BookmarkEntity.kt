// (19) File ini masih berada dalam package data agar bisa terhubung langsung dengan BookmarkDao dan AppDatabase.
package com.kelompoksatu.kafecraft.data

// (20) Entity dan PrimaryKey berasal dari Room.
// Keduanya mengubah data class Kotlin menjadi tabel database lokal.
import androidx.room.Entity
import androidx.room.PrimaryKey

// (21) @Entity memberi tahu Room bahwa BookmarkEntity adalah tabel lokal bernama "bookmarks".
@Entity(tableName = "bookmarks")

// (22) BookmarkEntity adalah bentuk data bookmark yang disimpan ke Room.
// BookmarkDao akan menerima dan mengembalikan object dengan tipe ini.
data class BookmarkEntity(
    // (23) recipeId menjadi PrimaryKey, artinya satu resep hanya punya satu row bookmark.
    @PrimaryKey val recipeId: String,

    // (24) title disimpan agar bookmark bisa menampilkan judul resep tanpa mengambil ulang seluruh Recipe.
    val title: String,

    // (25) description disimpan sebagai ringkasan resep di daftar bookmark.
    val description: String,

    // (26) imageUrl disimpan agar bookmark bisa menampilkan gambar resep.
    val imageUrl: String,

    // (27) authorName disimpan agar bookmark tetap punya informasi pembuat resep.
    val authorName: String
)
