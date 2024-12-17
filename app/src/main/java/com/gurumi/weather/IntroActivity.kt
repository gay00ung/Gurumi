package com.gurumi.weather

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class IntroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val introLogo = findViewById<TextView>(R.id.introLogo)
//        val animFadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
        val animFadeOut = android.view.animation.AlphaAnimation(1.0f, 0.0f).apply {
            duration = 1050
        }

        introLogo.startAnimation(animFadeOut)

        animFadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation) {
                startActivity(Intent(this@IntroActivity, MainActivity::class.java))
                finish()
            }

            override fun onAnimationRepeat(animation: Animation?) {
            }
        })


    }
}