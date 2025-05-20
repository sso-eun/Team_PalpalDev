package com.example.dundun_hi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.ui.screen.LastPhotoScreen
import com.example.dundun_hi.ui.theme.DundunHiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DundunHiTheme {
                Surface(Modifier.fillMaxSize()) {

                    val navController = rememberNavController()

                    NavHost(navController, startDestination = "home") {

                        // ───── 기본 플로우 ─────
                        composable("home") {
                            HomeScreen(
                                onLoginClick = { navController.navigate("login") },
                                onSignupClick = { navController.navigate("signup") },
                                onGuardianClick = { navController.navigate("guardian") }
                            )
                        }

                        composable("login") {
                            LoginScreen { navController.navigate("main") }
                        }

                        composable("signup") {
                            SignupScreen(navController)
                        }

                        composable("loading") {
                            LoadingScreen(navController, userName = "길동님")
                        }

                        composable("guardian") {
                            GuardianScreen(
                                onSubmit = { _, _ -> },
                                onSignupClick = { navController.navigate("guardian_signup") }
                            )
                        }

                        composable("guardian_signup") {
                            Guardian_SignupScreen()
                        }

                        // ───── 메인 메뉴 ─────
                        composable("main") {
                            MainScreen(
                                userName = "길동님",
                                temperature = 19,
                                highTemp = 25,
                                lowTemp = 7,
                                onPhonePageClick = {},
                                onMessagePageClick = {},
                                onCameraPageClick = { navController.navigate("camera") },
                                onMapPageClick = {},
                                onFindCultureCenter = {},
                                onKioskPageClick = { navController.navigate("kiosk") },
                                onProfileClick = { navController.navigate("profile") },
                                onGuardianProfileClick = { navController.navigate("guardian_profile") }
                            )
                        }

                        // ───── 서브 화면 ─────
                        composable("kiosk") {
                            KioskScreen()
                        }

                        composable("camera") {
                            CameraScreen(navController)
                        }

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

                        composable("profile") {
                            ProfileScreen(navController)
                        }

                        composable("guardian_profile") {
                            Guardian_Profile(navController)
                        }

                        //알림 페이지 경로 등록
                        composable("alert") {
                            AlertScreen()
                        }

                        //위치 페이지 경로 등록
                        composable("location") {
                            LocationScreen()
                        }
                    }
                }
            }
        }
    }
}
