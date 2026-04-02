plugins {
    id("java")
    id("org.jooq.jooq-codegen-gradle") version "3.20.11"
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
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
    implementation("org.jooq:jooq:3.20.11")
    implementation("org.openjfx:javafx:25.0.2")
    implementation("net.sf.jasperreports:jasperreports:7.0.6")
    implementation("net.sf.jasperreports:jasperreports-pdf:7.0.6")
    jooqCodegen("org.xerial:sqlite-jdbc:3.51.2.0") //Add SQLite to task's classpath
}

tasks.test {
    useJUnitPlatform()
}

jooq {
    version = "3.20.11"

    configuration {
        jdbc {
            driver = "org.sqlite.JDBC"
            url = "jdbc:sqlite:${projectDir}/iposca.db"
            user = ""
            password = ""
        }

        generator {
            name = "org.jooq.codegen.DefaultGenerator"

            database {
                name = "org.jooq.meta.sqlite.SQLiteDatabase"
                includes = ".*"
                excludes = ""
                outputSchemaToDefault = true
            }

            target {
                packageName = "schema"
                directory = "src/main/java/org/novastack/iposca/utils/db/generated"
            }
        }
    }
}

javafx {
    version = "25.0.2"
    modules("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("org.novastack.iposca.stock.UIMain")
}