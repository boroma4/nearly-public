#include <jni.h>
#include "Globals.h"
#include "Renderer2D.h"
#include <vector>

using namespace std;



static void printGlString(const char* name, GLenum s) {
    const char* v = (const char*)glGetString(s);
    LOGD("GL %s: %s\n", name, v);
}


static void releaseMatrixArray(JNIEnv *env, jobjectArray matrix) {
    int size = (*env).GetArrayLength(matrix);
    for (int i = 0; i < size; i++) {
        auto oneDim = (jbyteArray) (*env).GetObjectArrayElement(matrix, i);
        jbyte *elements = (*env).GetByteArrayElements(oneDim, 0);

        (*env).ReleaseByteArrayElements(oneDim, elements, 0);
        (*env).DeleteLocalRef(oneDim);
    }
}

static std::shared_ptr<Renderer2D> g_renderer = nullptr;

extern "C" JNIEXPORT void JNICALL
Java_com_hotukrainianboyz_nearly_gaming_MicroEngine_init(JNIEnv* env, jobject obj) {
    if (g_renderer) {
        g_renderer = nullptr;
    }

    printGlString("Version", GL_VERSION);
    printGlString("Vendor", GL_VENDOR);
    printGlString("Renderer", GL_RENDERER);
    printGlString("Extensions", GL_EXTENSIONS);

    const char* versionStr = (const char*)glGetString(GL_VERSION);
    if (strstr(versionStr, "OpenGL ES 3.") && gl3stubInit()) {
        g_renderer = Renderer2D::create();
    } else  {
        LOGE("Unsupported OpenGL ES version");
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_hotukrainianboyz_nearly_gaming_MicroEngine_resize(JNIEnv* env, jobject obj, jint width, jint height) {
    if (g_renderer) {
        g_renderer->resize(width, height);
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_hotukrainianboyz_nearly_gaming_MicroEngine_render2DMatrix(JNIEnv* env, jobject obj, jobjectArray matrix) {
    const int height = env -> GetArrayLength(matrix);
    auto firstRow =  (jbyteArray)env->GetObjectArrayElement(matrix, 0);
    const int width = env -> GetArrayLength(firstRow);
    vector<vector<uint8_t>> localArray(height);
    for(int i=0; i < height; ++i){
        auto row = (jbyteArray)env->GetObjectArrayElement(matrix, i);
        jbyte *element = env->GetByteArrayElements(row, 0);
        vector<uint8_t> newRow(width);
        for(int j=0; j < width; ++j) {
            newRow[j] = (element[j]);
        }
        localArray[i] = (newRow);
    }

    if (g_renderer) {
        g_renderer->renderMatrix(localArray);
    }
    releaseMatrixArray(env,matrix);
}

