//
// Created by bohda on 6/29/2020.
//

#ifndef NEARLY_SHADERHANDLER_H
#define NEARLY_SHADERHANDLER_H

#include <string>
#include "Globals.h"
#include "GlErrorHandler.h"



#define STR(s) #s
#define STRV(s) STR(s)

#define POS_ATTRIB 0
#define COLOR_ATTRIB 1


class ShaderHandler {
public:
    static GLuint createProgram();

private:
    static GLuint createShader(GLenum shaderType, const char *src);
    static void handleError(GLuint vtx, GLuint frag);
};


#endif //NEARLY_SHADERHANDLER_H
