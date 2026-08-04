plugins {
    `java-library`
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    api(project(":nms-api"))
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly("io.netty:netty-transport:4.2.16.Final")
    compileOnly("io.netty:netty-handler:4.2.16.Final")
    compileOnly("net.kyori:adventure-api:5.2.0")
    compileOnly("net.kyori:adventure-text-serializer-plain:5.2.0")
    testImplementation(platform("org.junit:junit-bom:6.1.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
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
