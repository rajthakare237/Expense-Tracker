package com.example.expensetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.Adapters.AllExpensesAdapter
import com.example.expensetracker.Models.Expense
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllExpensesActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var arrayList: ArrayList<Expense>
    lateinit var materialToolbar: MaterialToolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_expenses)

        materialToolbar = findViewById(R.id.profileToolbarAllExp)

        materialToolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewAllExp)
        arrayList = ArrayList()
        val adapter : AllExpensesAdapter = AllExpensesAdapter(this,arrayList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Expenses")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    arrayList.clear()
                    for(item in snapshot.children){
                        var model = item.getValue(Expense::class.java)!!
                        arrayList.add(model)
                    }
                    arrayList.reverse()
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }
}