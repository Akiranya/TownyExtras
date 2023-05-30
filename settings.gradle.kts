rootProject.name = "TownyExtras"

include(":towny-bonus")
include(":towny-link")
include(":towny-origin")
include(":towny-portal")

apply(from = "${System.getenv("HOME")}/MewcraftGradle/mirrors.settings.gradle.kts")