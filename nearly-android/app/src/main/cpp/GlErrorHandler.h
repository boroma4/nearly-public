//
// Created by bohda on 6/29/2020.
//

#ifndef NEARLY_GLERRORHANDLER_H
#define NEARLY_GLERRORHANDLER_H

#include <cstdlib>
#include "Globals.h"


class GlErrorHandler{

public:
    static bool checkGlError(const char* funcName);
    static bool getProgramIvAndPrintLog(const GLuint& id, const GLenum& attr);
    static bool getShaderIvAndPrintLog(const GLuint& id, const GLenum& shaderType);

};

#endif //NEARLY_GLERRORHANDLER_H
