package com.binh.android.cookies.newpost

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.binh.android.cookies.R
import com.binh.android.cookies.databinding.FragmentAddNewPostBinding
import com.binh.android.cookies.newpost.viewmodel.NewPostViewModel
import com.binh.android.cookies.newpost.viewmodel.NewPostViewModelFactory
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddNewPostFragment : Fragment() {
    companion object {
        const val RC_PHOTO_PICKER = 1
    }

    private lateinit var newPostViewModel: NewPostViewModel

    private lateinit var binding: FragmentAddNewPostBinding

    private lateinit var dialog: AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_add_new_post, container, false)

        val application = requireNotNull(this.activity).application

        val viewModelFactory = NewPostViewModelFactory(application)

        newPostViewModel =
            ViewModelProvider(this, viewModelFactory).get(NewPostViewModel::class.java)

        binding.lifecycleOwner = this

        binding.viewModel = newPostViewModel

        (activity as AppCompatActivity).supportActionBar?.title = "New recipe"

        val builder = AlertDialog.Builder(activity)

        val layoutInflater = activity?.layoutInflater
        builder.setView(layoutInflater?.inflate(R.layout.activity_loading, null))
        builder.setCancelable(true)

        dialog = builder.create()

        newPostViewModel.onUpload.observe(viewLifecycleOwner, {
            when (it) {
                true -> startLoadingDialog()
                null -> cancelLoadingDialog()
            }
        })

        newPostViewModel.uploadSuccess.observe(viewLifecycleOwner, {
            when (it) {
                true -> Snackbar.make(
                    view?.rootView!!,
                    "Successfully add new post!",
                    Snackbar.LENGTH_SHORT
                ).show()
                false -> Snackbar.make(
                    view?.rootView!!,
                    "Failed to add new post!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })

        binding.submit.setOnClickListener {
            newPostViewModel.addNewPost()
        }

        binding.postPhoto.setOnClickListener {
            setUpImagePicker()
        }

        setUpFoodTypeMenu()

        return binding.root
    }

    private fun startLoadingDialog() {
        dialog.show()
    }

    private fun cancelLoadingDialog() {
        dialog.dismiss()
    }

    private fun setUpFoodTypeMenu() {
        val items = listOf("Daily", "Occasions", "Easy", "Healthy")
        val adapter = ArrayAdapter(requireContext(), R.layout.item_food_menu, items)
        binding.foodMenu.setAdapter(adapter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val selectedPhoto = data?.data
            GlobalScope.launch(Dispatchers.Main) {
                newPostViewModel.updatePreviewPhoto(selectedPhoto)
            }
        }
    }

    private fun setUpImagePicker() {
        val photoPicker = Intent(Intent.ACTION_GET_CONTENT)
        photoPicker.type = "image/jpeg"
        photoPicker.putExtra(Intent.EXTRA_LOCAL_ONLY, true)
        startActivityForResult(
            Intent.createChooser(photoPicker, "Complete action using"),
            RC_PHOTO_PICKER
        )
    }
}