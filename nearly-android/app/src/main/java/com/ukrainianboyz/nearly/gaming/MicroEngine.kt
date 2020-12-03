package com.ukrainianboyz.nearly.gaming

class MicroEngine {
    init{
        System.loadLibrary("micro_engine")
    }
    external fun init()
    external fun resize(width: Int, height: Int)
    external fun render2DMatrix(matrix: Array<ByteArray>)
}