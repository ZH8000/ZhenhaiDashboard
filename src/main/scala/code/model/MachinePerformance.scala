package code.model

import code.lib._

import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._
import net.liftweb.common._
import net.liftweb.json.JsonDSL._

object MachinePerformance extends MachinePerformance with MongoMetaRecord[MachinePerformance] {
  override def collectionName = "machinePerformance"

  def find(machineID: String, productCode: String): Box[MachinePerformance] = {
    this.find(("machineID" -> machineID) ~ ("productCode" -> productCode))
  }

  def update(machineID: String, productCode: String, managementCount: Long, performanceCount: Long) = {
    find(machineID, productCode) match {
      case Full(record) => 
        record.managementCount(managementCount)
              .performanceCount(performanceCount)

      case _ =>

        MachinePerformance.machineID(machineID)
                          .productCode(productCode)
                          .managementCount(managementCount)
                          .performanceCount(performanceCount)
    }

  }
}

class MachinePerformance extends MongoRecord[MachinePerformance] with ObjectIdPk[MachinePerformance] {
  def meta = MachinePerformance

  val machineID = new StringField(this, 10)
  val productCode = new StringField(this, 10)
  val managementCount = new LongField(this)
  val performanceCount = new LongField(this)

}
