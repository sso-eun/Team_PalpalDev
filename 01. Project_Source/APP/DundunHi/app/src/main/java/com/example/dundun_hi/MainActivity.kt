package com.example.dundun_hi

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import com.example.dundun_hi.ui.signup.CombinedAuthScreen
import com.example.dundun_hi.ui.signup.SignupScreen
import com.example.dundun_hi.ui.signup.SignupViewModel
import com.example.dundun_hi.ui.theme.DundunHiTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : ComponentActivity() {
    companion object {
        private const val REQUEST_LOCATION = 1001
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 위치 권한 요청
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQUEST_LOCATION
            )
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            DundunHiTheme {
                Surface(Modifier.fillMaxSize()) {
                    // 날씨 ViewModel (Activity scope)
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

                    // 앱 시작 시 한 번만 위치 기반 날씨 로드
                    LaunchedEffect(Unit) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            fusedLocationClient.lastLocation
                                .addOnSuccessListener { location ->
                                    if (location != null) {
                                        Log.d(
                                            "LocationDebug",
                                            "latitude=${location.latitude}, longitude=${location.longitude}"
                                        )
                                        Toast.makeText(
                                            this@MainActivity,
                                            "위도: ${location.latitude}, 경도: ${location.longitude}",
                                            Toast.LENGTH_SHORT
                                        ).show()
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

                    // 한 번만 생성하는 SignupViewModel
                    val signupVm: SignupViewModel = viewModel()

                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        // ─── Home ───
                        composable("home") {
                            HomeScreen(
                                onLoginClick    = { navController.navigate("login") },
                                onSignupClick   = { navController.navigate("auth") },
                                onGuardianClick = { navController.navigate("guardian") }
                            )
                        }

                        // ─── Login ───
                        composable("login") {
                            val loginVm: LoginViewModel = viewModel()
                            LoginScreen(
                                vm = loginVm,
                                onLoginSuccess = { userId ->
                                    navController.navigate("main/${Uri.encode(userId)}") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ─── Combined Auth ───
                        composable("auth") {
                            CombinedAuthScreen(
                                viewModel = signupVm,
                                onNext = { navController.navigate("signup") }
                            )
                        }

                        // ─── 회원정보 입력(Signup) ───
                        composable("signup") {
                            SignupScreen(
                                viewModel = signupVm,
                                onSignupSuccess = {
                                    navController.navigate("loading/${Uri.encode(signupVm.lastUserId)}") {
                                        popUpTo("home") { inclusive = false }
                                    }
                                }
                            )
                        }

                        // ─── Loading ───
                        composable(
                            route = "loading/{userId}",
                            arguments = listOf(
                                navArgument("userId") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backEntry ->
                            val userId = backEntry.arguments?.getString("userId") ?: ""
                            LoadingScreen(
                                navController = navController,
                                userId = userId
                            )
                        }

                        // ─── Main ───
                        composable(
                            route = "main/{userName}",
                            arguments = listOf(navArgument("userName") {
                                type = NavType.StringType
                                defaultValue = ""
                            })
                        ) { backEntry ->
                            val name =
                                Uri.decode(backEntry.arguments?.getString("userName") ?: "손님")
                            val weatherState by weatherVM.uiState.collectAsState()
                            LaunchedEffect(weatherState) {
                                if (weatherState is WeatherUiState.Error) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "날씨 조회 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            val success = weatherState as? WeatherUiState.Success
                            val temp = success?.data?.currentTemp
                                ?.toDoubleOrNull()?.toInt() ?: 0
                            val max = success?.data?.maxTemp
                                ?.toDoubleOrNull()?.toInt() ?: 0
                            val min = success?.data?.minTemp
                                ?.toDoubleOrNull()?.toInt() ?: 0

                            MainScreen(
                                userName = "${name}님",
                                temperature = temp,
                                maxTemp = max,
                                minTemp = min,
                                onPhonePageClick = { navController.navigate("call") },
                                onMessagePageClick = { /* TODO */ },
                                onCameraPageClick = { navController.navigate("camera") },
                                onMapPageClick = { /* TODO */ },
                                onFindCultureCenter = { /* TODO */ },
                                onKioskPageClick = { navController.navigate("kiosk") },
                                onProfileClick = { navController.navigate("profile") }
                            )
                        }

                        // ─── Guardian ───
                        composable("guardian") {
                            GuardianScreen(
                                onSubmit = { _, _ -> },
                                onSignupClick = { navController.navigate("guardian_signup") }
                            )
                        }
                        composable("guardian_signup") {
                            Guardian_SignupScreen()
                        }

                        // ─── 기타 화면들 ───
                        composable("kiosk") { KioskScreen() }
                        composable("camera") { CameraScreen(navController) }
                        composable("lastphoto") {
                            val dummy = listOf(
                                SharedPhoto(R.drawable.img1, true),
                                SharedPhoto(R.drawable.img2, false),
                                SharedPhoto(R.drawable.img3, true)
                            )
                            LastPhotoScreen(photos = dummy, onAddPhoto = { /* TODO */ })
                        }
                        composable("profile") { ProfileScreen() }
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
                        composable("call_setup/{index}") { backEntry ->
                            val callVm: CallViewModel = viewModel()
                            val idx = backEntry.arguments?.getString("index")?.toIntOrNull() ?: 0
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
