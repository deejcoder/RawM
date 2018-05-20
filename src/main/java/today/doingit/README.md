## Abstraction Layers
There are different abstraction layers: Server & Application.

### Server
The server abstraction layer is represented by the folder Server. This contains all server reloaded logic: sending, accepting connections etc.

### Application
The application abstraction layer deals with the application logic. This includes request types (what to do when a particular request is made by the client?) and data structures.

### Note
JSON is used to parse data between the client and the server. It is also used between the RequestHandler and ResponseHandler for dealing with request forwarding and response sending. When I say request forwarding, that is passing requests onto the ideal request type. Likewise, response sending is simply sending the response provided by the RequestHandler to the given scope of clients.