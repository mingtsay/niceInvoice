package tw.mingtsay.niceinvoice.api

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.chrono.MinguoDate


data class InvoiceDate(
    val text: String,
    val from: Long,
    val to: Long
) {
    val dateFrom get() = MinguoDate.from(LocalDateTime.ofInstant(Instant.ofEpochSecond(from), ZoneId.systemDefault()))
    val dateTo get() = MinguoDate.from(LocalDateTime.ofInstant(Instant.ofEpochSecond(to), ZoneId.systemDefault()))
}
