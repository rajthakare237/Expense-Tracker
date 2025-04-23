package com.example.expensetracker

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.example.expensetracker.Models.User
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignUpVerification : AppCompatActivity() {

    lateinit var button: MaterialButton
    lateinit var confirmButton: MaterialButton
    lateinit var textView: TextView

    override fun onResume() {
        super.onResume()
        try {
            Firebase.auth.currentUser!!.reload().addOnSuccessListener {
                if (Firebase.auth.currentUser!!.isEmailVerified) {
                    registerUserOnDatabase(
                        intent.getStringExtra("email").toString(),
                        intent.getStringExtra("password").toString()
                    )
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Firebase.auth.currentUser!!.reload().addOnSuccessListener {
                if(!Firebase.auth.currentUser!!.isEmailVerified){
                    Firebase.auth.currentUser!!.delete()
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up_verification)

        val username = intent.getStringExtra("username").toString()
        val displayName = intent.getStringExtra("displayName").toString()
        val email = intent.getStringExtra("email").toString()
        val password = intent.getStringExtra("password").toString()

        button = findViewById(R.id.resendVerificationMail)
        confirmButton = findViewById(R.id.confirmVerificationMail)
        textView = findViewById(R.id.textViewVerification)

        textView.text = "Verification link has been sent to $email.\nPlease Verify.\nCheck 'spam' folder also."

        val user = Firebase.auth.currentUser!!


        try {
            confirmButton.setOnClickListener {
                user.reload().addOnSuccessListener {
                    if (user.isEmailVerified) {
                        registerUserOnDatabase(email, password)
                    }
                    else if(!user.isEmailVerified){
                        Toast.makeText(this, "Please verify email.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
        }

        button.setOnClickListener {
            try {
                val user = Firebase.auth.currentUser!!
                user.reload()
                user.sendEmailVerification().addOnSuccessListener {
                    Toast.makeText(this, "Verification Email Has Been Sent.", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failure occurred", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
            }
        }
    }

    // function to add details of user on firebase realtime database
    private fun registerUserOnDatabase(
        email: String,
        password: String
    ) {
        val db = Firebase.database
        val user = User()
        val auth = Firebase.auth
        user.uid = Firebase.auth.currentUser!!.uid
        user.email = email
        user.password = password
        db.reference.child("users").child(auth.currentUser!!.uid).setValue(user)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    SignUpVerification().finish()
                }
            }
    }
}