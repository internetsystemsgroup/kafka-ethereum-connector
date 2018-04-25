package com.internetsystemsgroup.ethereum

import java.math.BigInteger
import org.scalatest.{Matchers, FunSpec}

class EthereumBlockchainTransactionOffsetSpec extends FunSpec with Matchers {
  describe("EthereumBlockchainTransactionOffsetSpec") {
    describe("default constructor") {
      it("should create an object") {
        val offset = new EthereumBlockchainTransactionOffset(
          new BigInteger("1"), 2, new BigInteger("3"))
        offset.blockId shouldBe new BigInteger("1")
        offset.blockTransactionCount shouldBe 2
        offset.offset shouldBe new BigInteger("3")
      }
    }

    describe("parse") {
      it("should create an object when passed a scala.collection.immutable.Map") {
        val map = Map(
          EthereumBlockchainTransactionOffset.BlockIdField -> "1",
          EthereumBlockchainTransactionOffset.TransactionCountField -> 2L,
          EthereumBlockchainTransactionOffset.OffsetField -> "3"
        )
        val offset = EthereumBlockchainTransactionOffset.parse(map)
        offset.blockId shouldBe new BigInteger("1")
        offset.blockTransactionCount shouldBe 2
        offset.offset shouldBe new BigInteger("3")
      }

      it("should be able to parse a Map created with toMap including large BigInteger values") {
        val nTooBigForLong = BigInteger.valueOf(Long.MaxValue).add(BigInteger.ONE)
        val originalOffset = new EthereumBlockchainTransactionOffset(
          nTooBigForLong, Long.MaxValue, nTooBigForLong)
        val map = originalOffset.toMap()
        val newOffset = EthereumBlockchainTransactionOffset.parse(map)
        newOffset.blockId shouldBe originalOffset.blockId
        newOffset.blockTransactionCount shouldBe originalOffset.blockTransactionCount
        newOffset.offset shouldBe originalOffset.offset
      }

      it("should create an object when passed a java.util.Map") {
        val map = new java.util.HashMap[String, Any] {
          put(EthereumBlockchainTransactionOffset.BlockIdField, "1")
          put(EthereumBlockchainTransactionOffset.TransactionCountField, 2L)
          put(EthereumBlockchainTransactionOffset.OffsetField, "3")
        }
        val offset = EthereumBlockchainTransactionOffset.parse(map)
        offset.blockId shouldBe new BigInteger("1")
        offset.blockTransactionCount shouldBe 2
        offset.offset shouldBe new BigInteger("3")
      }
    }

    describe("toMap") {
      it("should return a Map with the appropriate elements") {
        val offset = new EthereumBlockchainTransactionOffset(
          new BigInteger("1"), 2, new BigInteger("3"))
        val expectedMap = Map(
          EthereumBlockchainTransactionOffset.BlockIdField -> "1",
          EthereumBlockchainTransactionOffset.TransactionCountField -> 2L,
          EthereumBlockchainTransactionOffset.OffsetField -> "3"
        )
        offset.toMap() shouldBe expectedMap
      }
    }

    describe("toJavaMap") {
      it("should return a java.util.Map with the appropriate elements") {
        val actualMap = new EthereumBlockchainTransactionOffset(
          new BigInteger("1"), 2, new BigInteger("3")).toJavaMap()
        actualMap.size() shouldBe 3
        actualMap.get(EthereumBlockchainTransactionOffset.BlockIdField) shouldBe "1"
        actualMap.get(EthereumBlockchainTransactionOffset.TransactionCountField) shouldBe 2L
        actualMap.get(EthereumBlockchainTransactionOffset.OffsetField) shouldBe "3"
      }
    }
  }
}
