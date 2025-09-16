package com.example.distractfree

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.distractfree.databinding.ActivityWelcomeBinding
import kotlin.collections.plusAssign
import kotlin.compareTo

class Welcome : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private var onBoardAdapter: OnBoardAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBoardAdapter = OnBoardAdapter(this, getItemsList())
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.offscreenPageLimit = 2
        binding.viewPager.adapter = onBoardAdapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                changeDots(position)
                if (position == 2) {
                    binding.nextBtn.setBackgroundColor(resources.getColor(R.color.colorAccent))
                    binding.nextBtn.text = "Finish"
                } else {
                    binding.nextBtn.setBackgroundColor(resources.getColor(R.color.colorAccentLight))
                    binding.nextBtn.text = "Next"
                }
            }
        })

        binding.nextBtn.setOnClickListener {
            if (binding.viewPager.currentItem >= 2) {
                SharedPrefsManager.setOnBoardStatus(this, true)
                startActivity(Intent(this, MainActivity::class.java))
                finishAffinity()
            } else {
                binding.viewPager.currentItem += 1
            }
        }

        binding.viewPager.post {

            YoYo.with(Techniques.SlideInRight)
                .duration(800)
                .playOn(binding.nextBtn)

            YoYo.with(Techniques.SlideInLeft)
                .duration(800)
                .playOn(binding.dotsConatiner)
        }

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })
    }

    private fun changeDots(position: Int) {
        binding.dot1.setBackgroundColor(resources.getColor(R.color.transparent))
        binding.dot2.setBackgroundColor(resources.getColor(R.color.transparent))
        binding.dot3.setBackgroundColor(resources.getColor(R.color.transparent))

        when (position) {
            1 -> binding.dot2.setBackgroundColor(resources.getColor(R.color.black))
            2 -> binding.dot3.setBackgroundColor(resources.getColor(R.color.black))
            else -> binding.dot1.setBackgroundColor(resources.getColor(R.color.black))
        }
    }

    private fun getItemsList(): ArrayList<OnBoardData> {
        val dataList = ArrayList<OnBoardData>()
        dataList.add(
            OnBoardData(
                "TAKE BACK",
                "CONTROL",
                R.raw.f1,
                "Welcome to a life without distractions. This app helps you block Instagram Reels, YouTube Shorts, and explicit content so you can regain your time, focus on what matters, and stay in charge of your digital habits."
            )
        )

        dataList.add(
            OnBoardData(
                "STAY",
                "FOCUSED",
                R.raw.f2,
                "Whether you’re studying, working, or just want some peace, our smart blocker uses Accessibility features to silently block addictive content. You choose what to block — we do the rest."
            )
        )

        dataList.add(
            OnBoardData(
                "BUILD",
                "BETTER HABITS",
                R.raw.f3,
                "This isn’t just an app, it’s a step toward a better you. Stay productive, improve your mental clarity, and create habits that stick — one distraction-free day at a time."
            )
        )


        return dataList
    }
}


object SharedPrefsManager{
    fun setOnBoardStatus(context: Context, status: Boolean) {
        context.getSharedPreferences("Shared Prefs", Context.MODE_PRIVATE).edit()
            .putBoolean("OnBoarding Status", status).apply()
    }
}