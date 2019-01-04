package tw.mingtsay.niceinvoice

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import tw.mingtsay.niceinvoice.model.*
import java.io.File
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Level
import kotlin.math.exp
import kotlin.math.min

class MainController {
    private var selectedIndex = 0

    private var invoice = mutableListOf<InvoiceNumber>()

    private var results = FXCollections.observableArrayList<InvoiceInputResult>()

    @FXML
    lateinit var _stage: Stage

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
        Main.logger.log(Level.INFO, "Initializing…")

        menuBar.isUseSystemMenuBar = true

        selectedIndex = Main.state.selectedIndex
        invoice = Main.state.invoice.toMutableList()
        results = FXCollections.observableArrayList(Main.state.results)

        tableColumnNumber.setCellValueFactory { it.value.numberProperty }
        tableColumnDate.setCellValueFactory { it.value.invoiceTitleProperty }
        tableColumnResult.setCellValueFactory { it.value.invoiceResultProperty }
        tableColumnPrice.setCellValueFactory { it.value.invoicePriceProperty }
        tableResults.items = results

        initializeInvoice()

        Main.logger.log(Level.INFO, "Initialized.")
    }

    fun shutdown() {
        Main.logger.log(Level.INFO, "Shutting down…")

        Main.state = State(
            selectedIndex = listInvoice.selectionModel.selectedIndex,
            invoice = invoice,
            results = results
        )
    }

    companion object {
        val stage: Stage
            get() = FXMLLoader(this::class.java.getResource("/layouts/main.fxml"))
                .apply { Main.logger.log(Level.INFO, "Loading layout…") }
                .let { loader ->
                    loader.load<Stage>()
                        .apply { initModality(Modality.APPLICATION_MODAL) }
                        .apply { setOnHidden { loader.getController<MainController>().shutdown() } }
                        .apply { Main.logger.log(Level.INFO, "Layout loaded.") }
                }
    }

    private fun initializeInvoice() {
        if (invoice.isEmpty()) {
            Main.logger.log(Level.INFO, "No invoice information found. Download from server.")
            InvoiceNumber.fetch()
                ?.apply { invoice = toMutableList() }
                ?.apply { Main.logger.log(Level.INFO, "Invoice information has been downloaded successfully.") }
                ?: Alert(Alert.AlertType.INFORMATION).apply {
                    Main.logger.log(Level.WARNING, "Failed to download invoice information.")
                    headerText = "發票開獎資訊下載失敗"
                    dialogPane.content = Label("無法取得發票開獎資訊，請檢查您的網際網路連線。")
                    showAndWait()
                    Platform.exit()
                }
        }

        if (!invoice.isEmpty()) {
            listInvoice.apply {
                listInvoice.items.clear()
                invoice.forEach { items.add(it.title) }
                selectedIndex = min(selectedIndex, listInvoice.items.size - 1)
                selectionModel.select(selectedIndex)
            }
            applyInvoiceNumber(selectedIndex)
        }
    }

    private fun applyInvoiceNumber(index: Int) {
        try {
            invoice[index].apply {
                val formatter = DateTimeFormatter.ofPattern("Gyyy/M/d", Locale.TAIWAN)

                labelSuper.text = superNumber
                labelSpecial.text = specialNumber
                labelFirst.text = firstNumbers.joinToString("、")
                labelAdditional.text = additionalNumbers.joinToString("、")
                labelDate.text =
                    String.format("自%s至%s止", date.dateFrom.format(formatter), date.dateTo.format(formatter))
            }
        } catch (e: IndexOutOfBoundsException) {
            Main.logger.log(Level.WARNING, "Invoice information size = ${invoice.size}, selecting index = $index")
        }
    }

    private fun openFile(title: String) =
        FileChooser()
            .also { it.title = title }
            .apply { extensionFilters.add(FileChooser.ExtensionFilter("JSON 檔案 (*.json)", "*.json")) }
            .apply { extensionFilters.add(FileChooser.ExtensionFilter("所有檔案 (*.*)", "*.*")) }
            .run { showOpenDialog(_stage) }

    private fun saveFile(title: String) =
        FileChooser()
            .also { it.title = title }
            .apply { extensionFilters.add(FileChooser.ExtensionFilter("JSON 檔案 (*.json)", "*.json")) }
            .apply { initialFileName = "niceInvoice-export.json" }
            .run { showSaveDialog(_stage) }
            .let { if (!it.exists() || confirmOverwrite()) it else null }

    private fun confirmOverwrite() =
        Alert(Alert.AlertType.CONFIRMATION)
            .apply { headerText = "是否取代檔案？" }
            .apply { contentText = "檔案已存在，是否覆蓋？" }
            .run { showAndWait().get() == ButtonType.OK }

    private fun confirmReplacement(what: String) =
        Alert(Alert.AlertType.CONFIRMATION)
            .apply { headerText = "是否取代原有資料？" }
            .apply { contentText = "${what}已有資料，是否要取代原有資料？\n\n若您想要保留原有資料，並將匯入的${what}與該些資料合併，請點選否。" }
            .run { showAndWait().get() == ButtonType.OK }

    private fun importExportDone(isImport: Boolean, what: String) =
        Alert(Alert.AlertType.INFORMATION)
            .apply {
                val importExport = if (isImport) "匯入" else "匯出"
                headerText = "${importExport}成功"
                contentText = "${what}已成功${importExport}！"
            }
            .run { showAndWait() }

    @FXML
    fun onMenuItemActionImport() {
        val export = Export.read(openFile("匯入…"))
        export.invoice?.apply {
            val replace = invoice.isEmpty() || confirmReplacement("發票資訊")
            if (replace) invoice = toMutableList()
            else invoice.addAll(this)
            initializeInvoice()
        }

        export.results?.apply {
            val replace = results.isEmpty() || confirmReplacement("對獎結果")
            if (replace) results.clear()
            results.addAll(this)
        }

        importExportDone(what = "資料", isImport = true)
    }

    @FXML
    fun onMenuItemActionExportInvoice() {
        saveFile("匯出發票資訊…")?.also { file ->
            Export.writeInvoice(file = file, invoice = invoice)
            importExportDone(what = "發票資訊", isImport = false)
        }
    }

    @FXML
    fun onMenuItemActionExportResults() {
        saveFile("匯出對獎結果…")?.also { file ->
            Export.writeResults(file = file, results = results)
            importExportDone(what = "對獎結果", isImport = false)
        }
    }

    @FXML
    fun onMenuItemActionExportAll() {
        saveFile("匯出全部…")?.also { file ->
            Export.writeAll(file = file, invoice = invoice, results = results)
            importExportDone(what = "資料", isImport = false)
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
        Main.logger.log(Level.INFO, "Invoice number selected. Index = ${listInvoice.selectionModel.selectedIndex}")
        applyInvoiceNumber(listInvoice.selectionModel.selectedIndex)
    }

    @FXML
    fun onInput() {
        val number = txtInput.text

        Main.logger.log(Level.INFO, "Process input: \"$number\"")

        try {
            when {
                number.any { it !in "0123456789" } -> throw IllegalCharacterException(number)
                number.length !in arrayOf(3, 8) -> throw  InvalidLengthException(number.length)
            }

            val invoice = invoice[listInvoice.selectionModel.selectedIndex]
            val result = invoice.compare(number)

            Main.logger.log(Level.INFO, "Result: ${result.text}")

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

            txtInput.text = ""
        } catch (e: InvalidInputException) {
            Alert(Alert.AlertType.ERROR).apply {
                headerText = e.title
                dialogPane.content = Label(
                    "${e.description}\n" +
                        "請輸入發票末三碼或完整號碼。"
                )
                showAndWait()
            }
            txtInput.selectAll()
        }
    }
}

private abstract class InvalidInputException(msg: String) : IllegalArgumentException(msg) {
    abstract val title: String
    abstract val description: String
}

private class IllegalCharacterException(input: String) : InvalidInputException("For input string: \"$input\"") {
    override val title get() = "輸入格式錯誤"
    override val description get() = "您輸入的格式不正確！"

}

private class InvalidLengthException(length: Int) : InvalidInputException("Expecting 3 or 8, actual is $length") {
    override val title get() = "輸入長度錯誤"
    override val description get() = "您輸入的長度不正確！"
}
