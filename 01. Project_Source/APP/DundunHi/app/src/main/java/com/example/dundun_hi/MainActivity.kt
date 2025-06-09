// app/src/main/java/com/example/dundun_hi/MainActivity.kt

package com.example.dundun_hi

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dundun_hi.data.RealUserRepository
import com.example.dundun_hi.data.UserRepository
import com.example.dundun_hi.data.WeatherRepository
import com.example.dundun_hi.model.CallViewModel
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.model.WeatherUiState
import com.example.dundun_hi.model.WeatherViewModel
import com.example.dundun_hi.network.RetrofitClient
import com.example.dundun_hi.network.WeatherService
import com.example.dundun_hi.ui.*
import com.example.dundun_hi.ui.login.FindIdScreen
import com.example.dundun_hi.ui.login.FindIdViewModel
import com.example.dundun_hi.ui.login.Guardian_FindIdScreen
import com.example.dundun_hi.ui.login.GuardianScreen
import com.example.dundun_hi.ui.login.LoginScreen
import com.example.dundun_hi.ui.login.LoginViewModel
import com.example.dundun_hi.ui.profile.ProfileScreen
import com.example.dundun_hi.ui.profile.ProfileViewModel
import com.example.dundun_hi.ui.profile.ProfileViewModelFactory
import com.example.dundun_hi.ui.profile.UpdatePasswordScreen
import com.example.dundun_hi.ui.profile.UpdateProfileScreen
import com.example.dundun_hi.ui.ActivityHistoryScreen
import com.example.dundun_hi.ui.screen.CallScreen
import com.example.dundun_hi.ui.KioskScreen
import com.example.dundun_hi.ui.screen.LastPhotoScreen
import com.example.dundun_hi.ui.signup.LoadingScreen
import com.example.dundun_hi.ui.SetupShortcutScreen
import com.example.dundun_hi.ui.HomeScreen
import com.example.dundun_hi.ui.MainScreen
import com.example.dundun_hi.ui.signup.CombinedAuthScreen
import com.example.dundun_hi.ui.signup.SignupResult
import com.example.dundun_hi.ui.signup.SignupScreen
import com.example.dundun_hi.ui.signup.SignupViewModel
import com.example.dundun_hi.ui.signup.LoadingScreen as SignupLoadingScreen
import com.example.dundun_hi.ui.theme.DundunHiTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 스플래시 화면이 표시되는 동안 하얀 배경 설정
        window.setBackgroundDrawableResource(android.R.color.white)

        // ── 위치 권한 요청 (registerForActivityResult 방식) ─────────────────────────────────────────
        val locationLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        // ────────────────────────────────────────────────────────────────────────────────────────

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            DundunHiTheme {
                Surface(Modifier.fillMaxSize()) {
                    // ── 날씨 ViewModel 생성 ──────────────────────────────────────────────────────────
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

                    // ── 앱 시작 시 한 번만 위치 기반 날씨 로드 ───────────────────────────────────────────
                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { location ->
                                    if (location != null) {
                                        weatherVM.load(
                                            lat = location.latitude,
                                            lon = location.longitude
                                        )
                                    } else {
                                        // 위치가 null일 때 기본 좌표(서울시청)로 로드
                                        weatherVM.load(lat = 37.5665, lon = 126.9780)
                                    }
                                }
                                .addOnFailureListener {
                                    weatherVM.load(lat = 37.5665, lon = 126.9780)
                                }
                        } else {
                            weatherVM.load(lat = 37.5665, lon = 126.9780)
                        }
                    }
                    // ─────────────────────────────────────────────────────────────────────────────────

                    // 회원가입/로그인 흐름용 ViewModel
                    val signupVm: SignupViewModel = viewModel()

                    // ── NavController + NavHost 설정 ──────────────────────────────────────────────────
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        // ─── Splash Screen
                        composable("splash") {
                            SplashScreen(
                                onTimeout = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ─── Home
                        composable("home") {
                            HomeScreen(
                                onLoginClick   = { navController.navigate("login") },
                                onSignupClick  = { navController.navigate("signup") },
                                onGuardianClick = { navController.navigate("guardian") }
                            )
                        }

                        // ─── Login
                        composable("login") {
                            val loginVm: LoginViewModel = viewModel()
                            LoginScreen(
                                vm = loginVm,
                                onLoginSuccess = { userNumStr, userId ->
                                    navController.navigate("main/$userNumStr/${Uri.encode(userId)}") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onFindIdClick = {
                                    navController.navigate("find_id")
                                }
                            )
                        }

                        // ─── Combined Auth (선택적)
                        composable("auth") {
                            CombinedAuthScreen(
                                viewModel = signupVm,
                                onNext = { navController.navigate("signup") }
                            )
                        }

                        // ─── Signup
                        composable("signup") {
                            val state by signupVm.state.collectAsState()
                            SignupScreen(
                                viewModel = signupVm,
                                onSignupSuccess = {
                                    val newUserId = signupVm.lastUserId
                                    navController.navigate("loadingScreen/$newUserId") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                }
                            )
                            if (state is SignupResult.Error) {
                                Toast.makeText(
                                    this@MainActivity,
                                    (state as SignupResult.Error).reason,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        // ─── Guardian 로그인
                        composable("guardian") {
                            GuardianScreen(
                                onSubmit = { _, _ -> },
                                onSignupClick = { navController.navigate("auth") },
                                onGuardianFindIdClick = { navController.navigate("guardian_find_id") }
                            )
                        }

                        // ─── Guardian 회원가입
                        composable("guardian_signup") {
                            val state by signupVm.state.collectAsState()
                            SignupScreen(
                                viewModel = signupVm,
                                onSignupSuccess = {
                                    val newUserId = signupVm.lastUserId
                                    navController.navigate("loadingScreen/$newUserId") {
                                        popUpTo("guardian_signup") { inclusive = true }
                                    }
                                }
                            )
                            if (state is SignupResult.Error) {
                                Toast.makeText(
                                    this@MainActivity,
                                    (state as SignupResult.Error).reason,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        // ─── LoadingScreen (회원가입 직후 로딩 화면)
                        composable(
                            route = "loadingScreen/{userId}",
                            arguments = listOf(navArgument("userId") {
                                type = NavType.StringType
                            })
                        ) { backEntry ->
                            LoadingScreen(
                                navController = navController,
                                userId = backEntry.arguments?.getString("userId") ?: ""
                            )
                        }

                        // ─── Main (인자(userNum, userId) 전달 버전)
                        composable(
                            route = "main/{userNum}/{userId}",
                            arguments = listOf(
                                navArgument("userNum") { type = NavType.StringType },
                                navArgument("userId")  { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val userNum = backStackEntry.arguments?.getString("userNum")?.toIntOrNull() ?: 0
                            val userId = backStackEntry.arguments?.getString("userId")?.let { Uri.decode(it) } ?: ""

                            // ProfileViewModel 생성
                            val repository: UserRepository = RealUserRepository()
                            val profileViewModel: ProfileViewModel = viewModel(
                                factory = ProfileViewModelFactory(repository, userNum)
                            )

                            // 프로필 정보 로드
                            LaunchedEffect(Unit) {
                                profileViewModel.fetchUserFromServer()
                            }
                            
                            // 날씨 상태 가져오기
                            val weatherState by weatherVM.uiState.collectAsState()
                            val weatherData = (weatherState as? WeatherUiState.Success)?.data
                            
                            // sky 값을 weatherState 코드로 변환
                            val weatherStateCode = when (weatherData?.sky?.lowercase()) {
                                "맑음" -> 1  // SUNNY
                                "구름많음" -> 3  // CLOUDY
                                "흐림" -> 4  // OVERCAST
                                else -> 1  // 기본값 SUNNY
                            }

                            MainScreen(
                                userName = "${userId}님",
                                userProfileImg = profileViewModel.userProfileImg,
                                temperature = weatherData?.currentTempInt ?: 0,
                                highTemp = weatherData?.maxTempInt ?: 0,
                                lowTemp = weatherData?.minTempInt ?: 0,
                                weatherState = weatherStateCode,
                                precipitationType = 0,
                                onPhonePageClick = { navController.navigate("call") },
                                onMessagePageClick = { /* TODO */ },
                                onCameraPageClick = { navController.navigate("camera") },
                                onMapPageClick = { navController.navigate("map") },
                                onFindCultureCenter = { navController.navigate("culture_center") },
                                onKioskPageClick = { navController.navigate("kiosk") },
                                onProfileClick = { 
                                    navController.navigate("profile/$userNum/${Uri.encode(userId)}") {
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        // ─── ProfileScreen (userNum, userId 인자 전달) ────────────────────────────────────────
                        composable(
                            route = "profile/{userNum}/{userId}",
                            arguments = listOf(
                                navArgument("userNum") { type = NavType.StringType },
                                navArgument("userId")  { type = NavType.StringType }
                            )
                        ) { backEntry ->
                            val userNumStr = backEntry.arguments?.getString("userNum") ?: "0"
                            val userId = Uri.decode(backEntry.arguments?.getString("userId") ?: "")
                            val userNumInt = userNumStr.toIntOrNull() ?: 0

                            val repository: UserRepository = RealUserRepository()
                            val profileVm: ProfileViewModel = viewModel(
                                key = "ProfileViewModel_$userNumInt",
                                factory = ProfileViewModelFactory(repository, userNumInt)
                            )

                            ProfileScreen(
                                viewModel = profileVm,
                                userId = userId,
                                onUpdateProfileClick = {
                                    navController.navigate("update_profile/$userNumStr/${Uri.encode(userId)}")
                                },
                                onUpdatePasswordClick = {
                                    navController.navigate("update_password/$userNumStr")
                                },
                                navController = navController
                            )
                        }

                        // ─── UpdateProfileScreen
                        composable(
                            route = "update_profile/{userNum}/{userId}",
                            arguments = listOf(
                                navArgument("userNum") { type = NavType.StringType },
                                navArgument("userId")  { type = NavType.StringType }
                            )
                        ) { backEntry ->
                            val userNumStr = backEntry.arguments?.getString("userNum") ?: "0"
                            val userId = Uri.decode(backEntry.arguments?.getString("userId") ?: "")
                            val userNumInt = userNumStr.toIntOrNull() ?: 0

                            val repository: UserRepository = RealUserRepository()
                            val profileVm: ProfileViewModel = viewModel(
                                key = "ProfileViewModel_$userNumInt",
                                factory = ProfileViewModelFactory(repository, userNumInt)
                            )

                            UpdateProfileScreen(
                                viewModel = profileVm,
                                userId = userId,
                                onUpdateSuccess = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ─── UpdatePasswordScreen
                        composable(
                            route = "update_password/{userNum}",
                            arguments = listOf(navArgument("userNum") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userNum = backStackEntry.arguments?.getString("userNum") ?: ""
                            val repository: UserRepository = RealUserRepository()
                            UpdatePasswordScreen(
                                userNum = userNum,
                                navController = navController,
                                repository = repository
                            )
                        }

                        // ─── MapScreen
                        composable("map") {
                            MapScreen()
                        }

                        // ─── AlarmRecordScreen
                        composable("alarm") {
                            AlarmRecordScreen(navController)
                        }

                        // ─── AddAlarmScreen
                        composable("add_alarm") {
                            AddAlarmScreen(navController)
                        }

                        // ─── EditAlertScreen
                        composable("edit_alarm/{alertId}") { backStackEntry ->
                            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
                            EditAlertScreen(navController, alertId)
                        }

                        // ─── ActivityHistoryScreen
                        composable("activity_history") {
                            ActivityHistoryScreen()
                        }

                        // ─── CameraScreen
                        composable("camera") {
                            CameraScreen(navController)
                        }

                        // ─── LastPhotoScreen
                        composable("lastphoto") {
                            val dummyPhotos = listOf(
                                SharedPhoto(R.drawable.img1, fromMe = true),
                                SharedPhoto(R.drawable.img2, fromMe = false),
                                SharedPhoto(R.drawable.img3, fromMe = true)
                            )
                            LastPhotoScreen(photos = dummyPhotos, onAddPhoto = { /* TODO */ })
                        }

                        // ─── KioskScreen
                        composable("kiosk") {
                            KioskScreen()
                        }

                        // ─── CallScreen
                        composable("call") {
                            val callVm: CallViewModel = viewModel()
                            val shortcuts by callVm.shortcuts.collectAsState()
                            CallScreen(
                                contacts = shortcuts,
                                onAddShortcut = { idx ->
                                    navController.navigate("call_setup/$idx")
                                }
                            )
                        }

                        // ─── SetupShortcutScreen
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

                        // ─── FindIdScreen
                        composable("find_id") {
                            val findIdVm: FindIdViewModel = viewModel()
                            FindIdScreen(
                                viewModel = findIdVm,
                                onIdFound = {
                                    navController.popBackStack()
                                },
                                onLoginClick = { navController.navigate("login") }
                            )
                        }

                        // ─── Guardian_FindIdScreen
                        composable("guardian_find_id") {
                            val findIdVm: FindIdViewModel = viewModel()
                            Guardian_FindIdScreen(
                                viewModel = findIdVm,
                                onIdFound = {
                                    navController.popBackStack()
                                },
                                onLoginClick = { navController.navigate("guardian") }
                            )
                        }
                    }
                }
            }
        }
    }
}

