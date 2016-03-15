import com.beust.kobalt.*
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.kotlin.*

val repos = repos("https://oss.sonatype.org/content/repositories/snapshots/")


val p = project {

    name = "spring"
    group = "com.zypus"
    artifactId = name
    version = "0.1"

    sourceDirectories {
        path("src/code")
    }

    sourceDirectoriesTest {
        path("src/test")
    }

    dependencies {
        compile("no.tornado:tornadofx:1.3.2")
        compile("org.reactfx:reactfx:2.0-SNAPSHOT")
    }

    dependenciesTest {
//        compile("org.testng:testng:6.9.5")

    }

    assemble {
        jar {
        }
    }
}
