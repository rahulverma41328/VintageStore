import android.util.Patterns
import com.example.vintagestore.util.RegisterValidation

fun validateEmail(email:String):RegisterValidation{
    if (email.isEmpty()){
        return RegisterValidation.Failed("field can't be empty")
    }
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
        return RegisterValidation.Failed("Wrong email format")
    }
    return RegisterValidation.Success
}
fun validatePassword(password:String) :RegisterValidation{
    if(password.isEmpty()){
        return RegisterValidation.Failed("Password can't be empty")
    }
    if (password.length<6){
        return RegisterValidation.Failed("Password must contain at least 6 characters")
    }
    return RegisterValidation.Success
}