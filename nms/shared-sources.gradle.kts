import org.gradle.api.plugins.JavaPluginExtension

val sharedOut = layout.buildDirectory.dir("generated-shared")
val versionSharedPackage = "org.mastersmp.packet.nms.${project.name}.shared"
val versionSharedPath = versionSharedPackage.replace('.', '/')
val era = project.findProperty("nmsEra")?.toString() ?: "modern"
val nmsVersion = project.findProperty("nmsVersion")?.toString() ?: project.name

data class SharedParts(
    val adapter: String,
    val connection: String,
    val player: String,
    val packet: String,
    val item: String,
    val menu: String,
    val world: String,
)

val parts: SharedParts = when (era) {
    "legacy16" -> SharedParts("legacy", "legacy", "legacy", "legacy", "legacy", "legacy", "legacy")
    "mid17" -> SharedParts("mid", "mid", "mid", "mid", "mid", "mid", "mid")
    "mid" -> SharedParts("mid", "mid", "mid", "mid", "mid", "mid", "mid")
    "modern" -> SharedParts("modern", "modern", "modern", "modern", "modern", "modern", "modern")
    "modern21_2" -> SharedParts("modern", "modern", "modern", "modern", "modern", "modern", "modern")
    "modern21_5" -> SharedParts("modern", "modern", "modern", "modern", "modern21_5", "modern", "modern")
    "modern26" -> SharedParts("modern", "modern", "modern", "modern", "modern21_5", "modern", "modern")
    else -> throw GradleException("Unknown nmsEra=$era")
}

val sharedVariantDirs: List<File> = buildList {
    val root = rootProject.file("nms-shared")
    add(root)
    add(File(root, "adapter/${parts.adapter}"))
    add(File(root, "connection/${parts.connection}"))
    add(File(root, "player/${parts.player}"))
    add(File(root, "packet/${parts.packet}"))
    add(File(root, "item/${parts.item}"))
    add(File(root, "menu/${parts.menu}"))
    add(File(root, "world/${parts.world}"))
}

val prepareSharedSources = tasks.register("prepareSharedSources") {
    sharedVariantDirs.forEach { inputs.dir(it) }
    inputs.property("nmsVersion", nmsVersion)
    inputs.property("era", era)
    outputs.dir(sharedOut)
    doLast {
        val outRoot = sharedOut.get().asFile
        outRoot.deleteRecursively()
        var copied = 0
        for (variantDir in sharedVariantDirs) {
            if (!variantDir.isDirectory) {
                throw GradleException("Missing shared variant: ${variantDir.absolutePath}")
            }
            variantDir.listFiles { f -> f.isFile && f.extension == "java" }?.forEach { file ->
                val dest = File(outRoot, "$versionSharedPath/${file.name}")
                dest.parentFile.mkdirs()
                var text = file.readText()
                    .replace(
                        "package org.mastersmp.packet.nms.shared;",
                        "package $versionSharedPackage;"
                    )
                    .replace(
                        "import static org.mastersmp.packet.nms.shared.",
                        "import static $versionSharedPackage."
                    )
                    .replace(
                        "import org.mastersmp.packet.nms.shared.",
                        "import $versionSharedPackage."
                    )
                if (era.startsWith("legacy")) {
                    text = text
                        .replace("net.minecraft.server.NMS", "net.minecraft.server.$nmsVersion")
                        .replace("org.bukkit.craftbukkit.NMS", "org.bukkit.craftbukkit.$nmsVersion")
                } else if (era.startsWith("mid")) {
                    text = text.replace(
                        "org.bukkit.craftbukkit.NMS",
                        "org.bukkit.craftbukkit.$nmsVersion"
                    )
                }
                dest.writeText(text)
                copied++
            }
        }
        if (copied == 0) {
            throw GradleException("No shared Java sources copied for era=$era")
        }
    }
}

val sourceSets = extensions.getByType(JavaPluginExtension::class.java).sourceSets
sourceSets.named("main") {
    java {
        setSrcDirs(listOf(sharedOut, file("src/main/java")))
    }
}

tasks.named("compileJava") {
    dependsOn(prepareSharedSources)
}

tasks.named("jar") {
    dependsOn(prepareSharedSources)
}
