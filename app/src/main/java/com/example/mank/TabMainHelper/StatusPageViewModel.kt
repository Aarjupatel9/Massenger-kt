package com.example.mank.TabMainHelper

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.mank.MainActivity
import com.example.mank.RecyclerViewClassesFolder.StatusRecyclerViewAdapter

class StatusPageViewModel : ViewModel() {
    private val mIndex = MutableLiveData<Int>()
    var statusRecyclerViewAdapter: StatusRecyclerViewAdapter? = null
    private val mText = Transformations.map(mIndex) { input ->
        Log.d("log-StatusPageViewModel", "Transformations.map || mIndex:" + mIndex.value)
        Log.d("log-StatusPageViewModel", "Transformations.map || input:$input")
        val contactStatusList = ArrayList<String?>()
        contactStatusList.add(MainActivity.user_login_id)
        statusRecyclerViewAdapter =
            StatusRecyclerViewAdapter(MainActivity.MainActivityStaticContext, contactStatusList)
        statusRecyclerViewAdapter!!
    }

    fun setIndex(index: Int) {
        mIndex.value = index
        Log.d("log-PageViewModel", "setIndex || mIndex:" + mIndex.value)
    }

    fun getStatusRecyclerViewAdapter(): LiveData<StatusRecyclerViewAdapter> {
        Log.d("log-StatusPageViewModel", "getText || mIndex:" + mIndex.value)
        return mText
    }

    val `try`: LiveData<String>
        get() {
            Log.d("log-PageViewModel", "mIndex:$mIndex")
            return tryString
        }
    private val tryString = Transformations.map(mIndex) { "null string" }
}