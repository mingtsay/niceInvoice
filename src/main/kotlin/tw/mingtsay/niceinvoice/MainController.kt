package tw.mingtsay.niceinvoice

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Modality
import javafx.stage.Stage
import tw.mingtsay.niceinvoice.model.InvoiceInputResult
import tw.mingtsay.niceinvoice.model.InvoiceNumber
import tw.mingtsay.niceinvoice.model.InvoiceResult
import java.time.format.DateTimeFormatter
import java.util.*

class MainController {
    private var invoice = mutableListOf<InvoiceNumber>()

    private var results = FXCollections.observableArrayList<InvoiceInputResult>()

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
    lateinit var tableResults: TableView<InvoiceInputResult>

    @FXML
    lateinit var tableColumnNumber: TableColumn<InvoiceInputResult, String>

    @FXML
    lateinit var tableColumnDate: TableColumn<InvoiceInputResult, String>

    @FXML
    lateinit var tableColumnResult: TableColumn<InvoiceInputResult, String>

    @FXML
    lateinit var tableColumnPrice: TableColumn<InvoiceInputResult, String>

    @FXML
    fun initialize() {
        menuBar.isUseSystemMenuBar = true

        tableColumnNumber.setCellValueFactory { it.value.numberProperty }
        tableColumnDate.setCellValueFactory { it.value.invoiceTitleProperty }
        tableColumnResult.setCellValueFactory { it.value.invoiceResultProperty }
        tableColumnPrice.setCellValueFactory { it.value.invoicePriceProperty }
        tableResults.items = results

        initializeInvoice()
    }

    companion object {
        val stage: Stage
            get() = FXMLLoader.load<Stage>(this::class.java.getResource("/layouts/main.fxml"))
                .apply { initModality(Modality.APPLICATION_MODAL) }
    }

    private fun initializeInvoice() {
        if (invoice.isEmpty()) {
            InvoiceNumber.fetch()
                ?.apply { invoice = toMutableList() }
                ?: Alert(Alert.AlertType.INFORMATION).apply {
                    headerText = "發票開獎資訊下載失敗"
                    dialogPane.content = Label("無法取得發票開獎資訊，請檢查您的網際網路連線。")
                    showAndWait()
                    Platform.exit()
                }
        }

        listInvoice.apply {
            invoice.forEach { items.add(it.title) }
            selectionModel.select(0)
        }
        applyInvoiceNumber(0)
    }

    private fun applyInvoiceNumber(index: Int) {
        invoice[index].apply {
            val formatter = DateTimeFormatter.ofPattern("Gyyy/M/d", Locale.TAIWAN)

            labelSuper.text = superNumber
            labelSpecial.text = specialNumber
            labelFirst.text = firstNumbers.joinToString("、")
            labelAdditional.text = additionalNumbers.joinToString("、")
            labelDate.text =
                String.format("自%s至%s止", date.dateFrom.format(formatter), date.dateTo.format(formatter))
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
        val number = txtInput.text

        val errorMessage = when {
            number.any { it !in "0123456789" } -> InputErrorMessage(title = "輸入格式錯誤", description = "您輸入的格式不正確！")
            number.length !in arrayOf(3, 8) -> InputErrorMessage(title = "輸入長度錯誤", description = "您輸入的長度不正確！")
            else -> null
        }

        if (errorMessage != null) {
            Alert(Alert.AlertType.ERROR).apply {
                headerText = errorMessage.title
                dialogPane.content = Label(
                    "${errorMessage.description}\n" +
                        "請輸入發票末三碼或完整號碼。"
                )
                showAndWait()
            }
            txtInput.selectAll()
            return
        } else {
            val invoice = invoice[listInvoice.selectionModel.selectedIndex]
            val result = invoice.compare(number)


            when (result) {
                InvoiceResult.INVOICE_RESULT_PERHAPS -> {
                    Alert(Alert.AlertType.INFORMATION).apply {
                        headerText = "可能中獎"
                        dialogPane.content = Label(
                            "您可能中獎了！\n" +
                                "請輸入完整發票號碼來確認是否中獎。"
                        )
                        showAndWait()
                    }
                    txtInput.selectAll()
                    return
                }
                InvoiceResult.INVOICE_RESULT_NONE -> {
                }
                else -> {
                    Alert(Alert.AlertType.INFORMATION).apply {
                        val margin = Insets(8.0)


                        headerText = result.text
                        dialogPane.content = VBox(
                            Label("恭喜您中了${result.text}！").also { VBox.setMargin(it, margin) },
                            Label(
                                "您輸入的發票號碼 $number 對中了${result.text}，\n" +
                                    "獎金為${result.price.text}。"
                            ).also { VBox.setMargin(it, margin) },
                            Label("請您妥善保存紙本發票以利領獎。").also { VBox.setMargin(it, margin) },
                            Label(
                                "歡迎使用發票載具保存電子發票，除了不用擔心發票不見以及自動化\n" +
                                    "對獎等好處外，無實體電子發票還有專屬獎項，綁定銀行帳戶後還可將\n" +
                                    "獎金直接匯入戶頭哦！"
                            ).apply { textFill = Color.GRAY }.also { VBox.setMargin(it, margin) }
                        ).apply { styleClass.add("font-sans-serif") }
                        showAndWait()
                    }
                }
            }

            results.add(InvoiceInputResult(number = number, invoiceTitle = invoice.title, invoiceResult = result))
        }

        txtInput.text = ""
    }
}

//System.getProperty("os.name")
private data class InputErrorMessage(val title: String, val description: String)
