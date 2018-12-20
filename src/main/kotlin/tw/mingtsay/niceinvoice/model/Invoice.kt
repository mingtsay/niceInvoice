package tw.mingtsay.niceinvoice.model

import com.beust.klaxon.Json
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.chrono.MinguoDate

data class Invoice(val numbers: Array<InvoiceNumber>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Invoice
        return numbers.contentEquals(other.numbers)
    }

    override fun hashCode(): Int {
        return numbers.contentHashCode()
    }
}

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

data class InvoiceDate(
    val text: String,
    val from: Long,
    val to: Long
) {
    val dateFrom: MinguoDate
        get() = MinguoDate.from(
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(from),
                ZoneId.systemDefault()
            )
        )
    val dateTo: MinguoDate
        get() = MinguoDate.from(
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(to),
                ZoneId.systemDefault()
            )
        )
}
