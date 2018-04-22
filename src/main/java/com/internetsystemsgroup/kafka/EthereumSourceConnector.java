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

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.common.utils.AppInfoParser;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EthereumSourceConnector extends SourceConnector {
    public static final String TOPIC_CONFIG = "topic";
    public static final String ENDPOINT_CONFIG = "endpoint";
    public static final String TASK_BATCH_SIZE_CONFIG = "batch.size";

    public static final int DEFAULT_TASK_BATCH_SIZE = 2000;

    private static final ConfigDef CONFIG_DEF = new ConfigDef()
            .define(ENDPOINT_CONFIG, Type.STRING, null, Importance.HIGH, "Ethereum server endpoint must be specified")
            .define(TOPIC_CONFIG, Type.LIST, Importance.HIGH, "The topic to publish data to")
            .define(TASK_BATCH_SIZE_CONFIG, Type.INT, DEFAULT_TASK_BATCH_SIZE, Importance.LOW,
                    "The maximum number of records the Source task can read from file one time");

    private String endpoint;
    private String topic;
    private int batchSize;

    @Override
    public String version() {
        return AppInfoParser.getVersion();
    }

    @Override
    public void start(Map<String, String> props) {
        AbstractConfig parsedConfig = new AbstractConfig(CONFIG_DEF, props);
        endpoint = parsedConfig.getString(ENDPOINT_CONFIG);
        List<String> topics = parsedConfig.getList(TOPIC_CONFIG);
        if (topics.size() != 1) {
            throw new ConfigException("'topic' in EthereumSourceConnector configuration requires definition of a single topic");
        }
        topic = topics.get(0);
        batchSize = parsedConfig.getInt(TASK_BATCH_SIZE_CONFIG);

        if (endpoint == null || endpoint.isEmpty()) {
            throw new ConfigException("Specify endpoint");
        } else if (endpoint.contains(".ipc")) {
            throw new ConfigException(".ipc endpoint not supported");
        }
    }

    @Override
    public Class<? extends Task> taskClass() {
        return EthereumSourceTask.class;
    }

    @Override
    public List<Map<String, String>> taskConfigs(int maxTasks) {
        ArrayList<Map<String, String>> configs = new ArrayList<>();
        // Only one input stream makes sense.
        Map<String, String> config = new HashMap<>();
        if (endpoint != null)
            config.put(ENDPOINT_CONFIG, endpoint);
        config.put(TOPIC_CONFIG, topic);
        config.put(TASK_BATCH_SIZE_CONFIG, String.valueOf(batchSize));
        configs.add(config);
        return configs;
    }

    @Override
    public void stop() {
        // TODO
    }

    @Override
    public ConfigDef config() {
        return CONFIG_DEF;
    }
}