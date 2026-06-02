plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "consumer"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awssdk:sqs:2.25.25")
    implementation("software.amazon.awssdk:s3:2.25.25")
    implementation("software.amazon.awssdk:sesv2:2.25.25")
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
    manifest {
        attributes["Main-Class"] = "consumer.ImageConsumer"
    }
}
