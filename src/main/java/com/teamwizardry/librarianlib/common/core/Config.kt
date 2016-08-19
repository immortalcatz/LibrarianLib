package com.teamwizardry.librarianlib.common.core

import net.minecraftforge.common.config.Configuration

import java.io.File

object Config {
    var shaders: Boolean = false

    fun initConfig(configurationFile: File) {
        val config = Configuration(configurationFile)
        config.load()
        shaders = config.get(Configuration.CATEGORY_CLIENT, "shaders", false, "Controls whether LibLib's shaders are used. If you're using the GLSL Shaders mod and are having graphical troubles with visuals, you may want to turn this off.").boolean
        config.save()
    }

}