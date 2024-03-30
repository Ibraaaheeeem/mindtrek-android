package com.haneef.mindtrek.ui.home

import DateTimePickerDialogFragment
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.google.gson.Gson
import com.haneef.mindtrek.HomeActivity
import com.haneef.mindtrek.R
import com.haneef.mindtrek.data.CategoryItem
import com.haneef.mindtrek.data.Mock
import com.haneef.mindtrek.data.MockData
import com.haneef.mindtrek.data.MockSubject
import com.haneef.mindtrek.data.Question
import com.haneef.mindtrek.data.QuizData
import com.haneef.mindtrek.data.ResponseQuizData
import com.haneef.mindtrek.databinding.FragmentHomeBinding
import com.haneef.mindtrek.util.AlertUtils
import com.haneef.mindtrek.util.ApiCallback
import com.haneef.mindtrek.util.BackgroundWorker
import com.haneef.mindtrek.util.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.Calendar
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment(),  DateTimePickerDialogFragment.DateTimePickerListener {

    private var mockScheduleMillis: Long = 0
    private lateinit var mockScheduleDate: String
    private lateinit var scheduleNowButton: CheckBox
    private lateinit var scheduleDateTimeButton: Button
    private var _binding: FragmentHomeBinding? = null
    private val MOCK_STYLE_TEST = "MOCK_MODE"
    private val FREE_STYLE_TEST = "FREE_MODE"
    private var testStyle = FREE_STYLE_TEST
    private lateinit var mock: Mock
    private val CREATE_MOCK_ENDPOINT="/attempt/mock"
    private val CREATE_MOCK_ENDPOINT_AI="/attempt/ai/mock"
    private var questionSource = "QBANK"
    private val binding get() = _binding!!
    private lateinit var quizbankSource: View
    private lateinit var aiSource: View
    private lateinit var freeStyleButton: View
    private lateinit var mockStyleButton: View
    private lateinit var startMockButton: View
    private lateinit var prefsManagerInstance: PrefsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        mock = Mock("user", 0)
        mockStyleButton = binding.showMockmodeButton
        freeStyleButton = binding.showFreemodeButton
        quizbankSource = binding.selectQBankSource
        scheduleDateTimeButton = binding.scheduleTime
        scheduleNowButton = binding.scheduleNow
        mockScheduleDate = ""
        aiSource = binding.selectAISource
        prefsManagerInstance = PrefsManager.getInstance(requireContext())
        startMockButton = binding.startMockButton
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        homeViewModel.setBaseLink(resources.getString(R.string.root_url))
        val categoriesFlexbox: FlexboxLayout = binding.categoriesFlexbox
        val root: View = binding.root
        
        homeViewModel.setLoadingImage(binding.loadingIcon)
        homeViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoriesFlexbox.removeAllViews()
            binding.loadingIcon.visibility = View.GONE

            categories.mapIndexed { index, categoryItem ->
                val categoriesListItemView = makeCategoryItemView(container, categoryItem)
                categoriesListItemView.setOnClickListener {
                    handleCategoryItemClick(homeViewModel, categoryItem, index)
                }
                categoriesFlexbox.addView(categoriesListItemView)
            }
        }

        aiSource.setOnClickListener{
            prefsManagerInstance.saveQuestionSource("AI")
            binding.tickAISource.visibility = View.VISIBLE
            binding.tickQBankSource.visibility = View.GONE
            homeViewModel.clearCategoriesList()
            homeViewModel.fetchCategories(source = "AI")
        }

        quizbankSource.setOnClickListener{
            prefsManagerInstance.saveQuestionSource("QBANK")
            binding.tickAISource.visibility = View.GONE
            binding.tickQBankSource.visibility = View.VISIBLE
            homeViewModel.clearCategoriesList()
            homeViewModel.fetchCategories(source = "QBANK")
        }

        mockStyleButton.setOnClickListener {
            val questionSource = prefsManagerInstance.getQuestionSource().toString()
            val jwtToken = prefsManagerInstance.getJwt()
            testStyle = MOCK_STYLE_TEST

            if (jwtToken == null) {
                findNavController().navigate(R.id.home_to_login)
                return@setOnClickListener
            }

            homeViewModel.clearCategoriesList()
            homeViewModel.fetchCategories(source=questionSource)
//            if (questionSource == "QBANK") {
//
//            } else if (questionSource == "AI") {
//                homeViewModel.fetchCategoriesAI()
//            }

            updateUIForMockStyle()
            prefsManagerInstance.saveTestStyle(MOCK_STYLE_TEST)
            setupMockSelectButtons()
        }


        freeStyleButton.setOnClickListener {
            val questionSource = prefsManagerInstance.getQuestionSource().toString()
            testStyle = FREE_STYLE_TEST
            prefsManagerInstance.saveTestStyle(FREE_STYLE_TEST)

            homeViewModel.clearCategoriesList()
            homeViewModel.fetchCategories(source = questionSource)
//            if (questionSource == "QBANK") {
//                homeViewModel.fetchCategoriesQBank()
//            } else if (questionSource == "AI") {
//                homeViewModel.fetchCategoriesAI()
//            }
            updateUIForFreeStyle()
        }

        binding.categoriesRefreshLayout.setOnClickListener {
            val questionSource = prefsManagerInstance.getQuestionSource().toString()
            homeViewModel.fetchCategories(source = questionSource)
//            if (questionSource == "AI") {
//                homeViewModel.fetchCategoriesAI()
//            } else if (questionSource == "QBANK") {
//                homeViewModel.fetchCategoriesQBank()
//            }
        }

        startMockButton.setOnClickListener {
            handleStartMockButtonClick()
        }

        binding.categoriesRefreshLayout.setOnClickListener {
            handleCategoriesRefreshLayoutClick(homeViewModel)
        }
        scheduleDateTimeButton.setOnClickListener{
            showDateTimePickerDialog()
        }
        scheduleNowButton.setOnCheckedChangeListener{view, isChecked ->
            if (isChecked) {
                scheduleDateTimeButton.setText("Schedule Date/Time")
                mockScheduleDate = ""
            }
            else {
                if (mockScheduleDate == "")
                    scheduleDateTimeButton.setText("Schedule Date/Time")
                else
                    scheduleDateTimeButton.setText(mockScheduleDate)
            }
        }
        questionSource = prefsManagerInstance.getQuestionSource().toString()
        testStyle = prefsManagerInstance.getTestStyle().toString()

        if ((testStyle == MOCK_STYLE_TEST) && prefsManagerInstance.getJwt() == null) {
            testStyle = FREE_STYLE_TEST
        }

        Glide.with(this)
            .load(R.drawable.infinity_icon)
            .into(binding.loadingIcon);

        Glide.with(this)
            .load(R.drawable.infinity_icon)
            .into(binding.mockLoading);

        showUsername()
        return root
    }

    private fun makeCategoryItemView(container: ViewGroup?, categoryItem: CategoryItem): View {
        val categoriesListItemView = layoutInflater.inflate(R.layout.item_layout, container, false)
        val titleTextView = categoriesListItemView.findViewById<TextView>(R.id.title)
        val startFreeStyleImageView = categoriesListItemView.findViewById<ImageView>(R.id.startFreeStyle)
        val addToMockImageView = categoriesListItemView.findViewById<ImageView>(R.id.addToMock)


        titleTextView.text = categoryItem.name
        titleTextView.setTextColor(getCategoryColor(categoryItem.level))

        when (testStyle) {
            FREE_STYLE_TEST -> {
                startFreeStyleImageView.visibility = View.VISIBLE
                addToMockImageView.visibility = View.GONE
            }
            MOCK_STYLE_TEST -> {
                startFreeStyleImageView.visibility = View.GONE
                addToMockImageView.visibility = View.VISIBLE
            }
        }
        startFreeStyleImageView.setOnClickListener {
            handleStartFreeStyleClick(categoryItem)
        }

        addToMockImageView.setOnClickListener {
            handleAddToMockClick(categoryItem)
        }

        return categoriesListItemView
    }

    private fun updateUIForFreeStyle() {
        binding.mockLayout.visibility = View.GONE
        binding.tickMockMode.visibility = View.GONE
        binding.tickFreeMode.visibility = View.VISIBLE
    }

    fun updateUIForMockStyle() {
        binding.mockLayout.visibility = View.VISIBLE
        binding.tickMockMode.visibility = View.VISIBLE
        binding.tickFreeMode.visibility = View.GONE
    }

    fun handleStartMockButtonClick() {
        setMockLoading(View.VISIBLE)
        questionSource = prefsManagerInstance.getQuestionSource().toString()
        val time = calculateTimeInSeconds()

        if (time == 0) {
            showAlert("Invalid time", "Select valid time for quiz")
            setMockLoading(View.GONE)
            return
        }

        if (!validateSubjects()) {
            setMockLoading(View.GONE)
            return
        }

        val mockData = MockData(mock.subjects, time, mockScheduleDate)

        if (questionSource == "AI") {
            handleAIMockCreation(mockData)
        } else if (questionSource == "QBANK") {
            handleQBankMockCreation(mockData)
        }
    }

    fun handleAIMockCreation(mockData: MockData) {
        val createMockUrl = "${resources.getString(R.string.root_url)}${CREATE_MOCK_ENDPOINT_AI}"
        val bgWorker = BackgroundWorker()
        bgWorker.fetchData(createMockUrl, "POST", mockData, token = prefsManagerInstance.getJwt().toString(), callback = object : ApiCallback {
            override fun onSuccess(response: String) {
                try {
                    val responseQuizDataAI = Gson().fromJson(response, ResponseQuizData::class.java)
                    handleAIMockResponse(responseQuizDataAI)
                } catch (e: Exception) {
                    e.printStackTrace()
                    handleMockCreationError()
                }
            }

            override fun onError(error: String) {
                Log.d("FETCH DATA ERROR", error)
                handleMockCreationError()
            }
        })
    }

    fun handleAIMockResponse(responseQuizDataAI: ResponseQuizData) {
        val message = responseQuizDataAI.msg
        if (message == "AI Mock created") {
            if (mockScheduleDate == "") {
                addQuestionsToMockData(mock.subjects, responseQuizDataAI)
            } else {
                saveQuizForDate()
                showAlert("Mock has been scheduled. You will be alerted to start 5 minutes before the time", "You have created your mock") {}
            }
        } else if (message == "Token has expired") {
            handleTokenExpiration()
        }
        setMockLoading(View.GONE)
    }

    fun handleQBankMockCreation(mockData: MockData) {
        val url = "${resources.getString(R.string.root_url)}${CREATE_MOCK_ENDPOINT}"
        val bgWorker = BackgroundWorker()
        bgWorker.fetchData(url, "POST", mockData, token = prefsManagerInstance.getJwt().toString(), callback = object : ApiCallback {
            override fun onSuccess(response: String) {
                try {
                    Log.d("ONSUCCESS", response)
                    val responseQuizData = Gson().fromJson(response, ResponseQuizData::class.java)
                    handleQBankMockResponse(responseQuizData)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.d("ONFAILURE", e.message.toString())
                    handleMockCreationError()
                }
            }

            override fun onError(error: String) {
                handleMockCreationError()
            }
        })
    }

    fun handleQBankMockResponse(responseQuizData: ResponseQuizData) {
        val message = responseQuizData.msg
        Log.d("ONSUCCESS", message)
        if (message == "Mock created") {
            // Process successful mock creation
            val quizDataHolder = responseQuizData.quiz_data.toMutableList()

            // Assign questions to subjects
            quizDataHolder.forEachIndexed { index, subject ->
                subject.questions = MutableList(subject.ids.size) { Question("", "", "", "", "", "", "", "") }

                // Alert for empty selection
                if (subject.ids.isEmpty()) {
                    val alertUtils = AlertUtils(requireContext())
                    alertUtils.showAlert(
                        "Invalid selection",
                        "No questions found for selection ${subject.subject}. Please remove it.",
                        "OK"
                    ) {
                        binding.mockLoading.visibility = View.GONE
                    }
                    return@forEachIndexed
                }
            }

            // Save quiz data
            prefsManagerInstance.saveQuizData(Gson().toJson(responseQuizData))

            // Navigate to Quiz Screen or display confirmation alert
            if (mockScheduleDate.isEmpty()) {
                navigateToQuizScreen()
            } else {
                saveQuizForDateAndDisplayConfirmation()
            }
        } else if (message == "Token has expired") {
            handleTokenExpiration()
        }
        setMockLoading(View.GONE)
    }
    private fun navigateToQuizScreen() {
        val bundle = Bundle().apply {
            putString("mode", testStyle)
            putString("source", "QBANK")
        }
        val myScope = CoroutineScope(Dispatchers.Main)
        myScope.launch {
            Log.d("INSIDE", "About to navigate")
            binding.mockLoading.visibility = View.GONE
            findNavController().navigate(R.id.home_to_quiz, bundle)
        }
    }
    private fun saveQuizForDateAndDisplayConfirmation() {
        saveQuizForDate()
        val alertUtils = AlertUtils(requireContext())
        val myScope = CoroutineScope(Dispatchers.Main)
        myScope.launch {
            alertUtils.showAlert(
                "Mock has been scheduled. You will be alerted to start 5 minutes prior to the time.",
                "You have created your mock",
                "OK"
            ) {
                binding.mockLoading.visibility = View.GONE
            }
        }
    }
    private fun setMockLoading(visibility: Int) {
        val myScope = CoroutineScope(Dispatchers.Main)
        myScope.launch {
            binding.mockLoading.visibility = visibility
        }

    }

    fun handleMockCreationError() {
        showAlert("Error", "Backend error")
        setMockLoading(View.GONE)
    }

    fun handleTokenExpiration() {
        showAlert("Login again", "Your login details have expired. You are required to login again") {
            findNavController().navigate(R.id.home_to_login)
        }
    }

    fun showAlert(title: String, message: String, onDismiss: () -> Unit = {}) {
        val myScope = CoroutineScope(Dispatchers.Main)
        myScope.launch {
            val alertUtils = AlertUtils(requireContext())
            alertUtils.showAlert(title, message, "OK", onDismiss)
        }

    }

    fun calculateTimeInSeconds(): Int {
        val hour = binding.hourValue.text.toString().toInt()
        val minute = binding.minuiteValue.text.toString().toInt()
        val second = binding.secondsValue.text.toString().toInt()
        return (hour * 3600) + (minute * 60) + second
    }

    fun validateSubjects(): Boolean {
        for (subject in mock.subjects) {
            if (subject.num_questions == 0) {
                showAlert("Invalid selection", "No question count selected for ${subject.name}")
                return false
            }
        }
        return true
    }

    fun handleCategoriesRefreshLayoutClick(homeViewModel: HomeViewModel) {
        questionSource = prefsManagerInstance.getQuestionSource().toString()
        homeViewModel.fetchCategories(source = questionSource)
//        if (questionSource == "AI") {
//            homeViewModel.fetchCategoriesAI()
//        } else if (questionSource == "QBANK") {
//            homeViewModel.fetchCategoriesQBank()
//        }
    }
    fun getCategoryColor(level: Int): Int {
        return when (level) {
            0 -> Color.CYAN
            1 -> Color.RED
            2 -> Color.GREEN
            3 -> Color.WHITE
            4 -> Color.MAGENTA
            5 -> Color.YELLOW
            else -> Color.WHITE
        }
    }

    fun handleCategoryItemClick(homeViewModel: HomeViewModel, categoryItem: CategoryItem, index: Int) {
        questionSource = prefsManagerInstance.getQuestionSource().toString()

        if (categoryItem.level == 5 && questionSource == "AI") return
        if (categoryItem.level == 3 && questionSource == "QBANK") return
        homeViewModel.fetchCategories(categoryItem.level + 1, categoryItem.id, categoryItem.name, index, questionSource)
//        if (questionSource == "AI") {
//
//        } else if (questionSource == "QBANK") {
//            homeViewModel.fetchCategoriesQBank(categoryItem.level + 1, categoryItem.id, index)
//        }
    }

    fun handleStartFreeStyleClick(categoryItem: CategoryItem) {
        val level = categoryItem.level
        val name = categoryItem.name
        val id = categoryItem.id

        val bundle = Bundle().apply {
            putInt("id", id)
            putString("tag", name)
            putInt("level", level)
            putString("mode", testStyle)
            putString("source", questionSource)
        }

        findNavController().navigate(R.id.home_to_quiz, bundle)
    }

    fun handleAddToMockClick(categoryItem: CategoryItem) {
        mock.addSubject(categoryItem.name, categoryItem.level + 1)
        Log.d("MOCK", categoryItem.toString() + "-" + mock.subjects.size.toString())
        updateMockSubjects(mock)
    }


    private fun showUsername() {
        val username = prefsManagerInstance.getUsername()
        val useremail = prefsManagerInstance.getUserEmail()

        if ((username != "GUEST" && useremail != "no email")){
            binding.usernameText.text = username
            binding.usernameText.setOnClickListener {
                findNavController().navigate(R.id.home_to_profile)
            }
        }
    }

    fun addQuestionsToMockData(subjects: List<MockSubject>, aiMockQuizData: ResponseQuizData){
        //val getQuestionsUrl = "http://10.0.2.2:5000/quizai/question"
        val getQuestionsUrl = "${resources.getString(R.string.root_url)}/quizai/question"
        val bgWorker = BackgroundWorker()
        binding.mockLoading.visibility = View.VISIBLE
        //val aiMockQuizData = mutableListOf<QuizDataAI>()
        subjects.forEachIndexed { index, subject ->
            val sujectMockQuizDataAI =
                QuizData(mutableListOf(), mutableListOf(), subject.level, subject.name, 0)
            bgWorker.fetchData(
                getQuestionsUrl + "?n=${subject.num_questions}&tag=${subject.name}&level=${subject.level}",
                "GET",
                null,
                callback = object : ApiCallback {
                    override fun onSuccess(response: String) {
                        Log.d("GET_AI_MOCK", response)
                        //val questionsData = Gson().fromJson(response.substring(response.indexOf("["), response.lastIndexOf("]")+1), Array<Question>::class.java).toList()
                        var questionsData: List<Question>
                        try {
                            questionsData =
                                Gson().fromJson(response.replace("```","").replace("json", ""), Array<Question>::class.java).toList()
                        }
                        catch (e: Exception){
                            e.printStackTrace()
                            return
                        }
                        sujectMockQuizDataAI.questions.addAll(questionsData)
                        Log.d("ADDING MOCK", ""+questionsData.size)
                        sujectMockQuizDataAI.ids = ((1..sujectMockQuizDataAI.questions.size).toList())
                        aiMockQuizData.quiz_data.add(sujectMockQuizDataAI)
                        Log.d("GET_AI_MOCK", aiMockQuizData.quiz_data.size.toString())
                        Log.d("GET_AI_MOCK", sujectMockQuizDataAI.questions.size.toString())
                        if (aiMockQuizData.quiz_data.size == subjects.size){
                            Log.d("GET_AI_MOCK", ""+index)
                            prefsManagerInstance
                                .saveQuizData(Gson().toJson(aiMockQuizData))

                            val myScope = CoroutineScope(Dispatchers.Main)
                            myScope.launch {
                                val bundle = Bundle()
                                bundle.putString("mode", testStyle)
                                bundle.putString("source", "AI")
                                binding.mockLoading.visibility = View.GONE
                                findNavController().navigate(R.id.home_to_quiz, bundle)
                            }//
                        }
                        //addQuestionsToMockData()
                    }

                    override fun onError(error: String) {
                        // Handle error if needed
                        Log.d("FETCHDATA", "error2 " + error)
                    }
                })

        }
    }
    fun showDateTimePickerDialog() {
        val dateTimePickerDialog = DateTimePickerDialogFragment()
        dateTimePickerDialog.setListener(this)
        dateTimePickerDialog.show(requireFragmentManager(), "Schedule Mock Date and Time")
    }

    private fun saveQuizForDate() {
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(requireActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        alarmManager.set(AlarmManager.RTC_WAKEUP, mockScheduleMillis, pendingIntent)
    }

    fun updateMockSubjects(mock: Mock){
        Log.d("MOCK", "HERE")
        Log.d("MOCK", mock.subjects.size.toString())
        binding.mockSubjectsLayout.removeAllViews()
        mock.subjects.forEachIndexed { index, it ->
            val view = layoutInflater.inflate(R.layout.item_mock_subject, null, false)
            view.tag = it
            view.findViewById<TextView>(R.id.subjectTextView).text = (index + 1).toString() +". "+ it.name + " (${it.num_questions})"

            view.findViewById<TextView>(R.id.indexTextView).text = (index + 1).toString()
            val mockSubjectQuestionCount = view.findViewById<TextView>(R.id.editTextNumber)
            mockSubjectQuestionCount.text = it.num_questions.toString()
            val removeMockImageView = view.findViewById<ImageView>(R.id.removeMockImageView)
            val updateQuestionNumber = view.findViewById<ImageView>(R.id.updateQuestionNumber)

            updateQuestionNumber.setOnClickListener{
                val mockSubject = (it.parent as View).tag as MockSubject
                val countEditText = (it.parent as View).findViewById<EditText>(R.id.editTextNumber)
                if (countEditText.text.toString() == "") return@setOnClickListener
                else if (countEditText.text.toString().toInt() > 5) countEditText.setText("5")
                mockSubject.setQuestionCount(countEditText.text.toString().toInt())
                updateMockSubjects(mock)
            }

            removeMockImageView.setOnClickListener{
                val mockSubject = (it.parent as View).tag as MockSubject
                mock.removeSubject(mockSubject)
                updateMockSubjects(mock)
            }
            /*mockSubjectQuestionCount.setOnFocusChangeListener { view, hasFocus ->
                    val mockSubject = (view.parent as View).tag as MockSubject
                    val countEditText = view as EditText
                    if (countEditText.text.toString() == "") return@setOnFocusChangeListener
                    mockSubject.setQuestionCount(countEditText.text.toString().toInt())
                }*/
            binding.mockSubjectsLayout.addView(view)
        }

    }

    fun format2(number: Int): String {
        return String.format("%02d", number)
    }

    private fun setupMockSelectButtons() {

        binding.hourUp.setOnClickListener{
            val hh = binding.hourValue.text.toString().toInt()
            if (hh == 99) binding.hourValue.setText("00")
            else binding.hourValue.setText(format2(binding.hourValue.text.toString().toInt() + 1))
        }

        binding.hourDown.setOnClickListener{
            val hh = binding.hourValue.text.toString().toInt()
            if (hh == 0) return@setOnClickListener
            else binding.hourValue.setText(format2(binding.hourValue.text.toString().toInt() - 1))
        }

        binding.minuiteUp.setOnClickListener{
            val mm = binding.minuiteValue.text.toString().toInt()
            if (mm == 59) binding.minuiteValue.setText("00")
            else binding.minuiteValue.setText(format2(binding.minuiteValue.text.toString().toInt() + 1))
        }

        binding.minuiteDown.setOnClickListener{
            val mm = binding.minuiteValue.text.toString().toInt()
            if (mm == 0) return@setOnClickListener
            else binding.minuiteValue.setText(format2(binding.minuiteValue.text.toString().toInt() - 1))
        }

        binding.secondsUp.setOnClickListener{
            val ss = binding.secondsValue.text.toString().toInt()
            if (ss == 59) binding.secondsValue.setText("00")
            else binding.secondsValue.setText(format2(binding.secondsValue.text.toString().toInt() + 1))
        }

        binding.secondsDown.setOnClickListener{
            val ss = binding.secondsValue.text.toString().toInt()
            if (ss == 0) return@setOnClickListener
            else binding.secondsValue.setText(format2(binding.secondsValue.text.toString().toInt() - 1))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        questionSource = prefsManagerInstance.getQuestionSource().toString()
        testStyle = prefsManagerInstance.getTestStyle()!!
        when(questionSource){
            "QBANK" -> {
                binding.tickAISource.visibility = View.GONE
                binding.tickQBankSource.visibility = View.VISIBLE
            }
            "AI" -> {
                binding.tickAISource.visibility = View.VISIBLE
                binding.tickQBankSource.visibility = View.GONE
            }
        }
        when(testStyle){
            MOCK_STYLE_TEST -> {
                binding.tickMockMode.visibility = View.VISIBLE
                binding.tickFreeMode.visibility = View.GONE
                mockStyleButton.performClick()
            }
            FREE_STYLE_TEST -> {
                binding.tickMockMode.visibility = View.GONE
                binding.tickFreeMode.visibility = View.VISIBLE
                freeStyleButton.performClick()
            }
        }

    }
    override fun onDateTimeSelected(day: Int, month: Int, year: Int, hour: Int, minute: Int) {
        // Create a Calendar object representing the selected date and time
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month) // Calendar months are 0-based
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        // Calculate the time in milliseconds from the current time
        val currentTimeMillis = System.currentTimeMillis()
        val selectedTimeMillis = calendar.timeInMillis

        // Calculate the time difference
        val timeDifferenceMillis = selectedTimeMillis - currentTimeMillis

        // Handle the selected date and time or time difference as needed
        // ...

        // Optionally, format the time difference for display
        val dateTime = "${format2(day)}-${format2(month)}-$year ${format2(hour)}:${format2(minute)}:00"
        val formattedTimeDifference = formatTimeDifference(timeDifferenceMillis)

        val myScope = CoroutineScope(Dispatchers.Main)
        myScope.launch {
            scheduleDateTimeButton.setText(dateTime)
        }
        scheduleNowButton.isChecked = false
        mockScheduleDate = dateTime
        mockScheduleMillis = selectedTimeMillis
    }

    private fun formatTimeDifference(timeDifferenceMillis: Long): String {
        val hours = TimeUnit.MILLISECONDS.toHours(timeDifferenceMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifferenceMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifferenceMillis) % 60
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

}