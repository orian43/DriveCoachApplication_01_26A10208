package com.example.drivecoachapplication_01_26a10208.utils
import android.content.Context
import android.media.MediaPlayer

object SoundManager {

    fun playSound(context: Context, soundRawId: Int) {

        val mediaPlayer = MediaPlayer.create(context, soundRawId)

        mediaPlayer.setOnCompletionListener { mp ->
            mp.release()
        }


        mediaPlayer.start()
    }
}