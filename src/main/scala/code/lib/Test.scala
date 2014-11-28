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

    barCode.setCode(worker.id.toString)
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
    val document = new Document(PageSize.A4)
    val pdfWriter = PdfWriter.getInstance(document, outputStream)
    val table = new PdfPTable(2)
    val workers = Worker.findAll("isDeleted", false)

    document.open()

    workers.foreach ( worker => table.addCell(createBarcodeLabel(worker, pdfWriter)) )
    table.completeRow()

    workers.isEmpty match {
      case true  => insertBlankPage(document, pdfWriter)
      case false => document.add(table)
    }

    document.close()
  }

}
