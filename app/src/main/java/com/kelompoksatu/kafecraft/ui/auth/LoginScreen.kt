// 123. [LoginScreen.kt] Deklarasi package untuk file tampilan UI.
package com.kelompoksatu.kafecraft.ui.auth

// 124-142. Mengimpor library yang dibutuhkan:
// - android.widget.Toast: untuk menampilkan pesan singkat ke user
// - androidx.compose.*: komponen UI, layout, modifier, tema Material3, dan state management Compose
// - androidx.compose.ui.platform.LocalContext: untuk mengakses Context Android di dalam fungsi Composable
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 143. Anotasi '@Composable'. Ini menandakan bahwa fungsi di bawahnya bukanlah fungsi Kotlin biasa.
// Istilah Teknis: Compiler Plugin. Anotasi ini memberitahu Compose Compiler untuk memodifikasi fungsi ini agar bisa memancarkan (emit) node UI Tree. Fungsi Composable idealnya bertindak sebagai "Pure Function" UI, di mana UI yang digambar dijamin sama untuk input state yang sama (Idempotent).
@Composable
// 144. 'LoginScreen' adalah "Route" atau titik masuk layar utama UI halaman login.
// Menerima parameter ViewModel dan kumpulan fungsi lambda '() -> Unit' sebagai penyambung antarlayar (Istilah Teknis: Event Hoisting / State Hoisting).
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToForgot: () -> Unit
) {
    // 145. Mengambil objek Context Android murni (Application Environment) di dalam semesta fungsional Compose.
    val context = LocalContext.current

    // 146. 'LaunchedEffect' adalah Side-Effect Handler di Compose yang menjalankan blok Coroutine.
    // Fungsi Composable harus bebas efek samping (navigasi, Toast). 'LaunchedEffect' adalah satu-satunya blok legal untuk menembakkan efek samping secara aman.
    // Menerima dua keys: 'isSuccess' dan 'error'. Blok ini diulang SETIAP KALI salah satu dari kedua key tersebut berubah nilainya.
    LaunchedEffect(viewModel.isSuccess, viewModel.error) {
        // 147. Jika state ViewModel 'isSuccess' diubah menjadi true (oleh fungsi Firebase login di ViewModel)...
        if (viewModel.isSuccess) {
            // 148. Panggil fungsi Lambda Hoisting untuk memicu router menavigasi paksa ke halaman Home.
            onNavigateToHome()
            // 149. Reset state di ViewModel agar fungsi trigger ini tidak terjebak di Loop Tanpa Akhir (Infinite Loop).
            viewModel.resetState()
        }
        // 150. Safe call Operator dengan scope function 'let'.
        // Istilah FP: 'let' adalah pembungkus HOF yang mengeksekusi blok lambda di dalamnya hanya jika 'viewModel.error' bernilai tidak null. 'it' menangkap string pesan error tersebut.
        viewModel.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.resetState()
        }
    }

    // 151. Memanggil 'LoginScreenContent', sebuah Composable "Stateless" terpisah yang hanya bertugas murni menggambar visual.
    // Memisahkan Composable menjadi tipe Stateless (Dumb) dan tipe Stateful penyambung ViewModel (Smart) adalah pola desain (Best Practice) tingkat lanjut di arsitektur Compose UI.
    LoginScreenContent(
        isLoading = viewModel.isLoading,
        // 152. Trailing Lambda Argument. Melempar input pengguna (email, pass) kembali ke parent caller (Event Bubbling) agar dikirim dan diproses eksekusi Login di sisi ViewModel.
        onLogin = { email, pass -> viewModel.login(email, pass) },
        onNavigateToRegister = onNavigateToRegister,
        onNavigateToForgot = onNavigateToForgot
    )
}

// 153. Anotasi Composable. Modifier 'private' melindungi fungsi UI internal ini agar tidak terpanggil bocor di kelas Route lain.
@Composable
private fun LoginScreenContent(
    // 154. Parameter Default Arguments (seperti = false) diinjeksikan agar fungsi ini bisa di-'Preview' (dilihat bayangannya) mandiri tanpa harus ada injeksi ViewModel saat mendesain.
    isLoading: Boolean = false,
    onLogin: (String, String) -> Unit = { _, _ -> },
    onNavigateToRegister: () -> Unit = {},
    onNavigateToForgot: () -> Unit = {}
) {
    // 155. 'remember { mutableStateOf("") }' adalah deklarasi state lokal yang bertahan saat Recomposition.
    // Tanpa 'remember', setiap Recomposition (terjadi saat user mengetik) akan me-reset variabel ke string kosong, menghapus input yang sudah diketik.
    // 'remember' memastikan nilai state bertahan selama Composable ini aktif di layar.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // 155b. 'LocalContext.current' mengambil Context Android dari CompositionLocal tree. Dibutuhkan untuk Toast di blok validasi tombol di bawah.
    val context = LocalContext.current

    // 156. Elemen struktural 'Column', memposisikan komponen berderet ke bawah dengan modifikasi mengisi penuh layar (fillMaxSize).
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome Back", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF332211))
        Spacer(Modifier.height(8.dp))
        Text("Sign in to discover delicious recipes", fontSize = 16.sp, color = Color.Gray)
        Spacer(Modifier.height(32.dp))
        
        // 157. Komponen Isian Teks Bergaris (Outlined TextField) khusus input email.
        OutlinedTextField(
            value = email,
            // 158. HOF callback parameter 'onValueChange'. Ini dieksekusi berulangkali (Ratusan milidetik) setiap kali keyboard HP ditekan.
            // Parameter 'it' (huruf yang ditekan) mengubah var 'email'. Mutasi state ini memicu Cascade Recomposition untuk memunculkan teks input di layar nyata.
            onValueChange = { email = it },
            label = { Text("Email") },
            placeholder = { Text("your@email.com") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7A45), unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        Spacer(Modifier.height(16.dp))
        
        // 159. Komponen Isian Teks khusus Password.
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            // 160. VisualTransformation merupakan kelas Utility yang memanipulasi string di permukaan grafik, mengubah font huruf menjadi tanda bintik-bintik demi privasi sensor password.
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFFFF7A45), unfocusedBorderColor = Color(0xFFE0E0E0))
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Forgot Password?",
            color = Color(0xFFFF7A45),
            // 161. Modifier Builder (Metode Chain Pattern). Menempelkan fungsi Lambda HOF yang memicu layar Forgot Password jika disentuh (.clickable).
            modifier = Modifier.align(Alignment.End).clickable { onNavigateToForgot() }.padding(8.dp)
        )
        Spacer(Modifier.height(24.dp))
        
        // 162. Komponen Tombol Eksekusi 'Log In' Utama.
        Button(
            // 163. Parameter 'onClick' dibekali dengan Lambda Validation.
            onClick = {
                // 164. Logical Validation Rules: Mencegah serangan data kosong (Validation Gates) yang menghemat kuota jaringan sebelum menembak server API Firebase.
                if (email.isNotEmpty() && password.isNotEmpty()) {
                    // 165. Jika gerbang validasi lolos, delegasikan data String ke eksekusi Lambda Trailing 'onLogin'.
                    onLogin(email, password)
                } else {
                    Toast.makeText(context, "Harap isi email dan password", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7A45)),
            // 166. Deklaratif State Disabling. Mencegah (Debouncing kasar) tombol diklik dua kali ketika animasi loading sedang berjalan (isLoading = true).
            enabled = !isLoading
        ) {
            // 167. Declarative Conditional UI Treeing (Percabangan Pohon UI secara Kondisional).
            // Berbeda dengan sistem UI Tradisional yang main "setHidden(true)", Compose menggambar pohon baru. 
            // Jika ViewModel memancarkan state 'isLoading' bernilai True, tombol akan merender Indikator Loading. Jika False, tombol merender tulisan normal. Ini adalah keajaiban Recomposition FP.
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(32.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Don't have an account? ", color = Color.Gray)
            Text(
                "Sign Up",
                color = Color(0xFFFF7A45),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToRegister() }
            )
        }
    }
}

// 168. Anotasi '@Preview' mengizinkan Android Studio menggambar mockup rendering visual Composable tanpa harus me-run build ke emulator HP (Preview Tooling).
@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreenContent()
}
