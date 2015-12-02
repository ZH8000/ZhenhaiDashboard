package code.lib

import code.model._
import com.itextpdf.text._
import com.itextpdf.text.pdf._
import java.io._

object WorkerBarcodePDF {

  val baseFont = BaseFont.createFont("/usr/share/fonts/arphicfonts/bsmi00lp.ttf",  BaseFont.IDENTITY_H, BaseFont.EMBEDDED)
  val chineseFont = new Font(baseFont, 10)

  def createBarcodeLabel(worker: Worker, pdfWriter: PdfWriter) = {

    val workerName = worker.name.get
    val department = worker.department.get
    val workerID = worker.workerID.get
    val cell = new PdfPCell
    val barCode = new Barcode128()

    val departmentTag = new Paragraph(s"【$department】 $workerID", chineseFont)
    val nameTag = new Paragraph(s"$workerName", chineseFont)

    val barcodePrefix = if (worker.workerType.get == "maintain") "M" else "P"

    barCode.setCode("XXX" + barcodePrefix + worker.id.toString.toUpperCase + worker.workerID.toString.toUpperCase)
    nameTag.setAlignment(Element.ALIGN_CENTER)
    departmentTag.setAlignment(Element.ALIGN_CENTER)

    cell.setPadding(10)
    cell.setHorizontalAlignment(Element.ALIGN_CENTER)
    cell.addElement(barCode.createImageWithBarcode(new PdfContentByte(pdfWriter), null, null))
    cell.addElement(departmentTag)
    cell.addElement(nameTag)

    cell
  }

  def insertBlankPage(document: Document, pdfWriter: PdfWriter) = {
    document.newPage()
    pdfWriter.setPageEmpty(false)
  }

  def createPDF(outputStream: OutputStream) = {
    val document = new Document(PageSize.A4, 20, 20, 0, 0)
    val pdfWriter = PdfWriter.getInstance(document, outputStream)
    val table = new PdfPTable(2)
    val workers = Worker.findAll("isDeleted", false)

    table.setWidthPercentage(95)

    document.open()
    document.newPage()

    pdfWriter.setPageEmpty(false)
    workers.foreach ( worker => table.addCell(createBarcodeLabel(worker, pdfWriter)) )
    table.completeRow()

    document.add(table)
    document.close()
  }

}
