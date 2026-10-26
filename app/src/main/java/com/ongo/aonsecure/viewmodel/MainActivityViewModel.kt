package com.ongo.aonsecure.viewmodel

import android.view.View
import androidx.lifecycle.MutableLiveData
import com.ongo.aonsecure.base.BaseViewModel
import com.ongo.aonsecure.network.PostApi
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MainActivityViewModel : BaseViewModel() {
    @Inject
    lateinit var postApi: PostApi
    private lateinit var subscription: Disposable
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    //val postListAdapter: PostListAdapter = PostListAdapter(premQuote.jobs)

    init {
        loadPosts()

    }



    private fun loadPosts() {

        // var observable:Observable<String> = Observable.just(listOf(1, 2, 3, 4, 5))

        /*subscription = postApi.getPosts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe { onRetrievePostListStart() }
            .doOnTerminate { onRetrievePostListFinish() }
            .subscribe(
                { result -> onRetrievePostListSuccess(result) },
                { onRetrievePostListError() }
            )*/


    }

    private fun onRetrievePostListStart() {
        loadingVisibility.value = View.VISIBLE
    }

    private fun onRetrievePostListFinish() {
        loadingVisibility.value = View.GONE
    }

/*    private fun onRetrievePostListSuccess(postList: List<Post>) {
        postListAdapter.updatePostList(postList)
    }*/

    private fun onRetrievePostListError() {

    }

    override fun onCleared() {
        super.onCleared()
      //  subscription.dispose()
    }

}