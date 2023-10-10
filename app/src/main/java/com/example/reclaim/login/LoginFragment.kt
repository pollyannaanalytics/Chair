package com.example.reclaim.login

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.UserManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.reclaim.R
import com.example.reclaim.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {


    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    lateinit var sharedPreferences: SharedPreferences
    private val loginViewModel: LoginViewModel by lazy {
        ViewModelProvider(this).get(LoginViewModel::class.java)
    }
    private lateinit var userManagerInSharePreference: String
    private lateinit var userIdInSharedPreferences: String
    private lateinit var userEmailInSharedPreferences: String




    lateinit var binding: FragmentLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater)
        binding.viewModel = loginViewModel
        userManagerInSharePreference = resources.getString(R.string.usermanager)
        userIdInSharedPreferences = resources.getString(R.string.userid)
        userEmailInSharedPreferences = resources.getString(R.string.useremail)

        // test
        // google singIn
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        sharedPreferences = requireContext().getSharedPreferences(
            userManagerInSharePreference, Context.MODE_PRIVATE
        )


        loginViewModel.canFindProfile.observe(viewLifecycleOwner) {
            try {
                if (it == true) {
                    loginViewModel.userProfile.value?.let { userProfile ->
                        loginViewModel.saveInUserManager(
                            userProfile
                        )
                    }
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                    )

                } else {

                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToAgreementFragment()
                    )


                }
            }catch (e: Exception){
                Log.e(TAG, "cannot navigate: $e")
            }

        }


        //
        binding.googleBtn.setOnClickListener {
            googleSignIn()


        }



        return binding.root
    }


    private fun googleSignIn() {

        val signInClient = googleSignInClient.signInIntent
        launcher.launch(signInClient)
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful) {
                manageResults(task)
            } else {
                val exception = task.exception
                if (exception is ApiException) {
                    Log.e(TAG, "Error code: ${exception.statusCode}")
                }
            }

            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    manageResults(task)


                } catch (e: Exception) {
                    Log.e(TAG, "write to share preference failed: $e")
                }


            } else {
                Log.e(TAG, "result failed: ${result.resultCode}")

            }

        }


    private fun manageResults(task: Task<GoogleSignInAccount>) {
        val account: GoogleSignInAccount? = task.result

        if (account != null) {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener {

                if (task.isSuccessful) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.successfullyAnimation.playAnimation()
                        val editor = sharedPreferences.edit()
                        editor.putString(userIdInSharedPreferences, auth.uid)
                        editor.putString(userEmailInSharedPreferences, auth.currentUser.let { it!!.email })
                        editor.apply()

                        Log.i(
                            TAG,
                            "share preference: ${
                                context?.getSharedPreferences(
                                    userManagerInSharePreference,
                                    Context.MODE_PRIVATE
                                )?.all
                            }"
                        )

                        auth.uid?.let { loginViewModel.findProfileInFirebase(it) }
                        com.example.reclaim.data.UserManager.email = sharedPreferences.all.get(userEmailInSharedPreferences).toString()
                    }, 1500)




                } else {
                    Toast.makeText(requireContext(), task.exception.toString(), Toast.LENGTH_SHORT)
                        .show()
                    Log.i(TAG, "fail")
                }
            }
        } else {
            Toast.makeText(requireContext(), task.exception.toString(), Toast.LENGTH_SHORT).show()

            Log.i(TAG, "fail")
        }
    }

    companion object {
        const val TAG = "LoginPage"
        const val SIGN_IN = 100

    }
}