package com.example.reclaim.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import com.example.reclaim.databinding.FragmentHomeBinding


/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    lateinit var dateText: TextView
    lateinit var calendarView: CalendarView



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentHomeBinding.inflate(inflater)

        dateText = binding.idTVDate
        calendarView = binding.calendarView


        // set on date change listener for calendar view
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->

            // save the date as String in variable
            val Date =  (dayOfMonth.toString() + "-"
                    + (month + 1) + "-" + year)

            // set this date in TextView for Display
            dateText.setText(Date)

        }





        return binding.root
    }


}