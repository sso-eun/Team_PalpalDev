package com.example.dundun_hi

import android.Manifest
import android.content.Context
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
                            val context = LocalContext.current
                            val prefs = remember { context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE) }
                            val userNum = prefs.getString("user_num", null)
                            val userId  = prefs.getString("user_id",  null)

                            HomeScreen(
                                onLoginClick = { navController.navigate("login") },
                                onSignupClick = { navController.navigate("signup_entry") },
                                onAutoSignedIn = {
                                    navController.navigate("main/$userNum/$userId") {
                                        popUpTo("home") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }

                        composable("login") {

                            val ocrName by navController.currentBackStackEntry!!
                                .savedStateHandle
                                .getStateFlow("ocr_name_result", "")
                                .collectAsState()

                            val loginVm: LoginViewModel = viewModel(
                                factory = LoginViewModelFactory(application = this@MainActivity.application)
                            )
                            LoginScreen(
                                navController = navController,
                                vm = loginVm,
                                onFindIdClick = { navController.navigate("find_id") },
                                onSignupClick = { navController.navigate("signup_entry") },
                                onRequestOcr = { navController.navigate("ocr") },
                                ocrPrefill = ocrName

                            )
                        }

                        composable("signup_entry") {

                            //ocr_sso
                            val ocrName by navController.currentBackStackEntry!!
                                .savedStateHandle
                                .getStateFlow("ocr_name_result", "")
                                .collectAsState()

                            CombinedAuthScreen(
                                viewModel = signupVm,
                                userType = 0,
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
                                },
//                                onRequestOcr = { navController.navigate("ocr") },
                                onRequestOcr = { navController.navigate("ocr") },
                                ocrPrefill = ocrName
                            )

                            //ocr_중복제거
                            LaunchedEffect(ocrName) {
                                if (ocrName.isNotBlank()) {
                                    navController.currentBackStackEntry!!
                                        .savedStateHandle
                                        .remove<String>("ocr_name_result")
                                }
                            }

                        }

                        composable("guardian_auth") {
                            CombinedAuthScreen(
                                viewModel = signupVm,
                                userType = 1,
                                onRequestOcr = {},
                                ocrPrefill = "",
                                onNext = { navController.navigate("family_certification") }
                            )
                        }

                        // MainActivity.kt에서 senior_final_signup 부분만 수정

                        composable("senior_final_signup") {
                            val state by signupVm.state.collectAsState()

                            SignupScreen(
                                viewModel = signupVm,
                                onSignupSuccess = {
                                    // 회원가입 성공 시 로그인 화면으로 이동
                                    navController.navigate("login") {
                                        popUpTo("senior_final_signup") { inclusive = true }
                                    }
                                },
                                onTimeout = {
                                    val userNum = signupVm.createdUserNum   // 혹은 lastUserNum (정확한 필드 확인)
                                    val userId = signupVm.createdUserId
                                    navController.navigate("main/$userNum/$userId")
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

                        composable(
                            route = "loadingScreen/{userId}",
                            arguments = listOf(navArgument("userId") { type = NavType.StringType })
                        ) { backEntry ->
                            SignupLoadingScreen(
                                navController = navController,
                                userId = backEntry.arguments?.getString("userId") ?: ""
                            )
                        }

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

                            // ▼▼▼ [수정 1] LocationViewModel 생성 ▼▼▼
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

                            // ▼▼▼ [수정 2] MainScreen에 ViewModel 전달 및 콜백 수정 ▼▼▼
                            MainScreen(
                                profileViewModel = profileViewModel,
                                locationViewModel = locationViewModel,
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
                                onCameraPageClick = {
                                    navController.navigate("camera/$currentUserNum")
                                },
                                onMapPageClick = { navController.navigate("map") },
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

                        // ▼▼▼ [추가] culture_center 화면을 위한 새로운 경로 ▼▼▼
                        composable("culture_center") {
                            CultureCenterScreen()
                        }

                        composable("guardian") {
                            GuardianScreen(
                                onSubmit = { userNumStr, userId ->
                                    val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
                                    val editor = sharedPreferences.edit()
                                    editor.putString("user_num", userNumStr)
                                    editor.putString("user_id", userId)
                                    editor.putString("user_type", "1")
                                    editor.commit()
                                    navController.navigate("guardian_profile/$userNumStr") {
                                        popUpTo("guardian") { inclusive = true }
                                    }
                                },
                                onSignupClick = { navController.navigate("signup_entry") },
                                onGuardianFindIdClick = { navController.navigate("guardian_find_id") }
                            )
                        }

                        composable(
                            route = "profile/{userNum}/{userId}",
                            arguments = listOf(
                                navArgument("userNum") { type = NavType.StringType },
                                navArgument("userId") { type = NavType.StringType }
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
                                navController = navController,
                                navigateToMain = {  // ✅ 이 부분 추가
                                    navController.navigate("main/$userNumStr/${Uri.encode(userId)}") {
                                        popUpTo("main/$userNumStr/${Uri.encode(userId)}") { inclusive = true }
                                    }
                                }

                            )
                        }

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

                        composable(
                            route = "update_profile/{userNum}/{userId}",
                            arguments = listOf(
                                navArgument("userNum") { type = NavType.StringType },
                                navArgument("userId") { type = NavType.StringType }
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

                        composable("map") { MapScreen() }
                        composable("alarm") { AlarmRecordScreen(navController) }
                        composable("add_alarm") { AddAlarmScreen(navController) }
                        composable("edit_alarm/{alertId}") { backStackEntry ->
                            val alertId = backStackEntry.arguments?.getString("alertId") ?: ""
                            EditAlertScreen(navController, alertId)
                        }
                        composable("activity_history") { ActivityHistoryScreen() }
                        // MainActivity.kt
                        composable(
                            route = "camera/{currentUserNum}",
                            arguments = listOf(navArgument("currentUserNum") { type = NavType.IntType })
                        ) { backStackEntry ->
                            val currentUserNum = backStackEntry.arguments?.getInt("currentUserNum") ?: 0

                            CameraScreen(
                                userId = currentUserNum,
                                navController = navController
                                // profileViewModel 전달하지 않음 (CameraScreen 내부에서 생성)
                            )
                        }


                        composable(
                            route = "lastphoto/{userId}/{receiverId}",
                            arguments = listOf(
                                navArgument("userId") { type = NavType.IntType },
                                navArgument("receiverId") { type = NavType.IntType }
                            )
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            val receiverId = backStackEntry.arguments?.getInt("receiverId") ?: 0

                            LastPhotoScreen(
                                senderId = userId,
                                receiverId = receiverId,
                                viewerId = userId,
                                onPhotoClick = { photoId, sortOrder, filter ->
                                    // 사진 ID와 필터/정렬 상태를 함께 상세 화면으로 이동
                                    // URL 인코딩으로 특수문자 처리
                                    val encodedPhotoId = java.net.URLEncoder.encode(photoId, "UTF-8")
                                    navController.navigate("photo_detail/$userId/$receiverId/$encodedPhotoId/${sortOrder.name}/${filter.name}")
                                }
                            )
                        }
// MainActivity.kt - photo_detail route 부분 오타 수정

                        composable(
                            route = "photo_detail/{senderId}/{receiverId}/{photoId}/{sortOrder}/{filter}",
                            arguments = listOf(
                                navArgument("senderId") { type = NavType.IntType },
                                navArgument("receiverId") { type = NavType.IntType },
                                navArgument("photoId") { type = NavType.StringType },
                                navArgument("sortOrder") { type = NavType.StringType },
                                navArgument("filter") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val senderId = backStackEntry.arguments?.getInt("senderId") ?: 0
                            val receiverId = backStackEntry.arguments?.getInt("receiverId") ?: 0
                            val encodedPhotoId = backStackEntry.arguments?.getString("photoId") ?: ""
                            val sortOrder = backStackEntry.arguments?.getString("sortOrder") ?: "NEWEST"
                            val filter = backStackEntry.arguments?.getString("filter") ?: "ALL"

                            // URL 디코딩으로 원본 photoId 복원
                            val photoId = try {
                                java.net.URLDecoder.decode(encodedPhotoId, "UTF-8")
                            } catch (e: Exception) {
                                encodedPhotoId
                            }

                            PhotoDetailScreen(
                                targetPhotoId = photoId,
                                sortOrder = sortOrder,
                                filter = filter,
                                senderId = senderId,
                                receiverId = receiverId,
                                viewerId = senderId, // senderId를 viewerId로 사용하고 있음
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable("kiosk") { KioskScreen(navController = navController) }
                        composable("call") {
                            val shortcuts by callVm.shortcuts.collectAsState()
                            CallScreen(
                                contacts = shortcuts,
                                onAddShortcut = { idx ->
                                    navController.navigate("call_setup/$idx")
                                },
                                navController = navController
                            )
                        }
                        composable("call_setup/{index}") { back ->
                            val idx = back.arguments?.getString("index")?.toIntOrNull() ?: 0
                            SetupShortcutScreen(
                                index = idx,
                                viewModel = callVm,
                                onDone = { navController.popBackStack() }
                            )
                        }
                        composable("find_id") {
                            val findIdVm: FindIdViewModel = viewModel()
                            FindIdScreen(
                                viewModel = findIdVm,
                                onIdFound = { navController.popBackStack() },
                                onLoginClick = { navController.navigate("login") }
                            )
                        }
                        composable("guardian_find_id") {
                            val findIdVm: FindIdViewModel = viewModel()
                            Guardian_FindIdScreen(
                                viewModel = findIdVm,
                                onIdFound = { navController.popBackStack() },
                                onLoginClick = { navController.navigate("login") }
                            )
                        }
//                        composable("ocr") { OCRScreen() }

                        composable("ocr") {
                            OCRScreen { scannedName ->
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set("ocr_name_result", scannedName)
                                navController.popBackStack()
                            }
                        }


                    }
                }
            }
        }
    }
}