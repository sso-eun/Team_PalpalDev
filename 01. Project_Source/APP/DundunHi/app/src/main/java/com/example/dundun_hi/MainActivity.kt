// MainActivity.kt
package com.example.dundun_hi

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dundun_hi.data.WeatherRepository
import com.example.dundun_hi.model.CallViewModel
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.model.WeatherUiState
import com.example.dundun_hi.model.WeatherViewModel
import com.example.dundun_hi.network.RetrofitClient
import com.example.dundun_hi.network.WeatherService
import com.example.dundun_hi.ui.GuardianScreen
import com.example.dundun_hi.ui.Guardian_SignupScreen
import com.example.dundun_hi.ui.HomeScreen
import com.example.dundun_hi.ui.KioskScreen
import com.example.dundun_hi.ui.MainScreen
import com.example.dundun_hi.ui.ProfileScreen
import com.example.dundun_hi.ui.SetupShortcutScreen
import com.example.dundun_hi.ui.login.LoginScreen
import com.example.dundun_hi.ui.login.LoginViewModel
import com.example.dundun_hi.ui.screen.CallScreen
import com.example.dundun_hi.ui.screen.LastPhotoScreen
import com.example.dundun_hi.ui.signup.SignupResult
import com.example.dundun_hi.ui.signup.SignupScreen
import com.example.dundun_hi.ui.signup.SignupViewModel
import com.example.dundun_hi.ui.theme.DundunHiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DundunHiTheme {
                Surface(Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        /* ───── 기본 플로우 ───── */
                        composable("home") {
                            HomeScreen(
                                onLoginClick = { navController.navigate("login") },
                                onSignupClick = { navController.navigate("signup") },
                                onGuardianClick = { navController.navigate("guardian") }
                            )
                        }
                        // ── 로그인 화면 ──
                        composable("login") {
                            // 1) LoginViewModel 인스턴스
                            val loginVm: LoginViewModel = viewModel()

                            // 2) LoginScreen에 vm 과 성공 콜백 전달
                            LoginScreen(
                                vm = loginVm,
                                onLoginSuccess = { userNum ->
                                    // 3) userNum을 받으면 저장하거나 로깅
                                    // 예: DataStore.saveUserNum(userNum)

                                    // 4) 메인 화면으로 이동
                                    navController.navigate("main") {
                                        // 이전 스택(로그인) 지우고 싶으면 아래 옵션 추가
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("signup") {
                            val vm: SignupViewModel = viewModel()
                            val state by vm.state.collectAsState()

                            SignupScreen { req ->
                                vm.signup(req)
                            }

                            // 화면 어딘가(예: LaunchedEffect)에서 state 변화를 감지해 내비게이션
                            LaunchedEffect(state) {
                                when (state) {
                                    is SignupResult.Success -> {
                                        navController.navigate("main") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    }
                                    is SignupResult.Error -> {
                                        Toast.makeText(
                                            this@MainActivity,
                                            (state as SignupResult.Error).reason,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    else -> { /* Idle 일 때는 아무 것도 안 함 */ }
                                }
                            }
                        }
                        composable("guardian") {
                            GuardianScreen(
                                onSubmit = { _, _ -> },
                                onSignupClick = { navController.navigate("guardian_signup") }
                            )
                        }
                        composable("guardian_signup") { Guardian_SignupScreen() }

                        /* ───── 메인 메뉴 ───── */
                        composable("main") {
                            // ◀ 날씨 ViewModel & 상태 설정
                            val weatherViewModel: WeatherViewModel = viewModel(
                                factory = object : ViewModelProvider.Factory {
                                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                        @Suppress("UNCHECKED_CAST")
                                        return WeatherViewModel(
                                            WeatherRepository(
                                                WeatherService(RetrofitClient.weatherApi)
                                            )
                                        ) as T
                                    }
                                }
                            )
                            val weatherState by weatherViewModel.uiState.collectAsState()

                            // ◀ 앱 시작 시 날씨 데이터 로드
                            LaunchedEffect(Unit) {
                                weatherViewModel.load(
                                    lat = 37.5665,
                                    lon = 126.9780
                                )
                            }

                            // ◀ 날씨 값 추출 (성공 시)
                            val temp = (weatherState as? WeatherUiState.Success)
                                ?.data?.currentTemp?.toIntOrNull() ?: 0
                            val high = (weatherState as? WeatherUiState.Success)
                                ?.data?.maxTemp?.toIntOrNull() ?: 0
                            val low = (weatherState as? WeatherUiState.Success)
                                ?.data?.minTemp?.toIntOrNull() ?: 0

                            MainScreen(
                                userName = "길동님",
                                temperature = temp,      // ◀ 수정된 부분
                                highTemp = high,         // ◀ 수정된 부분
                                lowTemp = low,
                                onPhonePageClick = { navController.navigate("call") },
                                onMessagePageClick = {},
                                onCameraPageClick = { navController.navigate("camera") },
                                onMapPageClick = {},
                                onFindCultureCenter = {},
                                onKioskPageClick = { navController.navigate("kiosk") },
                                onProfileClick = { navController.navigate("profile") }
                            )
                        }

                        /* ───── 서브 화면들 ───── */
                        composable("kiosk")  { KioskScreen() }
                        composable("camera") { CameraScreen(navController) }

                        /* 지난 사진 보기 – 더미 데이터 목업 */
                        composable("lastphoto") {
                            val dummyList = listOf(
                                SharedPhoto(R.drawable.img1, fromMe = true),
                                SharedPhoto(R.drawable.img2, fromMe = false),
                                SharedPhoto(R.drawable.img3, fromMe = true)
                            )
                            LastPhotoScreen(
                                photos     = dummyList,
                                onAddPhoto = { /* TODO: 사진 추가 */ }
                            )
                        }
                        composable("profile") { ProfileScreen() }

                        /* ───── 빠른전화 화면 ───── */
                        composable("call") {
                            val viewModel: CallViewModel = viewModel()
                            val shortcuts by viewModel.shortcuts.collectAsState()

                            CallScreen(
                                contacts      = shortcuts,
                                onAddShortcut = { idx ->
                                    navController.navigate("call_setup/$idx")
                                }
                            )
                        }

                        /* ───── 단축키 설정 화면 ───── */
                        composable("call_setup/{index}") { back ->
                            val viewModel: CallViewModel = viewModel()
                            val idx = back.arguments
                                ?.getString("index")
                                ?.toIntOrNull() ?: 0

                            SetupShortcutScreen(
                                index = idx,
                                onDone = {
                                    viewModel.loadShortcuts()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
