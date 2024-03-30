package com.haneef.mindtrek.ui.quiz

import TimerUtils
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.haneef.mindtrek.R
import com.haneef.mindtrek.data.Question
import com.haneef.mindtrek.data.QuizEndData
import com.haneef.mindtrek.data.QuizEndSubject
import com.haneef.mindtrek.data.QuizRunning
import com.haneef.mindtrek.data.ResponseQuizData
import com.haneef.mindtrek.databinding.FragmentQuizBinding
import com.haneef.mindtrek.util.AlertUtils
import com.haneef.mindtrek.util.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuizFragment : Fragment(), TimerUtils.TimerCallback  {

    private var bundle: Bundle? = null
    private lateinit var myAIQuizData: ResponseQuizData
    private lateinit var source: String
    private val GET_QUESTIONS_ENPOINT = "/quiz/questions"
    private val GET_QUESTION_ENPOINT = "/quiz/question"
    private lateinit var myQuizData: ResponseQuizData
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var thisQuestion: Question
    private lateinit var runningQuiz: QuizRunning
    private var _binding: FragmentQuizBinding? = null
    private val MOCK_STYLE_TEST = "MOCK_MODE"
    private val FREE_STYLE_TEST = "FREE_MODE"
    private var currentSubjectIndex = 0
    private var currentQuestionIndex = 0
    private var mode: String = FREE_STYLE_TEST
    private lateinit var tag: String
    private var level: Int = 0
    private var id: Int = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        Glide.with(this)
            .load(R.drawable.infinity_icon)
            .into(binding.loadingImage);

        val quizViewModel =
            ViewModelProvider(this).get(QuizViewModel::class.java)
        quizViewModel.setBaseLink(resources.getString(R.string.root_url))
        quizViewModel.setLoadingImage(binding.loadingLayout)
        bundle = arguments
        mode = bundle?.getString("mode").toString()
        source = bundle?.getString("source").toString()
        Log.d("INSIDE", "qzfg - "+mode)
        Log.d("INSIDE", "qzfg - "+source)
        if (mode == FREE_STYLE_TEST) {
            tag = bundle?.getString("tag").toString()
            mode = bundle?.getString("mode").toString()

            level = bundle!!.getInt("level")
            id = bundle!!.getInt("id")
            addRadioButtonListeners(quizViewModel)
            //if (source == "QBANK")
            quizViewModel.fetchQuestions(1, id, tag, level, source)
            //else if (source == "AI") quizViewModel.fetchQuestionsAI(1, id, tag, level, source)
            binding.quizTimer.visibility = View.GONE
            binding.previousQuestionButton.visibility = View.GONE
            binding.voluntarySubmitButton.visibility = View.GONE
            setupFreeMode(quizViewModel)
        }
        else if (mode == MOCK_STYLE_TEST) {
            if (source == "QBANK") {
                Log.d("INSIDE", "here23")
                val storedQuizData = PrefsManager.getInstance(requireContext()).getQuizData()
                if (storedQuizData == null) {
                    val alertUtils = AlertUtils(requireContext())
                    alertUtils.showAlert(
                        "No quiz data",
                        "No quiz data found. Return to select quiz data",
                        "YES"
                    ) {
                        findNavController().navigate(R.id.quiz_to_home)
                    }
                }
                Log.d("INSIDE", "QUIZ_MODE")
                Log.d("INSIDE", storedQuizData.toString())
                myQuizData = Gson().fromJson(storedQuizData, ResponseQuizData::class.java)
                setUpQuiz(source, quizViewModel)
            }
            else if (source == "AI") {
                val storedQuizData = PrefsManager.getInstance(requireContext()).getQuizData()
                if (storedQuizData == null) {
                    val alertUtils = AlertUtils(requireContext())
                    alertUtils.showAlert(
                        "No quiz data",
                        "No quiz data found. Return to select quiz data",
                        "YES"
                    ) {
                        findNavController().navigate(R.id.quiz_to_home)
                    }
                }
                Log.d("INSIDE", "AI QUIZ_MODE")
                Log.d("INSIDE", storedQuizData.toString())
                myQuizData = Gson().fromJson(storedQuizData, ResponseQuizData::class.java)
                setUpQuiz(source,  quizViewModel)
            }
        }

        val root: View = binding.root
        quizViewModel.currentQuestion.observe(viewLifecycleOwner) {

            if (it != null) {
                Log.d("What is Mode", mode.toString())
                when(mode){
                    MOCK_STYLE_TEST -> {
                        binding.questionNumber.text = ""+ (currentQuestionIndex+1)
                        binding.subjectName.text = myQuizData.quiz_data[currentSubjectIndex].subject
                        when (runningQuiz.answers[currentSubjectIndex][currentQuestionIndex]) {
                            'A' -> binding.optionA.isChecked = true
                            'B' -> binding.optionB.isChecked = true
                            'C' -> binding.optionC.isChecked = true
                            'D' -> binding.optionD.isChecked = true
                            'E' -> binding.optionE.isChecked = true
                        }

                            runningQuiz.correctAnswers[currentSubjectIndex][currentQuestionIndex] = it.answer[0]
                            Log.d("CORRECT", runningQuiz.answers.joinToString())

                        //binding.explanationLayout.visibility = View.GONE
                        //binding.loadingImage.visibility = View.GONE
                    }
                    FREE_STYLE_TEST -> {
                        currentQuestionIndex += 1
                        binding.questionNumber.text = ""+ (currentQuestionIndex)
                        binding.subjectName.text = tag
                    }
                }
                clearSelection()
                if (it.option_e == null){
                    binding.optionE.visibility = View.GONE
                }
                Log.d("GET QUESTION", "new question "+it.answer)
                var myScope = CoroutineScope(Dispatchers.Main);
                myScope.launch {
                    binding.questionText.text = it.question
                    binding.optionA.text = it.option_a
                    binding.optionB.text = it.option_b
                    binding.optionC.text = it.option_c
                    binding.optionD.text = it.option_d
                    binding.optionE.text = it.option_e
                }
            }
            //binding.loadingImage.visibility = View.GONE
        }


        return root

    }
    private fun initializeRunningQuiz(source: String): MutableList<MutableList<Char>> {
        val answers = mutableListOf<MutableList<Char>>()
        val correctAnswers = mutableListOf<MutableList<Char>>()
        var timer = TimerUtils(binding.quizTimer, 0)
        when(source){
            "QBANK" -> {
                for (i in 0..myQuizData.quiz_data.size - 1){
                    val oneSubjectAnswers = mutableListOf<Char>()
                    val oneSubjectCorrectAnswers = mutableListOf<Char>()
                    for(j in 0..myQuizData.quiz_data.get(i).ids.size - 1){
                        oneSubjectAnswers.add('O')
                        oneSubjectCorrectAnswers.add('O')
                    }
                    answers.add(oneSubjectAnswers)
                    correctAnswers.add(oneSubjectCorrectAnswers)
                }
                timer = TimerUtils(binding.quizTimer, myQuizData.duration)
            }
            "AI" -> {
                for (i in 0..myQuizData.quiz_data.size - 1){
                    val oneSubjectAnswers = mutableListOf<Char>()
                    val oneSubjectCorrectAnswers = mutableListOf<Char>()
                    for(j in 0..myQuizData.quiz_data.get(i).questions.size - 1){
                        oneSubjectAnswers.add('O')
                        oneSubjectCorrectAnswers.add('O')
                    }
                    answers.add(oneSubjectAnswers)
                    correctAnswers.add(oneSubjectCorrectAnswers)
                }
                timer = TimerUtils(binding.quizTimer, myQuizData.duration)
            }
        }

        runningQuiz = QuizRunning(answers, correctAnswers, 0, 0)
        timer.setCallback(this)
        timer.start()
        return answers
    }

    private fun setupFreeMode(quizViewModel: QuizViewModel){
        binding.nextQuestionButton.setOnClickListener{
            //quizViewModel.fetchQuestionQBank(1, tag, level, id)
            //if (source == "QBANK")
            quizViewModel.fetchQuestions(1, id, tag, level, source)
            //else if (source == "AI") quizViewModel.fetchQuestions(1, 0, tag, level, source)

        }
    }


    private fun setUpQuiz(source:String, quizViewModel: QuizViewModel) {
        optionsRadioGroup = binding.optionsRadioGroup
        optionsRadioGroup.setOnCheckedChangeListener(object : RadioGroup.OnCheckedChangeListener {
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                Log.d("SELECTED OPTION", "on checked change")
                when (checkedId){
                    R.id.option_a -> {
                        if (binding.optionA.isChecked)
                            runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'A'

                    }
                    R.id.option_b -> {
                        if (binding.optionB.isChecked)
                            runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'B'
                    }
                    R.id.option_c -> {
                        if (binding.optionC.isChecked)
                            runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'C'
                    }
                    R.id.option_d -> {
                        if (binding.optionD.isChecked)
                            runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'D'
                    }

                    R.id.option_e -> {
                        if (binding.optionE.isChecked)
                            runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'E'
                    }
                }
                Log.d("SELECTED OPTION", runningQuiz.answers[currentSubjectIndex][currentQuestionIndex]+"")
            }
        })
        binding.optionA.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                clearSelection()
                binding.optionA.isChecked = true
                runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'A'
                Log.d("SELECTED OPTION", runningQuiz.answers[currentSubjectIndex][currentQuestionIndex]+"")
            }
        })
        binding.optionB.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                clearSelection()
                binding.optionB.isChecked = true
                runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'B'
            }
        })
        binding.optionC.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                clearSelection()
                binding.optionC.isChecked = true
                runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'C'
            }
        })
        binding.optionD.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                clearSelection()
                binding.optionD.isChecked = true
                runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'D'
            }
        })
        binding.optionE.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                clearSelection()
                binding.optionE.isChecked = true
                runningQuiz.answers[currentSubjectIndex][currentQuestionIndex] = 'E'
            }
        })

        binding.submitQuizButton.setOnClickListener{
            markAnswers()
        }
        Log.d("GET QUESTION", "")
        initializeRunningQuiz(source)
        if (source == "QBANK") quizViewModel.fetchQuestionsQBank(requireContext(), myQuizData!!, currentSubjectIndex, currentQuestionIndex)
        else if (source == "AI") {
            quizViewModel.setAIQuizData(myQuizData)
            quizViewModel.setFirstQuestion()
            binding.loadingLayout.visibility = View.GONE
        }
        binding.previousQuestionButton.setOnClickListener{
            if (currentQuestionIndex > 0) {
                currentQuestionIndex -= 1
                when(source){
                    "QBANK" -> quizViewModel.fetchQuestionsQBank(requireContext(), myQuizData, currentSubjectIndex, currentQuestionIndex)
                    "AI" -> quizViewModel.setNextQuestion(currentSubjectIndex, currentQuestionIndex)
                }
            }
            else if (currentSubjectIndex > 0) {
                currentSubjectIndex -= 1
                currentQuestionIndex = myQuizData.quiz_data.get(currentSubjectIndex).ids.lastIndex
                when(source){
                    "QBANK" -> quizViewModel.fetchQuestionsQBank(requireContext(), myQuizData, currentSubjectIndex, currentQuestionIndex)
                    "AI" -> quizViewModel.setNextQuestion(currentSubjectIndex, currentQuestionIndex)
                }
            }
            else{

            }
        }
        binding.nextQuestionButton.setOnClickListener{
            Log.d("INDICES", ""+currentQuestionIndex+"-"+currentSubjectIndex)
            if (currentQuestionIndex < (myQuizData.quiz_data.get(currentSubjectIndex).ids.size - 1)) {
                currentQuestionIndex += 1
                when(source){
                    "QBANK" -> quizViewModel.fetchQuestionsQBank(requireContext(), myQuizData, currentSubjectIndex, currentQuestionIndex)
                    "AI" -> quizViewModel.setNextQuestion(currentSubjectIndex, currentQuestionIndex)
                }
            }
            else if (currentSubjectIndex < (myQuizData.quiz_data.size - 1)) {
                currentSubjectIndex += 1
                currentQuestionIndex = 0
                when(source){
                    "QBANK" -> quizViewModel.fetchQuestionsQBank(requireContext(), myQuizData, currentSubjectIndex, currentQuestionIndex)
                    "AI" -> quizViewModel.setNextQuestion(currentSubjectIndex, currentQuestionIndex)
                }
            }
            else{
                val alertUtils = AlertUtils(requireContext())
                alertUtils.showAlert("Submit?", "Will you like to submit your quiz?", "YES"){
                    markAnswers()
                }
            }
        }
        binding.voluntarySubmitButton.setOnClickListener {
            if (this.isAdded) {
                val alertUtils = AlertUtils(requireActivity())
                alertUtils.showAlert("Are you sure you want to submit your quiz", "YES") {
                    markAnswers()
                }
            }
        }
    }

    private fun addRadioButtonListeners(quizViewModel: QuizViewModel) {
        binding.optionA.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                if (quizViewModel.currentQuestion.value == null) return@OnCheckedChangeListener
                clearSelection()
                binding.optionA.isChecked = true
                if (quizViewModel.currentQuestion.value?.answer == "A") {
                    binding.optionA.background.setTint(Color.GREEN)
                }
                else {
                    binding.optionA.background.setTint(Color.RED)
                    markCorrectAnswer(quizViewModel.currentQuestion.value?.answer!![0])
                }
                showExplanation(quizViewModel.currentQuestion.value!!)
            }
        })
        binding.optionB.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                if (quizViewModel.currentQuestion.value == null) return@OnCheckedChangeListener
                clearSelection()
                binding.optionB.isChecked = true

                if (quizViewModel.currentQuestion.value?.answer == "B") {
                    binding.optionB.background.setTint(Color.GREEN)
                }
                else {
                    binding.optionB.background.setTint(Color.RED)
                    markCorrectAnswer(quizViewModel.currentQuestion.value?.answer!![0])
                }
                showExplanation(quizViewModel.currentQuestion.value!!)
            }
        })
        binding.optionC.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {
                if (quizViewModel.currentQuestion.value == null) return@OnCheckedChangeListener
                clearSelection()
                binding.optionC.isChecked = true

                if (quizViewModel.currentQuestion.value?.answer == "C"){
                    binding.optionC.background.setTint(Color.GREEN)
                }
                else{
                    binding.optionC.background.setTint(Color.RED)
                    markCorrectAnswer(quizViewModel.currentQuestion.value?.answer!![0])
                }
                showExplanation(quizViewModel.currentQuestion.value!!)
            }
        })
        binding.optionD.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (quizViewModel.currentQuestion.value == null) return@OnCheckedChangeListener
                clearSelection()
                binding.optionD.isChecked = true

                if (quizViewModel.currentQuestion.value?.answer == "D"){
                    binding.optionD.background.setTint(Color.GREEN)
                }
                else{
                    binding.optionD.background.setTint(Color.RED)
                    markCorrectAnswer(quizViewModel.currentQuestion.value?.answer!![0])
                }
                showExplanation(quizViewModel.currentQuestion.value!!)
            }
        })
        binding.optionE.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (quizViewModel.currentQuestion.value == null) return@OnCheckedChangeListener
                clearSelection()
                binding.optionE.isChecked = true

                if (quizViewModel.currentQuestion.value?.answer == "E"){
                    binding.optionE.background.setTint(Color.GREEN)
                }
                else{
                    binding.optionE.background.setTint(Color.RED)
                    markCorrectAnswer(quizViewModel.currentQuestion.value?.answer!![0])
                }
                showExplanation(quizViewModel.currentQuestion.value!!)
            }
        })


    }

    private fun showExplanation(thisQuestion: Question) {
        binding.explanationLayout.visibility = View.VISIBLE
        binding.explanationText.setText((thisQuestion.explanation))
    }

    private fun markCorrectAnswer(correctAnswer: Char) {
        when(correctAnswer){
            'A' -> binding.optionA.background.setTint(Color.GREEN)
            'B' -> binding.optionB.background.setTint(Color.GREEN)
            'C' -> binding.optionC.background.setTint(Color.GREEN)
            'D' -> binding.optionD.background.setTint(Color.GREEN)
            'E' -> binding.optionE.background.setTint(Color.GREEN)
        }
    }
    fun markAnswers(){
        Log.d("CORRECT", "MARK ANSWERS")
        var totalCorrect = 0
        var totalAnswered = 0
        var totalQuestions = 0

        val quizEndSubjects = mutableListOf<QuizEndSubject>()
        for(i in 0..myQuizData.quiz_data.size - 1){
            var subjectScore = 0
            var subjectQuestions = 0
            var subjectName = ""
            for(j in 0..myQuizData.quiz_data.get(i).ids.size - 1){
                subjectQuestions+=1
                totalQuestions += 1

                if (runningQuiz.answers[i][j] != 'O') {
                    totalAnswered += 1
                    Log.d("totalAnswered",""+totalAnswered+"-"+runningQuiz.correctAnswers[i][j]+"-"+runningQuiz.answers[i][j])
                    if (runningQuiz.correctAnswers[i][j] == runningQuiz.answers[i][j]){
                        subjectScore += 1
                        totalCorrect += 1
                    }
                }
            }
            val quizEndSubject = QuizEndSubject(
                myQuizData.quiz_data.get(i).subjectId,
                myQuizData.quiz_data.get(i).subject,
                subjectScore,
                subjectQuestions
            )
            quizEndSubjects.add(quizEndSubject)
        }
        Log.d("MARKING", totalCorrect.toString())
        Log.d("MARKING", totalAnswered.toString())
        Log.d("MARKING", totalQuestions.toString())
        val quizEndData = QuizEndData(
            myQuizData.mock_id,
            myQuizData.quiz_data.size,
            totalCorrect,
            totalQuestions,
            quizEndSubjects
        )

        bundle?.putSerializable("my_quiz_end_data", quizEndData)
        bundle?.putSerializable("my_running_quiz", runningQuiz)
        bundle?.putSerializable("my_quiz_data", myQuizData)
        bundle?.putSerializable("my_source", source)
        if (quizEndData == null){
            Log.d("INSIDE", "QEDNULL")
        }
        else if (runningQuiz == null){
            Log.d("INSIDE", "RQZNULL")
        }
        else if (myQuizData == null){
            Log.d("INSIDE", "MQDNULL")
        }
        Log.d("INSIDE", quizEndData.nSubjects.toString())
        Log.d("INSIDE", runningQuiz.correctAnswers.size.toString())
        Log.d("INSIDE", myQuizData.duration.toString())
        if (this.isAdded)
            findNavController().navigate(R.id.quiz_to_score, bundle)
    }
    override fun onTimerFinished() {
        if (this.isAdded) {
            Log.d("TIMER FINISHED", "GO AND SUBMIT")
            val alertUtils = AlertUtils(requireActivity())
            alertUtils.showAlert("Time is up", "Sorry, your time is up, now you will be required to submit your mock for grading", "OK"){
                markAnswers()
            }
            binding.nextQuestionButton.visibility = View.GONE
            binding.previousQuestionButton.visibility = View.GONE
            binding.submitQuizButton.visibility = View.GONE;
        }

        // Callback logic when the timer finishes
        // You can perform actions here when the timer stops
    }

    private fun clearSelection(){
        binding.optionA.background.setTint(Color.WHITE)
        binding.optionB.background.setTint(Color.WHITE)
        binding.optionC.background.setTint(Color.WHITE)
        binding.optionD.background.setTint(Color.WHITE)
        binding.optionE.background.setTint(Color.WHITE)

        binding.optionA.isChecked = false
        binding.optionB.isChecked = false
        binding.optionC.isChecked = false
        binding.optionD.isChecked = false
        binding.optionE.isChecked = false

        binding.explanationText.text = ""
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}