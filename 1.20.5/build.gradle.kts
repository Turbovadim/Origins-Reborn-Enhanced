plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")

    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "sonatype-oss-snapshots"
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":version"))
    paperweight.paperDevBundle("1.20.5-R0.1-SNAPSHOT")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    disableAutoTargetJvm()
}

tasks {
    compileJava {
        options.release.set(17)
    }
}

tasks.test {
    useJUnitPlatform()
}
