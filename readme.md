play-c100k-server
=================

This simple repo tests the upper limit on concurrent connections
served by a [Play Framework](https://www.playframework.com/) server.
There are two main questions to answer for any concurrency test:

1. How many simultaneous connections can be maintained?
2. How quickly can new connections be acquired?

We use [WebSockets](https://developer.mozilla.org/en-US/docs/Web/API/WebSockets_API) for testing
because they allow us to keep sockets open for an arbitrary length of time,
and they are a part of HTTP,
which we like and leverage extensively across our infrastructure.


Setup
-----


### Setting up the server

Build it.

    # output: target/universal/play-c100k-server-0.1.1-PLAY26.tgz
    ./bin/activator universal:packageZipTarball

A bigger server than your MacBook is needed to get impressive numbers,
so upload to an AWS instance or some such.

    scp target/universal/play-c100k-server-0.1.1-PLAY26.tgz c100k_server:
    ssh c100k_server
    tar xf play-c100k-server-0.1.1-PLAY26.tgz

More stuff to install:

* Java 8
* Telegraf to collect metrics from the server and output to InfluxDB
* InfluxDB for aggregating metrics
* Grafana for viewing metrics

Start the server on port 9000

    screen
    export JAVA_HOME=/path/to/java8
    ./play-c100k-server-0.1.1-PLAY26/bin/play-c100k-server


### Setting up the client

Build it.

    # output: client/target/universal/play-c100k-client-0.1.1-PLAY26.tgz
    ./bin/activator 'project client' universal:packageZipTarball

Upload to all your client servers.
I've been able to get 20k clients per instance.

    scp client/target/universal/play-c100k-client-0.1.1-PLAY26.tgz c100k_client:
    ssh c100k_client
    tar xf play-c100k-client-0.1.1-PLAY26.tgz

Install:

* Java 8
* Telegraf

Start the client

    screen
    export JAVA_HOME=/path/to/java8
    ./play-c100k-client-0.1.1-PLAY26/bin/play-c100k-client
    CTRL-a CTRL-d

Initiate some client connections

    curl localhost:9000/clients --data 'ws=ws://c100k_server:9000/ws/echo&count=5'

This creates 5 WebSocketClient instances connected to the c100k_server echo WebSocket.
All clients send a ping every 30 seconds, and aggregate the latency.
They should remain connected indefinitely, due to the pings.

Experiment 2
------------

![65k open files limit](./images/1._65k_files_limit.png "65k open files limit")

![100k connections](./images/2._100k_connections.png "100k connections")

![200k connections](./images/3._200k_connections.png "200k connections")

![200k 8 hours later](./images/4._200k_14_hours_later.png "200k 8 hours later")
