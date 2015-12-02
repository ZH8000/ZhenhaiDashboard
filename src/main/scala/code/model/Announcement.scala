package code.model

import com.mongodb.casbah.Imports._

import net.liftweb.util.BCrypt
import net.liftweb.common.{Box, Full, Empty, Failure}
import net.liftweb.http.SessionVar
import net.liftweb.mongodb.record.MongoRecord
import net.liftweb.mongodb.record.MongoMetaRecord
import net.liftweb.mongodb.record.field._
import net.liftweb.record.field._

import code.lib._
import net.liftweb.http.S

object Announcement extends Announcement with MongoMetaRecord[Announcement] {
  override def collectionName = "announcement"
}

class Announcement extends MongoRecord[Announcement] with ObjectIdPk[Announcement] {
  def meta = Announcement
  val content = new OptionalStringField(this, None)
}

