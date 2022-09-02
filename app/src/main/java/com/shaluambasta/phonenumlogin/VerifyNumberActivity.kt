package com.shaluambasta.phonenumlogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.shaluambasta.phonenumlogin.utils.phoneNumberKey
import java.util.concurrent.TimeUnit

class VerifyNumberActivity : AppCompatActivity() {


    private lateinit var textAuthenticateNum: MaterialTextView
    private lateinit var etOtp: TextInputLayout
    private lateinit var btnVerify: MaterialButton
    private lateinit var btnCancel: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    private var phoneNum: String = "+91"
    private var storedVerificationId: String? = null
    private val TAG = "VerifyNumberActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_number)


        textAuthenticateNum = findViewById(R.id.text_authenticate_num)
        etOtp = findViewById(R.id.et_otp)
        btnVerify = findViewById(R.id.btn_verify)
        btnCancel = findViewById(R.id.btn_cancel)

        auth = FirebaseAuth.getInstance()
        auth.useAppLanguage()


        if (intent != null) {
            val num = intent.getStringExtra(phoneNumberKey).toString()
            phoneNum += num
            Log.d(TAG, phoneNum)
            "Authenticate $phoneNum".also { textAuthenticateNum.text = it }
        } else {
            Toast.makeText(this, "Bad Gateway 😒", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnVerify.setOnClickListener {
            if (etOtp.editText?.text.toString().isNotEmpty()) {
                etOtp.clearFocus()
                verifyVerificationCode(etOtp.editText?.text.toString())
            } else {
                etOtp.error = "Enter OTP 🤨"
                etOtp.requestFocus()
                return@setOnClickListener
            }
        }

        btnCancel.setOnClickListener {
            returnToEnterNumberActivity()
        }


        verificationCallbacks()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNum)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    private fun verificationCallbacks() {

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")

                val code = credential.smsCode
                if (code != null) {
                    verifyVerificationCode(code)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        // Invalid request
                        Toast.makeText(
                            this@VerifyNumberActivity,
                            "Invalid request", Toast.LENGTH_SHORT
                        ).show()
                        returnToEnterNumberActivity()

                    }
                    is FirebaseTooManyRequestsException -> {
                        // The SMS quota for the project has been exceeded
                        Toast.makeText(
                            this@VerifyNumberActivity,
                            "The SMS quota for the project has been exceeded",
                            Toast.LENGTH_SHORT
                        ).show()
                        returnToEnterNumberActivity()

                    }
                    else -> {
                        // Show a message and update the UI
                        Toast.makeText(
                            this@VerifyNumberActivity,
                            e.message.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                        returnToEnterNumberActivity()
                    }
                }
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                Toast.makeText(
                    this@VerifyNumberActivity,
                    "OTP sent to $phoneNum",
                    Toast.LENGTH_SHORT
                ).show()

                super.onCodeSent(verificationId, resendToken)

            }
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    Toast.makeText(this, "Authorization Completed 🥳🥳", Toast.LENGTH_SHORT).show()

                    val user = task.result?.user

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()

                } else {

                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(
                            this,
                            "The verification code entered was invalid 🥺",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Update UI
                        Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                    returnToEnterNumberActivity()

                }
            }
    }

    private fun verifyVerificationCode(code: String) {

        //creating the credential
        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, code)
        //signing the user
        signInWithPhoneAuthCredential(credential)

    }

    private fun returnToEnterNumberActivity() {
        val intent = Intent(applicationContext, EnterNumberActivity::class.java)
        startActivity(intent)
        finish()
    }
}