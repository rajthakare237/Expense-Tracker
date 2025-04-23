package com.example.expensetracker

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.bumptech.glide.Glide
import com.example.expensetracker.Models.Expense
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

// home activity
class MainActivity : AppCompatActivity() {

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"

    lateinit var circleImageView: CircleImageView

    lateinit var dialog: AlertDialog

    lateinit var addExpenseButton: MaterialButton
    lateinit var chartsButton: MaterialButton
    lateinit var allExpButton: MaterialButton
    lateinit var goalButton: MaterialButton

    lateinit var anyChartView: AnyChartView
    lateinit var profilePicture : CircleImageView


    var food = 0.0
    var sports = 0.0
    var travel = 0.0
    var education = 0.0
    var shopping = 0.0
    var entertainment = 0.0
    var gifts = 0.0
    var clothes = 0.0
    var general = 0.0

    var category = arrayOf("Food","Sports","Travel","Education","Shopping","Entertainment","Gifts","Clothes","General")
    var expenseArray = arrayOf(food,sports,travel,education,shopping,entertainment,gifts,clothes,general)

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // code to remove decimals from amount
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



    // function to get last month expenses from firebase
    fun getLastMonthExpense() {

        var spentAmount = 0
        var monthlySpendingLimit = 0

        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("maxSpendingLimit").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){

                        if(!snapshot!!.value.toString().isNullOrEmpty()){
                            monthlySpendingLimit = snapshot!!.value.toString().toInt()
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


    // function to send notification in case of limit exceeded
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
        setContentView(R.layout.activity_main)
        profilePicture = findViewById(R.id.profilePicture)

        try {
            getLastMonthExpense()


            FirebaseDatabase.getInstance().reference.child("users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("profilePicture").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists())
                        {
                            val profilePictureUrl = snapshot.value
                            Glide.with(applicationContext)
                                .load(profilePictureUrl).placeholder(R.drawable.profile_placeholder)
                                .into(profilePicture)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            val pie = AnyChart.pie()


            anyChartView = findViewById(R.id.chartView)
            chartsButton = findViewById(R.id.chartsButton)
            allExpButton = findViewById(R.id.allExpButton)
            goalButton = findViewById(R.id.goalButton)

            chartsButton.setOnClickListener {
                startActivity(Intent(this,ChartsActivity::class.java))
    //            startActivity(Intent(this,ReadSMSActivity::class.java))
            }
            allExpButton.setOnClickListener {
                startActivity(Intent(this,AllExpensesActivity::class.java))
            }
            goalButton.setOnClickListener {
                startActivity(Intent(this,GoalActivity::class.java))
            }

            setupPieChart(pie)

            circleImageView = findViewById(R.id.profilePicture)
            addExpenseButton = findViewById(R.id.addExpenseButton)




            circleImageView.setOnClickListener {
                startActivity(Intent(this,ProfileActivity::class.java))
            }

            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Enter Expense")


            var view = layoutInflater.inflate(R.layout.add_expense_layout, null)
            val amount = view.findViewById<EditText>(R.id.amountET)
            val description = view.findViewById<EditText>(R.id.descriptionET)
            val submitExpenseButton = view.findViewById<MaterialButton>(R.id.submitExpenseButton)
            val cancelExpenseButton = view.findViewById<MaterialButton>(R.id.cancelExpenseButton)
            val pickDateButton = view.findViewById<MaterialButton>(R.id.pickDateButton)
            var autocompleteTV = view.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)

            val categories = resources.getStringArray(R.array.categories)
            val arrayAdapter = ArrayAdapter(this, R.layout.dropdown_item, categories)
            autocompleteTV.setAdapter(arrayAdapter)

            var categoryTemp = "General"
            var dateTemp = Date().time


            autocompleteTV.setOnItemClickListener { adapterView, view, i, l ->
                when(i){
                    0 -> {
                        categoryTemp = "Food"
                        true
                    }
                    1 -> {
                        categoryTemp = "Sports"
                        true
                    }
                    2 -> {
                        categoryTemp = "Travel"
                        true
                    }
                    3 -> {
                        categoryTemp = "Education"
                        true
                    }
                    4 -> {
                        categoryTemp = "Shopping"
                        true
                    }
                    5 -> {
                        categoryTemp = "Entertainment"
                        true
                    }
                    6 -> {
                        categoryTemp = "Gifts"
                        true
                    }
                    7 -> {
                        categoryTemp = "Clothes"
                        true
                    }
                    8 -> {
                        categoryTemp = "General"
                        true
                    }
                    else -> {
                        false
                    }
                }
            }

            pickDateButton.setOnClickListener {
                // on below line we are getting
                // the instance of our calendar.
                val c = Calendar.getInstance()

                // on below line we are getting
                // our day, month and year.
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                // on below line we are creating a
                // variable for date picker dialog.
                val datePickerDialog = DatePickerDialog(
                    // on below line we are passing context.
                    this,
                    { view, year, monthOfYear, dayOfMonth ->
                        // on below line we are setting
                        // date to our text view.
                        val dateT = Date(year,monthOfYear,dayOfMonth)
                        dateTemp = dateT.time
                        pickDateButton.text =
                            (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    },
                    // on below line we are passing year, month
                    // and day for the selected date in our date picker.
                    year,
                    month,
                    day
                )
                // at last we are calling show
                // to display our date picker dialog.
                datePickerDialog.show()
            }

            builder.setView(view)
            dialog = builder.create()
            addExpenseButton.setOnClickListener {
                val c = Calendar.getInstance()
                val year = c.get(Calendar.YEAR)
                val month = c.get(Calendar.MONTH)
                val day = c.get(Calendar.DAY_OF_MONTH)

                pickDateButton.text = "$day/$month/$year"

                dialog.show()
            }
            submitExpenseButton.setOnClickListener {

                if(!amount.text.toString().isNullOrEmpty())
                {

                    var amountTemp = amount.text.toString()
                    var descriptionTemp = description.text.toString()

                    var exactSubmitTime = Date().time.toString()

                    val expenseModel = Expense(amountTemp,pickDateButton.text.toString(),categoryTemp,descriptionTemp,exactSubmitTime)

                    amount.setText("")
                    description.setText("")


                    FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                        .child("Expenses").child(exactSubmitTime).setValue(expenseModel)
                        .addOnSuccessListener {
                            Toast.makeText(this,"Expense Submitted",Toast.LENGTH_SHORT).show()
                            setupPieChart(pie)
                            dialog.dismiss()
                        }
                }



            }
            cancelExpenseButton.setOnClickListener {
                dialog.dismiss()
            }
        } catch (e: Exception) {
        }

    }


    // function to setup pie chart
    private fun setupPieChart(pie : Pie) {
        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Expenses").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists())
                    {
                        for(item in snapshot.children){
                            val model = item.getValue(Expense::class.java)!!
                            when(model.category){

                                "Food" -> {
                                    food+=model.amount.toFloat()
                                    true
                                }
                                "Sports" -> {
                                    sports+=model.amount.toFloat()
                                    true
                                }
                                "Travel" -> {
                                    travel+=model.amount.toFloat()
                                    true
                                }
                                "Education" -> {
                                    education+=model.amount.toFloat()
                                    true
                                }
                                "Shopping" -> {
                                    shopping+=model.amount.toFloat()
                                    true
                                }
                                "Entertainment" -> {
                                    entertainment+=model.amount.toFloat()
                                    true
                                }
                                "Gifts" -> {
                                    gifts+=model.amount.toFloat()
                                    true
                                }
                                "Clothes" -> {
                                    clothes+=model.amount.toFloat()
                                    true
                                }
                                "General" -> {
                                    general+=model.amount.toFloat()
                                    true
                                }
                                else -> {
                                    false
                                }
                            }
                        }

                        expenseArray = arrayOf(food,sports,travel,education,shopping,entertainment,gifts,clothes,general)

                        var dataEntries = ArrayList<DataEntry>()

                        for(i in 1..9){
                            dataEntries.add(ValueDataEntry(category[i-1],expenseArray[i-1]))
                        }

                        pie.data(dataEntries)
                        anyChartView.setChart(pie)


                    }



                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}