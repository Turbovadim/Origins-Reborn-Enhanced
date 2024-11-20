plugins {
    id("java")
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") } // Paper
    //maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") } // Spigot
    //maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") } // Spigot
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.opencollab.dev/main/") }
    maven { url = uri("https://repo.viaversion.com") }
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    compileOnly("com.viaversion:viaversion-api:5.0.0")
    compileOnly("io.papermc.paper:paper-api:1.18-R0.1-SNAPSHOT") // Paper
    //compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT") // Spigot
    //implementation("net.kyori:adventure-platform-bukkit:4.3.3") // Spigot
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("com.github.aromaa:WorldGuardExtraFlags:v4.2.4")
    compileOnly("org.geysermc.geyser:api:2.2.0-SNAPSHOT")
    compileOnly("org.geysermc.floodgate:api:2.2.2-SNAPSHOT")
    compileOnly("com.github.authme:authmereloaded:5.6.0-beta2")
    compileOnly("me.clip:placeholderapi:2.11.5")
    compileOnly("com.github.stleary:JSON-java:20231013")
    compileOnly("com.github.SkriptLang:Skript:2.9.1")
    compileOnly("net.objecthunter:exp4j:0.4.8")
    compileOnly(project(":version"))
    compileOnly(project(":1.18.1"))
    compileOnly(project(":1.18.2"))
    compileOnly(project(":1.19"))
    compileOnly(project(":1.19.1"))
    compileOnly(project(":1.19.2"))
    compileOnly(project(":1.19.3"))
    compileOnly(project(":1.19.4"))
    compileOnly(project(":1.20"))
    compileOnly(project(":1.20.1"))
    compileOnly(project(":1.20.2"))
    compileOnly(project(":1.20.3"))
    compileOnly(project(":1.20.4"))
    compileOnly(project(":1.20.6"))
    compileOnly(project(":1.21"))
    compileOnly(project(":1.21.1"))
    compileOnly(project(":1.21.3"))
    compileOnly(files("libs/worldguard.jar"))
    compileOnly(files("libs/worldedit.jar"))
}

tasks {
    compileJava {
        options.release.set(17)
    }
}

tasks.test {
    useJUnitPlatform()
}