package com.example.uaipapo.feature.auth.signin

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.database
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    private val database = Firebase.database.reference.child("users")
    val state = _state.asStateFlow()

    val auth = FirebaseAuth.getInstance()
    var currentUser: FirebaseUser? = null
    var verificationId: String? = null
    var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private val _timeOutSeconds = MutableStateFlow(60L)
    val timeOutSeconds: StateFlow<Long> = _timeOutSeconds.asStateFlow()

    init {
        checkAuthStatus()
    }

    fun verifyOtpAndSignIn(enteredOTP: String) {
        _state.value = AuthState.Loading

        Log.d("TAG", "verificationId: $verificationId")

        val currentVerficationId = verificationId

        if(currentVerficationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, enteredOTP)
            signInWithCredential(credential)
        }
    }

    fun signInWithCredential(credential: PhoneAuthCredential){
        _state.value = AuthState.Loading

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.user?.let {
                        currentUser = task.result.user
                        _state.value = AuthState.WaitingForName
                        return@addOnCompleteListener
                    }
                    _state.value = AuthState.Error
                } else {
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        _state.value = AuthState.CodeError
                    }
                }
            }
    }

    fun sendOtp(activity: Activity, phoneNumber: String, isResend: Boolean) {
        _state.value = AuthState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                _state.value = AuthState.WaitingForName
                signInWithCredential(credential)
                Log.d("TAG", "onVerificationCompleted:$credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _state.value = AuthState.Error
                Log.w("TAG", "onVerificationFailed", e)
            }

            override fun onCodeSent(
                code: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Log.d("TAG", "onCodeSent:$code")

                verificationId = code
                resendToken = token

                _state.value = AuthState.CodeSent
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks

        if (isResend) {
            resendToken?.let { token ->
                optionsBuilder.setForceResendingToken(token)
                Log.d("SignInViewModel", "Resending OTP with token.")
            } ?: run {
                Log.w(
                    "SignInViewModel",
                    "isResend is true but resendToken is null. Sending as new OTP."
                )
            }
        }

        val phoneAuthOptions = optionsBuilder.build()
        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
    }

    fun updateUserName(name: String){
        if(currentUser != null) {
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            currentUser!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        database.child(currentUser!!.uid).child("name").setValue(name)
                            .addOnCompleteListener { dbTask ->
                                _state.value = AuthState.Authenticated
                            }
                    } else {
                        _state.value = AuthState.Error
                    }
            }
        } else {
            _state.value = AuthState.Error
        }
    }

    @SuppressLint("DiscouragedApi")
    fun startResendTimer(){
        val timer = Timer()

        timer.scheduleAtFixedRate(
            object : TimerTask() {
                override fun run() {
                    _timeOutSeconds.value--

                    if(_timeOutSeconds.value <= 0){
                        timer.cancel()
                    }
                }
            },
            0,
            1000
        )
    }

    fun signOut(){
        auth.signOut()
        _state.value = AuthState.Unauthenticated
    }

    fun checkAuthStatus(){
        if(currentUser == null){
            _state.value = AuthState.Unauthenticated
        } else{
            _state.value = AuthState.Authenticated
        }
    }
}

sealed class AuthState {

    object Unauthenticated : AuthState()
    object Loading : AuthState()
    object CodeSent : AuthState()
    object Verified : AuthState()
    object CodeError : AuthState()
    object Error : AuthState()
    object WaitingForName : AuthState()
    object Authenticated : AuthState()
}