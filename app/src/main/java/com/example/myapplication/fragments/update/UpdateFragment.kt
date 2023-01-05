package com.example.myapplication.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.data.models.Priority
import com.example.myapplication.data.models.ToDoData
import com.example.myapplication.data.viedwmodel.ToDoViewModel
import com.example.myapplication.databinding.FragmentUpdateBinding
import com.example.myapplication.fragments.SharedViewModel


class UpdateFragment : Fragment() {

    private val args by navArgs<UpdateFragmentArgs>()

    private val mSharedViewModel : SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_update, container, false)

        // Set Menu
        setHasOptionsMenu(true)

        view.findViewById<EditText>(R.id.current_title_et).setText(args.currentItem.title)
        view.findViewById<EditText>(R.id.current_description_et).setText(args.currentItem.description)
        view.findViewById<Spinner>(R.id.current_priorities_spinner).setSelection(mSharedViewModel.parsePriorityToInt(args.currentItem.priority))
        view.findViewById<Spinner>(R.id.current_priorities_spinner).onItemSelectedListener = mSharedViewModel.listener

        return view

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_save -> updateItem()
            R.id.menu_delete -> confirmItemRemoval()
        }
        return super.onOptionsItemSelected(item)
    }


    private fun updateItem() {
        val title = view?.findViewById<EditText>(R.id.current_title_et)?.text.toString()
        val description = view?.findViewById<EditText>(R.id.description_et)?.text.toString()
        val getPriority = view?.findViewById<Spinner>(R.id.current_priorities_spinner)?.selectedItem.toString()

        val validation = mSharedViewModel.verifyDataFromUser(title, description)
        if(validation){
            // Update
            val updatedItem = ToDoData(
                args.currentItem.id,
                title,
                mSharedViewModel.parsePriority(getPriority),
                description
            )
            mToDoViewModel.updateData(updatedItem)
            Toast.makeText(requireContext(), "Successfully updated", Toast.LENGTH_SHORT).show()
            // Navigate back
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }else{
            Toast.makeText(requireContext(), "Plz Fill Out", Toast.LENGTH_SHORT).show()

        }
    }

    // Show Alert!
    private fun confirmItemRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_,_ ->
            mToDoViewModel.deleteItem(args.currentItem)
            Toast.makeText(
                requireContext(),
                "Successfully Removed: ${args.currentItem.title}",
                Toast.LENGTH_SHORT
            ).show()
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
        }
        builder.setNegativeButton("No"){_,_ -> }
        builder.setTitle("Delete '${args.currentItem.title}'?")
        builder.setMessage("Are You Sure ${args.currentItem.title}?")
        builder.create().show()
    }
}