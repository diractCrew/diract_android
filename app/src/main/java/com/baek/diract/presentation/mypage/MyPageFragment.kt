package com.baek.diract.presentation.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.baek.diract.databinding.FragmentMyPageBinding
import com.baek.diract.presentation.login.AuthState
import com.baek.diract.presentation.login.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyPageFragment : Fragment() {
    private var _binding: FragmentMyPageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeAuthState()
        navigationTemporaryEx()
    }

    private fun setupClickListeners() {
        binding.loginBtn.setOnClickListener {
            viewModel.login()
        }

        binding.logoutBtn.setOnClickListener {
            viewModel.logout()
        }
    }

    private fun observeAuthState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isLoggedIn.collect { isLoggedIn ->
                        updateLoginButtonVisibility(isLoggedIn)
                    }
                }

                launch {
                    viewModel.authState.collect { state ->
                        handleAuthState(state)
                    }
                }
            }
        }
    }

    private fun updateLoginButtonVisibility(isLoggedIn: Boolean) {
        binding.loginBtn.visibility = if (isLoggedIn) View.GONE else View.VISIBLE
        binding.logoutBtn.visibility = if (isLoggedIn) View.VISIBLE else View.GONE
    }

    private fun handleAuthState(state: AuthState) {
        when (state) {
            is AuthState.None -> {
                // 초기 상태
            }

            is AuthState.Loading -> {
                binding.loginBtn.isEnabled = false
                binding.loginBtn.text = "로그인 중..."
            }

            is AuthState.LoggedIn -> {
                binding.loginBtn.isEnabled = true
                binding.loginBtn.text = "로그인"
                Toast.makeText(requireContext(), "로그인 성공: ${state.email}", Toast.LENGTH_SHORT)
                    .show()
            }

            is AuthState.LoggedOut -> {
                Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            }

            is AuthState.Error -> {
                binding.loginBtn.isEnabled = true
                binding.loginBtn.text = "로그인"
                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /*
        TODO: HomeFragment에서 navigation 설정 필요
        1. nav_graph에서 수정
        2. home fragment에서
            val action = HomeFragmentDirections.actionHomeFragmentToVideoListFragment(tracksId,tracksTitle)
            findNavController().navigate(action)
     */
    private fun navigationTemporaryEx() {
        val tracksId = "AndroidTestTracks1"
        val tracksTitle = "AndTracks"
        binding.toVideoBtn.setOnClickListener {
            val action = MyPageFragmentDirections.actionMyPageFragmentToVideoNavGraph(
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
