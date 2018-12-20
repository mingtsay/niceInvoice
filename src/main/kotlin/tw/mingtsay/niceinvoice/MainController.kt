package tw.mingtsay.niceinvoice

import com.beust.klaxon.Klaxon
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.stage.Modality
import javafx.stage.Stage
import okhttp3.OkHttpClient
import okhttp3.Request
import tw.mingtsay.niceinvoice.model.Invoice
import java.time.format.DateTimeFormatter
import java.util.*

class MainController {
    private fun fetchInvoice() =
        OkHttpClient().newCall(
            Request.Builder()
                .url("http://api.mingtsay.tw/invoice/")
                .build()
        ).execute().body()?.string()?.let { json -> Klaxon().parse<Invoice>(json) }

    private var invoice: Invoice? = null

    @FXML
    lateinit var menuBar: MenuBar

    @FXML
    lateinit var listInvoice: ComboBox<String>

    @FXML
    lateinit var labelSuper: Label

    @FXML
    lateinit var labelSpecial: Label

    @FXML
    lateinit var labelFirst: Label

    @FXML
    lateinit var labelAdditional: Label

    @FXML
    lateinit var labelDate: Label

    @FXML
    lateinit var txtInput: TextField

    @FXML
    fun initialize() {
        menuBar.isUseSystemMenuBar = true
        invoice = fetchInvoice()
        initializeInvoice()
    }

    companion object {
        val stage: Stage
            get() = FXMLLoader.load<Stage>(this::class.java.getResource("/layouts/main.fxml"))
                .apply { initModality(Modality.APPLICATION_MODAL) }
    }

    private fun initializeInvoice() {
        val invoice = this.invoice
        if (invoice != null) {
            listInvoice.apply {
                items.add(invoice.numbers[0].title)
                items.add(invoice.numbers[1].title)
                selectionModel.select(0)
            }
            applyInvoiceNumber(0)
        } else {
            Alert(Alert.AlertType.INFORMATION).apply {
                headerText = "發票開獎資訊下載失敗"
                dialogPane.content = Label("無法取得發票開獎資訊，請檢查您的網際網路連線。")
                showAndWait()
            }
            Platform.exit()
        }
    }

    private fun applyInvoiceNumber(index: Int) {
        invoice?.apply {
            with(numbers[index]) {
                val formatter = DateTimeFormatter.ofPattern("Gyyy/M/d", Locale.TAIWAN)

                labelSuper.text = superNumber
                labelSpecial.text = specialNumber
                labelFirst.text = firstNumbers.joinToString("、")
                labelAdditional.text = additionalNumbers.joinToString("、")
                labelDate.text =
                    String.format("自%s至%s止", date.dateFrom.format(formatter), date.dateTo.format(formatter))
            }
        }
    }

    @FXML
    fun onMenuItemActionQuit() {
        Platform.exit()
    }

    @FXML
    fun onMenuItemActionLicense() {
        LicenseController.stage.showAndWait()
    }

    @FXML
    fun onInvoice() {
        applyInvoiceNumber(listInvoice.selectionModel.selectedIndex)
    }

    @FXML
    fun onInput() {
        Alert(Alert.AlertType.INFORMATION).apply {
            headerText = "Hello JavaFX"
            dialogPane.content = ScrollPane(TextField(System.getProperty("os.name")).apply { isEditable = false })
            showAndWait()
        }
    }
}
