package com.example.vintagestore.data

data class Product(
    val id:String,
    val name: String,
    val category:String,
    val price: Float,
    val description:String?= null,
    val images: List<String>,
    val userId: String
){
    constructor(): this("0","","",0f,images = emptyList(),userId = "")
}