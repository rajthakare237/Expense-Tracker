package com.example.expensetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.example.expensetracker.Models.Expense
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ChartsActivity : AppCompatActivity() {

//    lateinit var barGraph: AnyChartView
    lateinit var materialToolbar: MaterialToolbar
    lateinit var weeklyButton : MaterialButton
    lateinit var monthlyButton : MaterialButton
    lateinit var yearlyButton : MaterialButton
    lateinit var allExpenseButton: MaterialButton

    var food = 0.0
    var sports = 0.0
    var travel = 0.0
    var education = 0.0
    var shopping = 0.0
    var entertainment = 0.0
    var gifts = 0.0
    var clothes = 0.0
    var general = 0.0

    var category = arrayOf(
        "Food",
        "Sports",
        "Travel",
        "Education",
        "Shopping",
        "Entertainment",
        "Gifts",
        "Clothes",
        "General"
    )
    var expenseArray =
        arrayOf(food, sports, travel, education, shopping, entertainment, gifts, clothes, general)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_charts)

        weeklyButton = findViewById(R.id.weeklyButton)
        monthlyButton = findViewById(R.id.monthlyButton)
        yearlyButton = findViewById(R.id.yearlyButton)
        allExpenseButton = findViewById(R.id.allButton)

        weeklyButton.setOnClickListener {
            startActivity(Intent(this,WeeklyExpenseChart::class.java))
        }
        monthlyButton.setOnClickListener {
            startActivity(Intent(this,MonthlyExpenseChart::class.java))
        }
        yearlyButton.setOnClickListener {
            startActivity(Intent(this,YearlyExpenseChart::class.java))
        }
        allExpenseButton.setOnClickListener {
            startActivity(Intent(this,AllExpenseChart::class.java))
        }

        materialToolbar = findViewById(R.id.profileToolbarCharts)

        materialToolbar.setNavigationOnClickListener {
            finish()
        }

//        barGraph = findViewById(R.id.barGraph)


//        FirebaseDatabase.getInstance().reference.child("users")
//            .child(FirebaseAuth.getInstance().currentUser!!.uid)
//            .child("Expenses").addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//
//                    for (item in snapshot.children) {
//                        val model = item.getValue(Expense::class.java)!!
//                        when (model.category) {
//
//                            "Food" -> {
//                                food += model.amount.toFloat()
//                                true
//                            }
//                            "Sports" -> {
//                                sports += model.amount.toFloat()
//                                true
//                            }
//                            "Travel" -> {
//                                travel += model.amount.toFloat()
//                                true
//                            }
//                            "Education" -> {
//                                education += model.amount.toFloat()
//                                true
//                            }
//                            "Shopping" -> {
//                                shopping += model.amount.toFloat()
//                                true
//                            }
//                            "Entertainment" -> {
//                                entertainment += model.amount.toFloat()
//                                true
//                            }
//                            "Gifts" -> {
//                                gifts += model.amount.toFloat()
//                                true
//                            }
//                            "Clothes" -> {
//                                clothes += model.amount.toFloat()
//                                true
//                            }
//                            "General" -> {
//                                general += model.amount.toFloat()
//                                true
//                            }
//                            else -> {
//                                false
//                            }
//                        }
//
//                    }
//
//                    expenseArray = arrayOf(
//                        food,
//                        sports,
//                        travel,
//                        education,
//                        shopping,
//                        entertainment,
//                        gifts,
//                        clothes,
//                        general
//                    )
//
//                    var column = AnyChart.column()
//                    var dataEntries = ArrayList<DataEntry>()
//
//                    for (i in 1..9) {
//                        dataEntries.add(ValueDataEntry(category[i - 1], expenseArray[i - 1]))
//                    }
//
//                    column.data(dataEntries)
//                    barGraph.setChart(column)
//
//
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    TODO("Not yet implemented")
//                }
//
//            })

    }


}