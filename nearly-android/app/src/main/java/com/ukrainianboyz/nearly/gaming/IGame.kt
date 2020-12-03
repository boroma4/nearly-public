package com.ukrainianboyz.nearly.gaming

interface IGame {
    fun attachUiUpdateFunction (fn: (Array<ByteArray>)-> Unit)
    suspend fun step()
}