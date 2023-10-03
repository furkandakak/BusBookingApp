package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.BusListItemBinding
import com.example.myapplication.dataClasses.Coordinate
import com.example.myapplication.dataClasses.TripsDetail

class BusListAdapter(private val list: List<Coordinate>, private val id: Int) : RecyclerView.Adapter<BusListAdapter.Holder>() {


    private val filteredList: List<Coordinate>
        get() = list.filter { it.id == id }

    // Function to extract TripsDetail objects from filteredList
    private fun getTripsDetailsList(): List<TripsDetail> {
        return filteredList.flatMap { it.trips ?: emptyList() }
    }

    private var itemClickListener: OnItemClickListener? = null

    class Holder(val binding: BusListItemBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = BusListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val tripsDetailsList = getTripsDetailsList()

        if (position < tripsDetailsList.size) {
            val trip = tripsDetailsList[position]
            holder.binding.busName.text = trip.bus_name
            holder.binding.time.text = trip.time

            holder.binding.bookButton.setOnClickListener {
                itemClickListener?.onButtonClick(position, trip,id)
            }
        } else {
            // Handle the case where the position is out of bounds.
        }
    }



    interface OnItemClickListener {
        fun onButtonClick(position: Int, item: TripsDetail, id: Int)
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.itemClickListener = listener
    }



    override fun getItemCount(): Int {
        val tripsDetailsList = getTripsDetailsList()
        return tripsDetailsList.size
    }
}
