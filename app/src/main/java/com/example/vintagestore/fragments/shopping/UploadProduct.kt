package com.example.vintagestore.fragments.shopping

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.Intent.ACTION_GET_CONTENT
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.vintagestore.R
import com.example.vintagestore.data.Product
import com.example.vintagestore.databinding.FragmentUploadProductBinding
import com.example.vintagestore.util.Resource
import com.example.vintagestore.viewModel.UploadProductViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.UUID

@AndroidEntryPoint
class UploadProduct: Fragment(R.layout.fragment_upload_product) {

    private lateinit var binding: FragmentUploadProductBinding
    private var selectedImages = mutableListOf<Uri>()
    private val viewModel by viewModels<UploadProductViewModel>()
    private var images: List<String> = emptyList()
    private lateinit var  firebaseAuth:FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUploadProductBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        // setup spinner
        val spinnerItems = resources.getStringArray(R.array.product_category_list)
        val adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_item,spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        var selectedOption = ""
        binding.edProductCategory.adapter = adapter

        // getting category
        binding.edProductCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedOption = parent?.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                selectedOption = "other"
            }
        }

        // getting image
        val selectedImagesActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == RESULT_OK){
                val intent = result.data
                if (intent?.clipData != null){
                    val count = intent.clipData?.itemCount ?:0
                    (0  until count).forEach{
                        val imageUri = intent.clipData?.getItemAt(it)?.uri
                        imageUri?.let {
                            selectedImages.add(imageUri)
                        }
                    }
                }else{
                    val imageUri = intent?.data
                    imageUri?.let {
                        selectedImages.add(imageUri)
                    }
                }
                updateImages()
            }
        }
        binding.apply {
            imageFromStorage.setOnClickListener {
                val intent = Intent(ACTION_GET_CONTENT)
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true)
                intent.type = "image/*"
                selectedImagesActivityResult.launch(intent)
            }
        }

        // upload product btn
        binding.apply {
            btnUploadProduct.setOnClickListener {
                btnUploadProduct.startAnimation()
                val productName = edProductName.text.toString()
                val productPrice = edProductPrice.text.toString()
                val productDescription = edProductDescription.text.toString()
                val productValidate = viewModel.validateInformation(productName,productPrice,productDescription)
                val imagesToByteArray = getImagesToByteArray()
                if (productValidate){

                    viewModel.saveImages(imagesToByteArray)
                }
            }
        }

        // upload image lifecycle
        lifecycleScope.launchWhenStarted {
            viewModel.uploadImage.collect{
                when(it){
                    is Resource.Success ->{
                        images = it.data!!
                        Log.d("product images",images.toString())
                        val product = Product(
                            UUID.randomUUID().toString(),
                            binding.edProductName.text.toString(),
                            selectedOption,
                            binding.edProductPrice.text.toString().toFloat(),
                            binding.edProductDescription.text.toString(),
                            images,
                            firebaseAuth.currentUser!!.uid
                        )
                        viewModel.saveProduct(product)
                    }
                    is Resource.Error ->{
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        // validate product lifecycle
        lifecycleScope.launchWhenStarted {
            viewModel.validation.collect{
                when(it){
                    is Resource.Error -> {
                        Toast.makeText(requireContext(),"field can't be empty",Toast.LENGTH_LONG).show()
                    }
                    else -> Unit
                }
            }
        }

        // upload btn lifecycle
        lifecycleScope.launchWhenStarted {
            viewModel.product.collect{
                when(it){
                    is Resource.Loading ->{
                        binding.btnUploadProduct.startAnimation()
                    }
                    is Resource.Success ->{
                        binding.btnUploadProduct.revertAnimation()
                        Toast.makeText(requireContext(),"product uploaded successfully",Toast.LENGTH_LONG).show()
                    }
                    is Resource.Error -> {
                        binding.btnUploadProduct.revertAnimation()
                        Toast.makeText(requireContext(),it.message,Toast.LENGTH_LONG).show()
                    }
                    else ->{
                        Unit
                    }
                }
            }
        }

    }

    // convert image to byte array
    private fun getImagesToByteArray(): List<ByteArray> {
        val imagesToByteArray = mutableListOf<ByteArray>()
        selectedImages.forEach {
            val stream = ByteArrayOutputStream()
            val imageBmp = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
            if (imageBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)){
                imagesToByteArray.add(stream.toByteArray())
            }
        }
        return imagesToByteArray
    }

    // updating text image
    private fun updateImages() {
//        if (selectedImages.size>0){
//            binding.apply {
//                btnUploadProduct.isEnabled = true
//                btnUploadProduct.setBackgroundColor(resources.getColor(R.color.g_blue))
//            }
//        }
        binding.tvNoOfImages.text = "images selected: ${selectedImages.size.toString()}"
    }
}