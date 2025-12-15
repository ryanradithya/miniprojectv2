package com.example.miniprojectv2.model

data class LoanRequest(
    val loan_amnt: Int,
    val term: String,
    val installment: Double,
    val grade: String,
    val home_ownership: String,
    val annual_inc: Double,
    val verification_status: String,
    val purpose: String,
    val delinq_2yrs: Int,
    val inq_last_6mths: Int,
    val open_acc: Int,
    val pub_rec: Int,
    val total_acc: Int
)
