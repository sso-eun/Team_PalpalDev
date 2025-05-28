package com.example.dundun_hi

import android.net.Uri
import android.os.Bundle
import android.util.Log
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

                    /* ───── 네비게이션 컨트롤러 ───── */
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {

                        /* ─────────── Home ─────────── */
                        composable("home") {
                            HomeScreen(
                                onLoginClick   = { navController.navigate("login") },
                                onSignupClick  = { navController.navigate("signup") },
                                onGuardianClick = { navController.navigate("guardian") }
                            )
                        }

                        /* ─────────── Login ─────────── */
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

                        /* ────────── Signup ─────────── */
                        composable("signup") {
                            val vm: SignupViewModel = viewModel()
                            val state by vm.state.collectAsState()

                            SignupScreen { req -> vm.signup(req) }

                            LaunchedEffect(state) {
                                when (state) {
                                    is SignupResult.Success -> {
                                        val success = state as SignupResult.Success

                                        // 🔴 아직도 번호를 쓰고 있을 가능성
                                        // val encoded = Uri.encode(success.userNum)

                                        // ✅ 아이디(userId)만 인코딩해서 넘깁니다
                                        val encoded = Uri.encode(success.userId)

                                        navController.navigate("loading/$encoded") {
                                            popUpTo("signup") { inclusive = true }
                                        }
                                    }
                                    is SignupResult.Error -> { /* 토스트 */ }
                                    else -> Unit
                                }
                            }
                        }

                        /* ────── Loading (3초) ─────── */
                        composable(
                            route = "loading/{userName}",
                            arguments = listOf(navArgument("userName") {
                                type = NavType.StringType
                                defaultValue = ""
                            })
                        ) { backEntry ->
                            val encoded = backEntry.arguments?.getString("userName") ?: ""
                            LoadingScreen(
                                navController = navController,
                                userName      = encoded           // 내부에서 URL decode
                            )
                        }

                        /* ───────── Guardian ───────── */
                        composable("guardian") {
                            GuardianScreen(
                                onSubmit     = { _, _ -> },
                                onSignupClick = { navController.navigate("guardian_signup") }
                            )
                        }
                        composable("guardian_signup") { Guardian_SignupScreen() }

                        /* ────────── Main ──────────── */
                        composable(
                            route = "main/{userName}",
                            arguments = listOf(navArgument("userName") {
                                type = NavType.StringType
                                defaultValue = ""
                            })
                        ) { backEntry ->

                            /* 1) 사용자 이름 추출 */
                            val name = Uri.decode(
                                backEntry.arguments?.getString("userName") ?: "손님"
                            )

                            /* 2) 날씨 ViewModel 설정 */
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
                            val weatherState by weatherVM.uiState.collectAsState()

                            /* 3) 날씨 로드 */
                            LaunchedEffect(Unit) {
                                weatherVM.load(lat = 37.5665, lon = 126.9780)
                            }

                            /* 4) 에러 토스트 */
                            LaunchedEffect(weatherState) {
                                if (weatherState is WeatherUiState.Error) {
                                    val err = (weatherState as WeatherUiState.Error).error
                                    Log.e("Weather", "조회 실패", err)
                                    Toast.makeText(
                                        this@MainActivity,
                                        "날씨 조회 실패: ${err.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }



                            /* 5) MainScreen */
                            val success = weatherState as? WeatherUiState.Success
                            val temp = success?.data?.currentTempInt ?: 0
                            val max  = success?.data?.maxTempInt     ?: 0
                            val min  = success?.data?.minTempInt     ?: 0

//                            val temp  = (weatherState as? WeatherUiState.Success)?.data?.currentTemp?.toIntOrNull() ?: 0
//                            val max  = (weatherState as? WeatherUiState.Success)?.data?.maxTemp?.toIntOrNull() ?: 0
//                            val min   = (weatherState as? WeatherUiState.Success)?.data?.minTemp?.toIntOrNull() ?: 0

                            MainScreen(
                                userName          = "${name}님",
                                temperature       = temp,
                                maxTemp          = max,
                                minTemp           = min,
                                onPhonePageClick  = { navController.navigate("call") },
                                onMessagePageClick= {},
                                onCameraPageClick = { navController.navigate("camera") },
                                onMapPageClick    = {},
                                onFindCultureCenter = {},
                                onKioskPageClick  = { navController.navigate("kiosk") },
                                onProfileClick    = { navController.navigate("profile") }
                            )
                        }

                        /* ───────── Sub Screens ────── */
                        composable("kiosk")  { KioskScreen() }
                        composable("camera") { CameraScreen(navController) }

                        composable("lastphoto") {
                            val dummy = listOf(
                                SharedPhoto(R.drawable.img1, true),
                                SharedPhoto(R.drawable.img2, false),
                                SharedPhoto(R.drawable.img3, true)
                            )
                            LastPhotoScreen(dummy) { }
                        }

                        composable("profile") { ProfileScreen() }

                        composable("call") {
                            val callVM: CallViewModel = viewModel()
                            val shortcuts by callVM.shortcuts.collectAsState()

                            CallScreen(
                                contacts = shortcuts,
                                onAddShortcut = { idx ->
                                    navController.navigate("call_setup/$idx")
                                }
                            )
                        }

                        composable("call_setup/{index}") { back ->
                            val callVM: CallViewModel = viewModel()
                            val idx = back.arguments?.getString("index")?.toIntOrNull() ?: 0

                            SetupShortcutScreen(
                                index = idx,
                                onDone = {
                                    callVM.loadShortcuts()
                                    navController.popBackStack()
                                }
                            )
                        }
                    } // ── NavHost 끝 ──
                }
            }
        }
    }
}
