package tw.mingtsay.niceinvoice.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class InvoiceNumberTest {

    @Test
    fun compare() {
        val invoiceNumber = InvoiceNumber(
            id = "test",
            title = "測試",
            superNumber = "12345678",
            specialNumber = "54321678",
            firstNumbers = arrayOf("13579246", "13576622"),
            additionalNumbers = arrayOf("226", "246", "666"),
            date = InvoiceDate(from = 0, to = 0, text = "")
        )

        assertEquals(InvoiceResult.INVOICE_RESULT_ADDITIONAL, invoiceNumber.compare("666"))
        assertEquals(InvoiceResult.INVOICE_RESULT_ADDITIONAL, invoiceNumber.compare("226"))

        assertEquals(InvoiceResult.INVOICE_RESULT_PERHAPS, invoiceNumber.compare("246"))
        assertEquals(InvoiceResult.INVOICE_RESULT_PERHAPS, invoiceNumber.compare("678"))
        assertEquals(InvoiceResult.INVOICE_RESULT_PERHAPS, invoiceNumber.compare("622"))

        assertEquals(InvoiceResult.INVOICE_RESULT_NONE, invoiceNumber.compare("888"))

        assertEquals(InvoiceResult.INVOICE_RESULT_ADDITIONAL, invoiceNumber.compare("87878666"))
        assertEquals(InvoiceResult.INVOICE_RESULT_ADDITIONAL, invoiceNumber.compare("78787226"))

        assertEquals(InvoiceResult.INVOICE_RESULT_SUPER, invoiceNumber.compare("12345678"))
        assertEquals(InvoiceResult.INVOICE_RESULT_NONE, invoiceNumber.compare("12435678"))

        assertEquals(InvoiceResult.INVOICE_RESULT_SPECIAL, invoiceNumber.compare("54321678"))
        assertEquals(InvoiceResult.INVOICE_RESULT_NONE, invoiceNumber.compare("53421678"))

        assertEquals(InvoiceResult.INVOICE_RESULT_FIRST, invoiceNumber.compare("13576622"))
        assertEquals(InvoiceResult.INVOICE_RESULT_SECOND, invoiceNumber.compare("23576622"))
        assertEquals(InvoiceResult.INVOICE_RESULT_THIRD, invoiceNumber.compare("12576622"))
        assertEquals(InvoiceResult.INVOICE_RESULT_FOURTH, invoiceNumber.compare("13276622"))
        assertEquals(InvoiceResult.INVOICE_RESULT_FIFTH, invoiceNumber.compare("13526622"))
        assertEquals(InvoiceResult.INVOICE_RESULT_SIXTH, invoiceNumber.compare("13572622"))

        assertEquals(InvoiceResult.INVOICE_RESULT_FIRST, invoiceNumber.compare("13579246"))
        assertEquals(InvoiceResult.INVOICE_RESULT_SECOND, invoiceNumber.compare("73579246"))
        assertEquals(InvoiceResult.INVOICE_RESULT_THIRD, invoiceNumber.compare("77579246"))
        assertEquals(InvoiceResult.INVOICE_RESULT_FOURTH, invoiceNumber.compare("77779246"))
        assertEquals(InvoiceResult.INVOICE_RESULT_FIFTH, invoiceNumber.compare("77769246"))
        assertEquals(InvoiceResult.INVOICE_RESULT_SIXTH, invoiceNumber.compare("77777246"))

        assertEquals(InvoiceResult.INVOICE_RESULT_NONE, invoiceNumber.compare("88888888"))
    }
}
