package tw.mingtsay.niceinvoice.model

import com.beust.klaxon.Klaxon
import org.apache.commons.io.FileUtils
import java.io.File

data class Export(
    val invoice: List<InvoiceNumber>? = null,
    val results: List<InvoiceInputResult>? = null
) {
    companion object {
        fun read(file: File) =
            Klaxon().parse<Export>(file)

        fun writeAll(export: Export, file: File) =
            FileUtils.write(file, Klaxon().toJsonString(export), "UTF-8")

        fun writeAll(invoice: List<InvoiceNumber>, results: List<InvoiceInputResult>, file: File) =
            writeAll(Export(invoice = invoice, results = results), file)

        fun writeInvoice(invoice: List<InvoiceNumber>, file: File) =
            writeAll(Export(invoice = invoice), file)

        fun writeResults(results: List<InvoiceInputResult>, file: File) =
            writeAll(Export(results = results), file)
    }
}
