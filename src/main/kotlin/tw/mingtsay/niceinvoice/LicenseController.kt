package tw.mingtsay.niceinvoice

import com.beust.klaxon.Klaxon
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.ListView
import javafx.scene.control.TextArea
import javafx.stage.Modality
import javafx.stage.Stage
import org.apache.commons.io.IOUtils
import tw.mingtsay.niceinvoice.model.Licenses
import java.util.logging.Level

class LicenseController {
    private fun getLicensesFromResource(): Licenses? {
        Main.logger.log(Level.INFO, "Loading resource: licenses.json")
        return javaClass.getResourceAsStream("/licenses/licenses.json")
            .let { inputStream -> IOUtils.toString(inputStream, "UTF-8") }
            .let { json -> Klaxon().parse<Licenses>(json) }
            ?.apply { Main.logger.log(Level.INFO, "Resource loaded: licenses.json") }
            .apply { this ?: Main.logger.log(Level.WARNING, "Cannot load resource: licenses.json") }
    }

    val licenses = getLicensesFromResource()
    private val licenseContents = HashMap<String, String>()
    private val licenseStrings = ArrayList<String>()

    @FXML
    lateinit var listLicense: ListView<String>

    @FXML
    lateinit var textLicense: TextArea

    @FXML
    fun initialize() {
        Main.logger.log(Level.INFO, "Initializing…")

        licenses?.also { licenses ->
            licenses.licenses.forEach { license ->
                Main.logger.log(Level.INFO, "Prepare license for ${license.category}: \"${license.name}\"")
                listLicense.items.add(license.name)
                licenseStrings.add(license.licenses.joinToString("\n") { filename ->
                    licenseContents[filename]
                        ?.apply { Main.logger.log(Level.INFO, "License file is already loaded: \"$filename\"") }
                        ?: javaClass.getResource(filename)
                            .apply { Main.logger.log(Level.INFO, "Loading license file: \"$filename\"") }
                            .let { url -> IOUtils.toString(url, "UTF-8") }
                            .also { licenseContent -> licenseContents[filename] = licenseContent }
                })
            }
        }

        listLicense.selectionModel.selectedIndexProperty()
            .addListener { _, _, index ->
                textLicense.text = licenseStrings[index as Int]
            }

        Main.logger.log(Level.INFO, "Initialized.")
    }

    companion object {
        val stage: Stage
            get() = FXMLLoader(this::class.java.getResource("/layouts/license.fxml"))
                .apply { Main.logger.log(Level.INFO, "Loading layout…") }
                .let { loader ->
                    loader.load<Stage>()
                        .apply { initModality(Modality.APPLICATION_MODAL) }
                        .apply { Main.logger.log(Level.INFO, "Layout loaded.") }
                }
    }
}
