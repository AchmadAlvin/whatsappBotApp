// (28) File DAO berada dalam package data yang sama dengan BookmarkEntity dan AppDatabase.
package com.kelompoksatu.kafecraft.data

// (29) Import Room ini dipakai untuk mendefinisikan operasi database lokal.
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

// (30) Flow membuat hasil query bisa diamati secara reactive.
// Jika data bookmark berubah, collector Flow bisa menerima list terbaru.
import kotlinx.coroutines.flow.Flow

// (31) @Dao memberi tahu Room bahwa interface ini berisi fungsi akses database.
@Dao

// (32) BookmarkDao adalah pintu operasi untuk tabel bookmarks.
// AppDatabase menyediakan instance DAO ini lewat bookmarkDao().
interface BookmarkDao {
    // (33) @Insert mendefinisikan operasi tambah/simpan bookmark.
    // REPLACE berarti jika recipeId sudah ada, data lama diganti data baru.
    @Insert(onConflict = OnConflictStrategy.REPLACE)

    // (34) insert menerima BookmarkEntity dan mengembalikan Long hasil operasi insert Room.
    // suspend berarti fungsi ini dipanggil dari coroutine karena operasi database tidak boleh memblokir UI thread.
    suspend fun insert(bookmark: BookmarkEntity): Long

    // (35) @Delete mendefinisikan operasi hapus berdasarkan object BookmarkEntity.
    @Delete

    // (36) delete menghapus row bookmark dan mengembalikan jumlah row yang terhapus.
    suspend fun delete(bookmark: BookmarkEntity): Int

    // (37) @Query menjalankan SQL untuk mengambil semua row dari tabel bookmarks.
    @Query("SELECT * FROM bookmarks")

    // (38) getAllBookmarks mengembalikan Flow list BookmarkEntity.
    // HomeViewModel dapat mengamati Flow ini agar UI bookmark ikut berubah saat data Room berubah.
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
}
