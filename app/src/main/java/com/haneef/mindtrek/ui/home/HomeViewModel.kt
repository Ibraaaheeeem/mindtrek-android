package com.haneef.mindtrek.ui.home

import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haneef.mindtrek.data.CategoryItem
import com.haneef.mindtrek.util.ApiCallback
import com.haneef.mindtrek.util.BackgroundWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import kotlin.random.Random

class HomeViewModel : ViewModel() {
    private lateinit var rootUrl: String
    private var category = ""
    private var subcategory = ""
    private var subject = ""
    private var unit = ""
    private var subunit = ""
    private var tag = ""
    private lateinit var questionLoadingImage: ImageView

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
    }
    private val linksArray = listOf("/categories", "/subcategories", "/subjects", "/units", "/subunits", "/tags")
    private val qBankLinksArray = listOf("/categories", "/subcategories", "/subjects", "/units")
    val text: LiveData<String> = _text

    private val _categories = MutableLiveData<List<CategoryItem>>()
    val categories: LiveData<List<CategoryItem>> = _categories


    private val _categoryItemLevel = MutableLiveData<Int>().apply { value = 0 }
    val categoryItemLevel: LiveData<Int> = _categoryItemLevel
    fun clearCategoriesList(){
        _categoryItemLevel.postValue(0)
        _categories.postValue(emptyList())
    }
    fun fetchCategories(categoryLevel: Int = 0, parentId: Int = 0, parentName: String = "", index: Int = 0, source: String) {

        val url = "$rootUrl${getSourceUrl(source)}${getCategoryLevelUrl(categoryLevel)}/" +
                if (source == "AI") parentName else parentId
        setLoadingStarted()
        val bgWorker = BackgroundWorker()
        setCategoryProperties(categoryLevel, parentName)

        bgWorker.fetchData("$url?${constructQueryParams()}", "GET", null, callback = object : ApiCallback {
            override fun onSuccess(response: String) {
                Log.d("API RESPONSE", response)
                val updatedList = updateCategories(categoryLevel)
                val childList = parseCategories(source, response, categoryLevel)
                val insertionCategory = updatedList.find { it.id == parentId}
                if (insertionCategory != null) {
                    val insertionPoint = updatedList.indexOf(insertionCategory)!!.toInt()
                    updatedList.addAll(insertionPoint + 1, childList)
                }
                else{
                    updatedList.addAll(childList)

                }
                _categoryItemLevel.postValue(categoryLevel)
                _categories.postValue(updatedList.toList())
                setLoadingEnded()
            }

            override fun onError(error: String) {
                Log.d("API ERROR", error)
                setLoadingEnded()
            }
        })
    }

    private fun getSourceUrl(source: String): String {
        return when (source) {
            "QBANK" -> "/quiz"
            "AI" -> "/quizai"
            else -> ""
        }
    }

    private fun getCategoryLevelUrl(categoryLevel: Int): String {
        return when (categoryLevel) {
            0 -> "/categories"
            1 -> "/subcategories"
            2 -> "/subjects"
            3 -> "/units"
            4 -> "/subunits"
            else -> ""
        }
    }

    private fun setCategoryProperties(categoryLevel: Int, parentName: String) {
        category = ""
        subcategory = ""
        subject = ""
        unit = ""
        subunit = ""
        when (categoryLevel) {
            1 -> category = parentName
            2 -> subcategory = parentName
            3 -> subject = parentName
            4 -> unit = parentName
            5 -> subunit = parentName
        }
    }

    private fun constructQueryParams(): String {
        val queryParams = mutableListOf<String>()
        if (category.isNotEmpty()) queryParams.add("category=$category")
        if (subcategory.isNotEmpty()) queryParams.add("subcategory=$subcategory")
        if (subject.isNotEmpty()) queryParams.add("subject=$subject")
        if (unit.isNotEmpty()) queryParams.add("unit=$unit")
        if (subunit.isNotEmpty()) queryParams.add("subunit=$subunit")
        return queryParams.joinToString("&")
    }

    private fun updateCategories(categoryLevel: Int): MutableList<CategoryItem> {
        return categories.value?.filter { (it as CategoryItem).level < categoryLevel }.orEmpty().toMutableList()
    }

    private fun parseCategories(source: String, response: String, level: Int): List<CategoryItem> {
        val categoriesList = mutableListOf<CategoryItem>()
        try {
            val jsonArray = JSONArray(response)
            for (i in 0 until jsonArray.length()) {
                when(source){
                    "QBANK" -> {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getInt("id")
                        val name = jsonObject.getString("name")
                        categoriesList.add(CategoryItem(id, name, "", level))
                    }
                    "AI" -> {
                        categoriesList.add(CategoryItem(Random.nextInt(2000), jsonArray[i].toString(), "", level))
                    }
                }

            }
        } catch (e: JSONException) {
            // Handle JSON parsing exception
            e.printStackTrace()
            Log.d("PARSE CATEGORIES", e.message.toString())
        }
        return categoriesList
    }

    fun setLoadingStarted(){
        questionLoadingImage.visibility = View.VISIBLE
        Log.d("LOADING", "started")
    }

    fun setLoadingEnded(){
        var myScope = CoroutineScope(Dispatchers.Main);
        myScope.launch {
            questionLoadingImage.visibility = View.GONE
            Log.d("LOADING", "stopped")
        }
    }
    fun setLoadingImage(loadingImage: ImageView) {
        questionLoadingImage = loadingImage
    }

    fun setBaseLink(string: String) {
        rootUrl = string
    }

}