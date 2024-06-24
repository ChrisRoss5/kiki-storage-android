package dev.k1k1.kikistorage

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import dev.k1k1.kikistorage.databinding.ActivitySplashScreenBinding
import dev.k1k1.kikistorage.framework.applyAnimation
import dev.k1k1.kikistorage.framework.callDelayed
import dev.k1k1.kikistorage.framework.isOnline
import dev.k1k1.kikistorage.framework.startActivity

private const val DELAY = 3000L

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
        callDelayed(DELAY) {
            if (!isOnline()) {
                binding.ivSplash.clearAnimation()
                binding.ivSplash.visibility = View.GONE
                binding.tvSplash.clearAnimation()
                binding.tvSplash.text = "No internet connection"
                callDelayed(DELAY) {
                    finish()
                }
                return@callDelayed
            }
            startActivity<MainActivity>()
            finish()
        }
    }
}