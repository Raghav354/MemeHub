package com.example.latestmeme

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.android.synthetic.main.activity_main.memeImageView
import kotlinx.android.synthetic.main.activity_main.progressBar


class MainActivity : AppCompatActivity() {
    private val memeList = mutableListOf<String>() // List to store loaded meme URLs
    private var currentImageIndex = -1 // Index of the currently displayed meme
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gestureDetector = GestureDetector(this, GestureListener())

        loadMeme()
        memeImageView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun loadMeme() {
        progressBar.visibility = View.VISIBLE

        val url = "https://meme-api.com/gimme"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            Response.Listener { response ->
                val imageUrl = response.getString("url")
                memeList.add(imageUrl) // Add the loaded meme URL to the list
                currentImageIndex = memeList.size - 1 // Update the current index
                loadImage(imageUrl)
            },
            Response.ErrorListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            })

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun loadImage(imageUrl: String) {
        Glide.with(this).load(imageUrl).listener(object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.visibility = View.GONE
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.visibility = View.GONE
                return false
            }
        }).into(memeImageView)
    }

    fun shareMeme(view: View) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, memeList[currentImageIndex])
        intent.type = "text/plain"
        val chooser = Intent.createChooser(intent, "Share this meme using...")
        startActivity(chooser)
    }

    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val diffX = e1?.x?.let { e2?.x?.minus(it) } ?: 0f
            val diffY = e1?.y?.let { e2?.y?.minus(it) } ?: 0f
            if (Math.abs(diffX) > Math.abs(diffY) &&
                Math.abs(diffX) > SWIPE_THRESHOLD &&
                Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
            ) {
                if (diffX < 0) {
                    // Swipe left
                    if (currentImageIndex < memeList.size - 1) {
                        currentImageIndex++
                        loadImage(memeList[currentImageIndex])
                    } else {
                        loadMeme()
                    }
                } else {
                    // Swipe right
                    if (currentImageIndex > 0) {
                        currentImageIndex--
                        loadImage(memeList[currentImageIndex])
                    } else {
                        Toast.makeText(this@MainActivity, "No previous meme", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                return true
            }
            return super.onFling(e1, e2, velocityX, velocityY)
        }
    }
}