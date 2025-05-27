// 插件
plugins {
    id("java") // Java
    id("com.gradleup.shadow") version "8.3.3" // Shadow
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14" // PaperDev
}


// 项目信息
group = "top.catnies"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.MOJANG_PRODUCTION


// 依赖
repositories {
    // 开发工具
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // PaperDev, Velocity
}

dependencies {
    // 开发工具
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT") // PaperDev
}


// 任务配置
tasks {
    assemble {
        dependsOn(reobfJar)     // 使 reobfJar 在 build 上运行
    }
}


// 资源配置
tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}