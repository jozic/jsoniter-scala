package com.github.plokhotnyuk.jsoniter_scala.examples

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._
import java.time.LocalDate

import scala.util.Try

case class UserData(
  user_id: String,
  @stringified is_user_logged_in: Boolean,
  checkin_date: LocalDate,
  checkout_date: LocalDate
) {
  require(!is_user_logged_in || (user_id ne null), "missing `user_id` for logged in user")
  require(checkout_date.isAfter(checkin_date), "`checkout_date` should be after `checkin_date`")
}

object Examples {
  def main(args: Array[String]): Unit = {
    implicit val codec: JsonValueCodec[UserData] = JsonCodecMaker.make[UserData](CodecMakerConfig())

    println(Try(readFromArray(
      """
        |{
        |"user_id":"x",
        |"is_user_logged_in":"true",
        |"checkin_date":"2018-12-12",
        |"checkout_date":"2019-12-13"
        |}
      """.stripMargin.getBytes("UTF-8"))))

    println(Try(readFromArray(
      """
        |{
        |"user_id":"x",
        |"is_user_logged_in":"true",
        |"checkin_date":"2222-12-12",
        |"checkout_date":"2019-12-13"
        |}
      """.stripMargin.getBytes("UTF-8"))))

    println(Try(readFromArray(
      """
        |{
        |"user_id":null,
        |"is_user_logged_in":"true",
        |"checkin_date":"2222-12-12",
        |"checkout_date":"2019-12-13"
        |}
      """.stripMargin.getBytes("UTF-8"))))
  }
}
