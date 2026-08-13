plugins {
    `java-library`
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    api(project(":nms-api"))
    compileOnly("io.papermc.paper:paper-api:${property("paperApiVersion")}")
    compileOnly("io.netty:netty-transport:4.1.115.Final")
    compileOnly("io.netty:netty-handler:4.1.115.Final")
    compileOnly("net.kyori:adventure-api:4.17.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:4.17.0")
    testImplementation(platform("org.junit:junit-bom:6.1.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.netty:netty-transport:4.1.115.Final")
    testImplementation("io.netty:netty-handler:4.1.115.Final")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.compileJava {
    options.release.set(21)
}

tasks.named<Javadoc>("javadoc") {
    options {
        (this as StandardJavadocDocletOptions).apply {
            encoding = "UTF-8"
            addBooleanOption("Xdoclint:none", true)
        }
    }
    isFailOnError = false
}
