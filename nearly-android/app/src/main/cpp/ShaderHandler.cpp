//
// Created by bohda on 6/29/2020.
//

#include "ShaderHandler.h"

static const char VERTEX_SHADER[] =
        "#version 300 es\n"
        "layout(location = " STRV(POS_ATTRIB) ") in vec4 pos;\n"
        "layout(location=" STRV(COLOR_ATTRIB) ") in vec4 color;\n"
        "out vec4 vColor;\n"
        "void main() {\n"
        "    gl_Position = pos;\n"
        "    vColor = color;\n"
        "}\n";

static const char FRAGMENT_SHADER[] =
        "#version 300 es\n"
        "precision mediump float;\n"
        "in vec4 vColor;\n"
        "out vec4 outColor;\n"
        "void main() {\n"
        "    outColor = vColor;\n"
        "}\n";


GLuint ShaderHandler::createProgram() {
    GLuint vtxShader = 0;
    GLuint fragShader = 0;
    GLuint program = 0;

    vtxShader = createShader(GL_VERTEX_SHADER, VERTEX_SHADER);
    if (!vtxShader)
        handleError(vtxShader,fragShader);

    fragShader = createShader(GL_FRAGMENT_SHADER, FRAGMENT_SHADER);
    if (!fragShader)
        handleError(vtxShader,fragShader);

    program = glCreateProgram();

    if (program) {
        glAttachShader(program, vtxShader);
        glAttachShader(program, fragShader);

        glLinkProgram(program);
        bool linked = GlErrorHandler::getProgramIvAndPrintLog(program,GL_LINK_STATUS);
        LOGD("Program is linked %d", linked);
        if (!linked) {
            glDeleteProgram(program);
            program = 0;
        }
    } else {
        handleError(vtxShader,fragShader);
    }

    return program;
}


GLuint ShaderHandler::createShader(GLenum shaderType, const char *src) {
    GLuint shader = glCreateShader(shaderType);
    if (!shader) {
        GlErrorHandler::checkGlError("glCreateShader");
        return 0;
    }
    glShaderSource(shader, 1, &src, nullptr);
    glCompileShader(shader);
    bool compiled = GlErrorHandler::getShaderIvAndPrintLog(shader,GL_COMPILE_STATUS);

    if (!compiled) {
        glDeleteShader(shader);
        return 0;
    }
    return shader;
}

void ShaderHandler::handleError(GLuint vtx, GLuint frag) {
    GlErrorHandler::checkGlError("glCreateProgram");
    glDeleteShader(vtx);
    glDeleteShader(frag);
}
