# CamelBee Quarkus AllComponents Project

## Introduction

The CamelBee Quarkus AllComponents Project serves as an illustrative demonstration of the camelbee-quarkus-core library's capabilities. 
This project showcases how the library seamlessly integrates with CamelBee WebGL application (https://www.camelbee.io), offering an immersive visualization experience.

## Running the Application with Maven

To execute this application, you must first ensure that you have successfully installed the camelbee-quarkus-core library from the camelbee/camelbee-core/camelbee-quarkus-core project. 
Once the library is in place, follow these steps to run the application:

`mvn clean compile quarkus:dev`

## Visualizing in the CamelBee WebGL Application

After launching the Java process using the previous command, open a web browser and navigate to https://www.camelbee.io/webgl/index.jsp.
Within the WebGL application interface, locate the text field that prompts for a URL. 
Replace the default value, "https://www.camelbee.io," with "http://localhost:8080" to establish a connection between the application and your locally running instance. 
This allows you to visualize and interact with the CamelBee Quarkus AllComponents Project in real-time.

Please ensure that you have the necessary prerequisites and dependencies in place before running the application for a smooth and seamless experience.