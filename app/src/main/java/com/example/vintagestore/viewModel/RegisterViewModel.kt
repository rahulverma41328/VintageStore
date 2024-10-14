package com.example.vintagestore.viewModel

import androidx.lifecycle.ViewModel
import com.example.vintagestore.data.User
import com.example.vintagestore.util.Constants.USER_COLLECTION
import com.example.vintagestore.util.RegisterFieldState
import com.example.vintagestore.util.RegisterValidation
import com.example.vintagestore.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.runBlocking
import validateEmail
import validatePassword
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
):ViewModel() {

    private val _register = MutableStateFlow<Resource<User>>(Resource.Unspecified())
    val register: Flow<Resource<User>> = _register

    private val _validation = Channel<RegisterFieldState>()
    val validation = _validation.receiveAsFlow()

    fun createAccountWithEmailAndPassword(user: User,password:String){
        if (checkValidation(user,password)){
        runBlocking {
            _register.emit(Resource.Loading())
        }
        firebaseAuth.createUserWithEmailAndPassword(user.email,password)
            .addOnSuccessListener {
                it.user?.let {
                    saveUserInfo(it.uid,user)
                }
            }.addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())
            }
        }else{
            val registerFieldState = RegisterFieldState(validateEmail(user.email),validatePassword(password))
            runBlocking {
                _validation.send(registerFieldState)
            }
        }
    }

    private fun checkValidation(user:User, password: String): Boolean{

        val emailValidation = validateEmail(user.email)
        val passwordValidation = validatePassword(password)

        return emailValidation is RegisterValidation.Success && passwordValidation is RegisterValidation.Success
    }

    private fun saveUserInfo(userUid:String,user: User){
        db.collection(USER_COLLECTION)
            .document(userUid)
            .set(user)
            .addOnSuccessListener {
                _register.value = Resource.Success(user)
            }.addOnFailureListener {
                _register.value = Resource.Error(it.message.toString())
            }
    }
}