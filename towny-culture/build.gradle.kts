plugins {
    id("cc.mewcraft.common")
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
}

group = "cc.mewcraft.townyculture"
version = "1.0.0"
description = "Brings culture to towns"

dependencies {
    compileOnly("com.google.inject", "guice", "5.1.0")
}

bukkit {
    main = "cc.mewcraft.townyculture.TownyCulture"
    name = "TownyCulture"
    version = "${project.version}"
    apiVersion = "1.17"
    authors = listOf("Nailm")
    depend = listOf("helper", "MewCore")
}

tasks {
    // register("deployJar") {
    //     doLast {
    //         exec {
    //             commandLine("rsync", jar.get().archiveFile.get().asFile.absoluteFile, "dev:data/dev/jar")
    //         }
    //     }
    // }
    // register("deployJarFresh") {
    //     dependsOn(build)
    //     finalizedBy(named("deployJar"))
    // }
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-dev")) "$this+${lastCommitHash()}" else this
