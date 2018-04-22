package com.internetsystemsgroup.ethereum

import java.io.ByteArrayOutputStream

import com.sksamuel.avro4s.{AvroOutputStream, RecordFormat}
import org.apache.avro.generic.GenericRecord

object EthereumTransaction {
  private[ethereum] val format = RecordFormat[EthereumTransaction]

  def NONE: EthereumTransaction = {EthereumTransaction(None)}
}
case class EthereumTransaction
(
  txn: Option[EthTransaction]
) {
  def asAvroRecord: GenericRecord = {
    EthereumTransaction.format.to(this)
  }

  def asAvroJson: String = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.json[EthereumTransaction](baos)
    output.write(this)
    output.close()
    baos.toString("UTF-8")
  }
}

object EthTransaction {
  private[ethereum] val format = RecordFormat[EthTransaction]
}
case class EthTransaction
(
  hash:     String,
  from:     String,
  to:       String,
  value:    BigDecimal,
  gasPrice: BigDecimal,
  gas:      BigDecimal,
) {

  def asAvroRecord: GenericRecord = {
   EthTransaction.format.to(this)
  }

  def asAvroJson: String = {
    val baos = new ByteArrayOutputStream()
    val output = AvroOutputStream.json[EthTransaction](baos)
    output.write(this)
    output.close()
    baos.toString("UTF-8")
  }
}
