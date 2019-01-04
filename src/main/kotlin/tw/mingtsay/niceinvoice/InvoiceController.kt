package tw.mingtsay.niceinvoice

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.control.*
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.StringConverter
import tw.mingtsay.niceinvoice.model.InvoiceDate
import tw.mingtsay.niceinvoice.model.InvoiceNumber
import tw.mingtsay.niceinvoice.model.State
import java.time.LocalDate
import java.time.chrono.MinguoChronology
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Level


class InvoiceController {
    private lateinit var invoice: MutableList<InvoiceNumber>

    private var isApplyingInvoice = false

    @FXML
    lateinit var thisStage: Stage

    @FXML
    lateinit var listInvoice: ListView<String>

    @FXML
    lateinit var txtTitle: TextField

    @FXML
    lateinit var txtSuper: TextField

    @FXML
    lateinit var txtSpecial: TextField

    @FXML
    lateinit var txtFirst: TextField

    @FXML
    lateinit var txtAdditional: TextField

    @FXML
    lateinit var datePickerFrom: DatePicker

    @FXML
    lateinit var datePickerTo: DatePicker

    @FXML
    lateinit var btnFetch: Button

    @FXML
    lateinit var btnDelete: Button

    private fun loadInvoice() {
        Main.logger.log(Level.INFO, "Loading invoice…")

        invoice = Main.state.invoice.toMutableList()

        initializeInvoice()

        Main.logger.log(Level.INFO, "Invoice loaded.")
    }

    private fun saveInvoice() {
        Main.logger.log(Level.INFO, "Saving invoice…")

        Main.state = State(invoice = invoice, results = Main.state.results, selectedIndex = Main.state.selectedIndex)

        Main.logger.log(Level.INFO, "Invoice saved.")
    }

    private fun initializeInvoice() {
        listInvoice.items.clear()
        listInvoice.items.addAll(invoice.map { it.title })
        listInvoice.items.add("（新增）")
    }

    private fun applyInvoice(index: Int) {
        Main.logger.log(Level.INFO, "Applying invoice: index = $index")

        isApplyingInvoice = true

        if (index in 0 until invoice.size)
            invoice[index].apply {
                txtTitle.text = title
                txtSuper.text = superNumber
                txtSpecial.text = specialNumber
                txtFirst.text = firstNumbers.joinToString(", ")
                txtAdditional.text = additionalNumbers.joinToString(", ")
                datePickerFrom.value = LocalDate.ofEpochDay(date.from / 86400)
                datePickerTo.value = LocalDate.ofEpochDay(date.to / 86400)
            }
        else {
            txtTitle.text = ""
            txtSuper.text = ""
            txtSpecial.text = ""
            txtFirst.text = ""
            txtAdditional.text = ""
            datePickerFrom.value = null
            datePickerTo.value = null
        }

        isApplyingInvoice = false
    }

    private fun needUpdateInvoice(index: Int) =
        !isApplyingInvoice && (
            index in 0 until invoice.size &&
                invoice[index].run {
                    title != txtTitle.text ||
                        superNumber != txtSuper.text.filter { "0123456789".contains(it) } ||
                        specialNumber != txtSpecial.text.filter { "0123456789".contains(it) } ||
                        firstNumbers.joinToString(",") != txtFirst.text.filter { "0123456789,".contains(it) } ||
                        additionalNumbers.joinToString(",") != txtAdditional.text.filter { "0123456789,".contains(it) } ||
                        date.from != (datePickerFrom.value?.toEpochDay() ?: 0) * 86400 ||
                        date.to != (datePickerTo.value?.toEpochDay() ?: 0) * 86400
                } ||
                txtSuper.text.isNotEmpty() ||
                txtSpecial.text.isNotEmpty() ||
                txtFirst.text.isNotEmpty() ||
                txtAdditional.text.isNotEmpty()
            )

    private fun updateInvoice(index: Int) {
        Main.logger.log(Level.INFO, "Updating invoice: index = $index")

        InvoiceNumber(
            id = txtTitle.text.filter { "0123456789".contains(it) },
            title = txtTitle.text,
            superNumber = txtSuper.text.filter { "0123456789".contains(it) },
            specialNumber = txtSpecial.text.filter { "0123456789".contains(it) },
            firstNumbers = txtFirst.text.filter { "0123456789,".contains(it) }.split(','),
            additionalNumbers = txtAdditional.text.filter { "0123456789,".contains(it) }.split(','),
            date = InvoiceDate(
                from = (datePickerFrom.value?.toEpochDay() ?: 0) * 86400,
                to = (datePickerTo.value?.toEpochDay() ?: 0) * 86400,
                text = "領獎期間自%d年%d月%d日起至%d年%02d月%02d日止".format(
                    datePickerFrom.value?.year ?: 0,
                    datePickerFrom.value?.month?.value ?: 0,
                    datePickerFrom.value?.dayOfMonth ?: 0,
                    datePickerTo.value?.year ?: 0,
                    datePickerTo.value?.month?.value ?: 0,
                    datePickerTo.value?.dayOfMonth ?: 0
                )
            )
        ).also {
            when {
                index in 0 until invoice.size -> {
                    listInvoice.items[index] = it.title
                    invoice[index] = it
                }
                index < 0 -> {
                    // Don't do anything since nothing selected
                }
                else -> {
                    listInvoice.items.add(invoice.size, it.title)
                    invoice.add(it)
                }
            }
        }
    }

    @FXML
    fun initialize() {
        Main.logger.log(Level.INFO, "Initializing…")

        loadInvoice()

        listInvoice.selectionModel.selectedIndexProperty()
            .addListener { _, _, index ->
                applyInvoice(index.toInt())
                btnDelete.isDisable = index.toInt() !in 0 until invoice.size
            }

        arrayOf(txtTitle, txtSuper, txtSpecial, txtFirst, txtAdditional).forEach {
            it.textProperty().addListener { _, _, _ -> onInvoiceChanged() }
        }

        MinguoChronology.INSTANCE.also { chronology ->
            datePickerFrom.chronology = chronology
            datePickerTo.chronology = chronology
        }

        DateTimeFormatter.ofPattern("Gyyy/M/d", Locale.TAIWAN).also { formatter ->
            object : StringConverter<LocalDate>() {
                override fun toString(date: LocalDate?) = date?.format(formatter)
                override fun fromString(string: String?) = string?.let { LocalDate.parse(it, formatter) }
            }.also { converter ->
                datePickerFrom.converter = converter
                datePickerTo.converter = converter
            }
        }

        Main.logger.log(Level.INFO, "Initialized.")
    }

    fun shutdown() {
        Main.logger.log(Level.INFO, "Shutting down…")

        saveInvoice()
    }

    @FXML
    fun onInvoiceChanged() {
        Main.logger.log(Level.INFO, "Invoice changed.")

        if (needUpdateInvoice(listInvoice.selectionModel.selectedIndex))
            updateInvoice(listInvoice.selectionModel.selectedIndex)
    }

    @FXML
    fun onFetch() {
        btnFetch.isDisable = true

        InvoiceNumber.fetch()
            ?.apply { invoice.addAll(0, toMutableList()) }
            ?.apply { Main.logger.log(Level.INFO, "Invoice information has been downloaded successfully.") }
            ?: Alert(Alert.AlertType.INFORMATION).apply {
                Main.logger.log(Level.WARNING, "Failed to download invoice information.")
                headerText = "發票開獎資訊下載失敗"
                dialogPane.content = Label("無法取得發票開獎資訊，請檢查您的網際網路連線。")
                showAndWait()
            }
        initializeInvoice()

        btnFetch.isDisable = false
    }

    @FXML
    fun onDelete() {
        val index = listInvoice.selectionModel.selectedIndex
        if (index in 0 until invoice.size &&
            Alert(Alert.AlertType.CONFIRMATION)
                .apply { headerText = "確定刪除？" }
                .apply { contentText = "您確定要刪除此筆資料嗎？\n\n此筆資料將會永遠消失（非常久）！" }
                .run { showAndWait().get() == ButtonType.OK }
        ) {
            invoice.removeAt(index)
            initializeInvoice()
        }
    }

    @FXML
    fun onClose() {
        thisStage.close()
    }

    companion object {
        val stage: Stage
            get() = FXMLLoader(this::class.java.getResource("/layouts/invoice.fxml"))
                .apply { Main.logger.log(Level.INFO, "Loading layout…") }
                .let { loader ->
                    loader.load<Stage>()
                        .apply { initModality(Modality.APPLICATION_MODAL) }
                        .apply { setOnHidden { loader.getController<InvoiceController>().shutdown() } }
                        .apply { Main.logger.log(Level.INFO, "Layout loaded.") }
                }
    }
}
