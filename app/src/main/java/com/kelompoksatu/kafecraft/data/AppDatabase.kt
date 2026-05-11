// (39) AppDatabase berada di package data yang sama dengan BookmarkEntity dan BookmarkDao.
package com.kelompoksatu.kafecraft.data

// (40) Context dibutuhkan Room untuk membuat database yang terikat ke aplikasi Android.
import android.content.Context

// (41) Import Room ini dipakai untuk mendefinisikan database dan membuat instance database lokal.
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// (42) @Database mendaftarkan BookmarkEntity sebagai tabel Room.
// version = 1 berarti ini versi awal skema database lokal.
@Database(entities = [BookmarkEntity::class], version = 1, exportSchema = false)

// (43) AppDatabase adalah database Room utama aplikasi.
// Class ini menghubungkan entity BookmarkEntity dengan DAO BookmarkDao.
abstract class AppDatabase : RoomDatabase() {
    // (44) bookmarkDao menyediakan akses ke fungsi insert, delete, dan getAllBookmarks.
    // AppNavigation mengambil DAO ini lalu memberikannya ke HomeViewModel.
    abstract fun bookmarkDao(): BookmarkDao

    // (45) companion object menyimpan singleton database.
    // Tujuannya agar aplikasi tidak membuat banyak instance AppDatabase.
    companion object {
        // (46) @Volatile memastikan perubahan INSTANCE terlihat antar thread.
        @Volatile

        // (47) INSTANCE menyimpan database yang sudah pernah dibuat.
        // Nilai awal null berarti database belum dibuat.
        private var INSTANCE: AppDatabase? = null

        // (48) getDatabase adalah pintu utama untuk mengambil AppDatabase.
        // Kode lain tidak membuat Room database langsung, tetapi lewat fungsi ini.
        fun getDatabase(context: Context): AppDatabase {
            // (49) Jika INSTANCE sudah ada, langsung return.
            // Jika masih null, masuk synchronized agar pembuatan database aman dari race condition.
            return INSTANCE ?: synchronized(this) {
                // (50) Room.databaseBuilder membuat database lokal.
                val instance = Room.databaseBuilder(
                    // (51) applicationContext dipakai agar database tidak bergantung pada Activity tertentu.
                    context.applicationContext,

                    // (52) AppDatabase::class.java memberi tahu Room class database yang sedang dibuat.
                    AppDatabase::class.java,

                    // (53) "kafecraft_database" adalah nama file database lokal di device.
                    "kafecraft_database"
                ).build()

                // (54) Simpan instance yang baru dibuat agar pemanggilan berikutnya memakai database yang sama.
                INSTANCE = instance

                // (55) Return database yang sudah dibuat.
                instance
            }
        }
    }
}
