package com.internetsystemsgroup.ethereum

import java.math.BigInteger
import scala.collection.immutable.Map
import collection.JavaConverters._

object EthereumBlockchainTransactionOffset {
  val BlockIdField = "block"
  val TransactionCountField = "size"
  val OffsetField = "offset"

  def parse(offset: Map[String, Any]): EthereumBlockchainTransactionOffset = {
    new EthereumBlockchainTransactionOffset(
      new BigInteger(offset.getOrElse(BlockIdField, "0").asInstanceOf[String]),
      offset.getOrElse(TransactionCountField, 0L).asInstanceOf[Long],
      new BigInteger(offset.getOrElse(OffsetField, "0").asInstanceOf[String])
    )
  }

  def parse(offset: java.util.Map[String, Any]): EthereumBlockchainTransactionOffset = {
    new EthereumBlockchainTransactionOffset(
      new BigInteger(offset.get(BlockIdField).asInstanceOf[String]),
      offset.get(TransactionCountField).asInstanceOf[Long],
      new BigInteger(offset.get(OffsetField).asInstanceOf[String])
    )
  }
}

class EthereumBlockchainTransactionOffset(
  val blockId: BigInteger,
  val blockTransactionCount: Long,
  val offset: BigInteger
) {
  def toMap(): Map[String, Any] = {
    Map(
      EthereumBlockchainTransactionOffset.BlockIdField -> blockId.toString,
      EthereumBlockchainTransactionOffset.TransactionCountField -> blockTransactionCount,
      EthereumBlockchainTransactionOffset.OffsetField -> offset.toString
    )
  }

  def toJavaMap(): java.util.Map[String, Any] = {
    toMap().asJava
  }
}
