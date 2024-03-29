# CamelBee - Apache Camel Library for Microservices Monitoring and Debugging in the CamelBee WebGL Application

CamelBee java core libraries are engineered to extract the architecture of Camel Routes, pinpoint endpoints, and map out the interconnections among them
to visualize the topology within either the CamelBee WebGL application (https://www.camelbee.io) or through the local Docker version (accessed at http://localhost:8083 by executing 'docker run -d -p 8083:80 camelbee/webgl').

[![ Watch the Video](images/startscene.png)](https://www.youtube.com/watch?v=W29ilyAsXlM)

## Features

### Route Visualization
- Effortlessly visualize complex Camel routes and their interconnections for a better understanding of your microservice architecture.
- Gain a clear overview of message routing and flow paths within your application.

[![ Watch the Video](images/debuggerscene.png)](https://www.youtube.com/watch?v=W29ilyAsXlM)

### Message Tracing
- Trace messages as they traverse through Camel routes, enabling real-time debugging and issue identification.
- Detect bottlenecks, errors, or unexpected behavior in your message processing.

[![ Watch the Video](images/messages.png)](https://www.youtube.com/watch?v=W29ilyAsXlM)
 
### Debugging and Replay
- Debug Camel routes interactively by inspecting message contents, and analyzing route behavior.
- Replay debug sessions to reproduce and investigate issues.
- Navigate through the debugging session's timeline, moving back and forth, to thoroughly analyze the process flow.

[![ Watch the Video](images/replay.png)](https://www.youtube.com/watch?v=W29ilyAsXlM)

### Filtering
- Seamlessly filter messages to display only the relevant ones.

[![ Watch the Video](images/filter.png)](https://www.youtube.com/watch?v=W29ilyAsXlM)

### Trigger Consumer Endpoints
- Initiate any kind of consumer endpoints direclty from the 3D environment and track message traffic.

[![ Watch the Video](images/routecaller.png)](https://www.youtube.com/watch?v=W29ilyAsXlM)

### Real-time Monitoring
- Monitor Camel microservices with essential metrics and variables, ensuring the health and performance of your application.
- Retrieve comprehensive metrics data to keep your microservices running smoothly.
- Concurrently invoke consumer endpoints to conduct a stress test.

[![ Watch the Video](images/metrics.png)](https://www.youtube.com/watch?v=W29ilyAsXlM)

  
---

## Project Structure

The project is structured as follows:

```shell
camelbee/
|-- camelbee-core/
| |-- camelbee-quarkus-core/
| | |-- README.md
| | |-- ...
| |-- camelbee-springboot-core/
| | |-- README.md
| | |-- ...
|-- camelbee-examples/
| |-- allcomponent-quarkus-sample/
| | |-- README.md
| | |-- ...
| |-- allcomponent-springboot-sample/
| | |-- README.md
| | |-- ...
|-- README.md
```


- `camelbee-core`: Contains the core modules for CamelBee.to integrate with either the CamelBee WebGL application (https://www.camelbee.io) or through the local Docker version (accessed at http://localhost:8083 by executing 'docker run -d -p 8083:80 camelbee/webgl').
  - `camelbee-quarkus-core`: Quarkus-specific core module.
  - `camelbee-springboot-core`: Spring Boot-specific core module.

- `camelbee-examples`: Contains example projects demonstrating the usage of CamelBee.
  - `allcomponent-quarkus-sample`:  Quarkus example project which uses camelbee-quarkus-core library.
  - `allcomponent-springboot-sample`: Spring Boot example project which uses camelbee-springboot-core library.

Each subproject have its own README file for detailed information specific to that project.

## Getting Started

To get started with any of the subprojects, follow the instructions in their respective README files located in their subdirectories. 
These README files provide step-by-step instructions on how to build, configure, and run each project.


## License

This project is licensed under the the Apache License, Version 2.0. Feel free to use, modify, and distribute it as per the license terms.

For specific license information for individual subprojects, refer to their respective README files.
