plugins {
    java
    id("org.springframework.boot") version "3.1.0"
    id("io.spring.dependency-management") version "1.1.0"
}

group = "org.tbl.microdaddy.authz"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.cloud:spring-cloud-starter-config:4.0.3")
    implementation("org.springframework.retry:spring-retry:2.0.2")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:4.0.3")
    implementation("org.springframework.security:spring-security-oauth2-authorization-server:1.1.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement{
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:2022.0.3")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

