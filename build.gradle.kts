import org.gradle.api.tasks.Exec

plugins {
    id("java")
    id("idea")
}

subprojects {

    group = "org.tbl.microdaddy"

    apply {
        plugin("java")
        plugin("idea")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }


    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("org.mapstruct:mapstruct:1.5.5.Final")
        implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.0.2")
        compileOnly("org.projectlombok:lombok:1.18.28")
        compileOnly("org.mapstruct:mapstruct-processor:1.5.5.Final")
        annotationProcessor("org.projectlombok:lombok-mapstruct-binding:0.2.0")
        annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
        annotationProcessor("org.projectlombok:lombok:1.18.28")
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
        testAnnotationProcessor("org.projectlombok:lombok:1.18.28")

    }

    tasks.test {
        useJUnitPlatform()
        environment("DOCKER_HOST", "unix:///home/edmangini/.docker/desktop/docker.sock")
        environment("TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE", "/var/run/docker.sock")
    }

}

