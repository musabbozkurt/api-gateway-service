<!-- TABLE OF CONTENTS -->
<details open="open">
  <summary><h2 style="display: inline-block">Table of Contents</h2></summary>
  <ol>
    <li>
      <a href="#summary">Summary</a>
      <ul>
        <li><a href="#services">Services</a></li>
        <li><a href="#features">Features</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#references">References</a></li>
  </ol>
</details>

---

<!-- SUMMARY -->

## Summary

- `api-gateway-service` project established by combination of the following services and features.

### Services

   ```
    * api-gateway
    * payment-service
    * student-service
    * swagger-application
    * openai-service
   ```

### Features

   ```
    * Spring Cloud Gateway
    * SpringDoc Swagger
    * Event Driven Architecture with RabbitMQ
    * micrometer-tracing dependencies to track the logs
    * Postman collection to test by using Postman
    * Keycloak integration is completed under the payment-service
    * Feign Client secure call with Keycloak integration is completed under the student-service
    * MDC was added to improve logging between microservices
    * Google reCAPTCHA was added to secure endpoint calls
    * HCaptcha repo migration is completed under the student-service
    * openai-service repo migration is completed
   ```

---

<!-- GETTING STARTED -->

## Getting Started

- To get a local copy up and running please follow these steps.

---

### Prerequisites

- Followings should be installed and links for how to install them.
  ####
    * Java 21 or higher [How to install Java](https://java.com/en/download/help/download_options.html)
        * Set `JAVA_HOME` to 21 -> `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`
    * Maven [How to install Maven](https://maven.apache.org/install.html)
    * Docker [How to install Docker](https://docs.docker.com/get-docker)
  ####
    * Create new secret key and replace all `YOUR_API_KEY_HERE` in code with this new
      key -> https://platform.openai.com/account/api-keys
  ####
    * Google reCAPTCHA installation (OPTIONAL)
        * Log in to the https://www.google.com/recaptcha/admin/create
        * Fill the necessary fields.
            * [This url](https://examples.javacodegeeks.com/wp-content/uploads/2020/12/springboot-google-captcha-google-config-img1.jpg)
              can guide you
        * Copy `SITE_KEY` and `SECRET_KEY` and add them into related fields that are in the [.env](.env)
        * HCaptcha integration was implemented in 4 different ways
          in [HCaptchaController.java](student-service%2Fsrc%2Fmain%2Fjava%2Fcom%2Fmb%2Fstudentservice%2Fapi%2Fcontroller%2FHCaptchaController.java) (
          OPTIONAL)

---

### Installation

1. Clone the repo
   ```sh
    git clone https://github.com/musabbozkurt/api-gateway-service.git
   ```

####

2. Run `docker-compose up -d` command in the [docker-compose.yml](docker-compose.yml) directory or
   enable [spring.docker.compose](https://github.com/musabbozkurt/api-gateway-service/blob/main/api-gateway/src/main/resources/application.yml#L49)
   property and just
   run [ApiGatewayApplication.java](api-gateway%2Fsrc%2Fmain%2Fjava%2Fcom%2Fmb%2Fapigateway%2FApiGatewayApplication.java)
   to install RabbitMQ, PostgreSQL and Keycloak

####

3. Log in to http://localhost:9090/admin with `username: admin` and `password: admin`
    1. `Create realm` -> Import [payment-service-realm-export.json](docs%2Fkeycloak%2Fpayment-service-realm-export.json)
    2. `Clients` -> `payment-service` -> `Credentials` -> `Regenerate` copy the value and use this value in Postman
       environment variable.
    3. `Users` -> `Add user` -> `Username` -> `payment-service-user`
    4. `Users` -> `payment-service-user` -> `Credentials` -> `Set password` to `test` and turn off `Temporary` toggle.
    5. `Users` -> `payment-service-user` -> `Role Mapping` -> `Assign role` add `admin` role
    6. [Postman](https://www.postman.com/downloads/) should be installed,
       follow [How to import postman collection](https://learning.postman.com/docs/getting-started/importing-and-exporting-data/#importing-postman-data)
       to import files that are under the [postman_collection](docs%2Fpostman_collection) folder

####

4. Run Spring Boot applications

    1. `mvn clean install` or `mvn clean package`
    2. `mvn spring-boot:run`

####

5. Additional information to access endpoints, swagger and actuator

    * Swagger: http://localhost:8080/swagger-ui.html
    * Actuator: http://localhost:8080/actuator

---

<!-- REFERENCES -->

## References

* [References](References.md)

---
