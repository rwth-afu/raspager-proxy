# DAPNET Proxy
The DAPNET Proxy is used to connect a legacy raspager server to the DAPNET. The raspager acts as a server and waits for incoming connections while the DAPNET expects transmitters to connect to a core server. Without the DAPNET Proxy, a raspager could not connect to the DAPNET as both systems expect the other one to open the connection first.

All messages received from the DAPNET are forwarded to the raspager and vice versa. Use the DAPNET Proxy only for legacy raspager devices that do not support connecting to the DAPNET natively (like C9000).

## How to build
This project uses maven to build. You can either build it with a maven-capable IDE or from the command line via `mvn package` if you have maven installed in your path.

This will create two jar files: _dapnet-proxy-version.jar_ and _dapnet-proxy-version-jar-with-dependencies.jar_. The first
file contains just the program and all dependencies must be present in the classpath when starting. The second file contains
all required dependencies and it can be used for starting the service right away.

## Configuration and running the service
The proxy service expects a configuration file (let's call it `conn1.properties`) with the following contents:

```
# Name of this connection profile
profile.mame = ExampleProfile
# Time to sleep in seconds before attempting a reconnect. Set to 0 to disable automatic reconnecting.
profile.reconnectDelay = 5

# DAPNET hostname or IP address
dapnet.hostname = dapnet-core-server
# DAPNET port number
dapnet.port = 43434
# DAPNET authentication name
dapnet.auth.name = transmitterName
# DAPNET authentication key
dapnet.auth.key = transmitterKey

# Backend (raspager) host name or IP address
transmitter.hostname = raspager-server
# Backend port number
transmitter.port = 1337
# Timeout in seconds for keep alive requests. Set to 0 to disable.
transmitter.timeout = 30
```

Then start the program by executing `java -jar dapnet-proxy-version.jar conn1.properties`. Note that you must choose the proper file when starting, depending on whether you need the dependencies or not. It is possible to specify multiple configuration files to manage multiple proxy connections with a single program instance.

## REST API
The DAPNET proxy features an optional REST API to query the current status of all registered connections via the path `/status`. It is disabled by default. In order to enable it, the system property `dapnet.proxy.rest.port` containing the port number must be passed as an JVM option during startup. Usage example:

```
java -Ddapnet.proxy.rest.port=8080 -jar dapnet-proxy-version.jar conn1.properties
```

A GET on `/status` returns a JSON array containing the following information (one JSON object per loaded connection profile file):

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

The field `profileName` contains the profile name taken from the properties file, `lastUpdate` contains the timestamp of the last status change and `connectedSince` contains the timestamp when the connection has been established (`null` if not connected). The `state` field may contain `CONNECTING` when a connection attempt is pending, `ONLINE` if a connection has been established or `OFFLINE` if a connection is permanently closed (no reconnect configured).

It is also possible to query the status of a single connection by adding the connection profile name to the path, e.g. `/status/Server1`. Note that the profile name is case-sensitive.

## License
This project is licensed under the GNU GPLv3. See [License](LICENSE.txt) for details.
