package com.binh.android.cookies.home.account

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.binh.android.cookies.R
import com.binh.android.cookies.databinding.FragmentAccountBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class AccountFragment : Fragment() {

    companion object {
        const val TAG = "AccountFragment"
        const val SIGN_IN_RESULT_CODE = 1
        const val RC_PHOTO_PICKER = 2
    }

    private lateinit var binding: FragmentAccountBinding

    private val accountViewModel by viewModels<AccountViewModel> {
        AccountViewModel.AccountViewModelFactory(requireActivity().application)
    }

    private lateinit var bottomNavView: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = "Account"

        binding.viewModel = accountViewModel

        binding.lifecycleOwner = this

        bottomNavView = activity?.findViewById(R.id.bottom_navigation)!!

        checkUserLoggedIn()

        checkUserProfileChanged()

        return binding.root
    }

    private fun checkUserProfileChanged() {
        accountViewModel.isProfileChanged.observe(viewLifecycleOwner, {
            it?.let {
                when (it) {
                    true -> Snackbar.make(
                        bottomNavView,
                        "User profile has been changed!",
                        Snackbar.LENGTH_SHORT
                    ).setAnchorView(bottomNavView).show()
                    false -> Snackbar.make(
                        bottomNavView,
                        "Failed to change user profile!",
                        Snackbar.LENGTH_SHORT
                    ).setAnchorView(bottomNavView).show()
                }
                accountViewModel.onProfileChangedComplete()
            }
        })
    }

    private fun checkUserLoggedIn() {
        accountViewModel.isLoggedIn.observe(viewLifecycleOwner, {
            if (it) {
                accountViewModel.onLoggedIn()
                binding.accountImage.setOnClickListener {
                    setUpImagePicker()
                }
                binding.loginButton.setOnClickListener {
                    AuthUI.getInstance().signOut(requireContext())
                    Snackbar.make(
                        bottomNavView,
                        "Successfully signed out!",
                        Snackbar.LENGTH_SHORT
                    ).setAnchorView(bottomNavView).show()
                }
            } else {
                accountViewModel.onLoggedOut()
                binding.loginButton.setOnClickListener {
                    launchSignInFlow()
                }
            }
        })
    }

    private fun launchSignInFlow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(
                    providers
                ).build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == RESULT_OK) {
                Snackbar.make(
                    bottomNavView,
                    "Successfully signed in user " + "${FirebaseAuth.getInstance().currentUser?.email}!",
                    Snackbar.LENGTH_SHORT
                ).setAnchorView(bottomNavView).show()
                accountViewModel.onLoggedIn()
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            val selectedPhoto = data?.data
            accountViewModel.updateProfileImagePreview(selectedPhoto)
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