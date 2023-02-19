plugins {
    id("cc.mewcraft.common")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "cc.mewcraft.townyportal"
version = "1.0"
description = "Enhance the communication between towns and nations"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly(files("libs/bettergui-8.2-shaded.jar"))
    compileOnly("me.hsgamer.bettergui", "MaskedGUI", "2.2-SNAPSHOT")
    // compileOnly("me.hsgamer", "hscore-minecraft-gui-advanced", "4.2.7") {
    //     exclude("me.hsgamer", "hscore-ui")
    //     exclude("me.hsgamer", "hscore-minecraft-gui")
    // }
}

tasks {
    jar {
        archiveClassifier.set("noshade")
    }
    assemble {
        dependsOn(shadowJar)
    }
    shadowJar {
        minimize()
        archiveClassifier.set("")
        archiveBaseName.set("TownyPortal")
        relocate("me.hsgamer.hscore", "me.hsgamer.bettergui.lib.core")
        relocate("org.bstats", "me.hsgamer.bettergui.lib.bstats")
    }
    processResources {
        filesMatching("addon.yml") {
            filter { string ->
                var result = string
                mapOf(
                    "project.name" to "TownyPortal",
                    "project.version" to "${project.version}",
                    "project.mainClass" to "cc.mewcraft.townyportal.TownyPortal",
                    "project.description" to project.description
                ).forEach { (key, value) ->
                    result = result.replace("\${$key}", value.toString())
                }
                result
            }
        }
    }
    register("deployJar") {
        doLast {
            exec {
                commandLine("rsync", shadowJar.get().archiveFile.get().asFile.absoluteFile, "dev:data/dev/jar")
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
