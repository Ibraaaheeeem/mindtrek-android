package com.haneef.mindtrek.ui.quiz

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.haneef.mindtrek.R
import com.haneef.mindtrek.data.Question
import com.haneef.mindtrek.data.ResponseQuizData
import com.haneef.mindtrek.util.AlertUtils
import com.haneef.mindtrek.util.ApiCallback
import com.haneef.mindtrek.util.BackgroundWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuizViewModel : ViewModel() {
    private lateinit var rootUrl: String
    private lateinit var questionLoadingLayout: LinearLayout
    private val GET_QUESTIONS_ENPOINT = "/quiz/questions"
    private val GET_QUESTION_ENPOINT = "/quiz/question"
    private lateinit var aiMockQuizData: ResponseQuizData
    private val _currentAnswer = MutableLiveData<Char>().apply {
        value = 'O'
    }
    val currentAnswer: LiveData<Char> = _currentAnswer

    private val _questions = MutableLiveData<Question>().apply {
        value = null
    }
    private val _currentQuestion = MutableLiveData<Question>().apply {
        value = null
    }
    val currentQuestion: LiveData<Question> = _currentQuestion

    fun fetchQuestions(n: Int, id: Int, tag: String, level: Int, source: String) {
        val bgWorker = BackgroundWorker()
        setLoadingStarted()
        bgWorker.fetchData("${getUrl(source, n, id, tag, level)}", "GET", null, callback = object : ApiCallback {
            override fun onSuccess(response: String) {
                handleSuccessResponse(response)
            }

            override fun onError(error: String) {
                handleError(error)
            }
        })
    }

    private fun getUrl(source: String, n:Int, id:Int, tag: String, level: Int): String {
        if (source == "AI") {
            return "$rootUrl/quizai/question?n=$n&tag=$tag&level=$level"
        }
        else if (source == "QBANK") {
            return "$rootUrl/quiz/questions/level/${level+1}/category/${id}?n=${n}"
        }
        return "$rootUrl/quiz/questions/level/${level+1}/category/${id}?n=${n}"
    }

    private fun handleSuccessResponse(response: String) {
        try {
            val questionsData = Gson().fromJson(response.replace("```", "").replace("json", "").trim(), Array<Question>::class.java).toList()
            Log.d("FETCHDATA", "response: $response")
            if (questionsData.isNotEmpty()) {
                _questions.postValue(questionsData[0])
                _currentQuestion.postValue(questionsData[0])
            }
            setLoadingEnded()
        } catch (e: Exception) {
            setLoadingStarted()
        }
    }

    private fun handleError(error: String) {
        // Handle error if needed
        Log.d("FETCHDATA", "error: $error")
        setLoadingEnded()
    }

    fun fetchQuestionsQBank(context: Context, quizData: ResponseQuizData, subjectIndex: Int, questionIndex: Int){

        val subject = quizData.quiz_data.get(subjectIndex).subject
        //val questionUrl = "${context.resources.getString(R.string.root_url)}${GET_QUESTIONS_ENPOINT}/level/${quizData.quiz_data.get(0).level}/category/${quizData.quiz_data.get(0).subjectId}?n=1"
        val questionUrl = "$rootUrl${GET_QUESTION_ENPOINT}/${quizData.quiz_data.get(subjectIndex).ids.get(questionIndex)}"
        if (quizData.quiz_data.size <= 0 || quizData.quiz_data.get(subjectIndex).ids.size <= 0) {
            val alertUtils = AlertUtils(context)
            alertUtils.showAlert("No questions found", "There are no questions available in "+subject, "O.K"){
                //findNavController().navigate(R.id.quiz_to_home)
            }
            return
        }
        setLoadingStarted()
        val bgWorker = BackgroundWorker()
        bgWorker.fetchData(questionUrl, "GET", null, callback = object :
            ApiCallback {
            override fun onSuccess(response: String) {
                // Parse the JSON response and update the LiveData
                Log.d("ONRESPONSE", response)
                //val responseBody = response.body?.string()
                Log.d("GET QUESTION", response.toString())
                val responseQuestion = Gson().fromJson(response.toString(), Question::class.java)
                var myScope = CoroutineScope(Dispatchers.Main);
                //val previousList = categories.value.orEmpty().toMutableList()
                _currentQuestion.postValue(responseQuestion)
                setLoadingEnded()
            }

            override fun onError(error: String) {
                // Handle error if needed
                Log.d("FETCHDATA", "error2 "+error)
                setLoadingEnded()
            }
        })
    }

    fun fetchQuestions(n: Int, tag: String, level: Int, id: Int? = null, isAI: Boolean = false) {
        val endpoint = if (isAI) "quizai/question" else "quiz/questions/level/${level + 1}/category/$id"
        val url = "$rootUrl/$endpoint"
        val bgWorker = BackgroundWorker()
        setLoadingStarted()
        bgWorker.fetchData("$url?n=$n&tag=$tag&level=$level", "GET", null, callback = object : ApiCallback {
            override fun onSuccess(response: String) {
                Log.d("ONRESPONSE", response)
                try {
                    val questionsData = Gson().fromJson(response.replace("```", "").replace("json", "").trim(), Array<Question>::class.java).toList()
                    if (questionsData.isNotEmpty()) {
                        _questions.postValue(questionsData[0])
                        _currentQuestion.postValue(questionsData[0])
                    }
                    setLoadingEnded()
                } catch (e: Exception) {
                    setLoadingStarted()
                }
            }

            override fun onError(error: String) {
                Log.d("FETCHDATA", "error: $error")
                setLoadingEnded()
            }
        })
    }

    fun setAIQuizData(data: ResponseQuizData){
        aiMockQuizData = data
    }
    fun setNextQuestion(subjectIndex: Int, questionIndex: Int) {
        _currentQuestion.postValue(aiMockQuizData.quiz_data[subjectIndex].questions[questionIndex])
    }

    fun setFirstQuestion() {
        _currentQuestion.postValue(aiMockQuizData.quiz_data[0].questions[0])
    }

    fun setLoadingImage(loadingImage: LinearLayout) {
        questionLoadingLayout = loadingImage
    }

    fun setLoadingStarted(){
        questionLoadingLayout.visibility = View.VISIBLE
        Log.d("LOADING", "started")
    }

    fun setLoadingEnded(){
        var myScope = CoroutineScope(Dispatchers.Main);
        myScope.launch {
            questionLoadingLayout.visibility = View.GONE
            Log.d("LOADING", "stopped")
        }
    }

    fun setBaseLink(string: String) {
        rootUrl = string
    }
}