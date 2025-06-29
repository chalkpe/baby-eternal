import kr.entree.spigradle.kotlin.*

plugins {
    kotlin("jvm") version "2.0.0"
    id("kr.entree.spigradle") version "2.4.6"
}

group = "pe.chalk.bukkit"
version = "0.0.2"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(spigot("1.21.4"))
}

spigot {
    apiVersion = "1.21"
    description = "Keep baby animals from growing up"
    commands {
        create("babies") {
            aliases = listOf("아기")
        }
    }
}
