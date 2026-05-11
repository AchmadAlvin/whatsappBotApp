package com.kelompoksatu.kafecraft.ui.auth

// Toast dipakai untuk menampilkan pesan singkat di Android.
// Di LoginScreen, Toast muncul saat input kosong atau login error.
import android.widget.Toast

// Import Compose foundation untuk background, klik, layout, shape, dan keyboard.
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions

// Import Material3 untuk Text, Button, OutlinedTextField, CircularProgressIndicator, dan colors.
import androidx.compose.material3.*

// Import runtime Compose:
// - @Composable
// - remember
// - mutableStateOf
// - LaunchedEffect
// - delegated property `by`
import androidx.compose.runtime.*

// Import kebutuhan UI: alignment, modifier, warna, context, font, input, dan ukuran.
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// @Composable menandai fungsi ini sebagai fungsi UI Compose.
//
// Compose memakai declarative UI:
// tampilan ditulis berdasarkan state saat ini.
//
// Jika state berubah, Compose bisa melakukan recomposition,
// yaitu menjalankan ulang bagian UI yang membaca state tersebut.
@Composable
fun LoginScreen(
    // AuthViewModel adalah sumber state dan aksi login untuk screen ini.
    // LoginScreen membaca isLoading, isSuccess, dan error dari ViewModel.
    // LoginScreen juga memanggil login(...) dan resetState().
    viewModel: AuthViewModel,

    // Function type `() -> Unit`.
    // Artinya fungsi tanpa parameter dan tidak mengembalikan nilai.
    // Ini callback navigasi ke halaman register.
    onNavigateToRegister: () -> Unit,

    // Callback navigasi ke Home setelah login berhasil.
    onNavigateToHome: () -> Unit,

    // Callback navigasi ke halaman lupa password.
    onNavigateToForgot: () -> Unit
) {
    // State lokal untuk input email.
    //
    // `var` berarti nilainya bisa berubah saat user mengetik.
    // `remember` menyimpan state selama Composable masih aktif.
    // `mutableStateOf("")` membuat state Compose dengan nilai awal string kosong.
    // `by` adalah delegated property agar bisa menulis `email = it`, bukan `email.value = it`.
    var email by remember { mutableStateOf("") }

    // State lokal untuk input password.
    // Polanya sama seperti email: remember + mutableStateOf + delegated property `by`.
    var password by remember { mutableStateOf("") }

    // Context Android dibutuhkan untuk membuat Toast.
    // LocalContext.current adalah cara Compose mengambil Context saat ini.
    val context = LocalContext.current

    // LaunchedEffect menjalankan side effect berdasarkan perubahan key.
    //
    // Key di sini:
    // - viewModel.isSuccess
    // - viewModel.error
    //
    // Jadi saat AuthViewModel mengubah isSuccess/error, block ini bisa berjalan.
    //
    // Side effect adalah aksi yang bukan sekadar menggambar UI,
    // contohnya navigasi dan menampilkan Toast.
    LaunchedEffect(viewModel.isSuccess, viewModel.error) {
        // Jika login berhasil, AuthViewModel mengubah isSuccess menjadi true.
        //
        // LoginScreen merespons dengan:
        // - menjalankan callback onNavigateToHome()
        // - memanggil resetState() agar isSuccess tidak terus true.
        if (viewModel.isSuccess) { onNavigateToHome(); viewModel.resetState() }

        // `viewModel.error?.let { ... }` memakai:
        // - safe call `?.`: block hanya jalan kalau error tidak null.
        // - let: scope function Kotlin.
        // - lambda: block `{ ... }`.
        // - `it`: nilai error String yang dikirim ke lambda.
        //
        // Setelah error ditampilkan sebagai Toast, state direset agar Toast tidak muncul berulang.
        viewModel.error?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.resetState() }
    }

    // Column menyusun elemen UI secara vertikal.
    Column(
        // Modifier chain:
        // - fillMaxSize(): memenuhi seluruh layar.
        // - padding(24.dp): memberi jarak dalam 24dp.
        //
        // Ini contoh method chaining di Kotlin.
        modifier = Modifier.fillMaxSize().padding(24.dp),

        // Semua child di dalam Column disejajarkan ke tengah secara horizontal.
        horizontalAlignment = Alignment.CenterHorizontally,

        // Semua child di dalam Column ditempatkan di tengah secara vertikal.
        verticalArrangement = Arrangement.Center
    ) {
        // Box dipakai untuk membuat area icon/logo.
        // contentAlignment = Alignment.Center membuat Text di dalam Box berada di tengah.
        Box(modifier = Modifier.size(80.dp).background(Color(0xFFFFEBE0), CircleShape), contentAlignment = Alignment.Center) {
            Text("🍴", fontSize = 32.sp)
        }
        // Spacer memberi jarak vertikal antar elemen.
        Spacer(Modifier.height(24.dp))
        // Judul layar login.
        Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
        // Jarak antara judul dan subtitle.
        Spacer(Modifier.height(8.dp))
        // Subtitle layar login.
        Text("Sign in to discover delicious recipes", fontSize = 16.sp, color = Color.Gray)
        // Jarak sebelum form input.
        Spacer(Modifier.height(32.dp))
        // Text field email.
        OutlinedTextField(
            // value mengambil data dari state email.
            //
            // onValueChange adalah lambda callback saat user mengetik.
            // `it` berisi teks terbaru dari input.
            // Saat `email = it`, state berubah dan UI bisa recomposition.
            //
            // label adalah lambda Composable untuk menampilkan label field.
            value = email, onValueChange = { email = it }, label = { Text("Email") },
            // placeholder muncul saat input kosong.
            // fillMaxWidth membuat field selebar parent.
            placeholder = { Text("your@email.com") }, modifier = Modifier.fillMaxWidth(),
            // KeyboardType.Email meminta sistem menampilkan keyboard email.
            // singleLine = true membatasi input menjadi satu baris.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true,
            // Warna border field saat fokus dan tidak fokus.
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7A45), unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        // Jarak antara input email dan password.
        Spacer(Modifier.height(16.dp))
        // Text field password.
        OutlinedTextField(
            // value mengambil data dari state password.
            // onValueChange memperbarui state password saat user mengetik.
            value = password, onValueChange = { password = it }, label = { Text("Password") },
            // PasswordVisualTransformation menyembunyikan karakter password di layar.
            // Ini hanya transformasi tampilan, bukan enkripsi.
            placeholder = { Text("Enter your password") }, visualTransformation = PasswordVisualTransformation(),
            // Field dibuat selebar parent, keyboard bertipe password, dan satu baris.
            modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true,
            // Warna border field password.
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7A45), unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        // Jarak sebelum link lupa password.
        Spacer(Modifier.height(8.dp))
        // Teks yang bisa diklik untuk menuju halaman lupa password.
        //
        // Modifier chain:
        // - align(Alignment.End): posisikan ke kanan dalam Column.
        // - clickable { onNavigateToForgot() }: lambda callback saat diklik.
        // - padding(8.dp): menambah area sentuh.
        Text("Forgot Password?", color = Color(0xFFFF7A45), modifier = Modifier.align(Alignment.End).clickable { onNavigateToForgot() }.padding(8.dp))
        // Jarak sebelum tombol login.
        Spacer(Modifier.height(24.dp))
        // Tombol login.
        Button(
            // onClick adalah lambda callback yang berjalan saat tombol ditekan.
            onClick = {
                // Validasi sederhana di UI:
                // email dan password harus tidak kosong.
                //
                // isNotEmpty() menghasilkan Boolean.
                // `&&` berarti kedua kondisi harus true.
                //
                // Jika valid, LoginScreen memanggil AuthViewModel.login().
                // Dari sini alurnya masuk ke Firebase lewat AuthViewModel.
                if (email.isNotEmpty() && password.isNotEmpty()) viewModel.login(email, password)
                // Jika input belum lengkap, tampilkan Toast.
                // Ini side effect dari event klik.
                else Toast.makeText(context, "Harap isi email dan password", Toast.LENGTH_SHORT).show()
            },
            // Tombol dibuat selebar parent dan tinggi 50dp.
            modifier = Modifier.fillMaxWidth().height(50.dp),
            // Bentuk tombol dengan sudut membulat.
            shape = RoundedCornerShape(12.dp),
            // Warna tombol.
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A45)),
            // Tombol disable saat login sedang loading.
            // State ini berasal dari AuthViewModel.login().
            enabled = !viewModel.isLoading
        ) {
            // Isi tombol bergantung pada state viewModel.isLoading.
            //
            // Jika isLoading true:
            // - tampilkan CircularProgressIndicator.
            //
            // Jika isLoading false:
            // - tampilkan teks Log In.
            //
            // Ini contoh declarative UI: tampilan mengikuti state.
            if (viewModel.isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            else Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        // Jarak sebelum area register.
        Spacer(Modifier.height(32.dp))
        // Row menyusun elemen secara horizontal.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account? ", color = Color.Gray)
            // Teks Sign Up bisa diklik untuk navigasi ke register.
            Text("Sign Up", color = Color(0xFFFF7A45), fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onNavigateToRegister() })
        }
    }
}
