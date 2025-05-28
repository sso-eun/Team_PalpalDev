package com.example.dundun_hi

import android.net.Uri
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                    // 1) Activity-scoped WeatherViewModel 생성
                    val weatherVM: WeatherViewModel = viewModel(
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

                    // 2) 앱 시작하자마자 날씨 한 번 로드
                    LaunchedEffect(Unit) {
                        weatherVM.load(lat = 37.5665, lon = 126.9780)
                    }

                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        // ───── Home ─────
                        composable("home") {
                            HomeScreen(
                                onLoginClick    = { navController.navigate("login") },
                                onSignupClick   = { navController.navigate("signup") },
                                onGuardianClick = { navController.navigate("guardian") }
                            )
                        }

                        // ───── Login ─────
                        composable("login") {
                            val loginVm: LoginViewModel = viewModel()
                            LoginScreen(
                                vm = loginVm,
                                onLoginSuccess = { userId ->
                                    val encoded = Uri.encode(userId)
                                    navController.navigate("main/$encoded") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ───── Signup ─────
                        composable("signup") {
                            val signupVm: SignupViewModel = viewModel()
                            val state by signupVm.state.collectAsState()

                            SignupScreen { req -> signupVm.signup(req) }

                            LaunchedEffect(state) {
                                when (state) {
                                    is SignupResult.Success ->
                                        navController.navigate("main/${Uri.encode((state as SignupResult.Success).userId)}") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    is SignupResult.Error ->
                                        Toast.makeText(
                                            this@MainActivity,
                                            (state as SignupResult.Error).reason,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    else -> Unit
                                }
                            }
                        }

                        // ───── Guardian ─────
                        composable("guardian") {
                            GuardianScreen(
                                onSubmit      = { _, _ -> },
                                onSignupClick = { navController.navigate("guardian_signup") }
                            )
                        }
                        composable("guardian_signup") {
                            Guardian_SignupScreen()
                        }

                        // ───── Main ─────
                        composable(
                            route = "main/{userName}",
                            arguments = listOf(navArgument("userName") {
                                type = NavType.StringType
                                defaultValue = ""
                            })
                        ) { backEntry ->
                            val name = Uri.decode(backEntry.arguments?.getString("userName") ?: "손님")

                            // 이미 로드된 날씨 상태 구독
                            val weatherState by weatherVM.uiState.collectAsState()

                            // 에러 발생 시 Toast
                            LaunchedEffect(weatherState) {
                                if (weatherState is WeatherUiState.Error) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "날씨 조회 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            // 성공 시 데이터 파싱
                            val success = weatherState as? WeatherUiState.Success
                            val temp  = success?.data?.currentTemp
                                ?.toDoubleOrNull()?.toInt() ?: 0
                            val max   = success?.data?.maxTemp
                                ?.toDoubleOrNull()?.toInt() ?: 0
                            val min   = success?.data?.minTemp
                                ?.toDoubleOrNull()?.toInt() ?: 0

                            MainScreen(
                                userName         = "${name}님",
                                temperature      = temp,
                                maxTemp          = max,
                                minTemp          = min,
                                onPhonePageClick   = { navController.navigate("call") },
                                onMessagePageClick = { /* TODO */ },
                                onCameraPageClick  = { navController.navigate("camera") },
                                onMapPageClick     = { /* TODO */ },
                                onFindCultureCenter = { /* TODO */ },
                                onKioskPageClick   = { navController.navigate("kiosk") },
                                onProfileClick     = { navController.navigate("profile") }
                            )
                        }

                        // ───── Kiosk ─────
                        composable("kiosk") {
                            KioskScreen()
                        }

                        // ───── Camera ─────
                        composable("camera") {
                            CameraScreen(navController)
                        }

                        // ───── LastPhoto ─────
                        composable("lastphoto") {
                            val dummy = listOf(
                                SharedPhoto(R.drawable.img1, true),
                                SharedPhoto(R.drawable.img2, false),
                                SharedPhoto(R.drawable.img3, true)
                            )
                            LastPhotoScreen(
                                photos     = dummy,
                                onAddPhoto = { /* TODO */ }
                            )
                        }

                        // ───── Profile ─────
                        composable("profile") {
                            ProfileScreen()
                        }

                        // ───── Call ─────
                        composable("call") {
                            val callVm: CallViewModel = viewModel()
                            val shortcuts by callVm.shortcuts.collectAsState()
                            CallScreen(
                                contacts      = shortcuts,
                                onAddShortcut = { idx ->
                                    navController.navigate("call_setup/$idx")
                                }
                            )
                        }

                        // ───── Call Setup ─────
                        composable("call_setup/{index}") { back ->
                            val callVm: CallViewModel = viewModel()
                            val idx = back.arguments?.getString("index")?.toIntOrNull() ?: 0
                            SetupShortcutScreen(
                                index = idx,
                                onDone = {
                                    callVm.loadShortcuts()
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
