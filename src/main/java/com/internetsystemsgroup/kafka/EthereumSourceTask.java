/*
    MIT License
    Copyright 2018 Internet Systems Group, Inc.

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
    associated documentation files (the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute,
    sublicense, and/or sell copies of the Software, and to permit persons to whom the Software
    is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or
    substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
    BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
    NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.internetsystemsgroup.kafka;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.internetsystemsgroup.ethereum.EthTransaction;
import com.internetsystemsgroup.ethereum.EthereumTransaction;
import com.internetsystemsgroup.ethereum.Web3jAdapter;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import scala.None;
import scala.Option;
import scala.Some;

/**
 * EthereumSourceTask reads transactions from an Ethereum server using the web3j API
 */
public class EthereumSourceTask extends SourceTask {
    private static final Logger log = LoggerFactory.getLogger(EthereumSourceTask.class);
    private static final String ENDPOINT_FIELD = "endpoint";
    private  static final String BLOCK_FIELD = "block";
    private  static final String TXN_COUNT_FIELD = "size";
    private  static final String TXN_OFFSET_FIELD = "offset";

    private static final Schema VALUE_SCHEMA = Schema.STRING_SCHEMA;

    private String endpoint;

    private ConcurrentLinkedQueue<EthBlock.Block> queue;

    private String topic = null;
    private int batchSize = EthereumSourceConnector.DEFAULT_TASK_BATCH_SIZE;

    private BigInteger startingBlock;
    private BigInteger startingOffset;

    private Web3j web3j;


    @Override
    public String version() {
        return new EthereumSourceConnector().version();
    }

    @Override
    public void start(Map<String, String> props) {
        log.info("Starting EthereumSourceTask");
        endpoint = props.get(EthereumSourceConnector.ENDPOINT_CONFIG);
        topic = props.get(EthereumSourceConnector.TOPIC_CONFIG);
        batchSize = Integer.parseInt(props.get(EthereumSourceConnector.TASK_BATCH_SIZE_CONFIG));

        // configuration is validated in the Connector class
        log.info("Connecting to Ethereum node:" + endpoint);
        web3j = Web3j.build(new HttpService(endpoint));

        queue = new ConcurrentLinkedQueue<>();

        //TODO determine last processed block and transaction
        // Continue from last offset
        Map<String, Object> offset = context.offsetStorageReader().offset(offsetKey(endpoint));
        if (offset != null) {
            BigInteger lastRecordedBlock = BigInteger.valueOf((long) offset.get(BLOCK_FIELD));
            BigInteger lastRecordedSize = BigInteger.valueOf((long) offset.get(TXN_COUNT_FIELD));
            BigInteger lastRecordedOffset = BigInteger.valueOf((long) offset.get(TXN_OFFSET_FIELD));

            startingBlock = lastRecordedBlock;
            startingOffset = lastRecordedOffset.add(BigInteger.ONE);
            if (startingOffset.longValue() == lastRecordedSize.longValue()) {
                startingBlock = startingBlock.add(BigInteger.ONE);
                startingOffset = BigInteger.ZERO;
            }
        } else {
            startingBlock = BigInteger.valueOf(46147);  // The 1st block that contains a transaction
            startingOffset = BigInteger.ZERO;
        }

        web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(DefaultBlockParameter.valueOf(startingBlock), true)
                .subscribe(this::writeBlockToQueue);
    }

    private void writeBlockToQueue(EthBlock response)
    {
        EthBlock.Block block = response.getResult();
        log.info("Writing block to queue: " + block.getNumber());

        queue.add(block);
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {
        ArrayList<SourceRecord> records = new ArrayList<>();

        log.info("Polling EthereumSourceTask");


        while (true) {
            if (queue.isEmpty()) {
                log.info("Queue is empty, yielding ...");
                // yield
                Thread.sleep(1000);
            } else {
                EthBlock.Block block = queue.remove();
                List<EthBlock.TransactionResult> transactions = block.getTransactions();
                log.info("Writing block to message array. block=" + block.getNumber() + " size=" + transactions.size());
                if (transactions.size() == 0) {
                    records.add(new SourceRecord(
                            offsetKey(endpoint),
                            offsetValue(block.getNumber(), transactions.size(), BigInteger.ZERO),
                            topic,
                            null,
                            null,
                            null,
                            VALUE_SCHEMA,
                            EthereumTransaction.NONE().asAvroJson(),
                            System.currentTimeMillis()
                    ));
                } else {
                    for (EthBlock.TransactionResult tx : transactions) {
                        EthBlock.TransactionObject txObj = (EthBlock.TransactionObject) tx;
                        log.info("this tran index = " + txObj.getTransactionIndex().longValue() + " vs startingOffset = " + startingOffset.longValue());
                        if (txObj.getTransactionIndex().longValue() < startingOffset.longValue())
                            continue;
                        EthereumTransaction etx = Web3jAdapter.web3jTransaction2EthereumTransaction(txObj);

                        log.info("Writing transaction to message array. block=" + txObj.getBlockNumber() + " size=" + transactions.size() + " offset=" + txObj.getTransactionIndex());
                        records.add(new SourceRecord(
                                offsetKey(endpoint),
                                offsetValue(txObj.getBlockNumber(), transactions.size(), txObj.getTransactionIndex()),
                                topic,
                                null,
                                null,
                                null,
                                VALUE_SCHEMA,
                                etx.asAvroJson(),
                                System.currentTimeMillis()
                        ));
                    }
                }
                startingOffset = BigInteger.ZERO;
                if (records.size() >= batchSize) {
                    log.info("Returning from poll() with " + records.size() + " records");
                    return records;
                }
            }
        }
    }

    @Override
    public void stop() {
        log.trace("Stopping EthereumSourceTask");
        synchronized (this) {
            this.notify();
        }
    }

    private Map<String, String> offsetKey(String filename) {
        return Collections.singletonMap(ENDPOINT_FIELD, filename);
    }

    private Map<String, Long> offsetValue(BigInteger block, long transactionsInBlock, BigInteger transactionOffset) {
        return new HashMap<String, Long>() {{
           put(BLOCK_FIELD, block.longValue());
           put(TXN_COUNT_FIELD, transactionsInBlock);
           put(TXN_OFFSET_FIELD, transactionOffset.longValue());
        }};
    }

    private String logFilename() {
        return endpoint == null ? "null" : endpoint;
    }

}