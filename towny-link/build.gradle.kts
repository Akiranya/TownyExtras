plugins {
    id("cc.mewcraft.common")
}

group = "cc.mewcraft.townylink"
version = "1.1.0"
description = "Sync Towny data between your server network"

dependencies {
    compileOnly("com.google.inject", "guice", "5.1.0")
    compileOnly("de.themoep.connectorplugin", "bukkit", "1.5-SNAPSHOT")
}

tasks {
    jar {
        archiveBaseName.set("TownyLink")
    }
    processResources {
        filesMatching("**/paper-plugin.yml") {
            expand(mapOf(
                "version" to "${project.version}",
                "description" to project.description
            ))
        }
    }
    register("deployJar") {
        doLast {
            exec {
                commandLine("rsync", jar.get().archiveFile.get().asFile.absoluteFile, "dev:data/dev/jar")
            }
        }
    }
    register("deployJarFresh") {
        dependsOn(build)
        finalizedBy(named("deployJar"))
    }
}

fun lastCommitHash(): String = indraGit.commit()?.name?.substring(0, 7) ?: error("Could not determine commit hash")
fun String.decorateVersion(): String = if (endsWith("-dev")) "$this+${lastCommitHash()}" else this
