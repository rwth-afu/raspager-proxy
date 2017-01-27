# raspager-proxy
A proxy service implementation to bridge a connection to a DAPNET master server with a connection to a raspager server.
All messages received from the frontend server are forwarded to the backend server and vice versa.

## How to build
This project uses maven to build. You can either build it with a maven-capable IDE or from the command line via `mvn package` if you have maven installed.

This will create two jar files: _raspager-proxy-version.jar_ and _raspager-proxy-version-jar-with-dependencies.jar_. The first
file just contains the program code and all dependencies must be present in the classpath when starting. The second file contains
all required dependencies and it can be used for starting the service right away.

## Configuration and running the service
The proxy service expects a configuration file _RaspagerProxy.properties_ with the following contents:

```
frontend.key=authKeyToUse
frontend.host=dapnet-core-server
frontend.port=43434
backend.host=raspager-server
backend.port=1337
```

Then start the program by executing `java -jar raspager-proxy-version.jar`. Note that you must choose the proper file when starting, depending on whether you need the dependencies or not.

## License
This project is licensed under the GNU GPLv3. See [License](LICENSE.txt) for details.