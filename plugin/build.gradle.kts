plugins {
    java
}

dependencies {
    implementation(project(":api"))
    implementation(project(":nms-api"))
    compileOnly("io.papermc.paper:paper-api:${property("paperApiVersion")}")
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
