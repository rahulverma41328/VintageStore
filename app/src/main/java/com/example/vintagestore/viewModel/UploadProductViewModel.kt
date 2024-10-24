package com.example.vintagestore.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vintagestore.data.Product
import com.example.vintagestore.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UploadProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel(){

    private val _validation = MutableStateFlow<Resource<Boolean>>(Resource.Unspecified())
    val validation = _validation.asStateFlow()

    private val _product = MutableStateFlow<Resource<Product>>(Resource.Unspecified())
    val product = _product.asStateFlow()

    private val _uploadImage = MutableStateFlow<Resource<List<String>>>(Resource.Unspecified())
    val uploadImage = _uploadImage.asStateFlow()

    fun validateInformation(name:String,price:String,description:String) :Boolean{
        if (name.isEmpty()|| price.isEmpty() || description.isEmpty()){
            _validation.value = Resource.Error("empty field")
            return false
        }
        _validation.value = Resource.Success(true)
        return true
    }

    fun saveProduct(product: Product){
        runBlocking {
            _product.value = Resource.Loading()
        }
        firestore.collection("Products").add(product)
            .addOnSuccessListener {
                _product.value = Resource.Success(product)
            }.addOnFailureListener {
                _product.value = Resource.Error(it.message.toString())
            }
    }

    fun saveImages(imagesToByteArray: List<ByteArray>){

        val images = mutableListOf<String>()
        viewModelScope.launch {
            try {
                async {
                    imagesToByteArray.forEach {
                        val id = UUID.randomUUID().toString()
                        launch {
                            val imageStorage = storage.reference.child("products/images/$id")
                            val result = imageStorage.putBytes(it).await()
                            val downloadUrl = result.storage.downloadUrl.await().toString()
                            images.add(downloadUrl)
                        }
                    }
                }.await()
                _uploadImage.value = Resource.Success(images)
            }catch (e:Exception){
                e.printStackTrace()
                _uploadImage.value = Resource.Error(e.message.toString())
            }
        }
    }
}