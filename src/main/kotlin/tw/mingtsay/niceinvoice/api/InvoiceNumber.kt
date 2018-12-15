package tw.mingtsay.niceinvoice.api

import com.beust.klaxon.Json

data class InvoiceNumber(
    val id: String,
    val title: String,
    @Json(name = "super")
    val superNumber: String,
    @Json(name = "special")
    val specialNumber: String,
    @Json(name = "first")
    val firstNumbers: Array<String>,
    @Json(name = "additional")
    val additionalNumbers: Array<String>,
    val date: InvoiceDate
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as InvoiceNumber
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
