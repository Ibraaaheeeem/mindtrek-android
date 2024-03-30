package com.haneef.mindtrek.ui.quiz

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.haneef.mindtrek.R
import com.haneef.mindtrek.data.Question
import com.haneef.mindtrek.data.QuizEndData
import com.haneef.mindtrek.data.QuizRunning
import com.haneef.mindtrek.data.ResponseQuizData
import com.haneef.mindtrek.databinding.FragmentQuizBinding
import com.haneef.mindtrek.util.AlertUtils
import com.haneef.mindtrek.util.ApiCallback
import com.haneef.mindtrek.util.BackgroundWorker
import com.haneef.mindtrek.util.PrefsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException

class CorrectionsFragment: Fragment() {

    private lateinit var source: String
    private var currentSubjectIndex = 0
    private var currentQuestionIndex = 0
    private lateinit var quizEndData: QuizEndData
    private lateinit var runningQuiz: QuizRunning
    private lateinit var myQuizData: ResponseQuizData
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val GET_QUESTION_ENPOINT = "/quiz/question"


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        myQuizData = Gson().fromJson(PrefsManager.getInstance(requireContext()).getQuizData(), ResponseQuizData::class.java)
        val bundle = arguments
        source = bundle?.getString("my_source").toString()
        if (bundle != null) {
            quizEndData = (bundle.getSerializable("my_quiz_end_data") as QuizEndData?)!!
            runningQuiz = (bundle.getSerializable("my_running_quiz") as QuizRunning?)!!
            val myQuizData = bundle.getSerializable("my_quiz_data") as ResponseQuizData?
            Log.d("runningquiz", runningQuiz.correctAnswers.joinToString())
            Log.d("runningquiz", runningQuiz.answers.joinToString())
            if (myQuizData != null) {
                if (source == "AI"){
                    binding.loadingLayout.visibility = View.GONE
                }
                // Now you have the quizEndData object, use it as needed
                setupQuestion(myQuizData, 0,0)
                binding.nextQuestionButton.setOnClickListener{
                    if (currentQuestionIndex < myQuizData.quiz_data.get(currentSubjectIndex).ids.size - 1) {
                        currentQuestionIndex += 1
                        setupQuestion(myQuizData, currentSubjectIndex, currentQuestionIndex)
                    }
                    else if (currentSubjectIndex < myQuizData.quiz_data.size - 1) {
                        currentSubjectIndex += 1
                        currentQuestionIndex = 0
                        setupQuestion(myQuizData, currentSubjectIndex, currentQuestionIndex)
                    }
                    else {
                        val alertUtils = AlertUtils(requireContext())
                        alertUtils.showAlert("Final Question", "This is the final question", "OK"){
                        }
                    }
                }
                binding.previousQuestionButton.setOnClickListener{
                    if (currentQuestionIndex > 0) {
                        currentQuestionIndex -= 1
                        setupQuestion(myQuizData, currentSubjectIndex, currentQuestionIndex)
                    }
                    else if (currentSubjectIndex > 0) {
                        currentSubjectIndex -= 1
                        currentQuestionIndex = myQuizData.quiz_data.get(currentSubjectIndex).ids.lastIndex
                        setupQuestion(myQuizData, currentSubjectIndex, currentQuestionIndex)
                    }
                    else{
                        val alertUtils = AlertUtils(requireContext())
                        alertUtils.showAlert("No more Question", "No more previous question", "OK"){
                        }
                    }
                }
                binding.voluntarySubmitButton.visibility = View.GONE
            }
        }
//        MobileAds.initialize(requireContext()){}
//        val mAdView = binding.adView
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
        return binding.root
    }

    private fun setupQuestion(quizData: ResponseQuizData, subjectIndex: Int, questionIndex: Int) {
        val subject = quizData.quiz_data.get(subjectIndex).subject
        //val questionUrl = "${resources.getString(R.string.root_url)}${GET_QUESTIONS_ENPOINT}/level/${quizData.quiz_data.get(0).level}/category/${quizData.quiz_data.get(0).subjectId}?n=1"
        if (quizData.quiz_data.size <= 0 || quizData.quiz_data.get(subjectIndex).ids.size <= 0) {
            return
        }
        if (source == "AI"){
            updateQuestionDataView(subjectIndex, subject, myQuizData.quiz_data[subjectIndex].questions[questionIndex], questionIndex)
            return
        }
        val questionUrl = "${resources.getString(R.string.root_url)}${GET_QUESTION_ENPOINT}/${quizData.quiz_data.get(subjectIndex).ids.get(questionIndex)}"
        val bgWorker = BackgroundWorker()
        binding.loadingImage.visibility= View.VISIBLE
        bgWorker.fetchData(questionUrl, "GET", "", "", callback = object : ApiCallback {

            override fun onSuccess(response: String) {
                Log.d("GET QUESTION", response)
                val responseQuestion = Gson().fromJson(response, Question::class.java)
                var myScope = CoroutineScope(Dispatchers.Main);
                myScope.launch {
                    updateQuestionDataView(subjectIndex, subject, responseQuestion, questionIndex);
                    binding.loadingImage.visibility= View.GONE
                }
            }

            override fun onError(error: String) {
                var myScope = CoroutineScope(Dispatchers.Main);
                myScope.launch {

                    binding.loadingImage.visibility= View.GONE
                }
            }

        })
        //bgWorker.fetchData(questionUrl, "GET", "", "", callback)
        //getQuestionService.fetchUrl("GET","", callback)
    }
    fun updateQuestionDataView(
        subjectIndex: Int,
        subjectName: String,
        question: Question,
        questionNumber: Int
    ){
        binding.optionsRadioGroup.clearCheck()
        binding.subjectName.text = subjectName
        binding.questionNumber.text = ""+(questionNumber + 1).toString()
        binding.questionText.text = question.question
        binding.optionA.text = question.option_a
        binding.optionB.text = question.option_b
        binding.optionC.text = question.option_c
        binding.optionD.text = question.option_d
        if ((question.option_e == "") || (question.option_e == null)) {
            binding.optionE.visibility = View.GONE
        }
        else binding.optionE.text = question.option_e
        runningQuiz.correctAnswers[subjectIndex][questionNumber] = question.answer[0]
        Log.d("CORRECT", runningQuiz.answers.joinToString())
        clearSelection()


        when (runningQuiz.answers[subjectIndex][questionNumber]){
            'A' -> {
                binding.optionA.isChecked = true
                if (question.answer[0] == 'A') {
                    binding.optionALayout.setBackgroundColor(Color.GREEN)
                    binding.optionATick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionATick.visibility = View.VISIBLE
                }
                else {
                    binding.optionALayout.setBackgroundColor(Color.RED)
                    binding.optionATick.setImageResource(R.drawable.baseline_cancel_24)
                    binding.optionATick.visibility = View.VISIBLE
                    markRightAnswer(runningQuiz.correctAnswers[subjectIndex][questionNumber])
                }
            }
            'B' -> {
                binding.optionB.isChecked = true
                if (question.answer[0] == 'B') {
                    binding.optionBLayout.setBackgroundColor(Color.GREEN)
                    binding.optionBTick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionBTick.visibility = View.VISIBLE
                }
                else {
                    binding.optionBLayout.setBackgroundColor(Color.RED)
                    binding.optionBTick.setImageResource(R.drawable.baseline_cancel_24)
                    binding.optionBTick.visibility = View.VISIBLE
                    markRightAnswer(runningQuiz.correctAnswers[subjectIndex][questionNumber])
                }
            }
            'C' -> {
                binding.optionC.isChecked = true
                if (question.answer[0] == 'C') {
                    binding.optionCLayout.setBackgroundColor(Color.GREEN)
                    binding.optionCTick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionCTick.visibility = View.VISIBLE
                }
                else {
                    binding.optionCLayout.setBackgroundColor(Color.RED)
                    binding.optionCTick.setImageResource(R.drawable.baseline_cancel_24)
                    binding.optionCTick.visibility = View.VISIBLE
                    markRightAnswer(runningQuiz.correctAnswers[subjectIndex][questionNumber])
                }
            }
            'D' -> {
                binding.optionD.isChecked = true
                if (question.answer[0] == 'D') {
                    binding.optionDLayout.setBackgroundColor(Color.GREEN)
                    binding.optionDTick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionDTick.visibility = View.VISIBLE
                }
                else {
                    binding.optionDLayout.setBackgroundColor(Color.RED)
                    binding.optionDTick.setImageResource(R.drawable.baseline_cancel_24)
                    binding.optionDTick.visibility = View.VISIBLE
                    markRightAnswer(runningQuiz.correctAnswers[subjectIndex][questionNumber])
                }
            }
            'E' -> {
                binding.optionE.isChecked = true
                if (question.answer[0] == 'E') {
                    binding.optionELayout.setBackgroundColor(Color.GREEN)
                    binding.optionETick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionETick.visibility = View.VISIBLE
                }
                else {
                    binding.optionELayout.setBackgroundColor(Color.RED)
                    binding.optionETick.setImageResource(R.drawable.baseline_cancel_24)
                    binding.optionETick.visibility = View.VISIBLE
                    markRightAnswer(runningQuiz.correctAnswers[subjectIndex][questionNumber])
                }
            }
            'O' -> {
                if (question.answer[0] == 'A') {
                    binding.optionALayout.setBackgroundColor(Color.GREEN)
                    binding.optionATick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionATick.visibility = View.VISIBLE
                }
                else if (question.answer[0] == 'B') {
                    binding.optionBLayout.setBackgroundColor(Color.GREEN)
                    binding.optionBTick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionBTick.visibility = View.VISIBLE
                }
                else if (question.answer[0] == 'C') {
                    binding.optionCLayout.setBackgroundColor(Color.GREEN)
                    binding.optionCTick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionCTick.visibility = View.VISIBLE
                }
                else if (question.answer[0] == 'D') {
                    binding.optionDLayout.setBackgroundColor(Color.GREEN)
                    binding.optionDTick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionDTick.visibility = View.VISIBLE
                }
                else if (question.answer[0] == 'E') {
                    binding.optionELayout.setBackgroundColor(Color.GREEN)
                    binding.optionETick.setImageResource(R.drawable.baseline_check_24)
                    binding.optionETick.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun markRightAnswer(answer: Char) {
        when(answer){
            'A' -> {
                binding.optionALayout.setBackgroundColor(Color.GREEN)
                binding.optionATick.setImageResource(R.drawable.baseline_check_24)
                binding.optionATick.visibility = View.VISIBLE
            }
            'B' -> {
                binding.optionBLayout.setBackgroundColor(Color.GREEN)
                binding.optionBTick.setImageResource(R.drawable.baseline_check_24)
                binding.optionBTick.visibility = View.VISIBLE
            }
            'C' -> {
                binding.optionCLayout.setBackgroundColor(Color.GREEN)
                binding.optionCTick.setImageResource(R.drawable.baseline_check_24)
                binding.optionCTick.visibility = View.VISIBLE
            }
            'D' -> {
                binding.optionDLayout.setBackgroundColor(Color.GREEN)
                binding.optionDTick.setImageResource(R.drawable.baseline_check_24)
                binding.optionDTick.visibility = View.VISIBLE
            }
            'E' -> {
                binding.optionELayout.setBackgroundColor(Color.GREEN)
                binding.optionETick.setImageResource(R.drawable.baseline_check_24)
                binding.optionETick.visibility = View.VISIBLE
            }
        }
    }

    private fun clearSelection() {
        binding.optionA.isChecked = false
        binding.optionB.isChecked = false
        binding.optionC.isChecked = false
        binding.optionD.isChecked = false
        binding.optionE.isChecked = false

        binding.optionALayout.setBackgroundColor(Color.WHITE)
        binding.optionBLayout.setBackgroundColor(Color.WHITE)
        binding.optionCLayout.setBackgroundColor(Color.WHITE)
        binding.optionDLayout.setBackgroundColor(Color.WHITE)
        binding.optionELayout.setBackgroundColor(Color.WHITE)

        binding.optionATick.visibility = View.GONE
        binding.optionBTick.visibility = View.GONE
        binding.optionCTick.visibility = View.GONE
        binding.optionDTick.visibility = View.GONE
        binding.optionETick.visibility = View.GONE
    }


}