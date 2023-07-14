package com.example.mank.TabMainHelper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mank.MainActivity
import com.example.mank.databinding.ActivityZTabStatusPageBinding

/**
 * A placeholder fragment containing a simple view.
 */
class CallsPageFragment : Fragment() {
    private var callsPageViewModel: CallsPageViewModel? = null
    private var binding: ActivityZTabStatusPageBinding? = null
    private var recyclerView: RecyclerView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callsPageViewModel = ViewModelProvider(this).get(CallsPageViewModel::class.java)
        var index = 1
        if (arguments != null) {
            index = requireArguments().getInt(ARG_SECTION_NUMBER)
        }
        callsPageViewModel!!.setIndex(index)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ActivityZTabStatusPageBinding.inflate(inflater, container, false)
        val root: View = binding!!.root
        recyclerView = binding!!.TSPRecyclerView
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(MainActivity.MainActivityStaticContext)
        callsPageViewModel!!.getCallsRecyclerViewAdapter()
            .observe(viewLifecycleOwner) { recyclerViewAdapter ->
                recyclerView!!.adapter = recyclerViewAdapter
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
        fun newInstance(index: Int): CallsPageFragment {
            val fragment = CallsPageFragment()
            val bundle = Bundle()
            bundle.putInt(ARG_SECTION_NUMBER, index)
            fragment.arguments = bundle
            return fragment
        }
    }
}