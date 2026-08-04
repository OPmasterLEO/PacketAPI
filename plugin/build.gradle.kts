plugins {
    java
}

dependencies {
    implementation(project(":api"))
    implementation(project(":nms-api"))
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("io.netty:netty-transport:4.1.115.Final")
}

tasks.compileJava {
    options.release.set(21)
}

tasks.processResources {
    val props = mapOf("version" to rootProject.version.toString())
    inputs.properties(props)
    filesMatching("plugin.yml") {
        expand(props)
    }
}
