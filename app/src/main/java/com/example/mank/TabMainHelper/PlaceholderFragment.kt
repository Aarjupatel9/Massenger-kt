package com.example.mank.TabMainHelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mank.MainActivity
import com.example.mank.databinding.ActivityZTabPageBinding

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment : Fragment() {
    private var pageViewModel: PageViewModel? = null
    private var binding: ActivityZTabPageBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(this).get(PageViewModel::class.java)
        var index = 1
        if (arguments != null) {
            index = requireArguments().getInt(ARG_SECTION_NUMBER)
        }
        pageViewModel!!.setIndex(index)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityZTabPageBinding.inflate(inflater, container, false)
        val root: View = binding!!.root
        MainActivity.ChatsRecyclerView = binding!!.TCPContactRecyclerView
        MainActivity.ChatsRecyclerView!!.setHasFixedSize(true)
        MainActivity.ChatsRecyclerView!!.layoutManager =
            LinearLayoutManager(MainActivity.MainActivityStaticContext)
        pageViewModel!!.recyclerViewAdapter.observe(viewLifecycleOwner) { recyclerViewAdapter ->
            MainActivity.ChatsRecyclerView!!.adapter = recyclerViewAdapter
            recyclerViewAdapter.notifyDataSetChanged()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        private const val ARG_SECTION_NUMBER = "section_number"
        @JvmStatic
        fun newInstance(index: Int): PlaceholderFragment {
            val fragment = PlaceholderFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_SECTION_NUMBER, index)
            fragment.arguments = bundle
            return fragment
        }
    }
}