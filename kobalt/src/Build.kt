import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.project
import com.beust.kobalt.repos

val repos = repos()

val p = project {

    name = "SLIP"
    group = "com.zypus"
    artifactId = name
    version = "0.1"

    sourceDirectories {
        path("src")
    }

    sourceDirectoriesTest {
        path("test")
    }

    dependencies {
        compile("no.tornado:tornadofx:1.4.1")
        compile("org.reactfx:reactfx:2.0-M5")
        compile("org.controlsfx:controlsfx:8.40.10")
        compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.7.1-2")
        compile("net.mikera:vectorz:0.64.0")
    }

    dependenciesTest {
//        compile("org.testng:testng:6.9.5")
        compile("junit:junit:4.4")
    }

    assemble {
		jar {
		}
    }
}
