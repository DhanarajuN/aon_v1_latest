package com.ongo.aonsecure.model

import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.base.BaseViewModel

class PostViewModel : BaseViewModel() {
    private val quoteNo = MutableLiveData<String>()
    private val generatedOn = MutableLiveData<String>()

    fun bind(post: Post) {
        quoteNo.value = post.QuoteNo
        generatedOn.value = post.QuoteGeneratedOn
    }

    fun getQuoteNo(): MutableLiveData<String> {
        return quoteNo
    }

    fun getGenerateNo(): MutableLiveData<String> {
        return generatedOn
    }
}