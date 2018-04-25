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
          EthereumBlockchainTransactionOffset.BlockIdField -> 1L,
          EthereumBlockchainTransactionOffset.TransactionCountField -> 2L,
          EthereumBlockchainTransactionOffset.OffsetField -> 3L
        )
        val offset = EthereumBlockchainTransactionOffset.parse(map)
        offset.blockId shouldBe new BigInteger("1")
        offset.blockTransactionCount shouldBe 2
        offset.offset shouldBe new BigInteger("3")
      }

      it("should create an object when passed a java.util.Map") {
        val map = new java.util.HashMap[String, Any] {
          put(EthereumBlockchainTransactionOffset.BlockIdField, 1L)
          put(EthereumBlockchainTransactionOffset.TransactionCountField, 2L)
          put(EthereumBlockchainTransactionOffset.OffsetField, 3L)
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
          EthereumBlockchainTransactionOffset.BlockIdField -> new BigInteger("1"),
          EthereumBlockchainTransactionOffset.TransactionCountField -> 2L,
          EthereumBlockchainTransactionOffset.OffsetField -> new BigInteger("3")
        )
        offset.toMap() shouldBe expectedMap
      }
    }

    describe("toJavaMap") {
      it("should return a java.util.Map with the appropriate elements") {
        val actualMap = new EthereumBlockchainTransactionOffset(
          new BigInteger("1"), 2, new BigInteger("3")).toJavaMap()
        actualMap.size() shouldBe 3
        actualMap.get(EthereumBlockchainTransactionOffset.BlockIdField) shouldBe new BigInteger("1")
        actualMap.get(EthereumBlockchainTransactionOffset.TransactionCountField) shouldBe 2L
        actualMap.get(EthereumBlockchainTransactionOffset.OffsetField) shouldBe new BigInteger("3")
      }
    }
  }
}
