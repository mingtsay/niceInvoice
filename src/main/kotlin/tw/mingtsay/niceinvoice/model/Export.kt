package tw.mingtsay.niceinvoice.model

import com.beust.klaxon.Klaxon
import org.apache.commons.io.FileUtils
import tw.mingtsay.niceinvoice.Main
import java.io.File
import java.util.logging.Level

data class Export(
    val invoice: List<InvoiceNumber>? = null,
    val results: List<InvoiceInputResult>? = null
) {
    companion object {
        fun read(file: File): Export =
            try {
                Main.logger.log(Level.INFO, "Importing from: \"${file.name}\"")
                Klaxon().parse<Export>(file)
                    ?.apply { Main.logger.log(Level.INFO, "Import file loaded: \"${file.name}\"") }
            } catch (e: Exception) {
                Main.logger.log(Level.WARNING, "Failed to load import file: \"${file.name}\"", e)
                null
            } ?: Export()

        private fun writeAll(export: Export, file: File) {
            try {
                Main.logger.log(Level.INFO, "Exporting to: \"${file.name}\"")
                FileUtils.write(file, Klaxon().toJsonString(export), "UTF-8")
                Main.logger.log(Level.INFO, "Export file saved: \"${file.name}\"")
            } catch (e: Exception) {
                Main.logger.log(Level.WARNING, "Failed to save export file: \"${file.name}\"", e)
            }
        }

        fun writeAll(invoice: List<InvoiceNumber>, results: List<InvoiceInputResult>, file: File) =
            writeAll(Export(invoice = invoice, results = results), file)

        fun writeInvoice(invoice: List<InvoiceNumber>, file: File) =
            writeAll(Export(invoice = invoice), file)

        fun writeResults(results: List<InvoiceInputResult>, file: File) =
            writeAll(Export(results = results), file)
    }
}
