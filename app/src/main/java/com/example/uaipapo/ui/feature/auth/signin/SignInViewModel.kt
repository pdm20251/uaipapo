package com.example.uaipapo.ui.feature.auth.signin

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<SignInState>(SignInState.Nothing)
    val state = _state.asStateFlow()

    val auth = FirebaseAuth.getInstance()

    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken

    fun signIn(phonenumber: String) {
        _state.value = SignInState.Loading
        // Firebase signIn

//        FirebaseAuth.getInstance().signInWithEmailAndPassword(phonenumber)
//            .addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    task.result.user?.let {
//                        _state.value = SignInState.Success
//                        return@addOnCompleteListener
//                    }
//                    _state.value = SignInState.Error
//
//                } else {
//                    _state.value = SignInState.Error
//                }
//            }
    }

    fun sendOtp(phoneNumber: String, isResend: Boolean) {
        _state.value = SignInState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                _state.value = SignInState.Verified
                Log.d("TAG", "onVerificationCompleted:$credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _state.value = SignInState.Error
                Log.w("TAG", "onVerificationFailed", e)
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}

sealed class SignInState {
    object Nothing : SignInState()
    object Loading : SignInState()
    object Verified : SignInState()
    object Success : SignInState()
    object Error : SignInState()
}