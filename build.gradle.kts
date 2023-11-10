plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "1.5.4"
}

group = "com.starshootercity"
version = "1.0.0"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://jitpack.io") }
    maven {
        url = uri("https://nexus.frengor.com/repository/public/")
    }
    maven { url = uri("https://repo.dmulloy2.net/repository/public/") }
    maven {
        url = uri("https://repo.opencollab.dev/main/")
    }
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}