package com.github.plokhotnyuk.jsoniter_scala.examples

import java.io.{FileInputStream, FileOutputStream}
import java.util.zip.{GZIPInputStream, GZIPOutputStream}

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

/*
Copied from the following benchmark:
https://github.com/nguyentoanit/scala-json-libraries-comparison/blob/master/src/main/scala/example/Jsoniter.scala
 */
object Examples {
  def main(args: Array[String]): Unit = {
    val startTime = System.currentTimeMillis

    case class OldReport(campaignId: String, campaignName: String)
    case class NewReport(reportDate: String, profileId: Long, data: Array[OldReport])

    implicit val codec: JsonValueCodec[Array[OldReport]] = JsonCodecMaker.make(CodecMakerConfig())
    implicit val codec2: JsonValueCodec[NewReport] = JsonCodecMaker.make(CodecMakerConfig())
    val fis: FileInputStream = new FileInputStream("old.json.gz")
    val gis: GZIPInputStream = new GZIPInputStream(fis)

    val report = readFromStream(gis)(codec)
    val dataToCompress = NewReport("20190101", 123, report)

    val fos: FileOutputStream = new FileOutputStream("new.json.gz")
    val gos: GZIPOutputStream = new GZIPOutputStream(fos)
    writeToStream(dataToCompress, gos)(codec2)
    gos.close()

    val stopTime = System.currentTimeMillis
    println(stopTime - startTime)
  }
}
