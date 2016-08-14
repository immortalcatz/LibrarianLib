package com.teamwizardry.librarianlib.fx.shader.uniforms

import com.teamwizardry.librarianlib.fx.shader.Shader
import org.lwjgl.opengl.ARBShaderObjects

class FloatTypes {

    class Float(owner: Shader, name: String, type: UniformType,
                size: Int, location: Int) : Uniform(owner, name, type, size, location) {

        fun set(value: kotlin.Float) {
            ARBShaderObjects.glUniform1fARB(location, value)
        }

        fun set(value: Double) {
            set(value.toFloat())
        }
    }

    class FloatVec2(owner: Shader, name: String, type: UniformType,
                    size: Int, location: Int) : Uniform(owner, name, type, size, location) {

        fun set(x: kotlin.Float, y: kotlin.Float) {
            ARBShaderObjects.glUniform2fARB(location, x, y)
        }

        fun set(x: Double, y: Double) {
            set(x.toFloat(), y.toFloat())
        }
    }

    class FloatVec3(owner: Shader, name: String, type: UniformType,
                    size: Int, location: Int) : Uniform(owner, name, type, size, location) {

        fun set(x: kotlin.Float, y: kotlin.Float, z: kotlin.Float) {
            ARBShaderObjects.glUniform3fARB(location, x, y, z)
        }

        fun set(x: Double, y: Double, z: Double) {
            set(x.toFloat(), y.toFloat(), z.toFloat())
        }
    }

    class FloatVec4(owner: Shader, name: String, type: UniformType,
                    size: Int, location: Int) : Uniform(owner, name, type, size, location) {

        fun set(x: kotlin.Float, y: kotlin.Float, z: kotlin.Float, w: kotlin.Float) {
            ARBShaderObjects.glUniform4fARB(location, x, y, z, w)
        }

        fun set(x: Double, y: Double, z: Double, w: Double) {
            set(x.toFloat(), y.toFloat(), z.toFloat(), w.toFloat())
        }
    }
}
