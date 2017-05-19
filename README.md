# DAPNET Proxy
A proxy service implementation to bridge a connection to a DAPNET master server with a connection to a legacy raspager server.
All messages received from the frontend server are forwarded to the backend server and vice versa. Use this proxy only for
legacy raspager devices that do not support connecting to the DAPNET master server natively (like C9000).

## How to build
This project uses maven to build. You can either build it with a maven-capable IDE or from the command line via `mvn package` if you have maven installed.

This will create two jar files: _dapnet-proxy-version.jar_ and _dapnet-proxy-version-jar-with-dependencies.jar_. The first
file contains just the program and all dependencies must be present in the classpath when starting. The second file contains
all required dependencies and it can be used for starting the service right away.

## Configuration and running the service
The proxy service expects a configuration file (let's call it `proxy.properties`) with the following contents:

```
# Name of this connection profile
profileName = Example Profile
# Time to sleep in milliseconds before attempting a reconnect. Set to 0 to disable automatic reconnecting.
reconnectSleepTime = 5000
# Frontend (DAPNET) authentication name
frontend.name = transmitterName
# Frontend authentication key
frontend.key = transmitterKey
# Frontend host name or IP address
frontend.host = dapnet-core-server
# Frontend port number
frontend.port = 43434
# Backend (raspager) host name or IP address
backend.host = raspager-server
# Backend port number
backend.port = 1337
# Timeout in milliseconds for keep alive requests. Set to 0 to disable.
backend.timeout = 30000
```

Then start the program by executing `java -jar dapnet-proxy-version.jar proxy.properties`. Note that you must choose the proper file when starting, depending on whether you need the dependencies or not.
It is possible to specify multiple configuration files to manage multiple proxy services with a single program instance.

## REST API
The DAPNET proxy features an optional REST API to query the current status of all registered connections via the path `/status`. It is disabled by default. In order to enable it, the system property
`dapnet.proxy.rest.port` containig the port number must be passed as an JVM option during startup. Usage example:

```
java -Ddapnet.proxy.rest.port=8080 -jar dapnet-proxy-version.jar proxy.properties
```

A GET on `/status` returns a JSON array containing the following information (one JSON object per loaded proxy properties file):

```
[
  {
    "profileName": "Server1",
    "lastUpdate": "2017-05-18T13:17:36.933Z",
    "connectedSince": null,
    "state": "CONNECTING"
  }
]
```

The field `profileName` contains the profile name taken from the properties file, `lastUpdate` contains the timestamp of the last status change and `connectedSince` contains the timestamp when
the connection has been established (`null` if not connected). The `state` field may contain `CONNECTING` when a connection attempt is pending, `ONLINE` if a connection has been established or
`OFFLINE` if a connection is permanently closed (no reconnect configured).

## License
This project is licensed under the GNU GPLv3. See [License](LICENSE.txt) for details.
