package com.tuomomees.myweatherapplication

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import androidx.core.content.ContextCompat.getSystemService



// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class WeatherDetailListFragment : androidx.fragment.app.Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    lateinit var weatherDetailObjectList: MutableList<MyWeatherDetailObject>
    private var listItems: ArrayList<String>? = null

    //private lateinit var adapter: ArrayAdapter<String>
    private lateinit var adapter: MyAdapter

    val TAG = "WeatherListFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val view = inflater.inflate(R.layout.fragment_weather_detail_list, container, false)
        val listView = view.findViewById<ListView>(R.id.weatherDetailListView)

        listView.setOnItemClickListener { _, _, position, _ ->
            (activity as MainActivity).viewPager.currentItem = position + 2
        }

        listItems = ArrayList()
        weatherDetailObjectList = (activity as MainActivity).weatherDetailObjectList

        Log.d(TAG, listView.toString())

        Log.d(TAG, "listItems" + listItems.toString())
        //adapter = ArrayAdapter(this.requireContext(), R.layout.simple_list_item_1, listItems)
        //adapter = ArrayAdapter(this.requireContext(), R.layout.custom_list_view_layout, R.id.textViewListText, listItems)
        adapter = MyAdapter(this.requireActivity(), weatherDetailObjectList)
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            WeatherDetailListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


    //Adapter for a single listview item, returns one listitem which has text and image
    class MyAdapter(
        context: Context,
        private val myWeatherDetailObjectList: List<MyWeatherDetailObject>) : BaseAdapter() {
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rowView = inflater.inflate(R.layout.custom_list_view_layout, parent, false)

            val formattedTemp = "%.0f".format(myWeatherDetailObjectList[position].temp_c) + "°C"
            val cityName = myWeatherDetailObjectList[position].cityName
            val formattedString = "$cityName $formattedTemp"

            //rowView.findViewById<TextView>(R.id.textViewListText).text = myWeatherDetailObjectList[position].cityName + " " + "%.0f".format(myWeatherDetailObjectList[position].temp_c) + "°C"
            rowView.findViewById<TextView>(R.id.textViewListText).text = formattedString
            rowView.findViewById<ImageView>(R.id.imageViewListIcon).setImageResource(myWeatherDetailObjectList[position].icon)

            return rowView
        }

        override fun getItem(position: Int): Any {
            return myWeatherDetailObjectList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return myWeatherDetailObjectList.size
        }

        private val inflater: LayoutInflater
                = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}




