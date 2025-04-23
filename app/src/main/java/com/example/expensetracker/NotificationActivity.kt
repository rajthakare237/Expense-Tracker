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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.bumptech.glide.Glide
import com.example.expensetracker.Models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : AppCompatActivity() {

    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        getLastMonthExpense()


    }

    fun getLastMonthExpense() {

        var spentAmount = 0
        var monthlySpendingLimit = 0

        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("maxSpendingLimit").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists())
                    {
                        monthlySpendingLimit = snapshot.value.toString().toLong().toInt()
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

}