package code.lib

import java.io._

import code.model._
import com.itextpdf.text._
import com.itextpdf.text.pdf._

/**
 *  用來產生網站上「網站管理」－＞「維修代碼」中的條碼的 PDF 檔案
 */
object MaintenanceCodePDF {

  // 中文字型設定
  val baseFont = BaseFont.createFont(
    "MHei-Medium",          // 內建中文字型
    "UniCNS-UCS2-H",        // 橫式中文
     BaseFont.NOT_EMBEDDED  // 非內嵌字型
  )

  val chineseFont = new Font(baseFont, 6)     // 項目的標題
  val titleFont = new Font(baseFont, 10)      // 標題的字型

  /**
   *  建立維修條碼的
   *
   *  @param    code            維修項目的編號
   *  @param    description     維修項目的描述
   *  @param    pdfWriter       寫到哪個 PDF
   */
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

  /**
   *  建立某個製程中的所有的維修條碼
   *
   *  @param    stepTitle     製程標頭
   *  @param    stepCode      製程的代碼（1 = 加締 / 2 = 組立……）
   *  @param    pdfWriter     要寫到哪個 PDF 中
   */
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

  /**
   *  建立維修代碼的條碼的 PDF 檔
   *
   *  @param      outputStream      要將維修代碼的條碼輸出到哪個 OutputStream
   */
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
