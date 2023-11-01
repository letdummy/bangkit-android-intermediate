package com.sekalisubmit.storymu.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sekalisubmit.storymu.R
import com.sekalisubmit.storymu.data.local.room.story.Story
import com.sekalisubmit.storymu.ui.activity.DetailActivity

class StoryListAdapter : ListAdapter<Story, StoryListAdapter.ViewHolder>(DiffCallback) {

    companion object DiffCallback: DiffUtil.ItemCallback<Story>() {
        override fun areItemsTheSame(oldStory: Story, newStory: Story): Boolean {
            return oldStory === newStory
        }

        override fun areContentsTheSame(oldStory: Story, newStory: Story): Boolean {
            return oldStory.id == newStory.id
        }
    }
    class ViewHolder(itemLayout: View): RecyclerView.ViewHolder(itemLayout) {
        private var image: ImageView = itemLayout.findViewById(R.id.iv_item_photo)
        private var title: TextView = itemLayout.findViewById(R.id.item_title)
        private var description: TextView = itemLayout.findViewById(R.id.item_description)
        private var author: TextView = itemLayout.findViewById(R.id.tv_item_name)

        @SuppressLint("SetTextI18n")
        fun init(story: Story) {
            val requestOptions = RequestOptions()
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)

            Glide.with(itemView.context)
                .load(story.photoUrl)
                .apply(requestOptions)
                .into(image)


            val text = story.description?.split("\\s+".toRegex())
            val maxDesc = 10

            // I already test this with weird input of description, and it works fine
            if ((text?.size ?: 0) > 3) {
                title.text = text?.subList(0, 3)?.joinToString(" ") ?: ""

                if ((text?.size ?: 0) > maxDesc) {
                    val limitedDesc = text?.subList(3, maxDesc)?.joinToString(" ") ?: ""
                    description.text = "$limitedDesc.."
                } else {
                    description.text = text?.subList(3, text.size)?.joinToString(" ") ?: ""
                }
            } else {
                story.description?.length?.let {
                    if (it in 26..100){
                        title.text = "${story.description?.substring(0, 25)}"
                        description.text = "${story.description?.substring(25, it)}.."
                    } else if (it > 100) {
                        title.text = "${story.description?.substring(0, 25)}"
                        description.text = "${story.description?.substring(25, 59)}.."
                    } else {
                        title.text = story.description ?: ""
                        description.text = ""
                    }
                }
            }

            author.text = "by ${story.name}"

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java)
                intent.putExtra("story", story)
                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(image, "imageDetail"),
                        Pair(title, "titleDetail"),
                        Pair(description, "descriptionDetail"),
                        Pair(author, "authorDetail")
                    )
                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val storyLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return ViewHolder(storyLayout)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = getItem(position)
        holder.init(story)
    }
}