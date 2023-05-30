plugins {
    val mewcraftVersion = "1.0.0"
    id("cc.mewcraft.java-conventions") version mewcraftVersion
    id("cc.mewcraft.repository-conventions") version mewcraftVersion
    id("cc.mewcraft.project-conventions")
    alias(libs.plugins.indra)
}

group = "cc.mewcraft.townylink"
version = "1.1.0"
description = "Sync Towny data between your server network"

dependencies {
    // server api
    compileOnly(libs.server.paper)

    // my own libs
    compileOnly(libs.mewcore)

    // plugin libs
    compileOnly(libs.towny)
    compileOnly(libs.helper) { isTransitive = false }
    compileOnly(libs.connector.bukkit)
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

indra {
    javaVersions().target(17)
}
