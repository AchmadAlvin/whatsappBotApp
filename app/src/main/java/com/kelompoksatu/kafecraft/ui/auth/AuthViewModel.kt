// 82. [AuthViewModel.kt] Deklarasi package untuk modul antarmuka pengguna (UI) bagian autentikasi.
package com.kelompoksatu.kafecraft.ui.auth

// 83-93. Mengimpor berbagai kelas dari Jetpack Compose (untuk State Management), ViewModel (Arsitektur MVVM), Firebase (BaaS), dan model internal.
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.kelompoksatu.kafecraft.data.SessionManager
import com.kelompoksatu.kafecraft.data.User

// 94. Deklarasi kelas 'AuthViewModel' yang mewarisi 'ViewModel'. Ini adalah pola desain MVVM. 'SessionManager' disuntikkan (Dependency Injection) melalui konstruktor.
class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    // 95. 'FirebaseAuth.getInstance()' mengambil instance tunggal (Singleton) dari SDK Firebase Auth.
    private val auth = FirebaseAuth.getInstance()
    // 96. Mendapatkan referensi node "users" dari Firebase Realtime Database.
    private val db = FirebaseDatabase.getInstance().getReference("users")

    // 97. Tiga state bersama yang dipakai oleh SEMUA fungsi auth (login, register, reset password, ganti password).
    // 'mutableStateOf' membuat state yang "diamati" (Observable State) oleh Compose. 'by' adalah Property Delegate Kotlin.
    // Istilah FP: Reactive State. Ketika salah satu nilai berubah, Compose otomatis melakukan Recomposition (render ulang UI) tanpa modifikasi manual.
    var isLoading by mutableStateOf(false)   // true = operasi sedang berjalan (tampilkan spinner)
    // 98. State 'error' bertipe String Nullable ('String?'). Null = tidak ada error, non-null = pesan error yang akan ditampilkan ke user.
    var error by mutableStateOf<String?>(null)
    // 99. State 'isSuccess' menandai operasi selesai sukses. Digunakan UI sebagai trigger navigasi pindah layar.
    var isSuccess by mutableStateOf(false)

    // 100. Fungsi 'resetState()' untuk mengembalikan ketiga state di atas ke nilai awal.
    // Dipanggil dari UI setelah navigasi atau setelah Toast error muncul, agar state tidak "bocor" ke operasi berikutnya (mencegah Infinite Loop recomposition).
    fun resetState() {
        isLoading = false
        error = null
        isSuccess = false
    }

    // 101. Fungsi 'login' (Fokus Utama). Menerima kredensial email dan password dari UI.
    fun login(email: String, pass: String) {
        // 102. Merubah state UI menjadi "Loading" (Biasanya memutar ikon spinner). Mutasi state ini memicu recomposition.
        isLoading = true
        // 103. Memanggil fungsi SDK Firebase 'signInWithEmailAndPassword'. Ini adalah operasi Asynchronous jarak jauh (Network call).
        auth.signInWithEmailAndPassword(email, pass)
            // 104. '.addOnSuccessListener' adalah fungsi callback.
            // Istilah FP: Higher-Order Function (HOF) & Callback / Continuation. Kita memberikan sebuah fungsi Lambda '{ result -> }' yang baru akan dieksekusi HANYA JIKA proses login jaringan sukses.
            .addOnSuccessListener { result ->
                // 105. 'result.user?.uid' menggunakan Safe Call Operator ('?.'). Mengambil ID unik jika user tidak null.
                val uid = result.user?.uid
                // 106. Evaluasi Guard Clause. Jika 'uid' null, batalkan proses.
                if (uid == null) {
                    isLoading = false
                    error = "Login gagal"
                    // 107. 'return@addOnSuccessListener' adalah sebuah 'Qualified Return'. Dalam FP (Lambda), return biasa tidak diizinkan melompati enclosing function. Ini secara spesifik mengakhiri eksekusi HANYA untuk scope lambda ini saja.
                    return@addOnSuccessListener
                }
                // 108. Jika login Firebase Auth berhasil, sistem lanjut mengambil profil lengkap dari Firebase Database berdasarkan 'uid'. Method Chaining 'child(uid).get()'.
                db.child(uid).get()
                    // 109. HOF callback kedua (Nested Callback). Dieksekusi jika data profil berhasil diambil.
                    .addOnSuccessListener { snap ->
                        // 110. 'snap.getValue(User::class.java)' melakukan deserialisasi JSON dari Firebase menjadi objek Kotlin Data Class 'User' menggunakan Reflection.
                        val user = snap.getValue(User::class.java)
                        // 111. Memanggil 'saveLoginSession' dari SessionManager.
                        // Operator Elvis '?:' (Fallback fallback). Jika 'user?.name' null, gunakan "", jika 'user?.email' null, gunakan 'email' yang diketik di login.
                        sessionManager.saveLoginSession(uid, user?.name ?: "", user?.email ?: email)
                        // 112. Mengubah state menjadi tidak loading, dan isSuccess menjadi true (Ini akan memicu navigasi UI berpindah layar).
                        isLoading = false
                        isSuccess = true
                    }
                    // 113. HOF Callback Penanganan Error tingkat Database. 'it' adalah implicit parameter tunggal dari lambda yang merepresentasikan Exception.
                    .addOnFailureListener {
                        isLoading = false
                        error = it.message
                    }
            }
            // 114. HOF Callback Penanganan Error tingkat Firebase Auth (misal: password salah, email tidak terdaftar, tidak ada koneksi).
            // Pola sama dengan failure listener Database di L113. 'it' adalah objek Exception yang mengandung pesan error dari server Firebase.
            .addOnFailureListener {
                isLoading = false
                error = it.message  // string error ini akan dibaca LaunchedEffect di UI untuk ditampilkan sebagai Toast
            }
    }

    // 115. Fungsi Register pengguna baru. Mirip dengan login secara struktural.
    fun register(name: String, email: String, pass: String) {
        isLoading = true
        // 116. Memanggil fungsi pembuatan user dari SDK Auth.
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    isLoading = false
                    error = "Gagal mendapat ID"
                    return@addOnSuccessListener
                }
                // 117. '.setValue()' mengirim objek 'User' untuk di-serialize menjadi JSON dan ditanam di Realtime Database pada path 'users/uid/'.
                db.child(uid).setValue(User(name, email))
                    .addOnSuccessListener {
                        isLoading = false
                        isSuccess = true
                    }
                    .addOnFailureListener {
                        isLoading = false
                        error = it.message ?: "Gagal simpan data"
                    }
            }
            .addOnFailureListener {
                isLoading = false
                error = it.message
            }
    }

    // 118. Fungsi pengiriman email reset password.
    fun sendPasswordReset(email: String) {
        isLoading = true
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                isLoading = false
                isSuccess = true
            }
            .addOnFailureListener {
                isLoading = false
                error = it.message
            }
    }

    // 119. Fungsi ganti password ketika user sedang login. Menggunakan Safe Call chaining '?.' secara ekstensif untuk menghindari NullPointerException jika 'currentUser' kosong.
    fun changePassword(newPass: String) {
        isLoading = true
        auth.currentUser
            ?.updatePassword(newPass)
            ?.addOnSuccessListener {
                isLoading = false
                isSuccess = true
            }
            ?.addOnFailureListener {
                isLoading = false
                error = it.message ?: "Gagal ganti password"
            }
    }

    // 120. 'Factory' class ini adalah Pola Desain 'Factory Method'. Dibutuhkan karena ViewModel kita memiliki parameter di konstruktornya (SessionManager). Android tidak tahu cara otomatis membuat ViewModel dengan parameter, jadi kita beri pabrik manualnya.
    class Factory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        // 121. Anotasi '@Suppress("UNCHECKED_CAST")' membungkam peringatan kompilator terkait casting tipe generik.
        @Suppress("UNCHECKED_CAST")
        // 122. Meng-override (menimpa) fungsi bawaan 'create' untuk me-return instance AuthViewModel lengkap dengan injeksinya, di-cast sebagai tipe T.
        override fun <T : ViewModel> create(modelClass: Class<T>) = AuthViewModel(sessionManager) as T
    }
}
