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
        compileOnly("org.projectlombok:lombok:1.18.28")
        annotationProcessor("org.projectlombok:lombok:1.18.28")
        testImplementation(platform("org.junit:junit-bom:5.9.1"))
        testImplementation("org.junit.jupiter:junit-jupiter")
    }
}


tasks.test {
    useJUnitPlatform()
}