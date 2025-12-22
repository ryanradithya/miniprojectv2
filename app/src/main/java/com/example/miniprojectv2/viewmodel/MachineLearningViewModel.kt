package com.example.miniprojectv2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.miniprojectv2.Repository.MLRepository
import com.example.miniprojectv2.model.LoanRequest
import kotlinx.coroutines.launch


class MachineLearningViewModel : ViewModel() {

    private val repository = MLRepository()

    private val _state = MutableLiveData<PredictionState>(PredictionState.Idle)
    val state: LiveData<PredictionState> = _state

    //fun untuk melakukan prediksi ke repo
    fun predict(request: LoanRequest) {
        _state.value = PredictionState.Loading

        viewModelScope.launch {
            val result = repository.predictLoan(request)

            result.onSuccess {
                _state.value = PredictionState.Success(
                    prediction = it.prediction,
                    description = getDescription(it.prediction)
                )
            }.onFailure {
                _state.value = PredictionState.Error("Failed to connect")
            }
        }
    }

    private var DEFAULT_DESCRIPTION =
        "Prediksi ini merepresentasikan kategori suku bunga yang diperkirakan berdasarkan informasi pinjaman dan peminjam yang diberikan." +
                "Hasil ini dihasilkan menggunakan model machine learning yang dilatih dengan data pinjaman historis dan hanya digunakan sebagai referensi, bukan sebagai jaminan atau keputusan final."

    //desc
    private fun getDescription(prediction: String): String {
        return when (prediction) {
            "Low" ->
                "Pinjaman ini diperkirakan memiliki suku bunga yang rendah, yang menunjukkan tingkat risiko keseluruhan yang lebih kecil berdasarkan profil keuangan yang diberikan."

            "Mid" ->
                "Pinjaman ini berada pada kategori suku bunga menengah, yang mencerminkan faktor risiko yang seimbang dari data yang dimasukkan."

            "High" ->
                "Pinjaman ini diperkirakan memiliki suku bunga yang tinggi karena adanya indikator risiko yang lebih besar pada peminjam atau karakteristik pinjaman."

            "Very High" ->
                "Pinjaman ini memiliki perkiraan suku bunga yang sangat tinggi, yang menunjukkan tingkat risiko yang signifikan berdasarkan pola pinjaman historis."

            else -> DEFAULT_DESCRIPTION
        }
    }
}
