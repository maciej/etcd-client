package me.maciejb.etcd.client.impl

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import me.maciejb.etcd.client.{EtcdError, EtcdNode, EtcdResponse}
import spray.json._

/**
  * Provides Spray JSON format implicits for `etcd` messages.
  */
private[client] object EtcdJsonProtocol extends DefaultJsonProtocol {

  /** Spray JSON format for [[EtcdError]] case class. */
  implicit val etcdErrorFormat: RootJsonFormat[EtcdError] = new RootJsonFormat[EtcdError] {

    override def write(obj: EtcdError) = {
      JsObject(
        "errorCode" -> JsNumber(obj.errorCode),
        "message" -> JsString(obj.message),
        "cause" -> JsString(obj.cause),
        "index" -> JsNumber(obj.index)
      )
    }

    override def read(json: JsValue) = {
      val fields = json.asJsObject.getFields("errorCode", "message", "cause", "index")
      fields.toList match {
        case JsNumber(errorCode) :: JsString(message) :: JsString(cause) :: JsNumber(index) :: Nil =>
          EtcdError(errorCode.toInt, message, cause, index.toInt)
        case _ => deserializationError(s"Could not deserialize $json to EtcdError")
      }
    }

  }

  /** Spray JSON format for `java.time.ZonedDateTime` represented as `ISO_ZONED_DATE_TIME` string */
  implicit object InstantJsonFormat extends RootJsonFormat[ZonedDateTime] {
    val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    def write(i: ZonedDateTime) =
      JsString(formatter.format(i))

    def read(value: JsValue) = value match {
      case JsString(s) ⇒ ZonedDateTime.from(formatter.parse(s))
      case _ ⇒ deserializationError("string expected")
    }
  }

  /** Spray JSON format for [[EtcdNode]] case class. */
  implicit val etcNodeFormat: JsonFormat[EtcdNode] = lazyFormat(jsonFormat7(EtcdNode))

  /** Spray JSON format for [[EtcdResponse]] case class. */
  implicit val etcdResponseFormat = jsonFormat3(EtcdResponse)
}