package com.example.expensetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class EditProfileActivity : AppCompatActivity() {

    lateinit var materialToolbar: MaterialToolbar
    lateinit var name : EditText
    lateinit var income : EditText
    lateinit var spendingLimit : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        name = findViewById(R.id.nameET)
        income = findViewById(R.id.incomeET)
        spendingLimit = findViewById(R.id.maxSpendingLimitET)

        name.setText(intent.getStringExtra("name"))
        income.setText(intent.getStringExtra("income"))
        spendingLimit.setText(intent.getStringExtra("spendingLimit"))

        materialToolbar = findViewById(R.id.editProfileToolBar)

        materialToolbar.setNavigationOnClickListener {
            finish()
        }

        materialToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.saveProfileEdits -> {
                    FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child("name").setValue(name.text.toString())
                    FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child("income").setValue(income.text.toString())
                    FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child("maxSpendingLimit").setValue(spendingLimit.text.toString()).addOnSuccessListener {
                            finish()
                        }

                    true
                }
                else -> true
            }
        }


    }
}