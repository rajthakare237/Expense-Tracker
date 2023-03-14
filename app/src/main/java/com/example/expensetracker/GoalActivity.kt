package com.example.expensetracker

import android.app.DatePickerDialog
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.Adapters.AllExpensesAdapter
import com.example.expensetracker.Adapters.GoalAdapter
import com.example.expensetracker.Models.Expense
import com.example.expensetracker.Models.Goal
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*

class GoalActivity : AppCompatActivity() {

    lateinit var addGoalButton: MaterialButton
    lateinit var dialog: AlertDialog

    lateinit var recyclerView: RecyclerView
    lateinit var arrayList: ArrayList<Goal>

    lateinit var materialToolbar: MaterialToolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goal)

        materialToolbar = findViewById(R.id.profileToolbarGoal)

        materialToolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewGoal)

        arrayList = ArrayList()
        val adapter : GoalAdapter = GoalAdapter(this,arrayList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Goals")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    arrayList.clear()
                    for(item in snapshot.children){
                        var model = item.getValue(Goal::class.java)!!
                        arrayList.add(model)
                    }
                    arrayList.reverse()
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        addGoalButton = findViewById(R.id.addGoalButton)

        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("Add Goal")


        var view = layoutInflater.inflate(R.layout.add_goal_layout, null)
        val amount = view.findViewById<EditText>(R.id.amountETGoal)
        val title = view.findViewById<EditText>(R.id.titleET)
        val submitGoalButton = view.findViewById<MaterialButton>(R.id.submitGoalButton)
        val cancelGoalButton = view.findViewById<MaterialButton>(R.id.cancelGoalButton)
        val pickDateButton = view.findViewById<MaterialButton>(R.id.pickDateButtonGoal)

        var dateGoal = Date().time

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
                    dateGoal = dateT.time
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

        addGoalButton.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            pickDateButton.text = "$day/$month/$year"
            dialog.show()
        }

        cancelGoalButton.setOnClickListener {
            dialog.dismiss()
        }

        submitGoalButton.setOnClickListener {
            var exactSubmitTime = Date().time.toString()
            val goalModel = Goal(title.text.toString(),pickDateButton.text.toString(),amount.text.toString(),exactSubmitTime)
            title.setText("")
            amount.setText("")
            FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Goals").child(exactSubmitTime).setValue(goalModel).addOnSuccessListener {
                    Toast.makeText(this,"Goal added successfully",Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
        }

    }
}