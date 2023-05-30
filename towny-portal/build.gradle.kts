plugins {
    val mewcraftVersion = "1.0.0"
    id("cc.mewcraft.java-conventions") version mewcraftVersion
    id("cc.mewcraft.repository-conventions") version mewcraftVersion
    id("cc.mewcraft.project-conventions")
    alias(libs.plugins.shadow)
    alias(libs.plugins.indra)
}

group = "cc.mewcraft.townyportal"
version = "1.1"
description = "Enhance the communication between towns and nations"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
}

dependencies {
    compileOnly(libs.server.paper)
    compileOnly(libs.towny)
    compileOnly("me.hsgamer", "bettergui", "8.3", classifier = "shaded")
    compileOnly("me.hsgamer.bettergui", "MaskedGUI", "2.2-SNAPSHOT")
    /*compileOnly("me.hsgamer", "hscore-minecraft-gui-advanced", "4.2.7") {
        exclude("me.hsgamer", "hscore-ui")
        exclude("me.hsgamer", "hscore-minecraft-gui")
    }*/
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
        filesMatching("**/addon.yml") {
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
                commandLine("rsync", shadowJar.get().archiveFile.get().asFile.absoluteFile, "dev:data/dev/jar")
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