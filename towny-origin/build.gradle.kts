plugins {
    id("cc.mewcraft.common")
}

group = "cc.mewcraft.townyorigin"
version = "1.0.0"
description = "Records the origin of players, powered by LuckPerms"

dependencies {
    // We use LuckPerms Metadata System to store the origin of players
    compileOnly("net.luckperms", "api", "5.4")
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
