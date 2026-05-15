// 61. [SessionManager.kt] Deklarasi package.
package com.kelompoksatu.kafecraft.data

// 62-63. Import Context dan interface SharedPreferences (API penyimpan I/O berbasis XML / key-value map).
import android.content.Context
import android.content.SharedPreferences

// 64. Deklarasi kelas 'SessionManager'. Parameter konstruktor 'context' digunakan sebagai Dependency Injection sederhana agar kelas ini bisa mengakses layanan OS Android.
class SessionManager(context: Context) {
    // 65. Properti 'prefs' dengan Akses Modifier 'private' (hanya bisa diakses di dalam scope ini). Diinisialisasi (Eager Initialization) dengan objek SharedPreferences. 
    // Parameter 'Context.MODE_PRIVATE' (yang merupakan konstanta integer bernilai 0) membatasi aksesibilitas file secara sistemik ke tingkat aplikasi (Sandbox mode).
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // 66. 'companion object' menyimpan konstanta statis (Konvensi penamaan UPPER_SNAKE_CASE).
    companion object {
        // 67. 'const val' (Compile-time constants). Berbeda dengan 'val' biasa (Run-time constant), tipe ini nilainya langsung ditanam (inlined) di bytecode oleh kompilator.
        private const val PREF_NAME = "kafecraft_session"
        // 68. Konstanta string (Key mapping).
        private const val KEY_USER_ID = "user_id"
        // 69. Konstanta nama.
        private const val KEY_USER_NAME = "user_name"
        // 70. Konstanta email.
        private const val KEY_USER_EMAIL = "user_email"
    }

    // 71. Fungsi public 'saveLoginSession' mengelola input parameter String.
    fun saveLoginSession(userId: String, userName: String, userEmail: String) {
        // 72. 'prefs.edit()' mereturn (mengembalikan) sebuah antarmuka 'SharedPreferences.Editor'.
        // 73. Memanggil ekstensi fungsi '.apply { }'.
        // Istilah Teknis: 'apply' adalah sebuah *Scope Function* dan *Higher-Order Function* (HOF) di Standard Library Kotlin. Ia menerima receiver berupa objek konteks (Editor) dan blok 'Lambda Function' sebagai eksekusinya. Sangat deklaratif.
        prefs.edit().apply {
            // 74. Karena berada dalam konteks receiver 'Editor' di dalam blok Lambda, method 'putString' dapat dipanggil langsung (Implicit 'this' object referential). Ini menulis key-value ke memori sementara (Buffer).
            putString(KEY_USER_ID, userId)
            // 75. Pemanggilan method putString ke-2.
            putString(KEY_USER_NAME, userName)
            // 76. Pemanggilan method putString ke-3.
            putString(KEY_USER_EMAIL, userEmail)
            // 77. Pemanggilan 'apply()' di sini adalah method khusus dari 'Editor'. Ini adalah proses I/O commit asynchronous (Berjalan di Background Thread). Berbeda dengan method '.commit()' jadul yang mengeksekusi secara synchronous (berpotensi menyebabkan lag / ANR - Application Not Responding).
            apply()
        }
    }

    // 78. Definisi Fungsi Inline / Single-Expression Function. Menggunakan tanda '='. Penulisan ini ekuivalen dengan Arrow Function di Javascript.
    // 'String?' (Tanda tanya) mendefinisikan tipe Nullable. Null-Safety sistem Kotlin ini mencegah Crash sistem dengan memastikan kompilator paham kalau nilai kembali bisa saja null (jika data kunci tidak tersimpan di memori).
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    
    // 79. Single-Expression Getter.
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)
    
    // 80. Single-Expression Getter.
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    // 81. Fungsi 'logout'.
    // Istilah Teknis: Method Chaining (Rantai Metode). Pemanggilan beruntun fungsi-fungsi: .edit() me-return objek 'Editor' -> .clear() me-return kembali objek 'Editor' tersebut yang sudah menghapus memori buffer -> .apply() mengeksekusi komit I/O memori secara asynchronous.
    fun logout() { prefs.edit().clear().apply() }
}
