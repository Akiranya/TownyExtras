plugins {
    val mewcraftVersion = "1.0.0"
    id("cc.mewcraft.java-conventions") version mewcraftVersion
    id("cc.mewcraft.repository-conventions") version mewcraftVersion
    id("cc.mewcraft.project-conventions")
    alias(libs.plugins.indra)
}

group = "cc.mewcraft.townyorigin"
version = "1.0.0"
description = "Records the origin of players, powered by LuckPerms"

dependencies {
    // server api
    compileOnly(libs.server.paper)

    // my own libs
    compileOnly(libs.mewcore)

    // plugin libs
    compileOnly(libs.towny)
    compileOnly(libs.helper) { isTransitive = false }
    compileOnly(libs.luckperms) // we use LuckPerms Metadata System to store the server-origin of players
    compileOnly(libs.papi)
    compileOnly(libs.minipapi)
}

tasks {
    jar {
        archiveBaseName.set("TownyOrigin")
    }
    processResources {
        filesMatching("**/paper-plugin.yml") {
            expand(
                mapOf(
                    "version" to "${project.version}",
                    "description" to project.description
                )
            )
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

indra {
    javaVersions().target(17)
}
