// 26. [BookmarkDao.kt] Mendeklarasikan package.
package com.kelompoksatu.kafecraft.data

// 27-31. Mengimpor antarmuka dan anotasi operasi Database dari Room.
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
// 32. Mengimpor 'Flow', sebuah API dari 'kotlinx.coroutines.flow' yang mengimplementasikan spesifikasi Reactive Streams.
import kotlinx.coroutines.flow.Flow

// 33. Anotasi antarmuka '@Dao' (Data Access Object). Ini adalah Pola Desain (Design Pattern) Struktural untuk memisahkan logika bisnis dari kode interaksi database persisten. Room akan membuatkan kelas implementasinya otomatis.
@Dao
interface BookmarkDao {

    // 34. Anotasi '@Insert' dengan parameter named argument 'onConflict = OnConflictStrategy.REPLACE'. Jika dieksekusi, Room akan menjalankan SQL 'INSERT OR REPLACE INTO'.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    // 35. 'suspend fun' adalah Kotlin Coroutines keyword.
    // Istilah Teknis: Suspending Function / Continuation-Passing Style (CPS). Fungsi ini bisa "dijeda" eksekusinya tanpa memblokir thread sistem operasi (Non-blocking I/O). Harus dipanggil dari Coroutine Scope lain. Mengembalikan ID baris (Long).
    suspend fun insert(bookmark: BookmarkEntity): Long

    // 36. Anotasi '@Delete'. Menghapus entitas berdasarkan kecocokan Primary Key.
    @Delete
    // 37. Suspending function mengembalikan 'Int' yang melambangkan jumlah baris yang terdampak (affected rows).
    suspend fun delete(bookmark: BookmarkEntity): Int

    // 38. Anotasi '@Query' menerima parameter string kueri SQL murni. Proses kompilasi Room akan mengecek sintaks SQL ini untuk mencegah kesalahan Typo.
    @Query("SELECT * FROM bookmarks")
    // 39. Mengembalikan tipe data 'Flow<List<BookmarkEntity>>'.
    // Istilah Teknis: Cold Stream / Observable. Berbeda dengan suspend yang mengeksekusi satu kali (One-shot), Flow akan memancarkan (emit) list terbaru setiap kali ada perubahan data di tabel 'bookmarks'. Ini mengikuti paradigma Functional Reactive Programming (FRP).
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>
    
}
