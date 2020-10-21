# ktor-socks
SOCKS (versions [4](http://ftp.icm.edu.pl/packages/socks/socks4/SOCKS4.protocol), [4a](https://www.openssh.com/txt/socks4a.protocol), [5](https://tools.ietf.org/html/rfc1928)) proxy server implementation using ktor's suspending sockets API

This is not meant to be fully compliant with the specifications in every detail at this stage, but rather a working implementation that covers the practical use-cases for usual SOCKS proxy clients (like browsers).

Missing details for full compliancy:
- GSSAPI authenticaion method (only Username/Password authentication is implemented at this stage)
- API support for USERID field in SOCKS4 requests
- API support for encapsulating client traffic (based on authentication method)
- BIND command
- UDP_ASSOCIATE command and UDP based clients
- Put specified timeouts into place

## Example
```kotlin
fun main() {
  runBlocking(Dispatchers.IO) {
    socksServer {
      allowSOCKS4 = false
      port = 8080
      addAuthenticationMethod(...)
    }.start()
  }
}
```

Calling `start` launches a coroutine that listens on a server socket, accepting clients and launching child-coroutines to negotiate the handshake and relay traffic between client and host. It will return immediately, but can be wrapped in `runBlocking` to block the thread while the server is running.

## Configuration
Configuration of the socks server can be done with the `SOCKSConfigBuilder` block parameter of the `socksServer` functions.

Due to lack of authentication possibilities, the `allowSOCKS4` property can be used to block SOCKS4 clients. In that case, the server will respond with the result code `91` (request rejected or failed) to all SOCKS4 requests.

To specify the address the server should bind to, either assign a `NetworkAddress` to the field `networkAddress`, or assign values to `hostname` and `port`. If left unspecified, the server will bind to `0.0.0.0:1080`.

Added authentication methods will be considered in the order they were added, i.e. the server will pick the first authentication method in the list that's supported by the client, which allows for prioritization.

If no authentication methods are added, the builder will default to `NoAuthentication`.

## Implemented authentication methods
#### `NoAuthentication`
Doesn't have any subnegotiation and allows any client.

#### `UsernamePasswordAuthentication`
Abstract class that handles communication with the client. Create a subclass and implement the function `verify(username: String, password: String): Boolean` to verify clients agains a database of users for example.

## Potential feature additions
Connection callbacks could be used to implement some sort of blacklist/whitelist system for target hosts, which could go hand in hand with a `UsernamePasswordAuthentication` subclass which manages users of different usergroups with different sorts of privileges.
Abstract class that handles communication with the client. Create a subclass and implement the function `verify(username: String, password: String): Boolean` to verify clients agains a database of users for example.
