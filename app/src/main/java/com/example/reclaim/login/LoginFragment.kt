package com.example.reclaim.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    var sharePref = requireActivity().getSharedPreferences(
        "usermanager", Context.MODE_PRIVATE
    )

lateinit var binding: FragmentLoginBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(inflater)

        // test
        // google singIn
        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)


        //
        binding.googleBtn.setOnClickListener {
            googleSignIn()

            Log.i(TAG, sharePref.all.toString())


        }



        return binding.root
    }




    private fun googleSignIn() {

        val signInClient = googleSignInClient.signInIntent
        launcher.launch(signInClient)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        if (task.isSuccessful) {
            manageResults(task)
        } else {
            val exception = task.exception
            if (exception is ApiException) {
                Log.e("GoogleSignIn", "Error code: ${exception.statusCode}")
            }
        }

        if (result.resultCode == Activity.RESULT_OK){
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            manageResults(task)
            binding.successfullyAnimation.playAnimation()
            sharePref.edit().putString("userid", auth.uid)

        }else{
            Log.e(TAG, "result failed: ${result.resultCode}")

        }

    }






    private fun manageResults(task: Task<GoogleSignInAccount>) {
        val account: GoogleSignInAccount? = task.result

        if (account != null){
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            auth.signInWithCredential(credential).addOnCompleteListener {

                if (task.isSuccessful){

                    Toast.makeText(requireContext(), "Account created", Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "success")
//                    dismissDialog()


                }else{
                    Toast.makeText(requireContext(), task.exception.toString(), Toast.LENGTH_SHORT).show()
                    Log.i(TAG, "fail")
                }
            }
        }else {
            Toast.makeText(requireContext(), task.exception.toString(), Toast.LENGTH_SHORT).show()

            Log.i(TAG, "fail")
        }
    }

    companion object {
        const val TAG = "Login"
        const val SIGN_IN = 100
    }
}