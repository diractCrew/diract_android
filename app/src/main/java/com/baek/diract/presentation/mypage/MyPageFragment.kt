package com.baek.diract.presentation.mypage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.baek.diract.databinding.FragmentMyPageBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyPageFragment : Fragment() {
    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: 기능 구현
        navigationTemporaryEx()
    }

    /*
        TODO: HomeFragment에서 navigation 설정 필요
        1. nav_graph에서 수정
        2. home fragment에서
            val action = HomeFragmentDirections.actionHomeFragmentToVideoListFragment(tracksId,tracksTitle)
            findNavController().navigate(action)
     */
    fun navigationTemporaryEx() {
        val tracksId = "abc"
        val tracksTitle = "곡 제목(tracksTitle)"
        binding.toVideoBtn.setOnClickListener {
            val action = MyPageFragmentDirections.actionMyPageFragmentToVideoListFragment(
                tracksId,
                tracksTitle
            )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
