package com.ongo.aonsecure.activity

import android.os.Bundle
import com.ongo.aonsecure.R
import com.ongo.aonsecure.activity.ui.home.HomeViewModel
import com.ongo.aonsecure.base.BaseCompatActivity

class TestActivity : BaseCompatActivity() {
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test)
    }
}