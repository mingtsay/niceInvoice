package tw.mingtsay.niceinvoice.model

import com.beust.klaxon.Json
import com.beust.klaxon.Klaxon
import javafx.beans.property.SimpleStringProperty
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.chrono.MinguoDate

private data class Invoice(val numbers: List<InvoiceNumber>)

data class InvoiceNumber(
    val id: String,
    val title: String,
    @Json(name = "super")
    val superNumber: String,
    @Json(name = "special")
    val specialNumber: String,
    @Json(name = "first")
    val firstNumbers: List<String>,
    @Json(name = "additional")
    val additionalNumbers: List<String>,
    val date: InvoiceDate
) {
    fun compare(number: String): InvoiceResult {
        if (number.length == 8) {
            if (superNumber == number) return InvoiceResult.INVOICE_RESULT_SUPER
            if (specialNumber == number) return InvoiceResult.INVOICE_RESULT_SPECIAL
            if (firstNumbers.contains(number)) return InvoiceResult.INVOICE_RESULT_FIRST
            if (firstNumbers.any { it.substring(1..7) == number.substring(1..7) }) return InvoiceResult.INVOICE_RESULT_SECOND
            if (firstNumbers.any { it.substring(2..7) == number.substring(2..7) }) return InvoiceResult.INVOICE_RESULT_THIRD
            if (firstNumbers.any { it.substring(3..7) == number.substring(3..7) }) return InvoiceResult.INVOICE_RESULT_FOURTH
            if (firstNumbers.any { it.substring(4..7) == number.substring(4..7) }) return InvoiceResult.INVOICE_RESULT_FIFTH
            if (firstNumbers.any { it.substring(5..7) == number.substring(5..7) }) return InvoiceResult.INVOICE_RESULT_SIXTH
            if (additionalNumbers.contains(number.substring(5..7))) return InvoiceResult.INVOICE_RESULT_ADDITIONAL
        }
        if (number.length == 3) {
            if (superNumber.endsWith(number)) return InvoiceResult.INVOICE_RESULT_PERHAPS
            if (specialNumber.endsWith(number)) return InvoiceResult.INVOICE_RESULT_PERHAPS
            if (firstNumbers.any { it.endsWith(number) }) return InvoiceResult.INVOICE_RESULT_PERHAPS
            if (additionalNumbers.contains(number)) return InvoiceResult.INVOICE_RESULT_ADDITIONAL
        }
        return InvoiceResult.INVOICE_RESULT_NONE
    }

    companion object {
        fun fetch() =
            OkHttpClient().newCall(
                Request.Builder()
                    .url("http://api.mingtsay.tw/invoice/")
                    .build()
            ).execute().body()?.string()?.let { json -> Klaxon().parse<Invoice>(json)?.numbers }
    }
}

data class InvoiceDate(
    val text: String,
    val from: Long,
    val to: Long
) {
    @Json(ignored = true)
    val dateFrom: MinguoDate
        get() = MinguoDate.from(
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(from),
                ZoneId.systemDefault()
            )
        )
    @Json(ignored = true)
    val dateTo: MinguoDate
        get() = MinguoDate.from(
            LocalDateTime.ofInstant(
                Instant.ofEpochSecond(to),
                ZoneId.systemDefault()
            )
        )
}

enum class InvoiceResult(val text: String, val price: InvoiceResultPrice) {
    INVOICE_RESULT_NONE(text = "未中獎", price = InvoiceResultPrice(text = "無", price = 0)),
    INVOICE_RESULT_PERHAPS(text = "可能中獎", price = InvoiceResultPrice(text = "未知", price = 0)),
    INVOICE_RESULT_SUPER(text = "特別獎", price = InvoiceResultPrice(text = "一千萬元", price = 10000000)),
    INVOICE_RESULT_SPECIAL(text = "特獎", price = InvoiceResultPrice(text = "二百萬元", price = 2000000)),
    INVOICE_RESULT_FIRST(text = "頭獎", price = InvoiceResultPrice(text = "二十萬元", price = 200000)),
    INVOICE_RESULT_SECOND(text = "二獎", price = InvoiceResultPrice(text = "四萬元", price = 40000)),
    INVOICE_RESULT_THIRD(text = "三獎", price = InvoiceResultPrice(text = "一萬元", price = 10000)),
    INVOICE_RESULT_FOURTH(text = "四獎", price = InvoiceResultPrice(text = "四千元", price = 4000)),
    INVOICE_RESULT_FIFTH(text = "五獎", price = InvoiceResultPrice(text = "一千元", price = 1000)),
    INVOICE_RESULT_SIXTH(text = "六獎", price = InvoiceResultPrice(text = "二百元", price = 200)),
    INVOICE_RESULT_ADDITIONAL(text = "增開六獎", price = InvoiceResultPrice(text = "二百元", price = 200)),
}

data class InvoiceResultPrice(val text: String, val price: Int)

data class InvoiceInputResult(
    val number: String,
    val invoiceTitle: String,
    val invoiceResult: InvoiceResult
) {
    @Json(ignored = true)
    val numberProperty = SimpleStringProperty("X".repeat(8 - number.length) + number)
    @Json(ignored = true)
    val invoiceTitleProperty = SimpleStringProperty(invoiceTitle)
    @Json(ignored = true)
    val invoiceResultProperty = SimpleStringProperty(invoiceResult.text)
    @Json(ignored = true)
    val invoicePriceProperty = SimpleStringProperty(invoiceResult.price.text)
}
