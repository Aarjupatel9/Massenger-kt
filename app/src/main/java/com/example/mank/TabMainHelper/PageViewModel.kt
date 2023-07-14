package com.example.mank.TabMainHelper

import android.util.Log
import androidx.arch.core.util.Function
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.ContactListHolder
import com.example.mank.LocalDatabaseFiles.entities.ContactWithMassengerEntity
import com.example.mank.MainActivity
import com.example.mank.RecyclerViewClassesFolder.RecyclerViewAdapter

class PageViewModel() : ViewModel() {
    private val mIndex = MutableLiveData<Int>()
    private val mText = Transformations.map<Int, RecyclerViewAdapter>(
        mIndex,
        Function<Int, RecyclerViewAdapter?> { input ->
            Log.d("log-PageViewModel", "Transformations.map || mIndex:" + mIndex.value)
            Log.d("log-PageViewModel", "Transformations.map || input:$input")
            Log.d("log-PageViewModel", "Transformations.map || enter null cond.")
            if (MainActivity.contactArrayList == null) {
                MainActivity.contactArrayList = ArrayList()
                MainActivity.MainContactListHolder = ContactListHolder(MainActivity.db)
                MainActivity.contactArrayList = MainActivity.contactList

                MainActivity.filteredContactArrayList = (MainActivity.contactArrayList as java.util.ArrayList<ContactWithMassengerEntity>?)?.toMutableList() as ArrayList<ContactWithMassengerEntity>

//                MainActivity.filteredContactArrayList =  MainActivity.contactArrayList
                //i have to fix this error because this is for serching oparation
                //.clone() as ArrayList<ContactWithMassengerEntity?>

                synchronized((MainActivity.statusForThread)!!) {
                    MainActivity.statusForThread!!.value = 1
                    Log.d(
                        "log-onMassegeArriveFromServer1",
                        "HomePageWithContactActivity.contactArrayList before notifyAll()"
                    )

                }
                MainActivity.recyclerViewAdapter = RecyclerViewAdapter(
                    MainActivity.MainActivityStaticContext,
                )
            } else {
                MainActivity.MainContactListHolder = ContactListHolder(MainActivity.db)
                MainActivity.contactArrayList = MainActivity!!.contactList
                MainActivity.recyclerViewAdapter = RecyclerViewAdapter(
                    MainActivity.MainActivityStaticContext,
                )
                MainActivity.filteredContactArrayList = (MainActivity.contactArrayList as java.util.ArrayList<ContactWithMassengerEntity>?)?.toMutableList() as ArrayList<ContactWithMassengerEntity>


            }
            MainActivity.recyclerViewAdapter
        }
    )

    fun setIndex(index: Int) {
        mIndex.value = index
        Log.d("log-PageViewModel", "setIndex || mIndex:" + mIndex.value)
    }

    val recyclerViewAdapter: LiveData<RecyclerViewAdapter>
        get() {
            Log.d("log-PageViewModel", "getText || mIndex:" + mIndex.value)
            return mText
        }
    val `try`: LiveData<String>
        get() {
            Log.d("log-PageViewModel", "mIndex:$mIndex")
            return tryString
        }
    private val tryString = Transformations.map(mIndex, object : Function<Int?, String> {
        override fun apply(input: Int?): String {
            return "null string"
        }
    })
}