package com.example.expensetracker.Adapters

import android.content.Context
import android.icu.text.MessageFormat.format
import android.text.format.DateFormat.format
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.Models.Goal
import com.example.expensetracker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.lang.String.format
import java.text.MessageFormat.format
import java.text.SimpleDateFormat
import java.util.*

// This is a adapter for recycler view to load goal items in goal activity
class GoalAdapter(val context: Context, val arrayList: ArrayList<Goal>) :
    RecyclerView.Adapter<GoalAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val goalTitle: TextView = itemView.findViewById(R.id.goalTitle)
        val dateGoal: TextView = itemView.findViewById(R.id.dateGoal)
        val amountGoal: TextView = itemView.findViewById(R.id.amountGoal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.goal_card_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val model = arrayList[position]

//        val dateString: String = SimpleDateFormat("dd/MM/yyyy").format(Date(model.date.toLong()))

        holder.goalTitle.text = "${model.title}"
//        holder.dateGoal.text = "Date : $dateString"
        holder.dateGoal.text = "Date : ${model.date}"
        holder.amountGoal.text = "Rs. ${model.amount}"

        holder.itemView.setOnLongClickListener {

            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle("Are you sure you want to Delete?")
            builder.setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->

                    try {
                        FirebaseDatabase.getInstance().reference.child("users")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child("Goals")
                            .child(model.submitTime)
                            .removeValue().addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT)
                                        .show()
                                    notifyDataSetChanged()
                                }
                            }
                    } catch (e: Exception) {
                    }
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()

            true

        }


    }

    override fun getItemCount(): Int {
        return arrayList.size
    }
}