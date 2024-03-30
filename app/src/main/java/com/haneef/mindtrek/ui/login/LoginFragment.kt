package com.haneef.mindtrek.ui.login

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.haneef.mindtrek.HomeActivity
import com.haneef.mindtrek.R
import com.haneef.mindtrek.data.BackendResponse
import com.haneef.mindtrek.data.CategoryItem
import com.haneef.mindtrek.data.UserData
import com.haneef.mindtrek.databinding.ActivityHomeBinding
import com.haneef.mindtrek.databinding.FragmentLoginBinding
import com.haneef.mindtrek.util.AlertUtils
import com.haneef.mindtrek.util.ApiCallback
import com.haneef.mindtrek.util.BackgroundWorker
import com.haneef.mindtrek.util.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val REGISTRATION_ENDPOINT = "/auth/register"
    private val LOGIN_ENDPOINT = "/auth/login"
    private val client = OkHttpClient()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.loginLayout.visibility = View.VISIBLE
        binding.registerLayout.visibility = View.GONE

        // Handle the click event of the "Login" button
        binding.showLoginButton.setOnClickListener {
            // Show the login controls
            binding.showRegisterButton.setBackgroundColor(Color.argb(127, 0,0,0))
            binding.showLoginButton.setBackgroundColor(Color.argb(127, 0,0,255))
            binding.loginLayout.visibility = View.VISIBLE
            // Hide the registration controls
            binding.registerLayout.visibility = View.GONE
        }

        // Handle the click event of the "Register" button
        binding.showRegisterButton.setOnClickListener {
            // Hide the login controls
            binding.loginLayout.visibility = View.GONE
            binding.showLoginButton.setBackgroundColor(Color.argb(127, 0,0,0))
            binding.showRegisterButton.setBackgroundColor(Color.argb(127, 0,0,255))
            // Show the registration controls
            binding.registerLayout.visibility = View.VISIBLE
        }

        // You can add code to handle login and registration form submission here
        binding.loginButton.setOnClickListener {
            // Handle login logic
            // Example: Call a login function or validate login credentials
            val email = binding.loginEmailEditText.text.toString().trim()
            val password = binding.loginPasswordEditText.text.toString().trim()
            if (
                isValidEmail(email)&&
                password.length >= 4 &&
                password.length < 20
            ) {
                val userData = UserData("", password, email,"")
                login(userData)
            }
        }

        binding.registerButton.setOnClickListener {
            val username = binding.registerNameEditText.text.toString().trim()
            val email = binding.registerEmailEditText.text.toString().trim()
            val password = binding.registerPasswordEditText.text.toString().trim()
            val password2 = binding.registerPasswordEditText2.text.toString().trim()
            Log.d("USERREG", "B4B4")
            if (
                isValidEmail(email) &&
                username != "" &&
                password.length >= 4 &&
                password.length < 20 &&
                password == password2
            ) {
                val userData = UserData(username, password, email, "")
                register(userData)
            }
        }


        /*val textView: TextView = binding.textGallery
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
        binding.showRegisterButton.setBackgroundColor(Color.argb(127, 0,0,0))
        //MobileAds.initialize(requireContext()){}
        //val mAdView = binding.adView
        //val adRequest = AdRequest.Builder().build()
        //mAdView.loadAd(adRequest)

        return root
    }

    private fun isValidEmail(email: String): Boolean {
        if (email == ""){
            alert("Invalid Email")
            return false
        }
        else if (!email.contains("@") || !email.contains(".")){
            alert("Invalid Email")
            return false
        }
        return true
    }

    private fun alert(message: String) {
        val myAlert = AlertUtils(requireContext())
        myAlert.showAlert(message, message,"OK")
    }

    private fun login(userData: UserData) {
        val url = "${resources.getString(R.string.root_url)}${LOGIN_ENDPOINT}"
        val bgWorker = BackgroundWorker()
        bgWorker.fetchData(url, "POST", userData, callback = object :
            ApiCallback {
            override fun onSuccess(response: String) {
                // Parse the JSON response and update the LiveData
                Log.d("ONRESPONSE", response)
                //val previousList = categories.value.orEmpty().toMutableList()
                val alertUtils = AlertUtils(requireContext()) // 'this' is the context of your activity or fragment
                val loginResponse = Gson().fromJson(response, BackendResponse::class.java)
                if (loginResponse.message == "Invalid credentials"){
                    val myScope = CoroutineScope(Dispatchers.Main)
                    myScope.launch {
                        alertUtils.showAlert("Username or Password incorrect", "OK"){
                        }
                        hideLoadingLayout()
                    }
                }
                else if (loginResponse.access_token != ""){

                    // Display an alert
                    val myScope = CoroutineScope(Dispatchers.Main)
                    myScope.launch {
                        PrefsManager.getInstance(requireContext()).saveJwt(loginResponse.access_token)
                        PrefsManager.getInstance(requireContext()).saveUserEmail(loginResponse.user_email)
                        PrefsManager.getInstance(requireContext()).saveUsername(loginResponse.username)

                        alertUtils.showAlert("Log in Successfful", "You have successfully logged in", "OK"){
                            findNavController().navigate(R.id.login_to_home)
                        }
                        hideLoadingLayout()
                    }
                }
                else{
                    val alertUtils = AlertUtils(requireContext())
                    val myScope = CoroutineScope(Dispatchers.Main)
                    myScope.launch {
                        PrefsManager.getInstance(requireContext()).saveJwt(loginResponse.access_token)
                        alertUtils.showAlert("Incorrect Login details", "Username or password incorrect", "OK"){
                            findNavController().navigate(R.id.login_to_home)
                        }
                        hideLoadingLayout()
                    }
                }
            }

            override fun onError(error: String) {
                // Handle error if needed
                Log.d("FETCHDATA", "error2 "+error)
            }
        })
    }

    private fun register(userData: UserData) {
        val url = "${resources.getString(R.string.root_url)}${REGISTRATION_ENDPOINT}"
        val bgWorker = BackgroundWorker()
        bgWorker.fetchData(url, "POST", userData, callback = object :
            ApiCallback {
            override fun onSuccess(response: String) {
                // Parse the JSON response and update the LiveData
                Log.d("ONRESPONSE", response)
                //val previousList = categories.value.orEmpty().toMutableList()

                val alertUtils = AlertUtils(requireContext()) // 'this' is the context of your activity or fragment
                val myScope = CoroutineScope(Dispatchers.Main)
                myScope.launch {
                    alertUtils.showAlert(
                        "Registration Successfful",
                        "We have received your registration. Now, you can log in.",
                        "OK"
                    ){
                        binding.showLoginButton.performClick()
                    }
                    hideLoadingLayout()
                }

            }

            override fun onError(error: String) {
                // Handle error if needed
                Log.d("FETCHDATA", "error2 "+error)
            }
        })
    }
    private fun hideLoadingLayout() {
        //(requireActivity() as HomeActivity).hideLoadingLayout()

    }
    private fun showLoadingLayout() {
        //val activityBinding: ActivityHomeBinding? =
          //  (requireActivity() as HomeActivity).getBinding()
        //activityBinding!!.appBarMain.loadingLayout.visibility = View.VISIBLE
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}