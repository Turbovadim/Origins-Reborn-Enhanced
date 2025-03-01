plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.6"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14" apply false
    kotlin("jvm") version "2.1.10"
}

group = "com.starshootercity"
version = "2.5.0"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.stleary:JSON-java:20241224")
    implementation("net.objecthunter:exp4j:0.4.8")
    implementation(project(":core"))
    implementation(project(":version"))
    implementation(project(":1.18.1", "reobf"))
    implementation(project(":1.18.2", "reobf"))
    implementation(project(":1.19", "reobf"))
    implementation(project(":1.19.1", "reobf"))
    implementation(project(":1.19.2", "reobf"))
    implementation(project(":1.19.3", "reobf"))
    implementation(project(":1.19.4", "reobf"))
    implementation(project(":1.20", "reobf"))
    implementation(project(":1.20.1", "reobf"))
    implementation(project(":1.20.2", "reobf"))
    implementation(project(":1.20.3", "reobf"))
    implementation(project(":1.20.4", "reobf"))
    implementation(project(":1.20.6", "reobf"))
    implementation(project(":1.21", "reobf"))
    implementation(project(":1.21.1", "reobf"))
    implementation(project(":1.21.3", "reobf"))
    implementation(project(":1.21.4", "reobf"))
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
}

tasks {
    compileJava {
        options.release.set(17)
    }
}

allprojects {
    tasks.withType<ProcessResources> {
        inputs.property("version", rootProject.version)
        filesMatching("**plugin.yml") {
            expand("version" to rootProject.version)
        }
    }
}


tasks {

    shadowJar {
        from(sourceSets.main.get().output)
        dependencies {
            exclude(dependency("com.github.Turbovadim:EnderaLib"))
            exclude {
                it.moduleGroup == "org.jetbrains.kotlin"
            }
        }
    }

    test {
        useJUnitPlatform()
    }

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}