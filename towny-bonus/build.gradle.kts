plugins {
    id("cc.mewcraft.common")
}

group = "cc.mewcraft.townybonus"
version = "1.3.0"
description = "Add bonus to towns and nations!"

dependencies {
    // External plugins
    compileOnly("net.luckperms", "api", "5.4")
    compileOnly("com.github.MilkBowl", "VaultAPI", "1.7") { isTransitive = false }
    compileOnly("me.clip", "placeholderapi", "2.11.2")
}

tasks {
    jar {
        archiveBaseName.set("TownyBonus")
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
