package com.sekalisubmit.storymu.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sekalisubmit.storymu.R
import com.sekalisubmit.storymu.data.local.room.story.Story
import com.sekalisubmit.storymu.databinding.ActivityDetailBinding


class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupData()
    }

    @SuppressLint("SetTextI18n")
    private fun setupData() {
        val story = intent.getParcelableExtra<Story>("story") as Story
        oriImage(story)

        val requestOptions = RequestOptions()
            .placeholder(R.drawable.loading)
            .error(R.drawable.error)

        Glide.with(applicationContext)
            .load(story.photoUrl)
            .apply(requestOptions)
            .centerCrop()
            .into(binding.ivDetailPhoto)

        val text = story.description?.split("\\s+".toRegex())

        if ((text?.size ?: 0) > 3) {
            binding.titleDetail.text = text?.subList(0, 3)?.joinToString(" ") ?: ""
            binding.tvDetailDescription.text = text?.subList(3, text.size)?.joinToString(" ") ?: ""
        } else {
            story.description?.length?.let {
                if (it > 25) {
                    binding.titleDetail.text = "${story.description?.substring(0, 25)}"
                    binding.tvDetailDescription.text = "${story.description?.substring(25, it)}"
                } else {
                    binding.titleDetail.text = story.description
                    binding.tvDetailDescription.text = ""
                }
            }
        }

        binding.tvDetailName.text = "by ${story.name}"
    }

    private fun oriImage(story: Story) {
        binding.btnDetail.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(story.photoUrl)
            startActivity(intent)
            Toast.makeText(this, "Opening Original Image", Toast.LENGTH_SHORT).show()
        }
    }
}
