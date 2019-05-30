package com.github.plokhotnyuk.jsoniter_scala.examples

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.{Path, Paths}

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

trait BigDocument

case class Meta(id: Int, name: String)

case class Feature(name: String, value: Double)

object Examples {
  def main(args: Array[String]): Unit = {
    val tmpFilePath: Path = Paths.get(File.createTempFile("big-document", ".json").getAbsolutePath)

    val fos = new FileOutputStream(tmpFilePath.toFile)
    try {
      writeToStream(new BigDocument {}, fos)(new BigDocumentCodec(
        metaProducer = _ => Meta(1, "VVV"),
        featureProducer = {
          var count = 1000000
          _ =>
            count -= 1
            if (count < 0) None
            else Some(Feature(s"WWW-$count", count / 10.0))
        },
        metaHandler = _ => (),
        featureHandler = _ => ()
      ))
    } finally fos.close()

    val fis = new FileInputStream(tmpFilePath.toFile)
    try {
      readFromStream[BigDocument](fis)(new BigDocumentCodec(
        metaProducer = _ => null,
        featureProducer = _ => None,
        metaHandler = x => println(x),
        featureHandler = x => println(x)
      ))
    } finally fis.close()
  }
}

class BigDocumentCodec(metaProducer:Unit => Meta,
                       featureProducer: Unit => Option[Feature],
                       metaHandler: Meta => Unit,
                       featureHandler: Feature => Unit) extends JsonValueCodec[BigDocument] {
  private val metaCodec: JsonValueCodec[Meta] = JsonCodecMaker.make(CodecMakerConfig())
  private val featureCodec: JsonValueCodec[Feature] = JsonCodecMaker.make[Feature](CodecMakerConfig())

  override def decodeValue(in:  JsonReader, default:  BigDocument): BigDocument = if (in.isNextToken('{')) {
    var p0 = 0x3
    if (!in.isNextToken('}')) {
      in.rollbackToken()
      var l = -1
      while (l < 0 || in.isNextToken(',')) {
        l = in.readKeyAsCharBuf()
        if (in.isCharBufEqualsTo(l, "meta")) {
          if ((p0 & 0x1) != 0) p0 ^= 0x1
          else in.duplicatedKeyError(l)
          metaHandler(metaCodec.decodeValue(in, metaCodec.nullValue))
        } else if (in.isCharBufEqualsTo(l, "features")) {
          if ((p0 & 0x2) != 0) p0 ^= 0x2
          else in.duplicatedKeyError(l)
          if (in.isNextToken('[')) {
            if (in.isNextToken(']')) default
            else {
              in.rollbackToken()
              do featureHandler(featureCodec.decodeValue(in, featureCodec.nullValue))
              while (in.isNextToken(','))
              if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
            }
          } else in.readNullOrTokenError(default, '[')
        } else in.skip()
      }
      if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
    }
    new BigDocument() {}
  } else in.readNullOrTokenError(default, '{')

  override def encodeValue(x:  BigDocument, out: JsonWriter): Unit = {
    out.writeObjectStart()
    out.writeNonEscapedAsciiKey("meta")
    metaCodec.encodeValue(metaProducer(), out)
    out.writeNonEscapedAsciiKey("features")
    out.writeArrayStart()
    while(featureProducer().fold(false) { feature =>
      featureCodec.encodeValue(feature, out)
      true
    }) {}
    out.writeArrayEnd()
    out.writeObjectEnd()
  }

  override val nullValue: BigDocument = null
}