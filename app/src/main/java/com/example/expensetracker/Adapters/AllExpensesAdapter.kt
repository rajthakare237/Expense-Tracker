package com.example.expensetracker.Adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.Models.Expense
import com.example.expensetracker.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AllExpensesAdapter(val context: Context, val arrayList: ArrayList<Expense>) :
    RecyclerView.Adapter<AllExpensesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val amountAllExp: TextView = itemView.findViewById(R.id.amountAllExp)
        val categoryAllExp: TextView = itemView.findViewById(R.id.categoryAllExp)
        val dateAllExp: TextView = itemView.findViewById(R.id.dateAllExp)
        val descriptionAllExp: TextView = itemView.findViewById(R.id.descriptionAllExp)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.all_expenses_card_view, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = arrayList[position]

//        val dateString: String = SimpleDateFormat("dd/MM/yyyy").format(Date(model.date.toLong()))

        holder.amountAllExp.text = "Rs ${model.amount}"
        holder.categoryAllExp.text = "Category : ${model.category}"
//        holder.dateAllExp.text = "Date : $dateString"
        holder.dateAllExp.text = "Date : ${model.date}"

        if(model.description.isEmpty()){
            holder.descriptionAllExp.visibility = View.GONE
        }
        else{
            holder.descriptionAllExp.text = "${model.description}"
        }

        holder.itemView.setOnLongClickListener {

            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle("Are you sure you want to Delete?")
            builder.setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->

                    try {
                        FirebaseDatabase.getInstance().reference.child("users")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid)
                            .child("Expenses")
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