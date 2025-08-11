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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.example.dundun_hi.model.CallShortcut
import com.example.dundun_hi.model.CallViewModel
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.model.WeatherUiState
import com.example.dundun_hi.model.WeatherViewModel
import com.example.dundun_hi.network.RetrofitClient
import com.example.dundun_hi.network.WeatherService
import com.example.dundun_hi.ui.*
import com.example.dundun_hi.ui.ActivityHistoryScreen
import com.example.dundun_hi.ui.KioskScreen
import com.example.dundun_hi.ui.LastPhotoScreen
import com.example.dundun_hi.ui.SetupShortcutScreen
import com.example.dundun_hi.ui.HomeScreen
import com.example.dundun_hi.ui.MainScreen
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
import com.example.dundun_hi.ui.signup.CombinedAuthScreen
import com.example.dundun_hi.ui.signup.LoadingScreen
import com.example.dundun_hi.ui.signup.SignupResult
import com.example.dundun_hi.ui.signup.SignupScreen
import com.example.dundun_hi.ui.signup.SignupViewModel
import com.example.dundun_hi.ui.signup.LoadingScreen as SignupLoadingScreen
import com.example.dundun_hi.ui.signup.FamilyCertificationScreen
import com.example.dundun_hi.ui.signup.SeniorInfoScreen
import com.example.dundun_hi.ui.signup.AuthLoadingScreen
import com.example.dundun_hi.ui.signup.SeniorProfileViewModel
import com.example.dundun_hi.ui.signup.SeniorProfileViewModelFactory
import com.example.dundun_hi.ui.signup.SearchState
import com.example.dundun_hi.ui.signup.FamilyCertViewModel
import com.example.dundun_hi.ui.signup.FamilyCertViewModelFactory
import com.example.dundun_hi.ui.theme.DundunHiTheme
import com.example.dundun_hi.ui.guardianProfile.GuardianProfileScreen
import com.example.dundun_hi.ui.guardianProfile.GuardianProfileViewModel
import com.example.dundun_hi.ui.guardianProfile.GuardianProfileViewModelFactory
import com.example.dundun_hi.ui.guardianProfile.GuardianUpdateProfileScreen
import com.example.dundun_hi.ui.guardianProfile.SeniorEditScreen
import com.example.dundun_hi.ui.HomeAddressPopup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

        // CallViewModel 인스턴스 생성
        val callVm: CallViewModel = CallViewModel(application)

        setContent {
            DundunHiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
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

                        // ─── Home (새로운 개선된 버전)
                        composable("home") {
                            HomeScreen(
                                // "구면이세요?" -> 통합 로그인 화면으로 연결
                                onLoginClick = { navController.navigate("login") },
                                // "초면이세요?" -> 새로운 회원가입 화면으로 연결
                                onSignupClick = { navController.navigate("signup_entry") }
                            )
                        }

                        // ─── 통합 Login (개선된 버전)
                        composable("login") {
                            // Factory를 사용하여 LoginViewModel을 올바르게 생성
                            val loginVm: LoginViewModel = viewModel(
                                factory = LoginViewModelFactory(application = this@MainActivity.application)
                            )

                            LoginScreen(
                                navController = navController,
                                vm = loginVm,
                                onFindIdClick = { navController.navigate("find_id") },
                                onSignupClick = { navController.navigate("signup_entry") } // 회원가입 화면으로
                            )
                        }

                        // ─── 회원가입 입구 (시니어가 디폴트)
                        composable("signup_entry") {
                            CombinedAuthScreen(
                                viewModel = signupVm,
                                userType = 0, // 시니어
                                onNext = { navController.navigate("senior_final_signup") },
                                bottomContent = {
                                    Spacer(Modifier.height(24.dp))
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "보호자이신가요? 보호자용 회원가입",
                                            modifier = Modifier.clickable { navController.navigate("guardian_auth") },
                                            color = Color.Gray,
                                            textDecoration = TextDecoration.Underline,
                                            fontSize = 18.sp
                                        )
                                    }
                                }
                            )
                        }

                        // ─── 보호자 전용 본인 인증
                        composable("guardian_auth") {
                            CombinedAuthScreen(
                                viewModel = signupVm,
                                userType = 1, // 보호자
                                onNext = { navController.navigate("family_certification") }
                            )
                        }

                        // ─── 시니어 최종 Signup
                        composable("senior_final_signup") {
                            val state by signupVm.state.collectAsState()
                            SignupScreen(
                                viewModel = signupVm,
                                onSignupSuccess = {
                                    val newUserId = signupVm.lastUserId
                                    navController.navigate("loadingScreen/$newUserId") {
                                        popUpTo("senior_final_signup") { inclusive = true }
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

                        // ─── Family Certification
                        composable("family_certification") {
                            val familyCertVm: FamilyCertViewModel = viewModel(
                                factory = FamilyCertViewModelFactory(RetrofitClient.memberService)
                            )

                            FamilyCertificationScreen(
                                signupViewModel = signupVm,
                                familyCertViewModel = familyCertVm,
                                onConfirm = {
                                    val userNum = signupVm.createdUserNum ?: 0
                                    val userId = signupVm.createdUserId ?: ""
                                    val currentSearchState = familyCertVm.searchState.value
                                    val seniorNum = if (currentSearchState is SearchState.Success) {
                                        currentSearchState.senior.userNum
                                    } else {
                                        0
                                    }
                                    navController.navigate("auth_loading/$userNum/${Uri.encode(userId)}/$seniorNum")
                                },
                                onTestConfirm = {
                                    val testUserNum = 47
                                    val testUserId = "박지성"
                                    val testSeniorNum = 50
                                    navController.navigate("auth_loading/$testUserNum/${Uri.encode(testUserId)}/$testSeniorNum")
                                }
                            )
                        }

                        // ─── 인증 상태 로딩 화면
                        composable(
                            route = "auth_loading/{userNum}/{userId}/{seniorNum}",
                            arguments = listOf(
                                navArgument("userNum") { type = NavType.IntType },
                                navArgument("userId") { type = NavType.StringType },
                                navArgument("seniorNum") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val userNum = backStackEntry.arguments?.getInt("userNum") ?: 0
                            val userId = backStackEntry.arguments?.getString("userId")?.let { Uri.decode(it) } ?: ""
                            val seniorNum = backStackEntry.arguments?.getInt("seniorNum") ?: 0

                            AuthLoadingScreen(
                                navController = navController,
                                userNum = userNum,
                                userId = userId,
                                seniorNum = seniorNum
                            )
                        }

                        // ─── Senior Info
                        composable(
                            route = "senior_profile/{guardianNum}/{guardianId}/{seniorNum}",
                            arguments = listOf(
                                navArgument("seniorNum") { type = NavType.IntType },
                                navArgument("guardianNum") { type = NavType.IntType },
                                navArgument("guardianId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val guardianNum = backStackEntry.arguments?.getInt("guardianNum") ?: 0
                            val guardianId = backStackEntry.arguments?.getString("guardianId")?.let { Uri.decode(it) } ?: ""
                            val seniorNum = backStackEntry.arguments?.getInt("seniorNum") ?: 0

                            val seniorViewModel: SeniorProfileViewModel = viewModel(
                                factory = SeniorProfileViewModelFactory(RetrofitClient.memberService)
                            )

                            LaunchedEffect(key1 = seniorNum) {
                                if (seniorNum > 0) {
                                    seniorViewModel.fetchSeniorProfile(seniorNum)
                                }
                            }

                            SeniorInfoScreen(
                                viewModel = seniorViewModel,
                                onConfirm = {
                                    navController.navigate("main/$guardianNum/${Uri.encode(guardianId)}") {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ─── LoadingScreen (시니어 회원가입 직후 로딩 화면)
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

                            // ✅ Main 화면에서도 SharedPreferences 업데이트 (보장)
                            LaunchedEffect(userNum, userId) {
                                val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("user_num", userNum.toString())
                                editor.putString("user_id", userId)
                                val saveSuccess = editor.commit()
                                android.util.Log.d("MainActivity", "Main화면 - SharedPreferences 업데이트: $saveSuccess")
                            }

                            // ProfileViewModel 생성
                            val repository: UserRepository = RealUserRepository()
                            val profileViewModel: ProfileViewModel = viewModel(
                                factory = ProfileViewModelFactory(repository, userNum, this@MainActivity)
                            )

                            // 프로필 정보 로드
                            LaunchedEffect(Unit) {
                                profileViewModel.fetchUserFromServer()
                            }

                            // ProfileViewModel의 상태를 실시간으로 관찰
                            val userProfileImg by remember { derivedStateOf { profileViewModel.userProfileImg } }
                            val currentUserNum by remember { derivedStateOf { profileViewModel.userNumber } }

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
                                profileViewModel = profileViewModel,
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
                                onFindCultureCenter = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.cjmh.or.kr/study.html"))
                                    startActivity(intent)
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

                        // ─── CameraScreen
                        composable(
                            route = "camera/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments!!.getInt("userId")
                            CameraScreen(userId = userId, navController = navController)
                        }

                        // ─── LastPhotoScreen
                        composable(
                            route = "lastphoto/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.IntType })
                        ) { back ->
                            val myId = back.arguments!!.getInt("userId")
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