package com.example.uaipapo.ui.feature.auth.signin

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.firestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor() : ViewModel() {

    private val db = Firebase.firestore
    private val _state = MutableStateFlow<SignInState>(SignInState.Nothing)
    val state = _state.asStateFlow()

    val auth = FirebaseAuth.getInstance()
    private var userCredential: PhoneAuthCredential? = null
    var otpCode: String? = null
    var storedVerificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        _state.value = SignInState.Loading
        // Firebase signIn

        userCredential?.let{
            auth.signInWithCredential(userCredential!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        task.result.user?.let {
                            _state.value = SignInState.Success
                            return@addOnCompleteListener
                        }
                        _state.value = SignInState.Error
                    } else {
                        Log.w("TAG", "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            _state.value = SignInState.CodeError
                        }
                    }
                }
        }
    }

    fun sendOtp(phoneNumber: String, isResend: Boolean) {
        _state.value = SignInState.Loading

        phoneNumber.replace("(", "").replace(")", "").replace("-", "").replace(" ", "")

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                _state.value = SignInState.Verified
                userCredential = credential
                signIn(userCredential!!)
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
                _state.value = SignInState.CodeSent

                Log.d("TAG", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }

    val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
        .setPhoneNumber(phoneNumber) // Phone number to verify
        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
        .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks

    if (isResend) {
        resendToken?.let { token ->
            optionsBuilder.setForceResendingToken(token)
            Log.d("SignInViewModel", "Resending OTP with token.")
        } ?: run {
            Log.w("SignInViewModel", "isResend is true but resendToken is null. Sending as new OTP.")
        }
    }

    val phoneAuthOptions = optionsBuilder.build()
    PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
}

sealed class SignInState {
    object Nothing : SignInState()
    object Loading : SignInState()
    object CodeSent : SignInState()
    object Verified : SignInState()
    object Success : SignInState()
    object CodeError : SignInState()
    object Error : SignInState()
}