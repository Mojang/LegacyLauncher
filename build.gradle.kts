plugins {
  // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
  id("java-library")
  id("com.palantir.git-version") version "0.15.0"
  id("maven-publish")
  id("com.diffplug.spotless") version "6.12.0"
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(8))
    vendor.set(JvmVendorSpec.ADOPTIUM)
  }
  withSourcesJar()
  withJavadocJar()
}

repositories {
  maven { url = uri("https://libraries.minecraft.net/") }
  mavenCentral()
}

spotless {
  encoding("UTF-8")
  java {
    toggleOffOn()
    importOrder()
    removeUnusedImports()
    palantirJavaFormat("1.1.0")
  }
  kotlinGradle {
    toggleOffOn()
    ktfmt("0.39")
    trimTrailingWhitespace()
    indentWithSpaces(4)
    endWithNewline()
  }
}

dependencies {
  api("net.sf.jopt-simple:jopt-simple:4.5")
  api("org.ow2.asm:asm-commons:9.4")
  api("org.ow2.asm:asm-tree:9.4")
  api("org.ow2.asm:asm-util:9.4")
  api("org.ow2.asm:asm-analysis:9.4")
  api("org.lwjgl.lwjgl:lwjgl:2.9.4-nightly-20150209")
  api("org.apache.logging.log4j:log4j-core:2.0-beta9")
  api("org.apache.logging.log4j:log4j-api:2.0-beta9")
}

val gitVersion: groovy.lang.Closure<String> by extra

group = "net.minecraft"

version = gitVersion()

publishing {
  publications { create<MavenPublication>("launchwrapper") { from(components["java"]) } }

  repositories {
    maven {
      url = uri("http://jenkins.usrv.eu:8081/nexus/content/repositories/releases")
      isAllowInsecureProtocol = true
      credentials {
        username = System.getenv("MAVEN_USER") ?: "NONE"
        password = System.getenv("MAVEN_PASSWORD") ?: "NONE"
      }
    }
  }
}
