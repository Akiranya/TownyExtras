plugins {
    val mewcraftVersion = "1.0.0"
    id("cc.mewcraft.java-conventions") version mewcraftVersion
    id("cc.mewcraft.repository-conventions") version mewcraftVersion
    id("cc.mewcraft.project-conventions")
    alias(libs.plugins.indra)
}

group = "cc.mewcraft.townybonus"
version = "1.3.0"
description = "Add bonus to towns and nations!"

dependencies {
    // server api
    compileOnly(libs.server.paper)

    // my own libs
    compileOnly(libs.mewcore)

    // libs that present as other plugins
    compileOnly(libs.towny)
    compileOnly(libs.helper)
    compileOnly(libs.luckperms)
    compileOnly(libs.vault) { isTransitive = false }
    compileOnly(libs.papi)
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

indra {
    javaVersions().target(17)
}
