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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.ui.*
import com.example.dundun_hi.ui.login.LoginScreen
import com.example.dundun_hi.ui.login.LoginViewModel
import com.example.dundun_hi.ui.screen.CallScreen
import com.example.dundun_hi.ui.screen.CallViewModel
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
                        // ───── 기본 플로우 ─────
                        composable("home") {
                            HomeScreen(
                                onLoginClick = { navController.navigate("login") },
                                onSignupClick = { navController.navigate("signup") },
                                onGuardianClick = { navController.navigate("guardian") }
                            )
                        }

                        // ───── 로그인 화면 ─────
                        composable("login") {
                            val loginVm: LoginViewModel = viewModel()
                            LoginScreen(
                                vm = loginVm,
                                onLoginSuccess = { userNum ->
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // ───── 회원가입 화면 ─────
                        composable("signup") {
                            val vm: SignupViewModel = viewModel()
                            val state by vm.state.collectAsState()

                            SignupScreen { req -> vm.signup(req) }

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
                                    else -> {}
                                }
                            }
                        }

                        // ───── 보호자 관련 ─────
                        composable("guardian") {
                            GuardianScreen(
                                onSubmit = { _, _ -> },
                                onSignupClick = { navController.navigate("guardian_signup") }
                            )
                        }
                        composable("guardian_signup") { Guardian_SignupScreen() }

                        // ───── 메인 화면 ─────
                        composable("main") {
                            MainScreen(
                                userName = "길동님",
                                temperature = 19,
                                highTemp = 25,
                                lowTemp = 7,
                                onPhonePageClick = { navController.navigate("call") },
                                onMessagePageClick = { navController.navigate("profile") }, // ✅ 수정됨
                                onCameraPageClick = { navController.navigate("camera") },
                                onMapPageClick = {},
                                onFindCultureCenter = {},
                                onKioskPageClick = { navController.navigate("kiosk") },
                                onProfileClick = { navController.navigate("profile") }
                            )
                        }

                        // ───── 서브 화면들 ─────
                        composable("kiosk") { KioskScreen() }
                        composable("camera") { CameraScreen(navController) }

                        composable("lastphoto") {
                            val dummyList = listOf(
                                SharedPhoto(R.drawable.img1, fromMe = true),
                                SharedPhoto(R.drawable.img2, fromMe = false),
                                SharedPhoto(R.drawable.img3, fromMe = true)
                            )
                            LastPhotoScreen(
                                photos = dummyList,
                                onAddPhoto = { /* TODO */ }
                            )
                        }

                        composable("profile") { ProfileScreen() }

                        // ───── 빠른전화 화면 ─────
                        composable("call") {
                            val viewModel: CallViewModel = viewModel()
                            val shortcuts by viewModel.shortcuts.collectAsState()

                            CallScreen(
                                contacts = shortcuts,
                                onAddShortcut = { idx ->
                                    navController.navigate("call_setup/$idx")
                                }
                            )
                        }

                        // ───── 단축키 설정 화면 ─────
                        composable("call_setup/{index}") { back ->
                            val viewModel: CallViewModel = viewModel()
                            val idx = back.arguments?.getString("index")?.toIntOrNull() ?: 0

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
