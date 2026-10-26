package com.ongo.aonsecure.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.ongo.aonsecure.R
import com.ongo.aonsecure.databinding.ItemPostAdapBinding
import com.ongo.aonsecure.model.Post
import com.ongo.aonsecure.model.PostViewModel
import com.ongo.aonsecure.model.QuoteList

class PostListAdapter : RecyclerView.Adapter<PostListAdapter.ViewHolder>() {
    private lateinit var postList:List<Post>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostListAdapter.ViewHolder {
        val binding: ItemPostAdapBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_post_adap, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PostListAdapter.ViewHolder, position: Int) {
        holder.bind(postList[position])
    }

    override fun getItemCount(): Int {
        return if(::postList.isInitialized) postList.size else 0
    }

    fun updatePostList(postList:QuoteList){
        this.postList = postList.jobs
        notifyDataSetChanged()
    }

    class ViewHolder(private val binding: ItemPostAdapBinding):RecyclerView.ViewHolder(binding.root){
        private val viewModel = PostViewModel()

        fun bind(post:Post){
            viewModel.bind(post)
            binding.viewModel = viewModel
        }
    }
}