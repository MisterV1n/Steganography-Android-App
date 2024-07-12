package com.mister.steganography

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mister.steganography.databinding.ActivityMainBinding
import com.mister.steganography.fragment.DecodeFragment
import com.mister.steganography.fragment.EncodeFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.title = "Steganography"
        setuptab()
    }

    private fun setuptab() {
        binding.viewpager.adapter = ViewPagerAdapter(supportFragmentManager).apply {
            addFragment(EncodeFragment(), "Encode")
            addFragment(DecodeFragment(), "Decode")
        }
        binding.tabs.setupWithViewPager(binding.viewpager)
    }
}
