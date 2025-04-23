package com.example.expensetracker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.expensetracker.Models.Expense
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ProfileActivity : AppCompatActivity() {

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"

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

    fun removeDecimal(input: String): Int {
        val index = input.indexOf('.')
        return if (index == -1) {
            // Input doesn't contain a decimal point
            input.toInt()
        } else {
            // Input contains a decimal point, remove the decimal and all digits after it
            input.substring(0, index).toInt()
        }
    }

    // function to read last month expenses
    fun getLastMonthExpense() {

        var spentAmount = 0
        var monthlySpendingLimit = 0

        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("maxSpendingLimit").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists()){


                        if(!snapshot!!.value.toString().isNullOrEmpty()){
                            monthlySpendingLimit = snapshot.value.toString().toLong().toInt()
                        }

                        Log.i("typed",monthlySpendingLimit.toString())


                        val calendar = Calendar.getInstance()
                        calendar.add(Calendar.DAY_OF_MONTH, -30)
                        val thirtyDaysAgo = calendar.timeInMillis

                        FirebaseDatabase.getInstance().reference.child("users")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child("Expenses")
                            .addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    spentAmount =0
                                    for(item in snapshot.children){
                                        if(snapshot.exists())
                                        {
                                            val model = item.getValue(Expense::class.java)!!
                                            var spentAmt = model.amount
                                            Log.i("spend",spentAmt)

                                            if(spentAmt.contains("."))
                                            {
                                                spentAmt = removeDecimal(spentAmt).toString()
                                            }


                                            val datet = model.date
                                            var timeInMillis = 0L
                                            if(datet.contains("/"))
                                            {
                                                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                                val date = format.parse(datet)
                                                timeInMillis = date?.time ?: 0
                                            }
                                            else
                                            {
                                                val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                                                val date = format.parse(datet)
                                                timeInMillis = date?.time ?: 0
                                            }

                                            if(timeInMillis >= thirtyDaysAgo){
                                                spentAmount += spentAmt.toInt()
                                            }
                                        }

                                    }
                                    Log.i("spentAmount",spentAmount.toString())
                                    if(spentAmount >= monthlySpendingLimit)
                                    {
                                        sendNotification()
                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {
                                }
                            })
                    }



                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })







    }

    fun sendNotification() {

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this,  ProfileActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setContentTitle("WARNING: Limit Exceeded")
                .setContentText("Your monthly spending limit exceeded.")
                .setSmallIcon(R.drawable.ic_baseline_warning_24)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_expense_tracker_icon))
                .setContentIntent(pendingIntent)

        }
        else {

            builder = Notification.Builder(this)
                .setContentTitle("WARNING: Limit Exceeded")
                .setContentText("Your monthly spending limit exceeded.")
                .setSmallIcon(R.drawable.ic_baseline_warning_24)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_expense_tracker_icon))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1234, builder.build())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        getLastMonthExpense()

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

                    if(snapshot.exists()){
                        val profilePictureUrl = snapshot.value
                        Glide.with(applicationContext)
                            .load(profilePictureUrl).placeholder(R.drawable.profile_placeholder)
                            .into(profilePictureIV)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })


        // code to read data from firebase
        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Priorities").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists())
                    {
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

                    if(snapshot.exists())
                    {
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