package com.github.plokhotnyuk.jsoniter_scala.examples

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

case class Device(id: Int, model: String)

case class User(name: String, devices: Seq[Device])

trait AbstractSerde[T] {
  def deserialize(message: Array[Byte]): T

  def serialize(element: T): Array[Byte]
}

class JsoniterSerde[T: JsonValueCodec] extends AbstractSerde[T] {
  override def deserialize(message: Array[Byte]): T = readFromArray(message)

  override def serialize(element: T): Array[Byte] = writeToArray(element)
}

object Boilerplate {
  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make[User](CodecMakerConfig())
}

object Examples {
  import Boilerplate._

  val serde = new JsoniterSerde[User]()

  def main(args: Array[String]): Unit = {
    val user = serde.deserialize("""{"name":"John","devices":[{"id":1,"model":"HTC One X"}]}""".getBytes("UTF-8"))
    val json = serde.serialize(User(name = "John", devices = Seq(Device(id = 2, model = "iPhone X"))))
    println(user)
    println(new String(json, "UTF-8"))
  }
}
