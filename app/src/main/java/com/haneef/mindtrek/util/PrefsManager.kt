package com.haneef.mindtrek.util

import android.content.Context
import android.content.SharedPreferences

class PrefsManager private constructor(context: Context) {
    private val MOCK_STYLE_TEST = "MOCK_MODE"
    private val FREE_STYLE_TEST = "FREE_MODE"
    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "MyPrefs"
        private const val KEY_JWT = "jwt_token"
        private const val QUIZ_DATA = "quiz_data"
        private const val QUIZ_POSITION_SUBJECT = "quiz_position_subject"
        private const val QUIZ_POSITION_QUESTION = "quiz_position_question"
        private const val QUIZ_MODE = "QUIZ_MODE"
        private const val USER_EMAIL = "no email"
        private const val USERNAME = "GUEST"
        private const val CATEGORIES_JSON = "CATEGORIES_JSON"
        private const val CATEGORIES_JSON_LAST_UPDATE = "CATEGORIES_JSON_LAST_UPDATE"
        private const val QUESTION_SOURCE = "QUESTION_SOURCE"
        private const val QUIZ_TYPE = "QUIZ_TYPE"


        @Volatile
        private var instance: PrefsManager? = null

        fun getInstance(context: Context): PrefsManager {
            return instance ?: synchronized(this) {
                instance ?: PrefsManager(context).also { instance = it }
            }
        }
    }

    fun saveQuestionSource(questionSource: String) {
        sharedPreferences.edit().putString(QUESTION_SOURCE, questionSource).apply()
    }

    fun getQuestionSource(): String? {
        return sharedPreferences.getString(QUESTION_SOURCE, "QBANK")
    }

    fun saveQuizType(quizType: String) {
        sharedPreferences.edit().putString(QUIZ_TYPE, quizType).apply()
    }

    fun getQuizType(): String? {
        return sharedPreferences.getString(QUIZ_TYPE, "FREE_MODE")
    }

    fun saveJwt(jwt: String) {
        sharedPreferences.edit().putString(KEY_JWT, jwt).apply()
    }

    fun getJwt(): String? {
        return sharedPreferences.getString(KEY_JWT, null)
    }

    fun clearJwt() {
        sharedPreferences.edit().remove(KEY_JWT).apply()
        sharedPreferences.edit().remove(USER_EMAIL).apply()
        sharedPreferences.edit().remove(USERNAME).apply()
    }
    fun saveQuizData(quizData: String) {
        sharedPreferences.edit().putString(QUIZ_DATA, quizData).apply()
    }

    fun getQuizData(): String? {
        return sharedPreferences.getString(QUIZ_DATA, null)
    }

    fun clearQuizData() {
        sharedPreferences.edit().remove(QUIZ_DATA).apply()
    }
    fun saveQuizPositionSubject(quizPositionSubject: String) {
        sharedPreferences.edit().putString(QUIZ_POSITION_SUBJECT, quizPositionSubject).apply()
    }

    fun getQuizPositionSubject(): String? {
        return sharedPreferences.getString(QUIZ_POSITION_SUBJECT, null)
    }

    fun clearQuizPositionSubject() {
        sharedPreferences.edit().remove(QUIZ_POSITION_SUBJECT).apply()
    }

    fun saveQuizPositionQuestion(quizPositionQuestion: String) {
        sharedPreferences.edit().putString(QUIZ_POSITION_QUESTION, quizPositionQuestion).apply()
    }

    fun getQuizPositionQuestion(): String? {
        return sharedPreferences.getString(QUIZ_POSITION_QUESTION, null)
    }

    fun clearQuizPositionQuestion() {
        sharedPreferences.edit().remove(QUIZ_POSITION_QUESTION).apply()
    }

    fun saveCategoriesJson(categoriesJson: String) {
        sharedPreferences.edit().putString(CATEGORIES_JSON, categoriesJson).apply()
    }
    fun getCategoriesJson(): String? {
        return sharedPreferences.getString(CATEGORIES_JSON, "")
    }

    fun clearCategoriesJson() {
        sharedPreferences.edit().remove(CATEGORIES_JSON).apply()
    }

    fun saveTestStyle(quizMode: String) {
        sharedPreferences.edit().putString(QUIZ_MODE, quizMode).apply()
    }


    fun getTestStyle(): String? {
        return sharedPreferences.getString(QUIZ_MODE, ""+FREE_STYLE_TEST)
    }

    fun clearQuizMode() {
        sharedPreferences.edit().remove(QUIZ_MODE).apply()
    }
    fun saveUserEmail(userEmail: String) {
        sharedPreferences.edit().putString(USER_EMAIL, userEmail).apply()
    }

    fun getUserEmail(): String? {
        return sharedPreferences.getString(USER_EMAIL, "no email")
    }

    fun clearUserEmail() {
        sharedPreferences.edit().remove(USER_EMAIL).apply()
    }
    fun saveUsername(username: String) {
        sharedPreferences.edit().putString(USERNAME, username).apply()
    }

    fun getUsername(): String? {
        return sharedPreferences.getString(USERNAME, "GUEST")
    }

    fun clearUsername() {
        sharedPreferences.edit().remove(USERNAME).apply()
    }

    fun saveCategoriesJsonLastUpdate(lastUpdate: Long) {
        sharedPreferences.edit().putLong(CATEGORIES_JSON_LAST_UPDATE, lastUpdate).apply()
    }

    fun getCategoriesJsonLastUpdate(): Long? {
        return sharedPreferences.getLong(CATEGORIES_JSON_LAST_UPDATE, System.currentTimeMillis())
    }

    fun clearCategoriesJsonLastUpdate() {
        sharedPreferences.edit().remove(CATEGORIES_JSON_LAST_UPDATE).apply()
    }
}
