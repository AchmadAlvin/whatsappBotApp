package com.kelompoksatu.kafecraft.ui.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kelompoksatu.kafecraft.data.SessionManager
import com.kelompoksatu.kafecraft.data.User

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {

    private val db = FirebaseDatabase.getInstance().getReference("users")

    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)
    var forgotEmail by mutableStateOf("")

    fun resetState() { isLoading = false; error = null; isSuccess = false }

    fun login(email: String, pass: String) {
        isLoading = true
        db.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading = false
                if (!snapshot.exists()) { error = "Email tidak terdaftar"; return }
                for (snap in snapshot.children) {
                    val user = snap.getValue(User::class.java) ?: continue
                    if (user.failedLoginAttempts >= 3) { error = "Akun diblokir karena terlalu banyak percobaan gagal."; return }
                    if (user.password == pass) {
                        if (user.failedLoginAttempts > 0) snap.ref.child("failedLoginAttempts").setValue(0)
                        sessionManager.saveLoginSession(snap.key ?: "", user.name, user.email)
                        isSuccess = true
                    } else {
                        val attempts = user.failedLoginAttempts + 1
                        snap.ref.child("failedLoginAttempts").setValue(attempts)
                        error = if (attempts >= 3) "Akun diblokir." else "Password salah. Sisa: ${3 - attempts}"
                    }
                    return
                }
            }
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

    class Factory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = AuthViewModel(sessionManager) as T
    }
}
