CoreMedia Livecontext
=====================

This module contains the CoreMedia Livecontext extension.


Tests
-----

Some Tests require a REST interface which provides required data (e.g. from an eCommerce server). For tests this REST
interface is mocked by the [Hoverfly] framework which is able to record and replay data from a web resource to JSON files.
To do so [Hoverfly] starts a proxy server which intercepts outgoing network communication. For each request [Hoverfly] 
checks if this request and its response have already been recorded on a tape. In this case the recorded answer will be 
returned to the client else the request will be forwarded to the recipient and the answer will be recorded for future use.

The default mode of [Hoverfly] is read-write, which means that non recorded requests will be forwarded to the original
server and the response will be recorded to the appropriate tape. Recorded requests will be replayed directly from the
tape.

The [Hoverfly] JUnit 5 extension is responsible for recording and replaying. It has to be added to each test class using
it. 

If you want to decide per test run, if the tests shall run against the recorded tapes or the real test systems, you 
should use 'com.coremedia.blueprint.lc.tes.SwitchableHoverflyExtension', a simple wrapper around the original [Hoverfly] 
junit extension. It enables you to deactivate the extension and run the tests against a real system.
This can be done via the system property 'hoverfly.ignoreTapes=true' at the command line.

[Hoverfly]: <https://hoverfly.readthedocs.io/en/latest/> "Hoverfly"

###Documentation

See the "CoreMedia Blueprint - Functionality for Websites" chapter of the CoreMedia Developer Manual.
