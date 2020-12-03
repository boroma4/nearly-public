package com.ukrainianboyz.nearly.utils.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.ukrainianboyz.nearly.R
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator
import java.io.IOException
import java.net.URL


class ImageUtils {
    companion object{

        private const val TAG = "ImageUtils"

         fun loadCircularImage(url: String?, imageView: ImageView){
             if(url.isNullOrBlank()) return
             loadImage(url)
                 .transform(CircleTransform())
                 .into(imageView)
        }

        fun loadBitmapFromUrl(context: Context, url:String): Bitmap{
           return try {
                BitmapFactory.decodeStream(URL(url).openStream())
            } catch (e: IOException) {
               Log.e(TAG,"loading image failed")
               BitmapFactory.decodeResource(context.resources, R.drawable.anonym);
            }
        }

        private fun loadImage(url: String) : RequestCreator{
            return Picasso.get().load(url)
        }
    }
}