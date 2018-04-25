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
      BigInteger.valueOf(offset.getOrElse(BlockIdField, 0L).asInstanceOf[Long]),
      offset.getOrElse(TransactionCountField, 0L).asInstanceOf[Long],
      BigInteger.valueOf(offset.getOrElse(OffsetField, 0L).asInstanceOf[Long])
    )
  }

  def parse(offset: java.util.Map[String, Any]): EthereumBlockchainTransactionOffset = {
    new EthereumBlockchainTransactionOffset(
      BigInteger.valueOf(offset.get(BlockIdField).asInstanceOf[Long]),
      offset.get(TransactionCountField).asInstanceOf[Long],
      BigInteger.valueOf(offset.get(OffsetField).asInstanceOf[Long])
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
      EthereumBlockchainTransactionOffset.BlockIdField -> blockId,
      EthereumBlockchainTransactionOffset.TransactionCountField -> blockTransactionCount,
      EthereumBlockchainTransactionOffset.OffsetField -> offset
    )
  }

  def toJavaMap(): java.util.Map[String, Any] = {
    toMap().asJava
  }
}
