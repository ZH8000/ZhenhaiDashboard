package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.http.OutputStreamResponse
import net.liftweb.sitemap.Loc._
import net.liftweb.common._
import net.liftweb.util.Helpers._

object MaintenanceCode extends MaintenanceCode with MongoMetaRecord[MaintenanceCode] {
  override def collectionName = "maintenanceCode"

  def barcodePDF = new EarlyResponse(() => 
    Full(OutputStreamResponse(MaintenanceCodePDF.createPDF _, -1, List("Content-Type" -> "application/pdf")))
  )

}

class MaintenanceCode extends MongoRecord[MaintenanceCode] with ObjectIdPk[MaintenanceCode] {
  def meta = MaintenanceCode

  val code = new StringField(this, 10)
  val description = new StringField(this, 100)
}
