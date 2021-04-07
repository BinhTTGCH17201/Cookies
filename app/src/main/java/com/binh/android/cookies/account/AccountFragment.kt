package com.binh.android.cookies.account

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.binh.android.cookies.R
import com.binh.android.cookies.databinding.FragmentAccountBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    companion object {
        const val TAG = "AccountFragment"
        const val SIGN_IN_RESULT_CODE = 1
        const val RC_PHOTO_PICKER = 2
    }

    private lateinit var binding: FragmentAccountBinding

    private lateinit var accountViewModel: AccountViewModel

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)

        (activity as AppCompatActivity).supportActionBar?.title = "Account"


        // testing firebase
//        val firebaseAuth = FirebaseAuth.getInstance().currentUser
//        if (firebaseAuth != null) Log.d(TAG, "Photo URL: ${firebaseAuth.displayName}")

        // Initialize view model
        val application = requireNotNull(this.activity).application

        val viewModelFactory = AccountViewModelFactory(application)

        accountViewModel =
            ViewModelProvider(this, viewModelFactory).get(AccountViewModel::class.java)

        binding.viewModel = accountViewModel

        binding.lifecycleOwner = this

        checkUserLoggedIn()

        checkUserProfileChanged()

        return binding.root
    }

    private fun checkUserProfileChanged() {
        accountViewModel.isProfileChanged.observe(viewLifecycleOwner, Observer {
            it?.let {
                when (it) {
                    true -> Snackbar.make(
                        view?.rootView!!,
                        "User profile has been changed!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    false -> Snackbar.make(
                        view?.rootView!!,
                        "Failed to change user profile!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                accountViewModel.onProfileChangedComplete()
            }
        })
    }

    private fun checkUserLoggedIn() {
        accountViewModel.isLoggedIn.observe(viewLifecycleOwner, Observer { isLoggedIn ->
            isLoggedIn?.let {
                if (isLoggedIn == true) {
                    accountViewModel.onLoggedIn()
                    binding.accountImage.setOnClickListener {
                        setUpImagePicker()
                    }
                    binding.loginButton.setOnClickListener {
                        AuthUI.getInstance().signOut(requireContext())
                    }
                } else {
                    //                    navController.popBackStack()
                    accountViewModel.onLoggedOut()
                    binding.loginButton.setOnClickListener {
                        launchSignInFlow()
                    }
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
                Log.i(
                    TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"
                )
                accountViewModel.onLoggedIn()
            } else {
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            val selectedPhoto = data?.data
            GlobalScope.launch(Dispatchers.IO) {
                accountViewModel.updateProfileImagePreview(selectedPhoto)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navController.popBackStack(R.id.accountFragment, false)
        }

        accountViewModel.authenticationState.observe(
            viewLifecycleOwner,
            Observer { authenticationState ->
                when (authenticationState) {
                    AccountViewModel.AuthenticationState.AUTHENTICATED -> {
                        accountViewModel.userLoggedIn()
                    }
                    AccountViewModel.AuthenticationState.UNAUTHENTICATED -> {
                        accountViewModel.userLoggedOut()
                    }
                    AccountViewModel.AuthenticationState.INVALID_AUTHENTICATION -> Snackbar.make(
                        view, requireActivity().getString(R.string.login_unsuccessful_msg),
                        Snackbar.LENGTH_LONG
                    ).show()
                    else -> Log.i(
                        TAG,
                        "Authentication state that doesn't require any UI change $authenticationState"
                    )
                }
            })
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