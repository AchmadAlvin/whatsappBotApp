// (56) SessionManager berada di package data karena menyimpan data session user lokal.
package com.kelompoksatu.kafecraft.data

// (57) Context dipakai untuk mengambil SharedPreferences milik aplikasi.
import android.content.Context

// (58) SharedPreferences adalah penyimpanan key-value lokal Android.
import android.content.SharedPreferences

// (59) SessionManager mengelola status login lokal.
// AuthViewModel menyimpan session lewat class ini setelah login berhasil.
class SessionManager(context: Context) {
    // (60) prefs adalah akses ke file SharedPreferences bernama PREF_NAME.
    // MODE_PRIVATE berarti data hanya bisa diakses oleh aplikasi ini.
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // (61) companion object menyimpan konstanta key agar tidak menulis string berulang.
    companion object {
        // (62) Nama file SharedPreferences untuk session KafeCraft.
        private const val PREF_NAME = "kafecraft_session"

        // (63) Key boolean untuk menandai apakah user sedang login.
        private const val KEY_IS_LOGGED_IN = "is_logged_in"

        // (64) Key id user yang berasal dari Firebase.
        private const val KEY_USER_ID = "user_id"

        // (65) Key nama user yang dipakai untuk tampilan dan author.
        private const val KEY_USER_NAME = "user_name"

        // (66) Key email user yang sedang login.
        private const val KEY_USER_EMAIL = "user_email"
    }

    // (67) saveLoginSession dipanggil setelah login berhasil.
    // Fungsi ini menyimpan identitas user agar aplikasi mengenali session aktif.
    fun saveLoginSession(userId: String, userName: String, userEmail: String) {
        // (68) prefs.edit().apply membuka editor SharedPreferences.
        // apply di sini adalah scope function Kotlin, bukan apply() penyimpan data di bawah.
        prefs.edit().apply {
            // (69) Simpan status bahwa user sudah login.
            putBoolean(KEY_IS_LOGGED_IN, true)

            // (70) Simpan id user aktif.
            putString(KEY_USER_ID, userId)

            // (71) Simpan nama user aktif.
            putString(KEY_USER_NAME, userName)

            // (72) Simpan email user aktif.
            putString(KEY_USER_EMAIL, userEmail)

            // (73) apply() menyimpan perubahan secara asynchronous ke SharedPreferences.
            apply()
        }
    }

    // (74) isLoggedIn membaca status login.
    // AppNavigation memakai nilai ini untuk memilih start destination: home atau login.
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // (75) getUserId dipakai fitur lain untuk tahu id user aktif.
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)

    // (76) getUserName dipakai untuk nama author resep atau komentar.
    fun getUserName(): String? = prefs.getString(KEY_USER_NAME, null)

    // (77) getUserEmail dipakai jika fitur membutuhkan email user aktif.
    fun getUserEmail(): String? = prefs.getString(KEY_USER_EMAIL, null)

    // (78) logout menghapus semua data session.
    // Setelah ini user dianggap tidak login lagi.
    fun logout() {
        prefs.edit().clear().apply()
    }
}
