//
// Created by bohda on 6/29/2020.
//

#include <vector>
#include "Renderer2D.h"

Renderer2D::Renderer2D()
        : mEglContext(eglGetCurrentContext()),
          mProgram(0),
          mVB(0),
          mIBO(0),
          mVAO(0) {

    mData.lastIndex = 0;
}

bool Renderer2D::initInternal() {

    mProgram = ShaderHandler::createProgram();
    if (!mProgram) {
        LOGE("Failed to create OpenGL shader program");
        return false;
    }

    glGenBuffers(1, &mVB);
    glBindBuffer(GL_ARRAY_BUFFER, mVB);
    glBufferData(GL_ARRAY_BUFFER, MAX_VERTIXES * sizeof(Vertex), nullptr,
                 GL_DYNAMIC_DRAW);

    glGenVertexArrays(1, &mVAO);
    glBindVertexArray(mVAO);

    glBindBuffer(GL_ARRAY_BUFFER, mVB);
    glVertexAttribPointer(POS_ATTRIB, 2, GL_FLOAT, GL_FALSE, sizeof(Vertex),
                          (const GLvoid *) offsetof(Vertex, pos));
    glVertexAttribPointer(COLOR_ATTRIB, 4, GL_UNSIGNED_BYTE, GL_TRUE, sizeof(Vertex),
                          (const GLvoid *) offsetof(Vertex, rgba));
    glEnableVertexAttribArray(POS_ATTRIB);
    glEnableVertexAttribArray(COLOR_ATTRIB);

    glGenBuffers(1, &mIBO);
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIBO);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, MAX_INDEXES * sizeof(Vertex), nullptr,
                 GL_DYNAMIC_DRAW);

    LOGV("Using OpenGL ES 3.0 renderer");
    return true;
}

Renderer2D::~Renderer2D() {
    if (eglGetCurrentContext() != mEglContext)
        return;
    glDeleteVertexArrays(1, &mVAO);
    glDeleteBuffers(1, &mVB);
    glDeleteProgram(mProgram);
}

void Renderer2D::render() {
    glBindVertexArray(mVAO);
    glDrawElements(GL_TRIANGLES, mData.indices.size(), GL_UNSIGNED_INT, nullptr);
    GlErrorHandler::checkGlError("Renderer2D::render");
}

void Renderer2D::resize(int w, int h) {
    glViewport(0, 0, w, h);
}

std::shared_ptr<Renderer2D> Renderer2D::create() {
    auto renderer = std::make_shared<Renderer2D>(Renderer2D());
    if (!renderer->initInternal()) {
        return nullptr;
    }
    return renderer;
}

Vertex* Renderer2D::createQuad(float horizontalSide, float verticalSide, float x, float y, Color color) {
    auto *buf = new Vertex[4];
    buf[0] = {{x,       y},
              {color.R, color.G, color.B, color.A}};
    buf[1] = {{x + horizontalSide, y},
              {color.R,            color.G, color.B, color.A}};
    buf[2] = {{x,       y + verticalSide},
              {color.R, color.G, color.B, color.A}};
    buf[3] = {{x + horizontalSide, y + verticalSide},
              {color.R,            color.G, color.B, color.A}};

    return buf;
}

void Renderer2D::renderMatrix(std::vector<std::vector<uint8_t>> &matrix) {

    glClearColor(254, 254, 254, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    glUseProgram(mProgram);

    int verticalCount = matrix.size();
    if (verticalCount == 0) return;
    int horizontalCount = matrix.at(0).size();

    float quadHorizontalSide = 2.0f / (float) horizontalCount;
    float quadVerticalSide = 2.0f / (float) verticalCount;

    for (int i = 0; i < horizontalCount; i++) {
        float xPos = -1.0f + (float) i * quadHorizontalSide;
        for (int j = 0; j < verticalCount; j++) {
            float yPos = -1.0f + (float) j * quadVerticalSide;

            if (mData.indices.size() >= MAX_INDEXES)
                this->flush(); // draw and clear data
            //handle colors
            Color clr = Colors[BLACK];
            if (Colors.find(matrix[j][i]) != Colors.end()) clr = Colors[matrix[j][i]];
            //create a quad of Vertices and push data to local structure
            Vertex *quad = createQuad(quadHorizontalSide, quadVerticalSide, xPos, yPos, clr);
            for (unsigned int k = 0; k < 4; k++) mData.vertices.push_back(quad[k]); // push vertices
            for (unsigned int k = mData.lastIndex; k < mData.lastIndex + 3; k++)
                mData.indices.push_back(k);
            for (unsigned int k = mData.lastIndex + 3; k > mData.lastIndex; k--)
                mData.indices.push_back(k);
            mData.lastIndex += 4;
            delete[] quad;
        }
    }
    this->flush();
}

void Renderer2D::flush() {
    //bind current data
    glBindBuffer(GL_ARRAY_BUFFER, mVB);
    glBufferSubData(GL_ARRAY_BUFFER, 0, mData.vertices.size() * sizeof(Vertex),
                    &mData.vertices.front());
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, mIBO);
    glBufferSubData(GL_ELEMENT_ARRAY_BUFFER, 0, mData.indices.size() * sizeof(unsigned int),
                    &mData.indices.front());
    GlErrorHandler::checkGlError("Renderer2D::renderMatrix");
    //draw it
    this->render();
    //clear data
    mData.lastIndex = 0;
    mData.indices.clear();
    mData.vertices.clear();
}
