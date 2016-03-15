
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.project
import com.beust.kobalt.repos

val repos = repos("https://oss.sonatype.org/content/repositories/snapshots/")


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
        compile("no.tornado:tornadofx:1.3.2")
        compile("org.reactfx:reactfx:2.0-SNAPSHOT")
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
