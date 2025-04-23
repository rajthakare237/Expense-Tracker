package com.example.expensetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.example.expensetracker.Models.Expense
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class MonthlyExpenseChart : AppCompatActivity() {

    lateinit var anyChartView: AnyChartView
    lateinit var materialToolbar: MaterialToolbar

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_expense_chart)

        materialToolbar = findViewById(R.id.monthExpToolbarCharts)

        materialToolbar.setNavigationOnClickListener {
            finish()
        }

        val pie = AnyChart.pie()

        anyChartView = findViewById(R.id.chartViewMonthExp)

        setupPieChart(pie)

    }


    // function to get format date
    private fun formatDateFun(date : String): Long {
        var timeInMillis = 0L
        if(date.contains("/"))
        {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = format.parse(date)
            timeInMillis = date?.time ?: 0
        }
        else
        {
            val format = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            val date = format.parse(date)
            timeInMillis = date?.time ?: 0
        }

        return timeInMillis
    }


    // function to setup pie chart
    private fun setupPieChart(pie : Pie) {

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, -30)
        val thirtyDaysAgo = calendar.timeInMillis

        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Expenses").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.exists())
                    {
                        for(item in snapshot.children){
                            val model = item.getValue(Expense::class.java)!!

                            val date = model.date

                            val time = formatDateFun(date)

                            if(time<thirtyDaysAgo){
                                continue
                            }

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