package com.ukrainianboyz.nearly.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.ukrainianboyz.nearly.R
import com.ukrainianboyz.nearly.gaming.GameSurface
import com.ukrainianboyz.nearly.gaming.games.tetris.ExampleGame
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GameActivity : AppCompatActivity() {

    private val mGameSurface by lazy {findViewById<GameSurface>(R.id.example_game)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        mGameSurface.attachGame(ExampleGame())
        lifecycleScope.launch(Dispatchers.Default){
            mGameSurface.launchGame()
        }
    }


    companion object {
        fun makeIntent(context: Context): Intent {
            return Intent(context, GameActivity::class.java)
        }
    }
}