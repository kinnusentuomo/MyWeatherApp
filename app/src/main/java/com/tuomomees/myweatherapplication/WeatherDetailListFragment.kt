package com.tuomomees.myweatherapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

import android.widget.ListView
import java.util.ArrayList




// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [WeatherDetailListFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [WeatherDetailListFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class WeatherDetailListFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    lateinit var weatherDetailObjectList: MutableList<MyWeatherDetailObject>
    private var listItems: ArrayList<String>? = null


    private lateinit var adapter: ArrayAdapter<String>

    val TAG = "WeatherListFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            //weatherDetailObjectList = it.getParcelableArrayList("sentWeatherDetailObjectList")




        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_weather_detail_list, container, false)
        val listView = view.findViewById<ListView>(R.id.weatherDetailListView)

        listItems = ArrayList()
        weatherDetailObjectList = (activity as MainActivity).weatherDetailObjectList

        Log.d(TAG, listView.toString())


        Log.d(TAG, "listItems" + listItems.toString())
        //adapter = ArrayAdapter(this.requireContext(), R.layout.simple_list_item_1, listItems)
        adapter = ArrayAdapter(this.requireContext(), R.layout.custom_list_view_layout, R.id.textView2, listItems)
        listView.adapter = adapter

        if (listView != null) {
            listView.adapter = adapter
        }
        for(myWeatherDetailObject in weatherDetailObjectList){
            addTextToListView(myWeatherDetailObject.cityName + " " + "%.0f".format(myWeatherDetailObject.temp_c) + "°C")
        }

        Log.d(TAG, "onCreateView")


        return view
    }



    private fun addTextToListView(str: String) {
        listItems?.add(str)
        adapter.notifyDataSetChanged()

        Log.d("Adding to list", str)
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WeatherDetailListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WeatherDetailListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
