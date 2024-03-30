package com.haneef.mindtrek.data

data class Category(val id: Int, val name: String, val description: String, val subcategories: List<Subcategory> )
data class Subcategory(val id: Int, val name: String, val description: String, val subjects: List<Subject> )
data class Subject(val id: Int, val name: String, val description: String, val units: List<Unit> )
data class Unit(val id: Int, val name: String, val description: String, val subunits: List<Subunit> )
data class Subunit(val id: Int, val name: String, val description: String, val tags: List<String> )

