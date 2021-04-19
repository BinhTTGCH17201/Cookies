package com.binh.android.cookies.home.newpost

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
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.basgeekball.awesomevalidation.AwesomeValidation
import com.basgeekball.awesomevalidation.ValidationStyle
import com.basgeekball.awesomevalidation.utility.RegexTemplate
import com.basgeekball.awesomevalidation.utility.custom.SimpleCustomValidation
import com.binh.android.cookies.R
import com.binh.android.cookies.databinding.FragmentAddNewPostBinding
import com.binh.android.cookies.home.newpost.viewmodel.NewPostViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddNewPostFragment : Fragment() {
    companion object {
        const val RC_PHOTO_PICKER = 1
    }

    private val newPostViewModel by viewModels<NewPostViewModel> {
        NewPostViewModel.NewPostViewModelFactory()
    }

    private val mAwesomeValidation = AwesomeValidation(ValidationStyle.TEXT_INPUT_LAYOUT)

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

        binding.lifecycleOwner = this

        binding.viewModel = newPostViewModel

        initFragment()

        setUpFoodTypeMenu()

        uploadProgressBar = binding.uploadProgressIndicator

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mAwesomeValidation.addValidation(
            activity,
            R.id.post_title_label,
            RegexTemplate.NOT_EMPTY,
            R.string.error_input_text
        )
        mAwesomeValidation.addValidation(
            activity,
            R.id.post_ingredient_label,
            RegexTemplate.NOT_EMPTY,
            R.string.error_input_text
        )
        mAwesomeValidation.addValidation(
            activity,
            R.id.post_people_label,
            RegexTemplate.NOT_EMPTY,
            R.string.error_input_text
        )
        mAwesomeValidation.addValidation(
            activity,
            R.id.post_time_label,
            RegexTemplate.NOT_EMPTY,
            R.string.error_input_text
        )
        mAwesomeValidation.addValidation(
            activity,
            R.id.post_preparation_label,
            RegexTemplate.NOT_EMPTY,
            R.string.error_input_text
        )
        mAwesomeValidation.addValidation(activity, R.id.food_type, SimpleCustomValidation { input ->
            return@SimpleCustomValidation (input != "")
        }, R.string.error_input_text)


    }

    private fun initFragment() {
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
            mAwesomeValidation.validate()
        }

        binding.postPhoto.setOnClickListener {
            setUpImagePicker()
        }

        progressBar = binding.progressBarAdd
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
                true -> {
                    Snackbar.make(
                        bottomNavView,
                        "Successfully delete post!",
                        Snackbar.LENGTH_SHORT
                    ).setAnchorView(bottomNavView).show()
                    view?.findNavController()?.navigate(R.id.postList)
                }
            }
        })
        binding.deleteButton.setOnClickListener {
            newPostViewModel.deletePost(postId)
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