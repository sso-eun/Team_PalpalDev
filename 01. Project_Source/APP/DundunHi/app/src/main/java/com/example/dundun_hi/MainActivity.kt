// app/src/main/java/com/example/dundun_hi/MainActivity.kt

package com.example.dundun_hi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.dundun_hi.data.LocationViewModel
import com.example.dundun_hi.data.RealUserRepository
import com.example.dundun_hi.data.UserRepository
import com.example.dundun_hi.data.WeatherRepository
import com.example.dundun_hi.model.CallShortcut
import com.example.dundun_hi.model.CallViewModel
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.model.WeatherUiState
import com.example.dundun_hi.model.WeatherViewModel
import com.example.dundun_hi.network.RetrofitClient
import com.example.dundun_hi.network.WeatherService
import com.example.dundun_hi.ui.*
import com.example.dundun_hi.ui.guardianProfile.GuardianProfileScreen
import com.example.dundun_hi.ui.guardianProfile.GuardianProfileViewModel
import com.example.dundun_hi.ui.guardianProfile.GuardianProfileViewModelFactory
import com.example.dundun_hi.ui.guardianProfile.GuardianUpdateProfileScreen
import com.example.dundun_hi.ui.guardianProfile.SeniorEditScreen
import com.example.dundun_hi.ui.login.FindIdScreen
import com.example.dundun_hi.ui.login.FindIdViewModel
import com.example.dundun_hi.ui.login.GuardianScreen
import com.example.dundun_hi.ui.login.Guardian_FindIdScreen
import com.example.dundun_hi.ui.login.LoginScreen
import com.example.dundun_hi.ui.login.LoginViewModel
import com.example.dundun_hi.ui.login.LoginViewModelFactory
import com.example.dundun_hi.ui.profile.ProfileScreen
import com.example.dundun_hi.ui.profile.ProfileViewModel
import com.example.dundun_hi.ui.profile.ProfileViewModelFactory
import com.example.dundun_hi.ui.profile.UpdatePasswordScreen
import com.example.dundun_hi.ui.profile.UpdateProfileScreen
import com.example.dundun_hi.ui.screen.CallScreen
import com.example.dundun_hi.ui.signup.*
import com.example.dundun_hi.ui.theme.DundunHiTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.example.dundun_hi.ui.signup.LoadingScreen as SignupLoadingScreen

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawableResource(android.R.color.white)

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val callVm: CallViewModel = CallViewModel(application)

        setContent {
            DundunHiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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

                    val signupVm: SignupViewModel = viewModel()
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(
                                onTimeout = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                onLoginClick = { navController.navigate("login") },
                                onSignupClick = { navController.navigate("signup_entry") }
                            )
                        }

                        composable("login") {
                            val loginVm: LoginViewModel = viewModel(
                                factory = LoginViewModelFactory(application = this@MainActivity.application)
                            )
                            LoginScreen(
                                navController = navController,
                                vm = loginVm,
                                onFindIdClick = { navController.navigate("find_id") },
                                onSignupClick = { navController.navigate("signup_entry") }
                            )
                        }

                        // ... (이하 다른 모든 회원가입, 로그인 라우트는 동일하므로 생략) ...

                        composable(
                            route = "main/{userNum}/{userId}",
                            arguments = listOf(
                                navArgument("userNum") { type = NavType.StringType },
                                navArgument("userId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val userNum = backStackEntry.arguments?.getString("userNum")?.toIntOrNull() ?: 0
                            val userId = backStackEntry.arguments?.getString("userId")?.let { Uri.decode(it) } ?: ""

                            LaunchedEffect(userNum, userId) {
                                val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("user_num", userNum.toString())
                                editor.putString("user_id", userId)
                                editor.commit()
                            }

                            val repository: UserRepository = RealUserRepository()
                            val profileViewModel: ProfileViewModel = viewModel(
                                factory = ProfileViewModelFactory(repository, userNum, this@MainActivity)
                            )

                            // (추가) LocationViewModel 인스턴스 생성
                            val locationViewModel: LocationViewModel = viewModel()

                            LaunchedEffect(Unit) {
                                profileViewModel.fetchUserFromServer()
                            }

                            val userProfileImg by remember { derivedStateOf { profileViewModel.userProfileImg } }
                            val currentUserNum by remember { derivedStateOf { profileViewModel.userNumber } }
                            val weatherState by weatherVM.uiState.collectAsState()
                            val weatherData = (weatherState as? WeatherUiState.Success)?.data
                            val weatherStateCode = when (weatherData?.sky?.lowercase()) {
                                "맑음" -> 1
                                "구름많음" -> 3
                                "흐림" -> 4
                                else -> 1
                            }

                            MainScreen(
                                profileViewModel = profileViewModel,
                                locationViewModel = locationViewModel, // (추가) ViewModel 전달
                                userName = "${userId}님",
                                userProfileImg = userProfileImg,
                                userNum = currentUserNum,
                                temperature = weatherData?.currentTempInt ?: 0,
                                highTemp = weatherData?.maxTempInt ?: 0,
                                lowTemp = weatherData?.minTempInt ?: 0,
                                weatherState = weatherStateCode,
                                precipitationType = 0,
                                onPhonePageClick = { navController.navigate("call") },
                                onMessagePageClick = { /* TODO */ },
                                onCameraPageClick = { navController.navigate("camera/$currentUserNum") },
                                onMapPageClick = { navController.navigate("map") },
                                // (수정) 새로운 콜백 함수에 내비게이션 연결
                                onNavigateToCultureCenter = {
                                    navController.navigate("culture_center")
                                },
                                onKioskPageClick = { navController.navigate("kiosk") },
                                onProfileClick = {
                                    when (profileViewModel.userType) {
                                        1 -> navController.navigate("guardian_profile/$currentUserNum")
                                        else -> navController.navigate("profile/$currentUserNum/${Uri.encode(userId)}")
                                    }
                                }
                            )
                        }


                        // (추가) 새로운 문화센터 화면 라우트
                        composable("culture_center") {
                            CultureCenterScreen()
                        }


// ─── Guardian (기존 기능 유지)
                        composable("guardian") {
                            GuardianScreen(
                                onSubmit = { userNumStr, userId ->
                                    // 가디언 로그인 시에도 SharedPreferences 저장
                                    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("user_num", userNumStr)
                                    editor.putString("user_id", userId)
                                    editor.putString("user_type", "1") // 가디언은 1
                                    val saveSuccess = editor.commit()

                                    android.util.Log.d("MainActivity", "가디언 로그인 - SharedPreferences 저장 결과: $saveSuccess")
                                    android.util.Log.d("MainActivity", "저장된 값 - userNum: $userNumStr, userId: $userId, userType: 1")

                                    navController.navigate("guardian_profile/$userNumStr") {
                                        popUpTo("guardian") { inclusive = true }
                                    }
                                },
                                onSignupClick = { navController.navigate("signup_entry") },
                                onGuardianFindIdClick = { navController.navigate("guardian_find_id") }
                            )
                        }

                        // ─── ProfileScreen (userNum, userId 인자 전달)
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
                                factory = ProfileViewModelFactory(repository, userNumInt, this@MainActivity)
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

                        // ─── GuardianProfileScreen
                        composable(
                            route = "guardian_profile/{userNum}",
                            arguments = listOf(navArgument("userNum") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val userNum = backStackEntry.arguments?.getString("userNum")?.toIntOrNull() ?: 0
                            val context = LocalContext.current

                            val repository = RealUserRepository()
                            val guardianViewModel: GuardianProfileViewModel = viewModel(
                                factory = GuardianProfileViewModelFactory(repository, userNum, context)
                            )

                            GuardianProfileScreen(
                                viewModel = guardianViewModel,
                                onEditSeniorClick = {
                                    navController.navigate("SeniorEditScreen/$userNum")
                                },
                                navController = navController
                            )
                        }

                        // ─── Guardian 프로필 수정(명시적 파라미터)
                        composable(
                            route = "edit_guardian_profile/{guardianUserNum}",
                            arguments = listOf(navArgument("guardianUserNum") {
                                type = NavType.IntType
                                defaultValue = -1
                            })
                        ) { backStackEntry ->
                            val guardianUserNum = backStackEntry.arguments?.getInt("guardianUserNum") ?: -1

                            if (guardianUserNum == -1) {
                                LaunchedEffect(Unit) {
                                    navController.popBackStack()
                                }
                                return@composable
                            }

                            val context = LocalContext.current
                            val repository = RealUserRepository()
                            val guardianViewModel = remember {
                                GuardianProfileViewModel(repository, guardianUserNum, context)
                            }

                            GuardianUpdateProfileScreen(
                                viewModel = guardianViewModel,
                                userId = guardianViewModel.guardianId,
                                userNum = guardianUserNum,
                                onUpdateSuccess = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        // ─── SeniorEditScreen
                        composable("SeniorEditScreen/{userNum}") { backStackEntry ->
                            val userNum = backStackEntry.arguments?.getString("userNum")?.toIntOrNull()
                            val context = LocalContext.current
                            val guardianViewModel = remember {
                                GuardianProfileViewModel(
                                    repository = RealUserRepository(),
                                    guardianUserNum = userNum ?: -1,
                                    context = context
                                )
                            }

                            SeniorEditScreen(
                                viewModel = guardianViewModel,
                                onSaveSuccess = { navController.popBackStack() },
                                onBackClick = { navController.popBackStack() }
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
                                factory = ProfileViewModelFactory(repository, userNumInt, this@MainActivity)
                            )

                            UpdateProfileScreen(
                                viewModel = profileVm,
                                userId = userId,
                                onUpdateSuccess = { navController.popBackStack() }
                            )
                        }

                        // ─── GuardianUpdateProfileScreen (다른 경로)
                        composable(
                            route = "guardian_update_profile/{userNum}",
                            arguments = listOf(navArgument("userNum") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userNum = backStackEntry.arguments?.getInt("userNum") ?: -1
                            if (userNum == -1) return@composable

                            val context = LocalContext.current
                            val guardianViewModel: GuardianProfileViewModel = viewModel(
                                factory = GuardianProfileViewModelFactory(
                                    RealUserRepository(),
                                    userNum,
                                    context
                                )
                            )

                            GuardianUpdateProfileScreen(
                                viewModel = guardianViewModel,
                                userId = guardianViewModel.guardianId,
                                userNum = userNum,
                                onUpdateSuccess = { navController.popBackStack() }
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
                        composable("map") { MapScreen() }

                        // ─── AlarmRecordScreen
                        composable("alarm") { AlarmRecordScreen(navController) }

                        // ─── AddAlarmScreen
                        composable("add_alarm") { AddAlarmScreen(navController) }

                        // ─── EditAlertScreen
                        composable("edit_alarm/{alertId}") { backStackEntry ->
                            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
                            EditAlertScreen(navController, alertId)
                        }

                        // ─── ActivityHistoryScreen
                        composable("activity_history") { ActivityHistoryScreen() }

                        composable(
                            route = "camera/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId")
                                ?: 0 // ← 아마 이렇게 되어있을 것입니다.
                            CameraScreen(userId = userId, navController = navController)
                        }
//
//                        // ─── LastPhotoScreen
//                        composable(
//                            route = "lastphoto/{userId}",
//                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
//                        ) { back ->
//                            val myId = back.arguments!!.getInt("userId")
//                            LastPhotoScreen(
//                                senderId = myId,
//                                receiverId = 3,
//                                viewerId = myId
//                            )
//                        }

                        composable(
                            // 1. 경로(Route) 정의
                            route = "lastphoto/{userId}",

                            // 2. 전달인자(Argument) 타입 정의
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { back -> // 3. Composable 컨텐츠 람다

                            // 4. 전달인자 값 가져오기
                            val myId = back.arguments!!.getInt("userId")

                            // 5. 화면(Screen) 호출
                            LastPhotoScreen(
                                senderId = myId,
                                receiverId = 3,
                                viewerId = myId
                            )
                        }

                        // ─── KioskScreen
                        composable("kiosk") { KioskScreen() }

                        // ─── CallScreen
                        composable("call") {
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
                            val idx = back.arguments?.getString("index")?.toIntOrNull() ?: 0
                            SetupShortcutScreen(
                                index = idx,
                                viewModel = callVm,
                                onDone = { navController.popBackStack() }
                            )
                        }

                        // ─── FindIdScreen
                        composable("find_id") {
                            val findIdVm: FindIdViewModel = viewModel()
                            FindIdScreen(
                                viewModel = findIdVm,
                                onIdFound = { navController.popBackStack() },
                                onLoginClick = { navController.navigate("login") }
                            )
                        }

                        // ─── Guardian_FindIdScreen
                        composable("guardian_find_id") {
                            val findIdVm: FindIdViewModel = viewModel()
                            Guardian_FindIdScreen(
                                viewModel = findIdVm,
                                onIdFound = { navController.popBackStack() },
                                onLoginClick = { navController.navigate("login") } // 통합 로그인으로 변경
                            )
                        }
                    }
                }
            }
        }
    }
}