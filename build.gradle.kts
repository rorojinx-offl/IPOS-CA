plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("ch.qos.logback:logback-classic:1.5.28")
    implementation("org.xerial:sqlite-jdbc:3.51.2.0")
}

tasks.test {
    useJUnitPlatform()
}