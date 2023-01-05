package com.example.myapplication.fragments.add

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.data.models.Priority
import com.example.myapplication.data.models.ToDoData
import com.example.myapplication.data.viedwmodel.ToDoViewModel
import com.example.myapplication.fragments.SharedViewModel
import kotlin.math.log


class AddFragment : Fragment() {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)


        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_add, container, false)


        // Set Menu
        setHasOptionsMenu(true)

        view?.findViewById<Spinner>(R.id.priorities_spinner)?.onItemSelectedListener = mSharedViewModel.listener

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_add){
            insertDataToDb()
        }
            return super.onOptionsItemSelected(item)
    }

    private fun insertDataToDb() {
        val mTitle = view?.findViewById<EditText>(R.id.title_et)?.text.toString()
        val mPriority = view?.findViewById<Spinner>(R.id.priorities_spinner)?.selectedItem.toString()
        val mDescription = view?.findViewById<EditText>(R.id.description_et)?.text.toString()

        Log.d("mTitle","$mTitle")
        Log.d("mPriority","$mPriority")
        Log.d("mDescription","$mDescription")
        val validation = mSharedViewModel.verifyDataFromUser(mTitle, mDescription)

        if(validation){
            // Inset Data to DB
            val newData = ToDoData(
                0,
                mTitle,
                mSharedViewModel.parsePriority(mPriority),
                mDescription
            )
            mToDoViewModel.insertData(newData)
            Toast.makeText(requireContext(), "Successfully Added", Toast.LENGTH_SHORT).show()
            // Navigate Back!
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        }else{
            Toast.makeText(requireContext(), "PLZ Fill", Toast.LENGTH_SHORT).show()

        }
    }

}