package com.example.vintagestore.fragments.shopping

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.example.vintagestore.R
import com.example.vintagestore.adapter.HomeViewPagerAdapter
import com.example.vintagestore.databinding.FragmentHomeBinding
import com.example.vintagestore.fragments.categories.ClothesFragment
import com.example.vintagestore.fragments.categories.ElectronicFragment
import com.example.vintagestore.fragments.categories.MainCategoryFragment
import com.example.vintagestore.fragments.categories.ShoesFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var showOptions : ChipGroup
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val categoriesFragments = arrayListOf(
            MainCategoryFragment(),
            ClothesFragment(),
            ShoesFragment(),
            ElectronicFragment()
        )

        val viewPager2Adapter = HomeViewPagerAdapter(categoriesFragments,childFragmentManager,lifecycle)
        binding.viewPagerHome.adapter = viewPager2Adapter
        TabLayoutMediator(binding.tabLayout,binding.viewPagerHome){ tab,position ->
            when(position){
                0 -> tab.text = "Main"
                1 -> tab.text = "Clothes"
                2 -> tab.text = "Shoes"
                3 -> tab.text = "Electronic"
            }
        }.attach()
        Log.d("viewpager", "ViewPager and TabLayoutMediator setup completed.")
        }
    }
