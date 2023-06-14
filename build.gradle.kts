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
        compileOnly("org.projectlombok:lombok:1.18.28")
        compileOnly("org.mapstruct:mapstruct-processor:1.5.5.Final")
        annotationProcessor("org.projectlombok:lombok:1.18.28")
        annotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
        testAnnotationProcessor("org.mapstruct:mapstruct-processor:1.5.5.Final")

    }
}


tasks.test {
    useJUnitPlatform()
}