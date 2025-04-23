package com.example.expensetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.provider.Telephony
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.expensetracker.Models.Expense
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ReadSMSActivity : AppCompatActivity() {

    var url = "https://expense-tracker-test-production.up.railway.app//predict"

    private data class SMSMessage(val date: Long, val body: String)

    private var list = ArrayList<SMSMessage>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read_smsactivity)


//        list =  readAllSMS(applicationContext)
//        getBankSMS(list)

    }

    fun extractDebitedAmount(message: String): String? {

        var amount = ""

        val pattern = Regex("(INR|Rs)([0-9,]+\\.[0-9]{2})")
        val matchResult = pattern.find(message)
        if (matchResult != null) {
            amount = matchResult.groupValues[2].replace(",", "")

        }
        return amount
    }

    fun removeDotAfterRs(message: String): String {
        val regex = Regex("(?<=Rs)\\.|\\s+")
        val outputMessage = message.replace(regex, "")
        return outputMessage
    }

    fun formatDate(timeInMillis: Long): String {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return dateFormat.format(calendar.time)
    }

    private fun readAllSMS(context: Context): ArrayList<ReadSMSActivity.SMSMessage> {
        val smsList = ArrayList<ReadSMSActivity.SMSMessage>()
        val uri: Uri = Uri.parse("content://sms/inbox")
        val resolver: ContentResolver = context.contentResolver

        val job = CoroutineScope(Dispatchers.IO).async {
            val cursor: Cursor? = resolver.query(uri, null, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val dateIndex: Int = cursor.getColumnIndex(Telephony.Sms.DATE)
                val bodyIndex: Int = cursor.getColumnIndex(Telephony.Sms.BODY)
                do {
                    val date: Long = cursor.getLong(dateIndex)
                    val smsBody: String = cursor.getString(bodyIndex)
                    smsList.add(ReadSMSActivity.SMSMessage(date, smsBody))
                } while (cursor.moveToNext())
            }

            cursor?.close()

            smsList
        }

        runBlocking {
            smsList.addAll(job.await())
        }

        return smsList
    }



//    private fun readAllSMS(context: Context): ArrayList<SMSMessage> {
//        val smsList = ArrayList<SMSMessage>()
//
//        val uri: Uri = Uri.parse("content://sms/inbox")
//        val resolver: ContentResolver = context.contentResolver
//        val cursor: Cursor? = resolver.query(uri, null, null, null, null)
//
//        if (cursor != null && cursor.moveToFirst()) {
//            val dateIndex: Int = cursor.getColumnIndex(Telephony.Sms.DATE)
//            val bodyIndex: Int = cursor.getColumnIndex(Telephony.Sms.BODY)
//            do {
//                val date: Long = cursor.getLong(dateIndex)
//                val smsBody: String = cursor.getString(bodyIndex)
//                smsList.add(SMSMessage(date, smsBody))
//            } while (cursor.moveToNext())
//        }
//
//        cursor?.close()
//
//        return smsList
//    }


    private fun getBankSMS(list: ArrayList<SMSMessage>) {

//        val list2 = ArrayList<SMSMessage>()
//
//        for (i in 0..list.size/2)
//        {
//            list2.add(list[i])
//        }

        CoroutineScope(Dispatchers.Default).launch {

            for (item in list) {
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
                                    val model = Expense(amount,date,"General",sms,item.date.toString())
                                    if(!amount.isNullOrEmpty() && !sms.contains("credited")){
                                        FirebaseDatabase.getInstance().reference.child("users").child(FirebaseAuth.getInstance().currentUser!!.uid)
                                            .child("onlineExpenses").child(item.date.toString()).setValue(model)
                                    }
                                }
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }
                        },
                        Response.ErrorListener { error ->
                            Toast.makeText(
                                applicationContext,
                                error.message,
                                Toast.LENGTH_SHORT
                            ).show()
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
    }



}