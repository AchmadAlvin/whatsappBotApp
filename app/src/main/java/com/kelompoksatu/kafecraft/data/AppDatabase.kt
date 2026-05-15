// 40. [AppDatabase.kt] Deklarasi package.
package com.kelompoksatu.kafecraft.data

// 41-44. Import library yang dibutuhkan.
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 45. Anotasi level kelas '@Database'. Menerima parameter berupa array dari kelas KClass (Kotlin Class reference) melalui '[BookmarkEntity::class]'. Menentukan versi skema (version) dan tidak mengekspor riwayat skema JSON (exportSchema).
@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = false)
// 46. 'abstract class' tidak bisa diinisialisasi secara langsung (instantiated). Kelas warisan 'RoomDatabase' ini mengikat Room ke ekosistem Android.
abstract class AppDatabase : RoomDatabase() {
    // 47. 'abstract fun' (Fungsi abstrak) yang bertindak sebagai Getter untuk objek DAO. Implementasinya akan dibuat otomatis (Generated Code) saat aplikasi di-compile (AOT - Ahead of Time).
    abstract fun bookmarkDao(): BookmarkDao

    // 48. 'companion object' di Kotlin digunakan untuk mendefinisikan anggota kelas yang terikat pada kelas itu sendiri (Static-like members), bukan pada instance objek.
    companion object {
        // 49. Anotasi '@Volatile'. Memaksa JVM (Java Virtual Machine) menyelaraskan nilai variabel ini di semua CPU caches/threads. Menghindari "Stale Data" (Data usang) dalam arsitektur Multi-threading.
        @Volatile
        // 50. Variabel 'INSTANCE' bersifat private (Enkapsulasi), tipe nullable 'AppDatabase?', diinisialisasi null. Menyimpan referensi tunggal ke database (Singleton Pattern).
        private var INSTANCE: AppDatabase? = null

        // 51. Fungsi publik menerima argumen 'context'.
        fun getDatabase(context: Context): AppDatabase {
            // 52. Elvis Operator (?:) mengevaluasi operan kiri. Jika tidak null, ia mengembalikannya. Jika null, eksekusi dilanjutkan ke sisi kanannya.
            return INSTANCE ?: synchronized(this) {
                // 53. Blok fungsi 'synchronized' mengambil kunci (monitor lock) pada objek 'this' (companion object). Ini mencegah dua thread/coroutine mengeksekusi inisialisasi pada saat yang sama (Race Condition).
                // Istilah Teknis: 'synchronized' di Kotlin sebenarnya mengambil bentuk 'Higher-Order Function' (HOF) yang menerima aksi (lambda) dan menjalankannya di dalam blok sinkronisasi yang aman (Thread Safety).
                
                // 54. Memanggil method pabrik statis (Static Factory Method) 'databaseBuilder'.
                val instance = Room.databaseBuilder(
                    // 55. Mem-passing 'applicationContext' (Konteks Global) sangat krusial di sini alih-alih Activity context, untuk mencegah "Context/Memory Leak" ketika layar dirotasi/dihancurkan.
                    context.applicationContext,
                    // 56. Reflection (Referensi KClass) ke kelas abstrak kita, agar Room tahu cetakan mana yang harus diimplementasikan.
                    AppDatabase::class.java,
                    // 57. String konstanta penamaan file SQLite fisik (.db).
                    "kafecraft_database"
                ).build() // 58. Pattern 'Builder Design Pattern'. Menjalankan pembangunan objek kompleks dan mengembalikan instance-nya.

                // 59. Menyimpan instance yang telah dibuat kembali ke dalam variabel static INSTANCE (Mutasi status).
                INSTANCE = instance

                // 60. 'instance' menjadi nilai pengembalian (Return value) dari blok lambda ini (Implicit Return pada Lambda Expression).
                instance
            }
        }
    }
}
