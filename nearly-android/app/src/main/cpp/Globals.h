//
// Created by bohda on 6/29/2020.
//

#ifndef NEARLY_GLOBALS_H
#define NEARLY_GLOBALS_H

#include <android/log.h>
#include "gl3stub.h"
#include <unordered_map>

#define LOG_TAG "MicroEngine"

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#define RED 0
#define GREEN 1
#define BLUE 2
#define BLACK 3



struct Color{
    GLubyte R;
    GLubyte G;
    GLubyte B;
    GLubyte A;
};


static std::unordered_map<uint8_t , Color> Colors = {

        {RED, {254,0,0,0}},
        {GREEN, {0,254,0}},
        {BLUE, {0,0,255,0}},
        {BLACK, {0,0,0,0}}

};



#endif //NEARLY_GLOBALS_H
