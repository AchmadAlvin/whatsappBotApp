package com.kelompoksatu.kafecraft.ui.auth

// getValue dipakai oleh delegated property `by`.
// Contoh di file ini: `var isLoading by mutableStateOf(false)`.
// Dengan `by`, kode bisa langsung membaca `isLoading`, bukan `isLoading.value`.
import androidx.compose.runtime.getValue

// mutableStateOf membuat state Compose.
// Kalau nilainya berubah, UI Compose yang membaca state itu bisa recomposition.
import androidx.compose.runtime.mutableStateOf

// setValue juga dipakai oleh delegated property `by`.
// Dengan ini, kode bisa langsung menulis `isLoading = true`.
import androidx.compose.runtime.setValue

// ViewModel menyimpan state dan logic UI agar tidak hilang saat konfigurasi berubah.
// Contoh perubahan konfigurasi: rotasi layar.
import androidx.lifecycle.ViewModel

// Factory dipakai karena AuthViewModel butuh parameter SessionManager.
// ViewModel default biasanya hanya mudah dibuat jika constructor kosong.
import androidx.lifecycle.ViewModelProvider

// DataSnapshot adalah data hasil baca dari Firebase Realtime Database.
import com.google.firebase.database.DataSnapshot

// DatabaseError adalah error dari Firebase jika query gagal/dibatalkan.
import com.google.firebase.database.DatabaseError

// FirebaseDatabase adalah pintu masuk ke Firebase Realtime Database.
import com.google.firebase.database.FirebaseDatabase

// ValueEventListener adalah callback Firebase untuk menerima hasil query.
// Callback login utama ada di onDataChange dan onCancelled.
import com.google.firebase.database.ValueEventListener

// SessionManager menyimpan sesi login lokal setelah password benar.
import com.kelompoksatu.kafecraft.data.SessionManager

// User adalah model data yang dipakai untuk mengubah DataSnapshot Firebase menjadi object Kotlin.
import com.kelompoksatu.kafecraft.data.User

// AuthViewModel adalah class yang menjadi penghubung antara LoginScreen dan data auth.
//
// `private val sessionManager: SessionManager` berarti SessionManager dikirim dari luar
// lewat constructor. Ini membuat ViewModel bisa menyimpan sesi login tanpa membuat
// SessionManager sendiri di dalam class.
//
// `: ViewModel()` berarti AuthViewModel mewarisi class ViewModel.
// Ini konsep OOP inheritance, bukan Functional Programming murni.
class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    // `private val` berarti:
    // - private: hanya bisa dipakai di dalam AuthViewModel.
    // - val: referensi db tidak bisa diganti setelah dibuat.
    //
    // FirebaseDatabase.getInstance()
    // - mengambil instance Firebase Realtime Database.
    //
    // getReference("users")
    // - menunjuk ke node/path `users` di Firebase.
    //
    // Jadi query login nanti mencari data di path:
    // users/{userId}/email
    private val db = FirebaseDatabase.getInstance().getReference("users")

    // State loading untuk LoginScreen.
    //
    // `var` berarti nilainya bisa berubah.
    // `mutableStateOf(false)` berarti nilai awalnya false dan diamati oleh Compose.
    // `by` adalah delegated property supaya aksesnya ringkas.
    //
    // Saat login mulai: isLoading = true.
    // Saat Firebase selesai: isLoading = false.
    //
    // Istilah:
    // - mutable state: state yang bisa berubah.
    // - reactive state: UI bereaksi saat state berubah.
    // - side effect: perubahan state ini memengaruhi tampilan tombol di LoginScreen.
    var isLoading by mutableStateOf(false)

    // State error untuk LoginScreen.
    //
    // `String?` berarti nullable String:
    // - String berisi pesan error.
    // - null berarti tidak ada error.
    //
    // LoginScreen membaca state ini di LaunchedEffect.
    var error by mutableStateOf<String?>(null)

    // State sukses untuk LoginScreen.
    //
    // Jika login berhasil, isSuccess diubah menjadi true.
    // LoginScreen membaca nilai ini lalu menjalankan navigasi ke Home.
    var isSuccess by mutableStateOf(false)
    var forgotEmail by mutableStateOf("")

    // resetState dipanggil oleh LoginScreen setelah sukses/error selesai ditangani.
    //
    // Bentuk satu baris ini tetap fungsi biasa.
    // Side effect-nya adalah mengubah state ViewModel.
    fun resetState() { isLoading = false; error = null; isSuccess = false }

    // Fungsi login dipanggil dari LoginScreen saat tombol Log In ditekan.
    //
    // Parameter:
    // - email: email yang diketik user di LoginScreen.
    // - pass: password yang diketik user di LoginScreen.
    //
    // Kotlin memakai static typing, jadi `email` dan `pass` wajib String.
    fun login(email: String, pass: String) {
        // Login mulai, jadi LoginScreen perlu menampilkan loading.
        // Ini side effect ke state Compose.
        isLoading = true

        // Membuat query Firebase dengan method chaining:
        //
        // db
        // - reference ke node `users`.
        //
        // orderByChild("email")
        // - query akan mencari berdasarkan child bernama email.
        //
        // equalTo(email)
        // - hanya ambil user yang email-nya sama dengan input LoginScreen.
        //
        // addListenerForSingleValueEvent(...)
        // - membaca data satu kali saja.
        //
        // `object : ValueEventListener { ... }` adalah anonymous object.
        // Firebase tidak langsung return data, tetapi memanggil callback:
        // - onDataChange jika berhasil membaca data.
        // - onCancelled jika query gagal.
        db.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            // onDataChange adalah callback saat Firebase berhasil mengembalikan data.
            //
            // `override` berarti fungsi ini mengisi fungsi milik interface ValueEventListener.
            // `snapshot` adalah kumpulan data hasil query email.
            override fun onDataChange(snapshot: DataSnapshot) {
                // Firebase sudah memberi hasil, jadi loading dimatikan.
                isLoading = false

                // Jika snapshot tidak ada, berarti tidak ada user dengan email tersebut.
                //
                // `!snapshot.exists()` berarti "snapshot tidak exists".
                // `{ error = "..."; return }` adalah guard clause:
                // kondisi gagal ditangani cepat, lalu callback dihentikan.
                if (!snapshot.exists()) { error = "Email tidak terdaftar"; return }

                // Firebase query mengembalikan children, walaupun hasilnya mungkin hanya satu user.
                // Karena itu kode melakukan loop.
                for (snap in snapshot.children) {
                    // Mengubah satu DataSnapshot menjadi object User.
                    //
                    // `val` berarti user adalah immutable local value:
                    // referensinya tidak bisa diganti setelah dibuat.
                    //
                    // `User::class.java` adalah class reference yang dibutuhkan Firebase.
                    //
                    // `?: continue` adalah Elvis operator:
                    // - jika getValue(...) berhasil, hasilnya masuk ke `user`.
                    // - jika null, lanjut ke item berikutnya dalam loop.
                    val user = snap.getValue(User::class.java) ?: continue

                    // Jika gagal login sudah 3 kali atau lebih, akun dianggap diblokir.
                    //
                    // Ini guard clause lagi:
                    // isi error, lalu `return` agar proses login berhenti.
                    if (user.failedLoginAttempts >= 3) { error = "Akun diblokir karena terlalu banyak percobaan gagal."; return }

                    // Mengecek apakah password dari Firebase sama dengan password dari LoginScreen.
                    //
                    // `==` di Kotlin membandingkan isi/value.
                    if (user.password == pass) {
                        // Jika sebelumnya ada percobaan gagal, reset counter gagal login ke 0.
                        //
                        // snap.ref menunjuk ke lokasi user ini di Firebase.
                        // child("failedLoginAttempts") menunjuk ke field counter gagal login.
                        // setValue(0) menulis angka 0 ke Firebase.
                        //
                        // Ini side effect karena mengubah database.
                        if (user.failedLoginAttempts > 0) snap.ref.child("failedLoginAttempts").setValue(0)

                        // Simpan sesi login lokal.
                        //
                        // snap.key adalah id user dari Firebase dan bertipe nullable String?.
                        // `snap.key ?: ""` memakai Elvis operator:
                        // jika snap.key null, pakai string kosong.
                        //
                        // Setelah ini, aplikasi bisa tahu user sudah login.
                        sessionManager.saveLoginSession(snap.key ?: "", user.name, user.email)

                        // Beri sinyal sukses ke LoginScreen.
                        // LoginScreen akan membaca isSuccess di LaunchedEffect dan navigasi ke Home.
                        isSuccess = true
                    } else {
                        // Password salah.
                        //
                        // Buat nilai baru dari failedLoginAttempts lama + 1.
                        // Ini memakai `val`, jadi attempts tidak bisa diganti setelah dihitung.
                        val attempts = user.failedLoginAttempts + 1

                        // Simpan jumlah percobaan gagal terbaru ke Firebase.
                        // Ini side effect ke database.
                        snap.ref.child("failedLoginAttempts").setValue(attempts)

                        // `if` di Kotlin bisa menjadi expression yang menghasilkan nilai.
                        //
                        // Jika attempts >= 3, error menjadi "Akun diblokir."
                        // Jika belum 3, error berisi sisa kesempatan login.
                        //
                        // `${3 - attempts}` adalah string template.
                        error = if (attempts >= 3) "Akun diblokir." else "Password salah. Sisa: ${3 - attempts}"
                    }

                    // Setelah satu user diproses, callback dihentikan.
                    // Ini mencegah loop lanjut memproses data lain.
                    return
                }
            }

            // onCancelled adalah callback saat Firebase gagal menjalankan query.
            //
            // Loading dimatikan, lalu pesan error Firebase dikirim ke LoginScreen lewat state error.
            override fun onCancelled(e: DatabaseError) { isLoading = false; error = e.message }
        })
    }

    fun register(name: String, email: String, pass: String, hint: String) {
        isLoading = true
        db.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading = false
                if (snapshot.exists()) { error = "Email sudah terdaftar"; return }
                val key = db.push().key ?: run { error = "Gagal mendapat ID Firebase"; return }
                db.child(key).setValue(User(name, email, pass, hint, 0))
                    .addOnSuccessListener { isSuccess = true }
                    .addOnFailureListener { error = it.message ?: "Gagal registrasi" }
            }
            override fun onCancelled(e: DatabaseError) { isLoading = false; error = e.message }
        })
    }

    fun verifyForgotHint(email: String, hint: String) {
        isLoading = true
        db.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading = false
                if (!snapshot.exists()) { error = "Email tidak terdaftar"; return }
                for (snap in snapshot.children) {
                    val user = snap.getValue(User::class.java) ?: continue
                    if (user.hint == hint) { forgotEmail = email; isSuccess = true }
                    else error = "Hint salah"
                    return
                }
            }
            override fun onCancelled(e: DatabaseError) { isLoading = false; error = e.message }
        })
    }

    fun changePassword(newPass: String) {
        isLoading = true
        if (forgotEmail.isEmpty()) { error = "Sesi tidak valid"; isLoading = false; return }
        db.orderByChild("email").equalTo(forgotEmail).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading = false
                if (!snapshot.exists()) { error = "User tidak ditemukan"; return }
                for (snap in snapshot.children) {
                    snap.ref.child("password").setValue(newPass)
                    snap.ref.child("failedLoginAttempts").setValue(0)
                        .addOnSuccessListener { isSuccess = true; forgotEmail = "" }
                        .addOnFailureListener { error = "Gagal menyimpan password baru" }
                    return
                }
            }
            override fun onCancelled(e: DatabaseError) { isLoading = false; error = e.message }
        })
    }

    // Factory dipakai di navigation saat membuat AuthViewModel.
    // Karena AuthViewModel butuh SessionManager, constructor-nya tidak kosong.
    class Factory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        // Warning unchecked cast dimatikan karena create() mengembalikan generic T,
        // sedangkan yang dibuat di sini selalu AuthViewModel.
        @Suppress("UNCHECKED_CAST")

        // Fungsi ini dipanggil Android ketika AuthViewModel perlu dibuat.
        //
        // `<T : ViewModel>` berarti T harus turunan ViewModel.
        // `as T` adalah cast dari AuthViewModel ke tipe generic T.
        override fun <T : ViewModel> create(modelClass: Class<T>) = AuthViewModel(sessionManager) as T
    }
}
