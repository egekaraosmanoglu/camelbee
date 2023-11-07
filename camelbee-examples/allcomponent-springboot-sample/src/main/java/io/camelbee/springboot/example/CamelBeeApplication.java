package io.camelbee.springboot.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * CamelBee Rest microservice example.
 *
 */
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan(
    basePackages = {"org.camelbee", "io.camelbee.springboot.example"})
public class CamelBeeApplication {

  /**
   * A main method to start this application.
   */
  public static void main(String[] args) {
    SpringApplication.run(CamelBeeApplication.class, args);
  }

}
