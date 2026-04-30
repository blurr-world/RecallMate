package com.madinaappstudio.recallmate.onboarding.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.madinaappstudio.recallmate.R
import com.madinaappstudio.recallmate.core.utils.PrefManager
import com.madinaappstudio.recallmate.databinding.ActivityOnboardingBinding
import com.madinaappstudio.recallmate.main.HomeActivity
import com.madinaappstudio.recallmate.auth.ui.AuthActivity
import com.madinaappstudio.recallmate.onboarding.model.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefManager = PrefManager(this)

        if (prefManager.isLogin()) {
            finishOnboarding(true)
        } else {
            if (!prefManager.isFirstStartup()) {
                finishOnboarding(false)
            }
        }

        val pages = listOf(
            OnboardingItem(
                "https://images.unsplash.com/photo-1434030216411-0b793f4b4173?auto=format&fit=crop&w=800&q=80",
                "Welcome to Study Assistant",
                resources.getStringArray(
                    R.array.onboarding_content
                )[0]
            ),
            OnboardingItem(
                "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=800&q=80",
                "AI-Powered Summaries",
                resources.getStringArray(
                    R.array.onboarding_content
                )[1]
            ),
            OnboardingItem(
                "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?auto=format&fit=crop&w=800&q=80",
                "Interactive AI Tutor",
                resources.getStringArray(
                    R.array.onboarding_content
                )[2]
            ),
            OnboardingItem(
                "https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=800&q=80",
                "Organized Library",
                resources.getStringArray(
                    R.array.onboarding_content
                )[3]
            )
        )

        val adapter = OnboardingAdapter(pages)
        binding.vpOnboarding.adapter = adapter

        binding.wdiOnboarding.attachTo(binding.vpOnboarding)

        binding.btnSkipOnboarding.setOnClickListener {
            finishOnboarding(false)
        }

        binding.btnNextOnboarding.setOnClickListener {
            if (binding.vpOnboarding.currentItem < pages.lastIndex) {
                binding.vpOnboarding.currentItem++
            } else {
                finishOnboarding(false)
            }
        }
    }

    private fun finishOnboarding(flag: Boolean) {
        prefManager.setFirstStartup(false)
        if (flag) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }

    }
}
