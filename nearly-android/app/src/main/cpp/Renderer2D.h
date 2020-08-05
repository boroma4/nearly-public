#ifndef NEARLY_RENDERER2D_H
#define NEARLY_RENDERER2D_H

#include <EGL/egl.h>
#include <string>
#include <fstream>
#include <iostream>
#include <memory>
#include <vector>
#include "Globals.h"
#include "ShaderHandler.h"

/**
 * One quad is approx 80 bytes, 6500 pixels per one draw will limit renderer's RAM usage to approx 0.5 mb
 */
#define MAX_PIXELS_PER_DRAW_CALL 6500
#define MAX_VERTIXES 4 * MAX_PIXELS_PER_DRAW_CALL
#define MAX_INDEXES 6 * MAX_PIXELS_PER_DRAW_CALL

struct Vertex {
    GLfloat pos[2];
    GLubyte rgba[4];
};

struct RenderData{
    std::vector<Vertex> vertices;
    std::vector<unsigned int> indices;
    unsigned int lastIndex;
};



class Renderer2D {
public:
    Renderer2D();
    ~Renderer2D();
    static std::shared_ptr<Renderer2D> create();
    static void resize(int w, int h);

private:
    void render();
    bool initInternal();
    static Vertex* createQuad (float horizontalSide, float verticalSide, float x, float y, Color color);
    void flush();
    EGLContext mEglContext;
    GLuint mProgram;
    GLuint mVB;
    GLuint mIBO;
    GLuint mVAO;
    RenderData mData;

public:
    void renderMatrix(std::vector<std::vector<uint8_t>>& matrix);
};


#endif //NEARLY_RENDERER2D_H
