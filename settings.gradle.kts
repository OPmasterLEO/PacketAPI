pluginManagement {
    repositories {
        maven("https://plugins.gradle.org/m2/")
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    plugins {
        id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
        id("com.gradleup.shadow") version "9.6.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "MasterPacketAPI"

include("nms-api")
include("api")
include("plugin")

val nmsBuckets = listOf(
    "v1_16_R1",
    "v1_16_R2",
    "v1_16_R3",
    "v1_17_R1",
    "v1_18_R1",
    "v1_18_R2",
    "v1_19_R1",
    "v1_19_R2",
    "v1_19_R3",
    "v1_20_R1",
    "v1_20_R2",
    "v1_20_R3",
    "v1_20_R4",
    "v1_21_R1",
    "v1_21_R2",
    "v1_21_R3",
    "v1_21_R4",
    "v1_21_R5",
    "v1_21_R6",
    "v1_21_R7",
    "v26_1",
    "v26_2"
)

nmsBuckets.forEach { bucket ->
    include("nms:$bucket")
    project(":nms:$bucket").projectDir = file("nms/$bucket")
}
