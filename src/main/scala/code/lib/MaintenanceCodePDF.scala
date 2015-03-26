package code.lib

import code.model._
import com.itextpdf.text._
import com.itextpdf.text.pdf._
import java.io._

object MaintenanceCodePDF {

  val baseFont = BaseFont.createFont("/usr/share/fonts/arphicfonts/bsmi00lp.ttf",  BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
  val chineseFont = new Font(baseFont, 10)

  def createBarcodeLabel(maintenanceCode: MaintenanceCode, pdfWriter: PdfWriter) = {

    val cell = new PdfPCell
    val barCode = new Barcode128()
    val descriptionTag = new Paragraph(maintenanceCode.description.get, chineseFont)

    barCode.setCode(maintenanceCode.code.get)
    descriptionTag.setAlignment(Element.ALIGN_CENTER)

    cell.setPadding(10)
    cell.setHorizontalAlignment(Element.ALIGN_CENTER)
    cell.addElement(barCode.createImageWithBarcode(new PdfContentByte(pdfWriter), null, null))
    cell.addElement(descriptionTag)

    cell
  }

  def insertBlankPage(document: Document, pdfWriter: PdfWriter) = {
    document.newPage()
    pdfWriter.setPageEmpty(false)
  }

  def createPDF(outputStream: OutputStream) = {
    val document = new Document(PageSize.A4)
    val pdfWriter = PdfWriter.getInstance(document, outputStream)
    val table = new PdfPTable(3)
    val codes = MaintenanceCode.findAll.toList.sortWith(_.code.get < _.code.get)

    document.open()
    document.newPage()

    pdfWriter.setPageEmpty(false)
    codes.foreach ( code => table.addCell(createBarcodeLabel(code, pdfWriter)) )
    table.completeRow()

    document.add(table)
    document.close()
  }

}
