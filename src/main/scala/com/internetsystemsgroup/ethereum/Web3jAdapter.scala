package com.internetsystemsgroup.ethereum

import java.io.OutputStream

import com.sksamuel.avro4s.{AvroBinaryOutputStream, AvroOutputStream}
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject

object Web3jAdapter {
  def web3jTransaction2EthTransaction(txObj: TransactionObject): EthTransaction = {
    EthTransaction(
      txObj.getHash,
      txObj.getFrom,
      if (txObj.getTo == null) "" else txObj.getTo,   // TODO need to match to transaction response object
      BigDecimal(txObj.getValue, 2),
      BigDecimal(txObj.getGasPrice, 2),
      BigDecimal(txObj.getGas, 2)
    )
  }

  def getAvroOutputStream(outputStream: OutputStream): AvroBinaryOutputStream[EthTransaction] = {
    AvroOutputStream.binary[EthTransaction](outputStream)
  }
}
