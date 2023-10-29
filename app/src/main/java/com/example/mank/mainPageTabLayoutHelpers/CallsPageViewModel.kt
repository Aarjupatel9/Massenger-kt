package com.example.mank.mainPageTabLayoutHelpers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


import androidx.lifecycle.ViewModel
import androidx.lifecycle.Transformations
import com.example.mank.MainActivity
import com.example.mank.recyclerViewClassesFolder.CallsRecyclerViewAdapter

class CallsPageViewModel : ViewModel() {
    private val mIndex = MutableLiveData<Int>()
    var callsRecyclerViewAdapter: CallsRecyclerViewAdapter? = null
    private val mText = Transformations.map(mIndex) { input ->
        Log.d("log-CallsPageViewModel", "Transformations.map || mIndex:" + mIndex.value)
        Log.d("log-CallsPageViewModel", "Transformations.map || input:$input")
        val contactStatusList = ArrayList<Long>()
        contactStatusList.add(99L)
        callsRecyclerViewAdapter =
            CallsRecyclerViewAdapter(MainActivity.MainActivityStaticContext, contactStatusList)
        callsRecyclerViewAdapter!!
    }


    fun setIndex(index: Int) {
        mIndex.value = index
        Log.d("log-CallsPageViewModel", "setIndex || mIndex:" + mIndex.value)
    }

    fun getCallsRecyclerViewAdapter(): LiveData<CallsRecyclerViewAdapter> {
        Log.d("log-CallsPageViewModel", "getText || mIndex:" + mIndex.value)
        return mText
    }

    val `try`: LiveData<String>
        get() {
            Log.d("log-CallsPageViewModel", "mIndex:$mIndex")
            return tryString
        }
    private val tryString = Transformations.map(mIndex) { "null string" }
}