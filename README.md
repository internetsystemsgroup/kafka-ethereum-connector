# Kafka Ethereum Connector

This connector reads transactions from the Ethereum blockchain and writes them to Kafka.

## Building

    >mvn clean package
        
## Running the connector

1. Install Kafka

2. Get an API key to the Ethereum service at www.infura.com

The following instructions assume the following directory structure:

    project-folder/
      |
      |---  kafka_2.12-1.1.0/
      |         |- bin/
      |         |- ...
      |---  kafka-ethereum-connector/
  
    >cd ../kafka_2.12-1.1.0  

2. Start Zookeeper

   >bin/zookeeper-server-start.sh config/zookeeper.properties
   
3. Start Kafka

   >bin/kafka-server-start.sh config/server.properties
   
4. Create a topic

   >bin/kafka-topics.sh --create     --zookeeper localhost:2181     --replication-factor 1     --partitions 1     --topic connect-test
   
5. List Topics

   >bin/kafka-topics.sh --zookeeper localhost:2181 --describe
   
6. Setup a subscriber

   >bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic connect-test --from-beginning
   
7. Start the connector

   >cd ../kafka-ethereum-connector
   >../kafka_2.12-1.1.0/bin/connect-standalone.sh config/connect-standalone.properties config/connect-ethereum-source.properties 
   
8. Verify that transactions are being printed in the subscriber (See step 6. above)   
