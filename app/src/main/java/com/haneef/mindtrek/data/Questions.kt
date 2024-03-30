package com.haneef.mindtrek.data

data class ResponseQuestions(val count: Int, val questions: List<Question>)
data class Questions(val questions: List<Question>)
data class Question(val question: String, val option_a: String, val option_b: String, val option_c: String, val option_d: String, val option_e: String, val answer: String, val explanation: String)