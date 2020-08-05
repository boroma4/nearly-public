package com.hotukrainianboyz.nearly.gaming.games.tetris

import com.hotukrainianboyz.nearly.gaming.BLUE
import com.hotukrainianboyz.nearly.gaming.GREEN
import com.hotukrainianboyz.nearly.gaming.IGame
import com.hotukrainianboyz.nearly.gaming.RED
import kotlinx.coroutines.delay

class ExampleGame: IGame {

    // UI update method
    private lateinit var setMatrix : (matrix: Array<ByteArray>) -> Unit

    val m1 = arrayOf(
        byteArrayOf(
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE
        ),
        byteArrayOf(
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE
        ),
        byteArrayOf(
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE
        ),
        byteArrayOf(
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE
        ),
        byteArrayOf(
            BLUE,
            BLUE,
            BLUE,
            GREEN,
            BLUE,
            BLUE,
            BLUE
        ),
        byteArrayOf(
            BLUE,
            BLUE,
            BLUE,
            GREEN,
            BLUE,
            BLUE,
            BLUE
        )
    )
    var idx = 3
    var up = true
    var redx = 0
    var redy = 0

    override fun attachUiUpdateFunction(fn: (Array<ByteArray>) -> Unit) {
        setMatrix = fn
    }

    override suspend fun step(){
        if(!this::setMatrix.isInitialized) return
        delay(200)
        setMatrix(m1) // THE MOST IMPORTANT FUNCTION THAT WILL CAUSE UI TO UPDATE
        if(redy < m1.size) {
            m1[redy][redx] = RED
            redx++
            if (redx >= m1[0].size) {
                redx = 0
                redy++
            }
        }
        if(idx < 0) {
            up = false
            idx = 2
        }
        if(idx >= m1.size) {
            up = true
            idx = 3
        }
        if(up) {
            m1[idx+2][3] = BLUE
            m1[idx--][3] = GREEN
        }else{
            m1[idx-2][3] = BLUE
            m1[idx++][3] = GREEN
        }
    }
}