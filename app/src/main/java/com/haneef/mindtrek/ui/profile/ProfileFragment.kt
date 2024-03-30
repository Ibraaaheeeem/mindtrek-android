package com.haneef.mindtrek.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.haneef.mindtrek.R
import com.haneef.mindtrek.data.Profile
import com.haneef.mindtrek.data.UserData
import com.haneef.mindtrek.databinding.FragmentProfileBinding
import com.haneef.mindtrek.util.ApiCallback
import com.haneef.mindtrek.util.BackgroundWorker
import com.haneef.mindtrek.util.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val GET_PROFILE_ENPOINT = "/auth/profile"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val profileUrl = "${resources.getString(R.string.root_url)}${GET_PROFILE_ENPOINT}"
        //val getProfileService = UrlFetchService(profileUrl)
        val bgWorker = BackgroundWorker()
        bgWorker.fetchData(
            "${resources.getString(R.string.root_url)}${GET_PROFILE_ENPOINT}",
            "GET",
            UserData("","","",""),
            PrefsManager.getInstance(requireContext()).getJwt().toString(),
            callback = object :
            ApiCallback {
            override fun onSuccess(response: String) {

                Log.d("GET PROFILE", response.toString())
                val responseProfile = Gson().fromJson(response, Profile::class.java)
                var myScope = CoroutineScope(Dispatchers.Main);
                myScope.launch { updateProfileView(responseProfile) }
            }

            override fun onError(error: String) {

            }
        })
//        val getProfile = AuthorizedEndpointCaller(
//            "${resources.getString(R.string.root_url)}${GET_PROFILE_ENPOINT}",
//            PrefsManager.getInstance(requireContext()).getJwt().toString()
//        )
//        getProfile.getEndpoint(UserData("","","",""), callback)


        // getProfileService.fetchUrl("POST",UserData("", "", "", PrefsManager.getInstance(requireContext()).getJwt().toString()), callback)
//        MobileAds.initialize(requireContext()){}
//        val mAdView = binding.adView
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
        return root
    }


    private fun updateProfileView(responseProfile: Profile?) {
        binding.textViewUserName.setText(responseProfile?.username)
        binding.textViewUserEmail.setText(responseProfile?.email)
        binding.textViewRegistrationDate.setText("Registration Date: "+responseProfile?.registration_date)
        binding.textViewLastSeenDate.setText("Last seen: "+responseProfile?.last_seen_date)
        binding.textViewExamsTaken.setText("Exams taken: "+responseProfile?.attempts_count)
        binding.btnLogout.setOnClickListener {
            PrefsManager.getInstance(requireContext()).clearJwt()
            findNavController().navigate(R.id.profile_to_home)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}