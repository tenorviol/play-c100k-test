play-connection-test
====================

Simple [Play Framework](https://www.playframework.com/) application
for testing the upper limit on simultaneous connections.

Testing
-------

This is server is designed to test
how well the Play Framework handles concurrent connections
on a massive scale.

1. Deploy this server on a machine of your choosing.
   AWS can be super convenient.
2. Make a lot of requests.
   I use Apache benchmark for this, `ab`.
   E.g. `ab -c 1000 -n 5000 -r http://<hostname_or_ip>:9000/`.
3. Adjust parameters and/or server code,
   repeat lots of requests.
   For massive concurrency,
   it may be necessary to run `ab` across multiple client hosts,
   and to increase the maximum open file handles for the server process.
4. Formulate results into a pretty graph.
5. Write blog post.
6. A bit late, but profit.

API
---

### GET /

Asynchronously wait 10 seconds,
then return a text document like this:

    Completed in 10012 milliseconds
