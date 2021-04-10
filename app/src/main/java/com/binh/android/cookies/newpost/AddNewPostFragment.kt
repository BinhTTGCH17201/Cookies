package com.binh.android.cookies.newpost

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.binh.android.cookies.R
import com.binh.android.cookies.databinding.FragmentAddNewPostBinding
import com.binh.android.cookies.newpost.viewmodel.NewPostViewModel
import com.binh.android.cookies.newpost.viewmodel.NewPostViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
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

    private val args: AddNewPostFragmentArgs by navArgs()

    private lateinit var progressBar: ProgressBar
    private lateinit var uploadProgressBar: ProgressBar

    private lateinit var bottomNavView: BottomNavigationView

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

        if (args.editPost) {
            (activity as AppCompatActivity).supportActionBar?.title =
                getString(R.string.edit_recipe_label)
            binding.submit.text = getString(R.string.update_button_label)
            newPostViewModel.getPost(args.postId!!)
            thisEditRecipe(args.postId!!)
        } else {
            (activity as AppCompatActivity).supportActionBar?.title =
                getString(R.string.add_new_recipe_label)
            thisAddNewRecipe()
            binding.submit.text = getString(R.string.add_new_post_button_text)
        }

        bottomNavView = activity?.findViewById(R.id.bottom_navigation)!!

        binding.submit.setOnClickListener {
            if (args.editPost) newPostViewModel.editPost(args.postId!!)
            else newPostViewModel.addNewPost()
        }

        binding.postPhoto.setOnClickListener {
            setUpImagePicker()
        }

        progressBar = binding.progressBarAdd


        setUpFoodTypeMenu()

        uploadProgressBar = binding.uploadProgressIndicator

        return binding.root
    }

    private fun thisAddNewRecipe() {
        binding.deleteButton.visibility = View.GONE

        newPostViewModel.onUpload.observe(viewLifecycleOwner, {
            when (it) {
                true -> startLoadingDialog()
                null -> cancelLoadingDialog()
            }
        })

        newPostViewModel.uploadSuccess.observe(viewLifecycleOwner, {
            when (it) {
                true -> Snackbar.make(
                    bottomNavView,
                    "Successfully add new post!",
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(bottomNavView).show()
                false -> Snackbar.make(
                    view?.rootView!!,
                    "Failed to add new post!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })

        newPostViewModel.onUploadImage.observe(viewLifecycleOwner, {
            when (it) {
                true -> {
                    uploadProgressBar.visibility = View.VISIBLE
                    uploadProgressBar.progress = newPostViewModel.uploadProgress.value!!
                }
                else -> {
                    uploadProgressBar.visibility = View.GONE
                    uploadProgressBar.progress = 0
                }
            }
        })
    }

    private fun thisEditRecipe(postId: String) {
        binding.deleteButton.visibility = View.VISIBLE

        newPostViewModel.deletedPost.observe(viewLifecycleOwner, {
            when (it) {
                true -> Snackbar.make(
                    bottomNavView,
                    "Successfully delete post!",
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(bottomNavView).show()
            }
        })
        binding.deleteButton.setOnClickListener {
            newPostViewModel.deletePost(postId)
            view?.findNavController()?.navigate(R.id.postList)
        }

        newPostViewModel.onUpload.observe(viewLifecycleOwner, {
            when (it) {
                true -> startLoadingDialog()
                null -> cancelLoadingDialog()
            }
        })

        newPostViewModel.uploadSuccess.observe(viewLifecycleOwner, {
            when (it) {
                true -> Snackbar.make(
                    bottomNavView,
                    "Successfully edit post!",
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(bottomNavView).show()
                false -> Snackbar.make(
                    view?.rootView!!,
                    "Failed to edit post!",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        })

        newPostViewModel.onUploadImage.observe(viewLifecycleOwner, {
            when (it) {
                true -> {
                    uploadProgressBar.visibility = View.VISIBLE
                    uploadProgressBar.progress = newPostViewModel.uploadProgress.value!!
                }
                else -> {
                    uploadProgressBar.visibility = View.GONE
                    uploadProgressBar.progress = 0
                }
            }
        })
    }

    private fun startLoadingDialog() {
        progressBar.visibility = View.VISIBLE
    }

    private fun cancelLoadingDialog() {
        progressBar.visibility = View.GONE
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