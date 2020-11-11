# kafka-examples
Diverse examples around Kafka

## Setup

### k8s & Strimzi
All the examples use k8s and the [Strimzi](https://strimzi.io/) Kafka operator. For a k8s cluster [kind](https://kind.sigs.k8s.io/) is used.

### Strimzi
Setup of Strimzi basically follows the [quick start guide](https://strimzi.io/quickstarts/) with only a little modification.\
This modification concerns the exposure of the Kafka Broker outside of the cluster mainly for testing purposes.\
To expose the broker an additional listener on a `NodePort` needs to be configured as described [here](https://strimzi.io/docs/operators/master/using.html#proc-accessing-kafka-using-nodeports-str).

Therefor instead of provisioning the `kafka-persistent-single.yaml` manifest directly from the strimzi repository, the files needs to downloaded for adding the additional listener:
```yaml
apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: my-cluster
spec:
  kafka:
    version: 2.5.0
    replicas: 1
    listeners:
      plain: {}
      tls: {}
      external:
        type: nodeport
        tls: false
...
```
Note the new listener of type `external`. Adding the listener results in a new service after applying:
```sh
$ kubectl get services -n kafka
NAME                                  TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)                      AGE
my-cluster-kafka-external-bootstrap   NodePort    10.101.173.116   <none>        9094:31147/TCP               20h
```
Note the service `my-cluster-kafka-external-bootstrap`.\
The port `31147` in this case is no accessible on any node ip. The following command can be used to get these ips:
```sh
kubectl get nodes -o jsonpath={.items[*].status.addresses[].address}
172.18.0.2 172.18.0.3
```

