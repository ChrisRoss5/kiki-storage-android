package dev.k1k1.kikistorage

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.k1k1.kikistorage.databinding.ActivitySplashScreenBinding
import dev.k1k1.kikistorage.framework.applyAnimation
import dev.k1k1.kikistorage.framework.callDelayed
import dev.k1k1.kikistorage.framework.startActivity

private const val DELAY = 0L  // todo

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()
        redirect()
    }

    private fun startAnimations() {
        binding.ivSplash.applyAnimation(R.anim.slide_left_and_scale_down)
        binding.tvSplash.applyAnimation(R.anim.slide_right_and_fade_in)
    }

    private fun redirect() {
        // This will be where you check login status or do other logic later todo
        callDelayed(DELAY) {
            startActivity<MainActivity>()
            finish()
        }
    }
}