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

class LicenseController {
    private fun getLicensesFromResource() =
        javaClass.getResourceAsStream("/licenses/licenses.json")
            .let { inputStream -> IOUtils.toString(inputStream, "UTF-8") }
            .let { json -> Klaxon().parse<Licenses>(json) }

    val licenses = getLicensesFromResource()
    private val licenseContents = HashMap<String, String>()
    private val licenseStrings = ArrayList<String>()

    @FXML
    lateinit var listLicense: ListView<String>

    @FXML
    lateinit var textLicense: TextArea

    @FXML
    fun initialize() {
        licenses?.also { licenses ->
            licenses.licenses.forEach { license ->
                listLicense.items.add(license.name)
                licenseStrings.add(license.licenses.map { filename ->
                    licenseContents[filename] ?: javaClass.getResource(filename)
                        .let { url -> IOUtils.toString(url, "UTF-8") }
                        .also { licenseContent -> licenseContents[filename] = licenseContent }
                }.joinToString("\n"))
            }
        }

        listLicense.selectionModel.selectedIndexProperty()
            .addListener { _, _, index ->
                textLicense.text = licenseStrings[index as Int]
            }
    }

    companion object {
        val stage: Stage
            get() = FXMLLoader.load<Stage>(this::class.java.getResource("/layouts/license.fxml"))
                .apply { initModality(Modality.APPLICATION_MODAL) }
    }
}
