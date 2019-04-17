import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  java
  application
  id("io.franzbecker.gradle-lombok") version "2.1"
}

tasks.withType(Wrapper::class.java) {
  val gradleWrapperVersion: String by project
  gradleVersion = gradleWrapperVersion
  distributionType = Wrapper.DistributionType.BIN
}

java {
  val javaVersion = JavaVersion.VERSION_12
  sourceCompatibility = javaVersion
  targetCompatibility = javaVersion
}

repositories {
  mavenCentral()
}

val lombokVersion: String by project

lombok {
  version = lombokVersion
}

val akkaVersion: String by project
val springVersion: String by project
val vavrVersion: String by project
val slf4jVersion: String by project
val logbackVersion: String by project
val junit4Version: String by project
val assertkVersion: String by project
val assertjVersion: String by project
val junitJupiterVersion: String by project

dependencies {
  implementation("com.typesafe.akka:akka-actor_2.12:$akkaVersion")

  implementation(platform("org.springframework:spring-framework-bom:$springVersion"))
  implementation("org.springframework:spring-context-support")

  implementation("io.vavr:vavr:$vavrVersion")
  implementation("org.slf4j:slf4j-api:$slf4jVersion")
  implementation("ch.qos.logback:logback-classic:$logbackVersion")
  annotationProcessor("org.projectlombok:lombok:$lombokVersion")

  testImplementation("org.assertj:assertj-core:$assertjVersion")
  testImplementation(platform("org.junit:junit-bom:$junitJupiterVersion"))
  testRuntime("org.junit.platform:junit-platform-launcher")
  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine")
  testImplementation("junit:junit:$junit4Version")
}

tasks.withType<Test> {
  useJUnitPlatform()
  testLogging {
    showExceptions = true
    showStandardStreams = true
    events(PASSED, SKIPPED, FAILED)
  }
}

val mainClass: String by project

application {
  mainClassName = mainClass
}

tasks {
  register<Jar>("fatJar") {
    //archiveAppendix.set("all")
    archiveClassifier.set("all")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
      attributes("Main-Class" to mainClass)
    }
    from(configurations.runtimeClasspath.get()
        .onEach { println("add from dependencies: ${it.name}") }
        .map { if (it.isDirectory) it else zipTree(it) })
    val sourcesMain = sourceSets.main.get()
    sourcesMain.allSource.forEach { println("add from sources: ${it.name}") }
    from(sourcesMain.output)
  }

  register<Zip>("sources") {
    dependsOn("clean")
    shouldRunAfter("clean", "assemble")
    description = "Archives sources in a zip file"
    group = "Archive"
    from("src") {
      into("src")
    }
    from(".gitignore")
    from(".java-version")
    from(".travis.yml")
    from("build.gradle.kts")
    from("pom.xml")
    from("README.md")
    from("settings.gradle.kts")
    archiveFileName.set("${project.buildDir}/sources-${project.version}.zip")
  }

  named("clean") {
    doLast {
      delete(
          project.buildDir,
          "${project.projectDir}/out"
      )
    }
  }
}

defaultTasks("clean", "sources", "fatJar", "test")
//defaultTasks("clean", "sources", "fatJar", "installDist", "distZip")
