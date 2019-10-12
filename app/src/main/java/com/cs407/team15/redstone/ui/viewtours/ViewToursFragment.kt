package com.cs407.team15.redstone.ui.viewtours

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cs407.team15.redstone.R
import com.cs407.team15.redstone.model.Tour
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ViewToursFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ViewToursFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewToursFragment : Fragment(), RecyclerAdapter.ItemClickListener {
    var tours: MutableList<Tour> = mutableListOf()

    override fun onItemClick(view: View, position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        GlobalScope.launch { getAndDisplayTourData() }
        return inflater.inflate(R.layout.fragment_view_tours, container, false)
    }

    fun fillRecyclerView(list: List<String>) {
        val recyclerView = view!!.findViewById<RecyclerView>(R.id.tourList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = RecyclerAdapter(context as Context, list)
        adapter.setClickListener(this)
        recyclerView.adapter = adapter
        getView()!!.invalidate()
    }

    suspend fun getAndDisplayTourData() {
        tours.addAll(0, Tour.getAllTours())
        activity!!.runOnUiThread { fillRecyclerView(tours.map { tour -> tour.name }) }
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewToursFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ViewToursFragment().apply {
                arguments = Bundle().apply {
                }
            }
    }
}
