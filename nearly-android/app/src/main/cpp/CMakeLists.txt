# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)


set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wall")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -fno-rtti -fno-exceptions -Wall")
set(OPENGL_LIB GLESv3)

add_library(micro_engine SHARED
        gl3stub.c
        MicroEngine.cpp
        Renderer2D.cpp
        ShaderHandler.cpp
        GlErrorHandler.cpp)


target_link_libraries(micro_engine
        ${OPENGL_LIB}
        android
        EGL
        log
        m)