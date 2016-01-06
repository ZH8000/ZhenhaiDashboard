package code.lib

import java.io._

import code.model._
import com.itextpdf.text._
import com.itextpdf.text.pdf._

/**
 *  產生員工編號
 */
object WorkerBarcodePDF {

  val baseFont = BaseFont.createFont(
    "/usr/share/fonts/arphicfonts/bsmi00lp.ttf",    // 字型檔的路徑
    BaseFont.IDENTITY_H,                            // 橫式字型
    BaseFont.EMBEDDED                               // 內嵌 
  )

  val chineseFont = new Font(baseFont, 10)

  /**
   *  產生員工編號的條碼到 PDF 中
   *
   *  @param      worker        要產生哪個員工的條碼
   *  @param      pdfWriter     要輸出到哪個 PDF 檔中
   */
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

  /**
   *  建立定定員工編號的 PDF 檔並輸出到 OutputStream 中
   *
   *  @param    outputStream      要把 PDF 檔輸出到哪個 OutputStream 中
   */
  def createPDF(workerID: scala.collection.immutable.List[String])(outputStream: OutputStream) = {
    val document = new Document(PageSize.A4, 20, 20, 0, 0)
    val pdfWriter = PdfWriter.getInstance(document, outputStream)
    val table = new PdfPTable(2)
    val workers = Worker.findAll("isDeleted", false).filter(worker => workerID contains worker.id.toString)

    table.setWidthPercentage(95)

    document.open()
    document.newPage()

    pdfWriter.setPageEmpty(false)
    workers.foreach ( worker => table.addCell(createBarcodeLabel(worker, pdfWriter)) )
    table.completeRow()

    document.add(table)
    document.close()
  }

  /**
   *  建立員工編號的 PDF 檔並輸出到 OutputStream 中
   *
   *  @param    outputStream      要把 PDF 檔輸出到哪個 OutputStream 中
   */
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
