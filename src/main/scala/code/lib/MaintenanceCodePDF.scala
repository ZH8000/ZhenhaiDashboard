package code.lib

import code.model._
import com.itextpdf.text._
import com.itextpdf.text.pdf._
import java.io._

object MaintenanceCodePDF {

  val baseFont = BaseFont.createFont(
    "MHei-Medium",
    "UniCNS-UCS2-H", // 橫式中文
     BaseFont.NOT_EMBEDDED
  )

  val chineseFont = new Font(baseFont, 6) 
  val titleFont = new Font(baseFont, 10)

  def createBarcodeLabel(code: Int, description: String, pdfWriter: PdfWriter) = {

    val cell = new PdfPCell
    val barCode = new Barcode39()
    val descriptionTag = new Paragraph(s"$description", chineseFont)

    barCode.setCode("UUU" + code)
    barCode.setBarHeight(5)
    barCode.setSize(1.5f)
    barCode.setBaseline(1.5f)
    barCode.setFont(baseFont)

    descriptionTag.setAlignment(Element.ALIGN_CENTER)
    cell.setPadding(5)
    cell.setHorizontalAlignment(Element.ALIGN_CENTER)
    cell.addElement(barCode.createImageWithBarcode(new PdfContentByte(pdfWriter), null, null))
    cell.addElement(descriptionTag)

    cell
  }

  def insertBlankPage(document: Document, pdfWriter: PdfWriter) = {
    document.newPage()
    pdfWriter.setPageEmpty(false)
  }

  def createCell(stepTitle: String, stepCode: Int, pdfWriter: PdfWriter) = {
    val outterCell = new PdfPCell
    val titleParagraph = new Paragraph(stepTitle, titleFont)
    val table = new PdfPTable(1)

    titleParagraph.setAlignment(Element.ALIGN_CENTER)
    outterCell.setHorizontalAlignment(Element.ALIGN_CENTER)

    for {
      codeMapping <- MaintenanceCode.mapping.get(stepCode)
      code        <- 1 to 9
      codeTitle   <- codeMapping.get(code)
    } {
      table.addCell(createBarcodeLabel(code, codeTitle, pdfWriter))
    }
    table.completeRow()
    outterCell.addElement(titleParagraph)
    outterCell.addElement(table)
    outterCell
  }

  def createPDF(outputStream: OutputStream) = {
    val document = new Document(PageSize.A4.rotate)
    val pdfWriter = PdfWriter.getInstance(document, outputStream)
    val steps = 
      (1 -> "加締卷取") ::
      (2 -> "組立") ::
      (3 -> "老化") ::
      (4 -> "選別") ::
      (5 -> "加工切角") :: Nil

    document.open()
    pdfWriter.setPageEmpty(false)

    val table = new PdfPTable(3)
    table.addCell(createCell("加締", 1, pdfWriter))
    table.addCell(createCell("組立", 2, pdfWriter))
    table.addCell(createCell("老化", 3, pdfWriter))
    table.addCell(createCell("選別", 4, pdfWriter))
    table.addCell(createCell("加工切角", 5, pdfWriter))
    table.completeRow()
    document.add(table)
    document.close()
  }

}
