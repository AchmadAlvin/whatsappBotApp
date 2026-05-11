package com.kelompoksatu.kafecraft.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.kelompoksatu.kafecraft.data.SessionManager
import com.kelompoksatu.kafecraft.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String = "") : AuthState()
    data class Error(val error: String) : AuthState()
}

class AuthViewModel(private val sessionManager: SessionManager) : ViewModel() {
    private val database = FirebaseDatabase.getInstance().getReference("users")

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _forgotPasswordEmail = MutableStateFlow("")
    val forgotPasswordEmail = _forgotPasswordEmail.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun login(email: String, pass: String) {
        _authState.value = AuthState.Loading
        
        database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user != null) {
                            if (user.failedLoginAttempts >= 3) {
                                _authState.value = AuthState.Error("Akun diblokir karena terlalu banyak percobaan login yang gagal.")
                                return
                            }

                            if (user.password == pass) {
                                // Login sukses, reset failed attempts
                                if (user.failedLoginAttempts > 0) {
                                    userSnapshot.ref.child("failedLoginAttempts").setValue(0)
                                }
                                
                                val userId = userSnapshot.key ?: ""
                                sessionManager.saveLoginSession(userId, user.name, user.email)
                                _authState.value = AuthState.Success("Login berhasil")
                            } else {
                                // Password salah
                                val newAttempts = user.failedLoginAttempts + 1
                                userSnapshot.ref.child("failedLoginAttempts").setValue(newAttempts)
                                if (newAttempts >= 3) {
                                    _authState.value = AuthState.Error("Akun diblokir karena terlalu banyak percobaan login yang gagal.")
                                } else {
                                    _authState.value = AuthState.Error("Password salah. Sisa percobaan: ${3 - newAttempts}")
                                }
                            }
                            return
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Email tidak terdaftar")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _authState.value = AuthState.Error(error.message)
            }
        })
    }

    fun register(name: String, email: String, pass: String, hint: String) {
        _authState.value = AuthState.Loading
        
        database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    _authState.value = AuthState.Error("Email sudah terdaftar")
                } else {
                    val newUser = User(name, email, pass, hint, 0)
                    val key = database.push().key
                    if (key != null) {
                        database.child(key).setValue(newUser)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Success("Registrasi berhasil")
                            }
                            .addOnFailureListener { e ->
                                _authState.value = AuthState.Error(e.message ?: "Gagal registrasi")
                            }
                    } else {
                        _authState.value = AuthState.Error("Gagal mendapatkan ID Firebase")
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _authState.value = AuthState.Error(error.message)
            }
        })
    }

    fun verifyForgotHint(email: String, hint: String) {
        _authState.value = AuthState.Loading
        
        database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)
                        if (user != null) {
                            if (user.hint == hint) {
                                _forgotPasswordEmail.value = email // Simpan email sementara
                                _authState.value = AuthState.Success("Hint cocok")
                            } else {
                                _authState.value = AuthState.Error("Hint salah")
                            }
                            return
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Email tidak terdaftar")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _authState.value = AuthState.Error(error.message)
            }
        })
    }

    fun changePassword(newPass: String) {
        _authState.value = AuthState.Loading
        val email = _forgotPasswordEmail.value
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Sesi tidak valid")
            return
        }

        database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        userSnapshot.ref.child("password").setValue(newPass)
                        userSnapshot.ref.child("failedLoginAttempts").setValue(0)
                            .addOnSuccessListener {
                                _authState.value = AuthState.Success("Password berhasil diubah")
                                _forgotPasswordEmail.value = "" // Bersihkan sesi
                            }
                            .addOnFailureListener {
                                _authState.value = AuthState.Error("Gagal menyimpan password baru")
                            }
                        return
                    }
                } else {
                    _authState.value = AuthState.Error("User tidak ditemukan")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _authState.value = AuthState.Error(error.message)
            }
        })
    }

    class Factory(private val sessionManager: SessionManager) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(sessionManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
