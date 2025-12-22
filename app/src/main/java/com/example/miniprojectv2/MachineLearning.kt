package com.example.miniprojectv2

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.miniprojectv2.model.LoanRequest
import com.example.miniprojectv2.viewmodel.MachineLearningViewModel
import com.example.miniprojectv2.viewmodel.PredictionState
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class MachineLearningFragment : Fragment(R.layout.fragment_machine_learning) {

    private val viewModel: MachineLearningViewModel by viewModels()

//    spinner attributes
    private val termOptions = listOf("36 months", "60 months")
    private val gradeOptions = listOf("A", "B", "C", "D", "E", "F", "G")
    private val homeOwnershipOptions = listOf("MORTGAGE", "NONE", "OTHER", "OWN", "RENT")
    private val verificationOptions = listOf("Not Verified", "Source Verified", "Verified")
    private val purposeOptions = listOf(
        "car", "credit_card", "debt_consolidation", "educational",
        "home_improvement", "house", "major_purchase", "medical",
        "moving", "other", "renewable_energy", "small_business",
        "vacation", "wedding"
    )

    private var DEFAULT_DESCRIPTION =
        "Prediksi ini merepresentasikan kategori suku bunga yang diperkirakan berdasarkan informasi pinjaman dan peminjam yang diberikan." +
        "Hasil ini dihasilkan menggunakan model machine learning yang dilatih dengan data pinjaman historis dan hanya digunakan sebagai referensi, bukan sebagai jaminan atau keputusan final."


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val spinnerTerm =
            view.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerTerm)
        val spinnerGrade =
            view.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerGrade)
        val spinnerHome =
            view.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerHomeOwnership)
        val spinnerVerification =
            view.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerVerification)
        val spinnerPurpose =
            view.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerPurpose)

        setupDropdown(spinnerTerm, termOptions)
        setupDropdown(spinnerGrade, gradeOptions)
        setupDropdown(spinnerHome, homeOwnershipOptions)
        setupDropdown(spinnerVerification, verificationOptions)
        setupDropdown(spinnerPurpose, purposeOptions)

        val loanAmountEt = view.findViewById<EditText>(R.id.etLoanAmount)
        val installmentEt = view.findViewById<EditText>(R.id.etInstallment)
        val annualIncomeEt = view.findViewById<EditText>(R.id.etAnnualIncome)

        val predictBtn = view.findViewById<Button>(R.id.btnPredict)
        val statusTv = view.findViewById<TextView>(R.id.tvStatus)
        val predictionTv = view.findViewById<TextView>(R.id.tvPrediction)
        val explanationTv = view.findViewById<TextView>(R.id.tvExplanation)

        predictBtn.setOnClickListener {
            val loanAmount = loanAmountEt.text.toString().toIntOrNull()
            val installment = installmentEt.text.toString().toIntOrNull()
            val annualIncome = annualIncomeEt.text.toString().toIntOrNull()

            if (loanAmount == null || loanAmount !in 5000..10000) {
                loanAmountEt.error = "Harus diantara 5000 dan 10000"
                return@setOnClickListener
            }

            if (installment == null || installment !in 100..1000) {
                installmentEt.error = "Harus diantara 100 dan 1000"
                return@setOnClickListener
            }

            if (annualIncome == null || annualIncome < 0) {
                annualIncomeEt.error = "Income tidak boleh minus"
                return@setOnClickListener
            }

            //field untuk dikirim
            val request = LoanRequest(
                loan_amnt = loanAmount,
                term = spinnerTerm.text.toString(),
                installment = installment.toDouble(),
                grade = spinnerGrade.text.toString(),
                home_ownership = spinnerHome.text.toString(),
                annual_inc = annualIncome.toDouble(),
                verification_status = spinnerVerification.text.toString(),
                purpose = spinnerPurpose.text.toString(),
                delinq_2yrs = 0, //default parameter model
                inq_last_6mths = 0,
                open_acc = 0,
                pub_rec = 0,
                total_acc = 0
            )

            explanationTv.text = DEFAULT_DESCRIPTION
            viewModel.predict(request)
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PredictionState.Loading -> {
                    statusTv.text = "Connecting to API..."
                }
                is PredictionState.Success -> {
                    statusTv.text = "Success"
                    predictionTv.visibility = View.VISIBLE
                    explanationTv.visibility = View.VISIBLE

                    predictionTv.text = state.prediction
                    explanationTv.text = state.description

                    predictionTv.setTextColor(
                        when (state.prediction) {
                            "Low" -> Color.GREEN
                            "Mid" -> Color.BLUE
                            "High" -> Color.parseColor("#FFA500")
                            "Very High" -> Color.RED
                            else -> Color.GRAY
                        }
                    )
                }
                is PredictionState.Error -> {
                    statusTv.text = state.message
                }
                else -> Unit
            }
        }
    }
    private fun setupDropdown(
        dropdown: MaterialAutoCompleteTextView,
        items: List<String>
    ) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            items
        )
        dropdown.setAdapter(adapter)
    }

}
