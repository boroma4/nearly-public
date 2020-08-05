//
// Created by bohda on 6/29/2020.
//

#include "GlErrorHandler.h"
#include "gl3stub.h"
#include "Globals.h"

bool GlErrorHandler::checkGlError(const char* funcName) {
    GLint err = glGetError();
    if (err != GL_NO_ERROR) {
        LOGE("GL error after %s(): 0x%08x\n", funcName, err);
        return true;
    }
    return false;
}

bool GlErrorHandler::getProgramIvAndPrintLog(const GLuint& id,const GLenum& attr) {
    GLint success = GL_FALSE;
    glGetProgramiv(id, attr, &success);
    if (!success) {
        LOGE("Could not link program");
        GLint infoLogLen = 0;
        glGetProgramiv(id, GL_INFO_LOG_LENGTH, &infoLogLen);
        if (infoLogLen) {
            auto *infoLog = (GLchar *) malloc(infoLogLen);
            if (infoLog) {
                glGetProgramInfoLog(id, infoLogLen, nullptr, infoLog);
                LOGE("Could not link program:\n%s\n", infoLog);
                free(infoLog);
            }
        }
    }
    return success;
}

bool GlErrorHandler::getShaderIvAndPrintLog(const GLuint& id, const GLenum& shaderType) {
    GLint compiled = GL_FALSE;
    glGetShaderiv(id, GL_COMPILE_STATUS, &compiled);
    if (!compiled) {
        GLint infoLogLen = 0;
        glGetShaderiv(id, GL_INFO_LOG_LENGTH, &infoLogLen);
        if (infoLogLen > 0) {
            auto *infoLog = (GLchar *) malloc(infoLogLen);
            if (infoLog) {
                glGetShaderInfoLog(id, infoLogLen, nullptr, infoLog);
                LOGE("Could not compile %s shader:\n%s\n",
                     shaderType == GL_VERTEX_SHADER ? "vertex" : "fragment",
                     infoLog);
                free(infoLog);
            }
        }
        return false;
    }
    return true;
}