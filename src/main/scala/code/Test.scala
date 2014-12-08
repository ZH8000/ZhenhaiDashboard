import code.model._
import code.lib._
import bootstrap.liftweb.Boot
import scala.util.Random
import code.model.MongoDB
import com.mongodb.casbah.Imports._

object Test {

  def main(args: Array[String]) {
    val boot = new Boot
    boot.boot

    /*
    val workerID = Worker.findAll.toList.map(_.id.toString)
    val workerDaily = MongoDB.zhenhaiDB("workerDaily")
    println(workerID.size)
    println(workerID)

    for (i <- 0 until 100000) {

      val workerIndex = Random.nextInt(workerID.size)
      val machineIDIndex = Random.nextInt(MachineInfo.machineList.size)
      val timeOffset = Random.nextLong % 10000000000L
      val date = new java.util.Date((new java.util.Date).getTime - timeOffset)
      val dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd")
      val timestamp = dateFormatter.format(date)
      val countQty = scala.math.abs(Random.nextInt(1000))
      val worker = workerID(workerIndex).toString
      val machineID = MachineInfo.machineList(machineIDIndex)

      println(s"[$i] $timestamp $worker $machineID $countQty")

      workerDaily.update(
        MongoDBObject("workerMongoID" -> worker, "timestamp" -> timestamp, "machineID" -> machineID),
        $inc("countQty" -> countQty),
        upsert = true
      )
    }
  */

    MachineInfo.machineList.foreach { machineID =>
      println("Insert machineID... " + machineID)
      MachineLevel.createRecord.machineID(machineID).levelC(100).levelB(500).levelA(1000).saveTheRecord()
    }



  }

}
