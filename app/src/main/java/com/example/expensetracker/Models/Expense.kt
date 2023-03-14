package com.example.expensetracker.Models

data class Expense(
    var amount : String = "",
    var date : String = "",
    var category: String = "",
    var description: String = "",
    var submitTime : String = ""
)
