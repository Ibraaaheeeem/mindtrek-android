package com.haneef.mindtrek.data

import java.io.Serializable

data class ResponseQuizData (var quiz_data: MutableList<QuizData>, val mock_id: Int, val duration: Long, val msg: String, val question_source: String): Serializable
data class QuizData (var ids: List<Int>, var questions: MutableList<Question>, val level: Int, val subject: String, val subjectId: Int): Serializable
//data class ListQuizDataAI (var quiz_data: MutableList<QuizDataAI>, val mock_id: Int, val duration: Long, val msg: String): Serializable
//data class QuizDataAI (val questions: MutableList<Question>, val level: Int, val subject: String, val subjectId: Int): Serializable
data class QuizRunning (var answers: MutableList<MutableList<Char>>, val correctAnswers: MutableList<MutableList<Char>>, var lastSubjectIndex: Int, var lastQuestionIndex: Int): Serializable
/*
data class QuizQuestionIds(val subject: String, val ids: List<Int>)*/
