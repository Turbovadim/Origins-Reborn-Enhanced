plugins {
    id("java")
    id("io.papermc.paperweight.userdev")
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation(project(":version"))
    implementation("net.kyori:adventure-platform-bukkit:4.3.4")
    paperweight.paperDevBundle("1.17.1-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}