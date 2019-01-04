package tw.mingtsay.niceinvoice.model

import com.beust.klaxon.Klaxon
import org.apache.commons.io.FileUtils
import tw.mingtsay.niceinvoice.Main
import java.io.File
import java.util.logging.Level

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
                Main.logger.log(Level.INFO, "Loading state…")
                Klaxon().parse<State>(File(STATE_FILE_FULL))
                    ?.apply { Main.logger.log(Level.INFO, "State loaded.") }
            } catch (e: Exception) {
                Main.logger.log(Level.WARNING, "Failed to load state.", e)
                null
            } ?: State()

        fun save(state: State) {
            try {
                Main.logger.log(Level.INFO, "Saving state…")
                File(STATE_FILE_PATH).apply {
                    if (!exists() && !mkdirs())
                        throw FileSystemException(reason = "Cannot create directories.", file = this)
                    if (!isDirectory) throw FileAlreadyExistsException(reason = "Not a directory.", file = this)
                    FileUtils.writeStringToFile(File(STATE_FILE_FULL), Klaxon().toJsonString(state), "UTF-8")
                }
                Main.logger.log(Level.INFO, "State saved.")
            } catch (e: Exception) {
                Main.logger.log(Level.WARNING, "Failed to save state.", e)
            }
        }
    }
}
