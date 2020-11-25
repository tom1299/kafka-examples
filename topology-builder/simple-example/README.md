# Using the Kafka topology builder
A simple example using the [Kafka topology builder](https://github.com/kafka-ops/kafka-topology-builder) to provision a kafka cluster.

Running:

```sh
$ export BOOTSTRAP_SERVERS="172.18.0.2:30195"
$ sed -i 's/${BOOTSTRAP_SERVERS}/'$BOOTSTRAP_SERVERS'/g' topology-builder.properties
$ docker run --net=host -v ${PWD}:/example \
    purbon/kafka-topology-builder:1.0.0-rc.2 \
    kafka-topology-builder.sh \
    --clientConfig /example/topology-builder.properties \
    --topology /example/topology.yaml \
    --allowDelete

log4j:WARN No appenders could be found for logger (org.apache.kafka.clients.admin.AdminClientConfig).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
List of Topics:
kafka-examples.order-example.incoming-orders
kafka-examples.order-example.finished-orders
List of ACLs: 
Kafka Topology updated
```
The option `--net=host` is not necessary when the bootstrap servers are reachable from within the image. Beware that the `--allow-delete` option will delete any topics not defined in the topology file.\
Configuration and file format are described in detail [here](https://kafka-topology-builder.readthedocs.io/en/latest/config-values.html) and [here](https://kafka-topology-builder.readthedocs.io/en/latest/the-descriptor-files.html).