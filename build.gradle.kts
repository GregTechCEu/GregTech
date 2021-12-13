import net.minecraftforge.gradle.user.UserBaseExtension
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.ObjectId

buildscript {
    repositories {
        mavenCentral()
        /*
        maven {
            name = "jitpack"
            setUrl("https://jitpack.io")
        }
         */
        maven {
            name = "forge"
            setUrl("https://maven.minecraftforge.net/")
        }
    }
    dependencies {
        // classpath("com.github.GregTechCE:ForgeGradle:FG_2.3-SNAPSHOT")
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
}

val git: Git = Git.open(projectDir)

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
}

repositories {
    maven {
        name = "CCL Maven New"
        setUrl("https://minecraft.curseforge.com/api/maven")
    }
    maven {
        name = "ChickenBones maven"
        setUrl("http://chickenbones.net/maven/")
    }
}

dependencies {
    "deobfCompile"("codechicken:ChickenASM:1.12-1.0.2.9")
    "deobfCompile"("codechicken-lib-1-8:CodeChickenLib-1.12.2:3.2.3.358:universal")

    "compile"(files("etc/CraftTweaker2-1.12-4.1.20.670.jar"))
    "compile"(files("etc/jei_1.12.2-4.16.1.301.jar"))
    "compile"(files("etc/theoneprobe-1.12-1.4.28.jar"))
    "compile"(files("etc/CTM-MC1.12.2-1.0.2.31.jar"))

    "testImplementation"("junit:junit:4.13.1")
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
            "FMLCorePlugin" to "gregtech.core.GregTechLoadingPlugin",
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

artifacts {
    add("archives", jar)
    add("archives", sourceTask)
    add("archives", energyApiTask)
}

fun Project.idea(configure: org.gradle.plugins.ide.idea.model.IdeaModel.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("idea", configure)
idea {
    module {
        inheritOutputDirs = true
    }
}

fun getBuildNumber(): String {
    val gitLog = git.log()
    val headCommitId = git.repository.resolve(Constants.HEAD)
    val startCommitId = ObjectId.fromString("f867923385572819385b53651f397a380a29c6b6")
    gitLog.addRange(startCommitId, headCommitId)
    return gitLog.call().toList().size.toString()
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

    val branchNameOrTag = System.getenv("CI_COMMIT_REF_NAME")
    if (branchNameOrTag != null && !branchNameOrTag.startsWith("v") && branchNameOrTag != "master") {
        if (extra != "") {
            return "$major.$minor.$revision-$extra-$branchNameOrTag"
        }
        return "$major.$minor.$revision-$branchNameOrTag"
    }

    var version = "$major.$minor.$revision"
    if (branchNameOrTag != null && branchNameOrTag == "master") {
        version += ".${getBuildNumber()}"
    }

    if (extra != "") {
        version += "-$extra"
    }

    return version
}
