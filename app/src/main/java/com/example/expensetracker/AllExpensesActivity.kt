package com.example.expensetracker

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Telephony
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.expensetracker.Adapters.AllExpensesAdapter
import com.example.expensetracker.Models.Expense
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap



class AllExpensesActivity : AppCompatActivity() {

    lateinit var recyclerView: RecyclerView
    lateinit var arrayList: ArrayList<Expense>
    lateinit var materialToolbar: MaterialToolbar

//    var url = "https://expense-tracker-test-production.up.railway.app//predict"       // old

    var url = "https://expense-tracker-production-56cc.up.railway.app//predict"

    private data class SMSMessage(val date: Long, val body: String)

    private var list = ArrayList<SMSMessage>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_expenses)



        //  to read sms from device
        val job = CoroutineScope(Dispatchers.Default).async {
            list =  readAllSMS(applicationContext)
            try {
                getBankSMS(list)
            } catch (e: Exception) {
            }
        }

        runBlocking {
            job.await()
        }


        materialToolbar = findViewById(R.id.profileToolbarAllExp)

        materialToolbar.setNavigationOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.recyclerViewAllExp)
        arrayList = ArrayList()
        val adapter : AllExpensesAdapter = AllExpensesAdapter(this,arrayList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // to read expenses from firebase
        FirebaseDatabase.getInstance().reference.child("users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child("Expenses")
            .orderByChild("Expenses")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        arrayList.clear()
                        for(item in snapshot.children){
                            var model = item.getValue(Expense::class.java)!!
                            arrayList.add(model)
                        }
                        arrayList.reverse()
                        adapter.notifyDataSetChanged()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }


    // function to extract the amount from the message
    fun extractDebitedAmount(message: String): String? {

        var amount = ""

        val pattern = Regex("(INR|Rs)([0-9,]+\\.[0-9]{2})")
        val matchResult = pattern.find(message)
        if (matchResult != null) {
            amount = matchResult.groupValues[2].replace(",", "")

        }
        return amount
    }

    // function to clean message
    fun removeDotAfterRs(message: String): String {
        val regex = Regex("(?<=Rs)\\.|\\s+")
        val outputMessage = message.replace(regex, "")
        return outputMessage
    }

    // function to format the date
    fun formatDate(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return dateFormat.format(calendar.time)
    }



    // function to read message from the device and store it in array list
    private fun readAllSMS(context: Context): ArrayList<AllExpensesActivity.SMSMessage> {
        val smsList = ArrayList<AllExpensesActivity.SMSMessage>()
        val uri: Uri = Uri.parse("content://sms/inbox")
        val resolver: ContentResolver = context.contentResolver

        val job = CoroutineScope(Dispatchers.IO).async {
            val cursor: Cursor? = resolver.query(uri, null, null, null, Telephony.Sms.DATE + " DESC")

            if (cursor != null && cursor.moveToFirst()) {
                val dateIndex: Int = cursor.getColumnIndex(Telephony.Sms.DATE)
                val bodyIndex: Int = cursor.getColumnIndex(Telephony.Sms.BODY)

                do {
                    val date: Long = cursor.getLong(dateIndex)
                    val smsBody: String = cursor.getString(bodyIndex)
                    smsList.add(AllExpensesActivity.SMSMessage(date, smsBody))
                } while (cursor.moveToNext())
            }

            cursor?.close()

            smsList
        }

        runBlocking {
            smsList.addAll(job.await())
        }

        return smsList
        Log.i("yyy",smsList.toString())
    }


    // function to make api call and get bank sms from ml model
    private fun getBankSMS(list: ArrayList<AllExpensesActivity.SMSMessage>) {

        try {
            var list2 = ArrayList<SMSMessage>()

            for (i in 0..list.size/25)
            {
                list2.add(list[i])
            }


            CoroutineScope(Dispatchers.Default).launch {

                for (item in list2) {
                    withContext(Dispatchers.IO) {
                        val stringRequest: StringRequest = object : StringRequest(
                            Method.POST, url,
                            Response.Listener { response ->
                                try {
                                    val jsonObject = JSONObject(response)
                                    val data = jsonObject.getString("target")
                                    if (data == "1") {
                                        val sms = removeDotAfterRs(item.body)
                                        val amount = extractDebitedAmount(sms).toString()
                                        val date = formatDate(item.date)
                                        val model = Expense(amount,date,"General","Online Expense",item.date.toString())
                                        if(!amount.isNullOrEmpty() && !sms.contains("credited")){
                                            FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                                                .child("onlineExpenses").child(item.date.toString()).setValue(model)
                                            FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                                                .child("Expenses").child(item.date.toString()).setValue(model)
                                        }
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                }
                            },
                            Response.ErrorListener { error ->
//                                Toast.makeText(
//                                    applicationContext,
//                                    error.message,
//                                    Toast.LENGTH_SHORT
//                                ).show()
                            }) {
                            override fun getParams(): Map<String, String>? {
                                val params: MutableMap<String, String> = HashMap()
                                params["text"] = item.body!!
                                return params
                            }
                        }

                        val queue = Volley.newRequestQueue(applicationContext)
                        queue.add(stringRequest)
                    }
                }
            }
        } catch (e: Exception) {
        }
    }



}