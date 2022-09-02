package com.shaluambasta.phonenumlogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.shaluambasta.phonenumlogin.utils.phoneNumberKey

class EnterNumberActivity : AppCompatActivity() {


    private lateinit var etPhoneNum: TextInputLayout
    private lateinit var btnGetOtp: MaterialButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_number)


        etPhoneNum = findViewById(R.id.et_phone_num)
        btnGetOtp = findViewById(R.id.btn_get_otp)


        btnGetOtp.setOnClickListener {
            validateNumber()
        }
    }

    private fun validateNumber() {

        if (etPhoneNum.editText?.text.toString().isEmpty()) {
            etPhoneNum.error = "Enter your Phone Number"
            etPhoneNum.requestFocus()
            return
        }

        if (etPhoneNum.editText?.text.toString().count() == 10) {

            etPhoneNum.clearFocus()
            val intent = Intent(this, VerifyNumberActivity::class.java).apply {
                putExtra(phoneNumberKey, etPhoneNum.editText?.text.toString())
            }
            startActivity(intent)
            finish()

        } else {
            Toast.makeText(this, "Enter 10 digit number", Toast.LENGTH_SHORT).show()
        }
    }

}