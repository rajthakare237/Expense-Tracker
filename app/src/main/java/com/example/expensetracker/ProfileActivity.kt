package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView

class ProfileActivity : AppCompatActivity() {

    lateinit var profileToolbar: MaterialToolbar
    lateinit var logout: ImageView
    lateinit var changeProfilePictureIV: ImageView
    lateinit var name: TextView
    lateinit var income: TextView
    lateinit var spendingLimit: TextView
    lateinit var editProfileButton: MaterialButton
    lateinit var savePriorities: MaterialButton
    lateinit var profilePictureIV: CircleImageView

    lateinit var chipSports: Chip
    lateinit var chipFood: Chip
    lateinit var chipEducation: Chip
    lateinit var chipTravel: Chip
    lateinit var chipShopping: Chip
    lateinit var chipEntertainment: Chip
    lateinit var chipGifts: Chip
    lateinit var chipClothes: Chip

    lateinit var chipGroup: ChipGroup

    var prioritiesArrayList = ArrayList<String>()

    var checkPriorList = ArrayList<String>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        profileToolbar = findViewById(R.id.profileToolbar)
        editProfileButton = findViewById(R.id.editProfileBtn)
        changeProfilePictureIV = findViewById(R.id.changeProfilePictureIV)
        profilePictureIV = findViewById(R.id.profilePictureIV)
        savePriorities = findViewById(R.id.savePriorities)

        chipSports = findViewById(R.id.chipSports)
        chipFood = findViewById(R.id.chipFood)
        chipEducation = findViewById(R.id.chipEducation)
        chipTravel = findViewById(R.id.chipTravel)
        chipShopping = findViewById(R.id.chipShopping)
        chipEntertainment = findViewById(R.id.chipEntertainment)
        chipGifts = findViewById(R.id.chipGifts)
        chipClothes = findViewById(R.id.chipClothes)

        chipGroup = findViewById(R.id.chipGroup)

        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("profilePicture").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profilePictureUrl = snapshot.value
                    Glide.with(applicationContext)
                        .load(profilePictureUrl)
                        .into(profilePictureIV)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Priorities").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    checkPriorList.clear()
                    for (item in snapshot.children) {
                        checkPriorList.add(item.key.toString())
                    }
                    if (checkPriorList.contains("Sports")) {
                        chipSports.isChecked = true
                    }
                    if (checkPriorList.contains("Food")) {
                        chipFood.isChecked = true
                    }
                    if (checkPriorList.contains("Education")) {
                        chipEducation.isChecked = true
                    }
                    if (checkPriorList.contains("Entertainment")) {
                        chipEntertainment.isChecked = true
                    }
                    if (checkPriorList.contains("Travel")) {
                        chipTravel.isChecked = true
                    }
                    if (checkPriorList.contains("Gifts")) {
                        chipGifts.isChecked = true
                    }
                    if (checkPriorList.contains("Clothes")) {
                        chipClothes.isChecked = true
                    }
                    if (checkPriorList.contains("Shopping")) {
                        chipShopping.isChecked = true
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })





        name = findViewById(R.id.nameTV)
        income = findViewById(R.id.incomeTV)
        spendingLimit = findViewById(R.id.maxSpendingLimitTV)

        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) {
                        if (item.key.toString() == "name") {
                            name.text = "Name : ${item.value.toString()}"
                        }
                        if (item.key.toString() == "income") {
                            income.text = "Monthly Income : ${item.value.toString()}"
                        }
                        if (item.key.toString() == "maxSpendingLimit") {
                            spendingLimit.text = "Monthly Spending Limit : ${item.value.toString()}"
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        chipSports.setOnClickListener(View.OnClickListener { // check whether the chips is filtered by user or
            // not and give the suitable Toast message
            if (chipSports.isChecked) {
                prioritiesArrayList.add("Sports")
            } else {
                prioritiesArrayList.remove("Sports")
            }
        })
        chipFood.setOnClickListener(View.OnClickListener { // check whether the chips is filtered by user or
            // not and give the suitable Toast message
            if (chipFood.isChecked) {
                prioritiesArrayList.add("Food")
            } else {
                prioritiesArrayList.remove("Food")
            }
        })
        chipEducation.setOnClickListener(View.OnClickListener { // check whether the chips is filtered by user or
            // not and give the suitable Toast message
            if (chipEducation.isChecked) {
                prioritiesArrayList.add("Education")
            } else {
                prioritiesArrayList.remove("Education")
            }
        })
        chipTravel.setOnClickListener(View.OnClickListener { // check whether the chips is filtered by user or
            // not and give the suitable Toast message
            if (chipTravel.isChecked) {
                prioritiesArrayList.add("Travel")
            } else {
                prioritiesArrayList.remove("Travel")
            }
        })
        chipShopping.setOnClickListener(View.OnClickListener { // check whether the chips is filtered by user or
            // not and give the suitable Toast message
            if (chipShopping.isChecked) {
                prioritiesArrayList.add("Shopping")
            } else {
                prioritiesArrayList.remove("Shopping")
            }
        })
        chipEntertainment.setOnClickListener(View.OnClickListener { // check whether the chips is filtered by user or
            // not and give the suitable Toast message
            if (chipEntertainment.isChecked) {
                prioritiesArrayList.add("Entertainment")
            } else {
                prioritiesArrayList.remove("Entertainment")
            }
        })
        chipGifts.setOnClickListener(View.OnClickListener { // check whether the chips is filtered by user or
            // not and give the suitable Toast message
            if (chipGifts.isChecked) {
                prioritiesArrayList.add("Gifts")
            } else {
                prioritiesArrayList.remove("Gifts")
            }
        })
        chipClothes.setOnClickListener(View.OnClickListener { // check whether the chips is filtered by user or
            // not and give the suitable Toast message
            if (chipClothes.isChecked) {
                prioritiesArrayList.add("Clothes")
            } else {
                prioritiesArrayList.remove("Clothes")
            }
        })

        savePriorities.setOnClickListener {


            for (i in 1..prioritiesArrayList.size) {
                FirebaseDatabase.getInstance().reference.child("users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    .child("Priorities").child(prioritiesArrayList[i - 1]).setValue("true").addOnSuccessListener {
                        Toast.makeText(this,"Sucess",Toast.LENGTH_SHORT).show()
                    }

            }


//            FirebaseDatabase.getInstance().reference.child("users")
//                .child(FirebaseAuth.getInstance().currentUser!!.uid)
//                .child("Priorities").removeValue().addOnSuccessListener {
//                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
//                    for (i in 1..prioritiesArrayList.size) {
//                        FirebaseDatabase.getInstance().reference.child("users")
//                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
//                            .child("Priorities").child(prioritiesArrayList[i - 1]).setValue("true")
//
//                    }
//                }
        }


        changeProfilePictureIV.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 11)

        }


        logout = findViewById(R.id.logoutIV)

        editProfileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            var inc = income.text.toString().drop(17)
            var limit = spendingLimit.text.toString().drop(25)
            var namet = name.text.toString().drop(7)
//            intent.putExtra("name",name.text.toString())
//            intent.putExtra("income",income.text.toString())
//            intent.putExtra("spendingLimit",spendingLimit.text.toString())
            intent.putExtra("name", namet)
            intent.putExtra("income", inc)
            intent.putExtra("spendingLimit", limit)
            startActivity(intent)
        }

        logout.setOnClickListener {


            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Are you sure you want to Sign Out?")
            builder.setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->

                    try {
                        Firebase.auth.signOut()
                        startActivity(Intent(this, SignInActivity::class.java))
                    } catch (e: Exception) {
                    }
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()
        }

        profileToolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11) {
            if (data?.data != null) {

                val uri = data.data
                profilePictureIV.setImageURI(uri)

                val reference = Firebase.auth.currentUser?.let {
                    FirebaseStorage.getInstance().reference.child("profilePictures")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                }
                if (uri != null) {

                    try {
                        reference?.putFile(uri)?.addOnSuccessListener {
                            if (it.task.isSuccessful) {


                                reference.downloadUrl.addOnSuccessListener {
                                    FirebaseDatabase.getInstance().reference.child("users")
                                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                        .child("profilePicture").setValue(it.toString()).addOnSuccessListener {
                                            Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        }
                    } catch (e: Exception) {
                    }

                }
            }
        }

    }
}