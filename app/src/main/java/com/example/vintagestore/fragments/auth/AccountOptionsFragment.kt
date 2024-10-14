package com.example.vintagestore.fragments.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.vintagestore.R
import com.example.vintagestore.databinding.FragmentAccountOptionsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountOptionsFragment: Fragment(R.layout.fragment_account_options) {

    private lateinit var binding: FragmentAccountOptionsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountOptionsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            buttonLoginAccountOptions.setOnClickListener {
                val action = AccountOptionsFragmentDirections.actionAccountOptionsFragmentToLoginFragment()
                findNavController().navigate(action)
            }
        }
        binding.apply {
            buttonRegisterAccountOptions.setOnClickListener {
                val action = AccountOptionsFragmentDirections.actionAccountOptionsFragmentToRegisterFragment()
                findNavController().navigate(action)
            }
        }
    }
}