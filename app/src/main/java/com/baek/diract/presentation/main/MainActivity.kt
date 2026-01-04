package com.baek.diract.presentation.main

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.baek.diract.R
import com.baek.diract.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    //바텀 네비게이션바 필요한 뷰
    private val bottomDestinations = setOf(
        R.id.homeFragment,
        R.id.inboxFragment,
        R.id.myPageFragment
    )

    //상태바까지 확장할 뷰
    private val edgeToEdgeDestinations: Set<Int> = setOf(

    )

    //상태바 확장 상태
    private var isEdgeToEdge: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //상태바, 네비게이션바 아이콘 밝게(배경은 어둡게)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        //인셋 리스너 설정
        ViewCompat.setOnApplyWindowInsetsListener(binding.navHostFragment) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, if (isEdgeToEdge) 0 else bars.top, 0, 0)
            insets
        }

        //바텀 네비게이션 설정
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNav.setupWithNavController(navController)


        //화면 전환에 따른 상태 변경(바텀 네비게이션바, 상태바 뷰 확장)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            //바텀 네비게이션 여부 결정
            binding.bottomNav.visibility =
                if (destination.id in bottomDestinations) View.VISIBLE else View.GONE

            //상태바 확장 상태 변경
            val newIsEdgeToEdge = destination.id in edgeToEdgeDestinations
            //상태 바뀌면 인셋 적용
            if (isEdgeToEdge != newIsEdgeToEdge) {
                isEdgeToEdge = newIsEdgeToEdge
                ViewCompat.requestApplyInsets(binding.navHostFragment)
            }
        }

        //초기 인셋 적용
        ViewCompat.requestApplyInsets(binding.navHostFragment)
    }
}