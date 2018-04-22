package com.internetsystemsgroup.ethereum

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.sksamuel.avro4s.{AvroInputStream, AvroSchema, RecordFormat}
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificDatumWriter
import org.scalatest.{FunSpec, Matchers}
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject

class EthTransactionSpec extends FunSpec with Matchers {

  // Create sample EthTransaction
  private val txObj = new TransactionObject {
    setFrom("0x12345678")
    setHash("0xthisIsAHash")
    setTo("0xtoAddress")
    setValue("0x10000000")
    setGasPrice("0x10000000")
    setGas("0x10000000")
  }

  private val tx = Web3jAdapter.web3jTransaction2EthTransaction(txObj)
  private val schema = AvroSchema[EthTransaction]
  private val format = RecordFormat[EthTransaction]
  private val record = format.to(tx)

  describe("an EthTransaction") {
    it("should have correct schema") {
      schema.toString(true) shouldBe
        """{
          |  "type" : "record",
          |  "name" : "EthTransaction",
          |  "namespace" : "com.internetsystemsgroup.ethereum",
          |  "fields" : [ {
          |    "name" : "hash",
          |    "type" : "string"
          |  }, {
          |    "name" : "from",
          |    "type" : "string"
          |  }, {
          |    "name" : "to",
          |    "type" : "string"
          |  }, {
          |    "name" : "value",
          |    "type" : {
          |      "type" : "bytes",
          |      "logicalType" : "decimal",
          |      "precision" : 8,
          |      "scale" : 2
          |    }
          |  }, {
          |    "name" : "gasPrice",
          |    "type" : {
          |      "type" : "bytes",
          |      "logicalType" : "decimal",
          |      "precision" : 8,
          |      "scale" : 2
          |    }
          |  }, {
          |    "name" : "gas",
          |    "type" : {
          |      "type" : "bytes",
          |      "logicalType" : "decimal",
          |      "precision" : 8,
          |      "scale" : 2
          |    }
          |  } ]
          |}""".stripMargin
      }

    it("should serialize to Avro JSON") {
      val writer = new SpecificDatumWriter[GenericRecord](schema)
      val out = new ByteArrayOutputStream()
      val encoder = EncoderFactory.get.jsonEncoder(schema, out, true)
      writer.write(record, encoder)
      encoder.flush()
      out.close()

      out.toString shouldBe
        s"""{
          |  "hash" : "0xthisIsAHash",
          |  "from" : "0x12345678",
          |  "to" : "0xtoAddress",
          |  "value" : "\\u0010\\u0000\\u0000\\u0000",
          |  "gasPrice" : "\\u0010\\u0000\\u0000\\u0000",
          |  "gas" : "\\u0010\\u0000\\u0000\\u0000"
          |}""".stripMargin
    }

    it("should serialize to and from Avro binary") {
      val writer = new SpecificDatumWriter[GenericRecord](schema)
      val out = new ByteArrayOutputStream()
      val encoder = EncoderFactory.get.binaryEncoder(out, null)
      writer.write(record, encoder)
      encoder.flush()
      out.close()

      val in = new ByteArrayInputStream(out.toByteArray)
      val input = AvroInputStream.binary[EthTransaction](in)
      val result = input.iterator.toSeq.head
      result shouldBe tx
    }

  }

}
