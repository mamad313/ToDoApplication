package com.example.myapplication.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.*
import com.example.myapplication.R
import com.example.myapplication.data.models.ToDoData
import com.example.myapplication.data.viedwmodel.ToDoViewModel
import com.example.myapplication.databinding.FragmentListBinding
import com.example.myapplication.fragments.SharedViewModel
import com.example.myapplication.fragments.list.adapter.ListAdapter
import com.example.myapplication.fragments.list.adapter.SwipeToDelete
import com.example.myapplication.utils.hideKeyboard
import com.example.myapplication.utils.observeOnce
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator


class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val adapter: ListAdapter by lazy { ListAdapter() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding= FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mSharedViewModel = mSharedViewModel

        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 300
        }

        swipeToDelete(binding.recyclerView)


        mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer { data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            adapter.setData(data)
        })


        // menu
        setHasOptionsMenu(true)

        // Hide Soft Keyboard
        hideKeyboard(requireActivity())


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(viewLifecycleOwner, Observer { adapter.setData(it) })
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(viewLifecycleOwner, Observer { adapter.setData(it) })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if(query != null){
            searchThroughDatabase(query)
        }
        return true
    }


    override fun onQueryTextChange(query: String?): Boolean {
        if(query != null){
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observeOnce(viewLifecycleOwner, Observer { list ->
            list?.let{
                adapter.setData(it)
            }
        })
    }

    private fun swipeToDelete(recyclerView: RecyclerView){
        val swipeToDeleteCallback = object : SwipeToDelete() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Delete Item
                val deletedItem = adapter.dataList[viewHolder.adapterPosition]
                mToDoViewModel.deleteItem(deletedItem)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
                // Restore
                restoreDeletedData(viewHolder.itemView, deletedItem, viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }


    private fun restoreDeletedData(view: View, deletedItem: ToDoData, position: Int){
        val snackBar = Snackbar.make(
            view, "Deleted '${deletedItem.title}'",
            Snackbar.LENGTH_LONG
        )
        snackBar.setAction("Undo"){
            mToDoViewModel.insertData(deletedItem)
        }
        snackBar.show()
    }


    //Show Alert to Confirm Removal of All Items from
    private fun confirmRemoval() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes"){_,_ ->
            mToDoViewModel.deleteAll()
            Toast.makeText(
                requireContext(),
                "Successfully Removed All",
                Toast.LENGTH_SHORT
            ).show()
        }
        builder.setNegativeButton("No"){_,_ -> }
        builder.setTitle("Delete All?")
        builder.setMessage("Are You Sure All?")
        builder.create().show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}