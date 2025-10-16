import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Transaction(
    val itemName: String,
    val qty: Int,
    val totalPrice: Int,
    val buyer: String,
    val expedition: String,            // new field for delivery service
    var status: String = "Pesanan Masuk", // default pending
    var trackingNumber: String? = null,
    val date: String
)

object TransactionManager {
    val transactions = mutableListOf<Transaction>()

    // updated addTransaction to include expedition
    fun addTransaction(itemName: String, qty: Int, price: Int, buyer: String, expedition: String) {
        val total = qty * price
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val dateNow = dateFormat.format(Date())

        transactions.add(Transaction(itemName, qty, total, buyer, expedition, date = dateNow))
    }

    fun updateStatus(transaction: Transaction, newStatus: String, trackingNumber: String? = null) {
        transaction.status = newStatus
        if (trackingNumber != null) transaction.trackingNumber = trackingNumber
    }

    fun getTransactionsForBuyer(buyer: String): List<Transaction> {
        return transactions.filter { it.buyer == buyer }
    }

    fun getTransactionsForSeller(): List<Transaction> {
        return transactions
    }
}
