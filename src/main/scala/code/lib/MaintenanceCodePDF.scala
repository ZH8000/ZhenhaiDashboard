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

  def createBarcodeLabel(code: Int, step: String, description: String, pdfWriter: PdfWriter) = {

    val cell = new PdfPCell
    val barCode = new Barcode39()
    val stepTag = new Paragraph(s"$step", chineseFont)
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

  def createPDF(outputStream: OutputStream) = {
    val document = new Document(PageSize.A4)
    val pdfWriter = PdfWriter.getInstance(document, outputStream)
    val table = new PdfPTable(1)
    val steps = 
      (1 -> "加締卷取") ::
      (2 -> "組立") ::
      (3 -> "老化") ::
      (4 -> "選別") ::
      (5 -> "加工切角") :: Nil

    document.open()
    pdfWriter.setPageEmpty(false)

    for {
      (step, stepTitle) <- steps
    } {
      val pageTitle = new Paragraph(stepTitle, titleFont)
      pageTitle.setAlignment(Element.ALIGN_CENTER)
      pageTitle.setSpacingAfter(10)
      document.add(pageTitle)
      val table = new PdfPTable(1)
      table.setWidthPercentage(20f)

      for {
        code <- 1 :: 2 :: 3 :: 4 :: 5 :: 6 :: 7 :: 8 :: 9 :: Nil
        codeMapping <- MaintenanceCode.mapping.get(step)
        codeTitle <- codeMapping.get(code)
      } {
        table.addCell(createBarcodeLabel(code, stepTitle, codeTitle, pdfWriter))
      }
      table.completeRow()
      document.add(table)
      document.newPage()
    }

    document.close()
  }

}
