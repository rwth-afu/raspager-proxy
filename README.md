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
The proxy service expects a configuration file _proxy.properties_ with the following contents:

```
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

Then start the program by executing `java -jar dapnet-proxy-version.jar`. Note that you must choose the proper file when starting, depending on whether you need the dependencies or not.

## License
This project is licensed under the GNU GPLv3. See [License](LICENSE.txt) for details.
