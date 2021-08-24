package com.kamal.chatterbox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.kamal.chatterbox.databinding.ActivityLoginBinding
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var _binding : ActivityLoginBinding

    companion object {
        const val TAG = "AUTH"
    }

    private val auth = FirebaseAuth.getInstance()
    private val loginCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            Toast.makeText(this@LoginActivity, "VERIFICATION COMPLETED", Toast.LENGTH_SHORT).show()
            auth.signInWithCredential(credential)
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Toast.makeText(this@LoginActivity, "VERIFICATION FAILED", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            Toast.makeText(this@LoginActivity, "VERIFICATION CODE SENT", Toast.LENGTH_SHORT).show()
            updateUIState(UIState.ENTER_OTP)

            _binding.verifyOtpButton.setOnClickListener {
                val credential = PhoneAuthProvider.getCredential(
                    verificationId,
                    _binding.otpEditText.text.toString()
                )
                signInWithPhoneAuthCredential(credential)

            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        _binding.phoneLoginButton.setOnClickListener {
            initiatePhoneLogin(_binding.phoneNumberEditText.text.toString())
        }
        _binding.resetButton.setOnClickListener {
            updateUIState(UIState.ENTER_PHONE)
        }
    }


    fun initiatePhoneLogin(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(loginCallbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    enum class UIState {
        ENTER_PHONE,
        ENTER_OTP
    }

    fun updateUIState(uiState: UIState) = when (uiState) {
        UIState.ENTER_PHONE -> {
            _binding.phoneNumberEditText.apply {
                isEnabled = true
                setText("")
            }
            _binding.otpEditText.apply {
                isEnabled = false
                isVisible = false
            }
            _binding.phoneLoginButton.isEnabled = true
            _binding.resetButton.isEnabled = false
            _binding.verifyOtpButton.isEnabled = false
        }
        UIState.ENTER_OTP -> {
            _binding.phoneNumberEditText.isEnabled = false
            _binding.otpEditText.apply {
                isVisible = true
                isEnabled = true
                setText("")
            }
            _binding.phoneLoginButton.isEnabled = false
            _binding.resetButton.isEnabled = true
            _binding.verifyOtpButton.isEnabled = true
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "LOGIN SUCCESSFUL", Toast.LENGTH_SHORT).show()
                    val user = task.result?.user
                    goToProfileScreen()
                } else {
                    Toast.makeText(this, "LOGIN FAILED", Toast.LENGTH_SHORT).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "WRONG OTP", Toast.LENGTH_SHORT).show()
                    }
                    updateUIState(UIState.ENTER_PHONE)
                }
            }
    }


    fun goToProfileScreen() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }
}