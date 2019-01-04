package tw.mingtsay.niceinvoice.model

import com.beust.klaxon.Klaxon
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.FileNotFoundException

data class State(
    val selectedIndex: Int = 0,
    val invoice: List<InvoiceNumber> = arrayListOf(),
    val results: List<InvoiceInputResult> = arrayListOf()
) {
    fun save() {
        save(this)
    }

    companion object {
        private val STATE_FILE_PATH = "${System.getProperty("user.home")}${File.separator}.niceInvoice"
        private const val STATE_FILE_NAME = "saved_state.json"
        private val STATE_FILE_FULL = "$STATE_FILE_PATH${File.separator}$STATE_FILE_NAME"

        fun load(): State =
            try {
                Klaxon().parse<State>(File(STATE_FILE_FULL))
            } catch (_: FileNotFoundException) {
                null
            } catch (_: NoSuchFieldException) {
                null
            } ?: State()

        fun save(state: State) {
            File(STATE_FILE_PATH).apply {
                if (!exists()) mkdirs()
                if (isDirectory) FileUtils.writeStringToFile(
                    File(STATE_FILE_FULL),
                    Klaxon().toJsonString(state),
                    "UTF-8"
                )
            }
        }
    }
}
