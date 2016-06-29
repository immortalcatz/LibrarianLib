package com.teamwizardry.librarianlib.client.fx.shader;

import com.teamwizardry.librarianlib.api.LibrarianLog;
import com.teamwizardry.librarianlib.client.fx.shader.uniforms.FloatTypes;
import com.teamwizardry.librarianlib.client.fx.shader.uniforms.Uniform;
import com.teamwizardry.librarianlib.client.fx.shader.uniforms.UniformType;

import org.lwjgl.opengl.ARBShaderObjects;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public abstract class Shader {
    public static final Shader NONE = new Shader("", "") {
        @Override
        public void initUniforms() {
        }
    };

    public FloatTypes.Float time;

    private int glName = 0;
    private Uniform[] uniforms;

    private String vert, frag;

    public Shader(String vert, String frag) {
        this.vert = vert;
        this.frag = frag;
    }

    public void init(int program) {
        glName = program;

        int uniformCount = GL20.glGetProgrami(getGlName(), GL20.GL_ACTIVE_UNIFORMS);
        int uniformLength = GL20.glGetProgrami(getGlName(), GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH);
        uniforms = new Uniform[uniformCount];
        
        int index = 0;
        for (int i = 0; i < uniformCount; i++) {
            String name = GL20.glGetActiveUniform(getGlName(), i, uniformLength);
            int type = GL20.glGetActiveUniformType(getGlName(), i);
            int size = GL20.glGetActiveUniformSize(getGlName(), i);
            int location = GL20.glGetUniformLocation(getGlName(), name);

            Uniform uniform = makeUniform(name, type, size, location);
            uniforms[index++] = uniform;
        }
        
        if(uniformCount > 0) {
        	String msg = "Found " + uniformCount + " uniforms. [";
        	for (Uniform uniform : uniforms) {
				msg += uniform.getType().toString() + " `" + uniform.getName() + "` @" + uniform.getLocation() + ", ";
			}
        	msg += "]";
        	LibrarianLog.I.info(msg);
        }
        time = getUniform("time", true);

        initUniforms();
    }

    public abstract void initUniforms();

    public String getVert() {
        return vert;
    }

    public String getFrag() {
        return frag;
    }

    public int getGlName() {
        return glName;
    }

    public <T extends Uniform> T getUniform(String name) {
    	return getUniform(name, false);
    }
    
    @SuppressWarnings("unchecked")
	public <T extends Uniform> T getUniform(String name, boolean quiet) {
        for (int i = 0; i < uniforms.length; i++) {
            if (uniforms[i].getName().equals(name)) {
                try {
                    return (T) uniforms[i];
                } catch (ClassCastException e) {
                	LibrarianLog.I.debug("Uniform %s was wrong type. (%s)", name, uniforms[i].getType().name());
                }
            }
        }
        if(!quiet) LibrarianLog.I.debug("Can't find uniform %s", name);
        return null;
    }

    private Uniform makeUniform(String name, int type, int size, int location) {
        UniformType enumType = UniformType.getByGlEnum(type);
        return enumType.make(this, name, enumType, size, location);
    }
}
