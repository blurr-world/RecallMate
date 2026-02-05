package com.madinaappstudio.recallmate.auth.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.madinaappstudio.recallmate.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}