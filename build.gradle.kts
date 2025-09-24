plugins {
    alias(libs.plugins.java)
    alias(libs.plugins.spring.dependency.management)
    jacoco
}

allprojects {
    group = "com.leorces"
    version = "0.0.1-SNAPSHOT"
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "jacoco")

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    repositories { mavenCentral() }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.4")
        }
    }

    tasks.test {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
            csv.required.set(false)
        }
    }

    jacoco {
        toolVersion = "0.8.13"
    }
}
