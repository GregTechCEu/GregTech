import net.minecraftforge.gradle.user.UserBaseExtension

buildscript {
    repositories {
        mavenCentral()
        maven {
            name = "forge"
            setUrl("https://maven.minecraftforge.net/")
        }
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT")
        classpath("org.eclipse.jgit:org.eclipse.jgit:5.8.0.202006091008-r")
        classpath("org.apache.commons:commons-lang3:3.12.0")
    }
}

plugins {
    id("com.matthewprenger.cursegradle") version "1.1.0"
    id("maven-publish")
}

apply {
    plugin("net.minecraftforge.gradle.forge")
    plugin("idea")
}

val mcVersion = "1.12.2"
val forgeVersion = "14.23.5.2847"
val mcFullVersion = "$mcVersion-$forgeVersion"
val modVersion = getVersionFromJava(file("src/main/java/gregtech/GregTechVersion.java"))
version = "$mcVersion-$modVersion"
group = "gregtech"

configure<BasePluginConvention> {
    archivesBaseName = "gregtech"
}

fun minecraft(configure: UserBaseExtension.() -> Unit) = project.configure(configure)

minecraft {
    version = mcFullVersion
    mappings = "stable_39"
    runDir = "run"
    isUseDepAts = true
    replace("@VERSION@", modVersion)
    replaceIn("GregTechVersion.java")
}

repositories {
    maven {
        name = "CurseMaven"
        setUrl("https://www.cursemaven.com")
    }
    maven { //JEI
        name = "Progwml6 maven"
        setUrl("http://dvs1.progwml6.com/files/maven/")
    }
    maven {
        setUrl("https://maven.cleanroommc.com")
    }
}

dependencies {
    "deobfCompile"("curse.maven:codechicken-lib-1-8-242818:2779848")

    "deobfCompile"("curse.maven:ae2-extended-life-570458:3649419")

    "deobfCompile"("mezz.jei:jei_1.12.2:4.16.1.302")

    "compile"("curse.maven:crafttweaker-239197:3935788")
    "compile"(files("etc/theoneprobe-1.12-1.4.28.jar"))
    "compile"(files("etc/CTM-MC1.12.2-1.0.2.31.jar"))
    "compile"(files("etc/groovyscript-0.1.0.jar"))

    "deobfCompile"("zone.rong:mixinbooter:4.2")

    "testImplementation"("org.junit.jupiter:junit-jupiter:5.9.1")
    "testImplementation"("org.hamcrest:hamcrest:2.2")
    "implementation"("org.projectlombok:lombok:1.18.16")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val processResources: ProcessResources by tasks
val sourceSets: SourceSetContainer = the<JavaPluginConvention>().sourceSets

processResources.apply {
    inputs.property("version", modVersion)
    inputs.property("mcversion", mcFullVersion)

    from(sourceSets["main"].resources.srcDirs) {
        include("mcmod.info")
        expand(mapOf("version" to modVersion,
            "mcversion" to mcFullVersion))
    }

    // copy everything else, thats not the mcmod.info
    from(sourceSets["main"].resources.srcDirs) {
        exclude("mcmod.info")
    }
    // access transformer
    rename("(.+_at.cfg)", "META-INF/$1")
}

val jar: Jar by tasks
jar.apply {
    manifest {
        attributes(mapOf("FMLAT" to "gregtech_at.cfg",
            "FMLCorePlugin" to "gregtech.asm.GregTechLoadingPlugin",
            "FMLCorePluginContainsFMLMod" to "true"))
    }
}

tasks.withType<Test>() {
    testLogging {
        events("failed")
        showExceptions = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        showStackTraces = true
        showCauses = true
        showStandardStreams = false
    }
    useJUnitPlatform()
}

val sourceTask: Jar = tasks.create("source", Jar::class.java) {
    from(sourceSets["main"].allSource)
    classifier = "sources"
}

val devTask: Jar = tasks.create("dev", Jar::class.java) {
    from(sourceSets["main"].output)
    classifier = "dev"
}

val energyApiTask: Jar = tasks.create("energyApi", Jar::class.java) {
    from(sourceSets["main"].allSource)
    from(sourceSets["main"].output)

    include("gregtech/api/capability/IElectricItem.*")
    include("gregtech/api/capability/IEnergyContainer.*")
    include("gregtech/api/capability/GregtechCapabilities.*")

    classifier = "energy-api"
}

fun Project.idea(configure: org.gradle.plugins.ide.idea.model.IdeaModel.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("idea", configure)
idea {
    module {
        inheritOutputDirs = true
        setDownloadSources(true)
        setDownloadJavadoc(true)
    }
}

// used for GitHub Actions CI releases
task<Exec>("getVersionFromJava") {
    commandLine("echo", getVersionFromJava(file("src/main/java/gregtech/GregTechVersion.java")))
}

fun getVersionFromJava(file: File): String  {
    var major = "0"
    var minor = "0"
    var revision = "0"
    var extra = ""

    val prefix = "public static final int"
    val extraPrefix = "public static final String"
    file.forEachLine { line ->
        var s = line.trim()
        if (s.startsWith(prefix)) {
            s = s.substring(prefix.length, s.length - 1)
            s = s.replace("=", " ").replace(" +", " ").trim()
            val pts = s.split(" ")

            when {
                pts[0] == "MAJOR" -> major = pts[pts.size - 1]
                pts[0] == "MINOR" -> minor = pts[pts.size - 1]
                pts[0] == "REVISION" -> revision = pts[pts.size - 1]
            }
        } else if (s.startsWith(extraPrefix)) {
            s = s.substring(extraPrefix.length, s.length - 2)
            s = s.replace("=", " ").replace(" +", " ").replace("\"", " ").trim()
            val pts = s.split(" ")
            when {
                pts[0] == "EXTRA" -> extra = pts[pts.size - 1]
            }
        }
    }
    if (extra != "") {
        return "$major.$minor.$revision-$extra"
    }
    return "$major.$minor.$revision"
}
