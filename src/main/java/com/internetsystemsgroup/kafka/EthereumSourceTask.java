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

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

/**
 * EthereumSourceTask reads transactions from an Ethereum server using the web3j API
 */
public class EthereumSourceTask extends SourceTask {
    private static final Logger log = LoggerFactory.getLogger(EthereumSourceTask.class);
    public static final String ENDPOINT_FIELD = "endpoint";
    public  static final String POSITION_FIELD = "position";
    private static final Schema VALUE_SCHEMA = Schema.STRING_SCHEMA;

    private String endPoint;
    private PipedInputStream stream;
    private PipedOutputStream outputStream;
    private PrintWriter pw;

    private BufferedReader reader = null;

    private char[] buffer = new char[1024];
    private int offset = 0;
    private String topic = null;
    private int batchSize = EthereumSourceConnector.DEFAULT_TASK_BATCH_SIZE;

    private Long streamOffset;

    private Long startingBlock;

    private Web3j web3j;


    @Override
    public String version() {
        return new EthereumSourceConnector().version();
    }

    @Override
    public void start(Map<String, String> props) {
        endPoint = props.get(EthereumSourceConnector.ENDPOINT_CONFIG);
        // Missing topic or parsing error is not possible because we've parsed the config in the
        // Connector
        topic = props.get(EthereumSourceConnector.TOPIC_CONFIG);
        batchSize = Integer.parseInt(props.get(EthereumSourceConnector.TASK_BATCH_SIZE_CONFIG));

        try {
            initializeWeb3j();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set up the pipe
        stream = new PipedInputStream();
        try {
            outputStream = new PipedOutputStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pw = new PrintWriter(outputStream);

        // Setup the reader
        reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));

        //TODO determine last processed block and transaction
        startingBlock = 5470634L;

        web3j.catchUpToLatestAndSubscribeToNewBlocksObservable(DefaultBlockParameter.valueOf(BigInteger.valueOf(startingBlock)), true)
                .subscribe(block -> {
                    writeBlockToPipe(block);
                });
    }

    /**
     * Persist blocks and transactions.
     *
     * @param block
     *            the block
     */
    private void writeBlockToPipe(EthBlock block)
    {
        log.info("Writing block: " + block.getBlock().getNumber());

        for (EthBlock.TransactionResult tx : block.getResult().getTransactions())
        {
            EthBlock.TransactionObject txObj = (EthBlock.TransactionObject) tx;

            log.info("Writing transaction: " + txObj.getTransactionIndex());
            pw.println(
                    txObj.getHash() + "," +
                    txObj.getFrom() + "," +
                    txObj.getTo() + "," +
                    txObj.getValue() + "," +
                    txObj.getGasPrice() + "," +
                    txObj.getGas() + "," +
                    txObj.getInput() + "," +
                    txObj.getCreates() + "," +
                    txObj.getRaw()
            );
        }

        pw.flush();  // TODO is this needed
    }

    @Override
    public List<SourceRecord> poll() throws InterruptedException {

        // TODO is there a more concise way to read this stream?  The Kafka sample code suggests that readLine() will not work correctly

        //read as much as we can through the pipe
        try {
            final BufferedReader readerCopy;
            synchronized (this) {
                readerCopy = reader;
            }
            if (readerCopy == null)
                return null;

            ArrayList<SourceRecord> records = null;

            int nread = 0;
            while (readerCopy.ready()) {
                nread = readerCopy.read(buffer, offset, buffer.length - offset);
                log.trace("Read {} bytes from {}", nread, logFilename());

                if (nread > 0) {
                    offset += nread;
                    if (offset == buffer.length) {
                        char[] newbuf = new char[buffer.length * 2];
                        System.arraycopy(buffer, 0, newbuf, 0, buffer.length);
                        buffer = newbuf;
                    }

                    String line;
                    do {
                        line = extractLine();
                        if (line != null) {
                            log.trace("Read a line from {}", logFilename());
                            if (records == null)
                                records = new ArrayList<>();
                            records.add(new SourceRecord(offsetKey(endPoint), offsetValue(streamOffset), topic, null,
                                    null, null, VALUE_SCHEMA, line, System.currentTimeMillis()));

                            if (records.size() >= batchSize) {
                                return records;
                            }
                        }
                    } while (line != null);
                }
            }

            if (nread <= 0)
                synchronized (this) {
                    this.wait(1000);
                }

            return records;
        } catch (IOException e) {
            // Underlying stream was killed, probably as a result of calling stop. Allow to return
            // null, and driving thread will handle any shutdown if necessary.
        }
        return null;
    }

    private String extractLine() {
        int until = -1, newStart = -1;
        for (int i = 0; i < offset; i++) {
            if (buffer[i] == '\n') {
                until = i;
                newStart = i + 1;
                break;
            } else if (buffer[i] == '\r') {
                // We need to check for \r\n, so we must skip this if we can't check the next char
                if (i + 1 >= offset)
                    return null;

                until = i;
                newStart = (buffer[i + 1] == '\n') ? i + 2 : i + 1;
                break;
            }
        }

        if (until != -1) {
            String result = new String(buffer, 0, until);
            System.arraycopy(buffer, newStart, buffer, 0, buffer.length - newStart);
            offset = offset - newStart;
            if (streamOffset != null)
                streamOffset += newStart;
            return result;
        } else {
            return null;
        }
    }

    @Override
    public void stop() {
        log.trace("Stopping");
        pw.close();
        // TODO
//        synchronized (this) {
//            try {
//                if (stream != null && stream != System.in) {
//                    stream.close();
//                    log.trace("Closed input stream");
//                }
//            } catch (IOException e) {
//                log.error("Failed to close EthereumSourceTask stream: ", e);
//            }
//            this.notify();
//        }
    }

    private Map<String, String> offsetKey(String filename) {
        return Collections.singletonMap(ENDPOINT_FIELD, filename);
    }

    private Map<String, Long> offsetValue(Long pos) {
        return Collections.singletonMap(POSITION_FIELD, pos);
    }

    private String logFilename() {
        return endPoint == null ? "null" : endPoint;
    }

    /**
     * Initialize web3j.
     */
    private void initializeWeb3j() throws Exception {

        if (endPoint == null || endPoint.isEmpty()) {
            log.error("Specify endpoint");
            throw new Exception("Specify endpoint");
        } else if (endPoint.contains(".ipc")) {
            throw new Exception(".ipc endpoint not supported");
        } else {
            log.info("Connecting via Endpoint - " + endPoint);
            web3j = Web3j.build(new HttpService(endPoint));
        }
    }
}