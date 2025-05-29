// MainActivity.kt
package com.example.dundun_hi

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dundun_hi.model.SharedPhoto
import com.example.dundun_hi.ui.*
import com.example.dundun_hi.ui.login.LoginScreen
import com.example.dundun_hi.ui.login.LoginViewModel
import com.example.dundun_hi.ui.screen.*
import com.example.dundun_hi.ui.signup.SignupResult
import com.example.dundun_hi.ui.signup.SignupScreen
import com.example.dundun_hi.ui.signup.SignupViewModel
import com.example.dundun_hi.ui.theme.DundunHiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                1001
            )
        }

        setContent {
            DundunHiTheme {
                Surface(Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onLoginClick = { navController.navigate("login") },
                                onSignupClick = { navController.navigate("signup") },
                                onGuardianClick = { navController.navigate("guardian") }
                            )
                        }
                        composable("login") {
                            val loginVm: LoginViewModel = viewModel()
                            LoginScreen(
                                vm = loginVm,
                                onLoginSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("signup") {
                            val vm: SignupViewModel = viewModel()
                            val state by vm.state.collectAsState()
                            SignupScreen { req -> vm.signup(req) }
                            LaunchedEffect(state) {
                                when (state) {
                                    is SignupResult.Success -> navController.navigate("main") {
                                        popUpTo("signup") { inclusive = true }
                                    }
                                    is SignupResult.Error -> Toast.makeText(
                                        this@MainActivity,
                                        (state as SignupResult.Error).reason,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    else -> {}
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
                        composable("main") {
                            MainScreen(
                                userName = "길동님",
                                temperature = 19,
                                highTemp = 25,
                                lowTemp = 7,
                                onPhonePageClick = { navController.navigate("call") },
                                onMessagePageClick = { navController.navigate("profile") },
                                onCameraPageClick = { navController.navigate("camera") },
                                onMapPageClick = { navController.navigate("map") },
                                onFindCultureCenter = {},
                                onKioskPageClick = { navController.navigate("kiosk") },
                                onProfileClick = { navController.navigate("profile") }
                            )
                        }
                        composable("map") { MapScreen() }
                        composable("profile") { ProfileScreen(navController) }
                        composable("alarm") { AlarmRecordScreen(navController) }
                        composable("add_alarm") { AddAlarmScreen() }
                        composable("activity_history") { ActivityHistoryScreen() }
                        composable("camera") { CameraScreen(navController) }
                        composable("lastphoto") {
                            LastPhotoScreen(
                                photos = listOf(
                                    SharedPhoto(R.drawable.img1, fromMe = true),
                                    SharedPhoto(R.drawable.img2, fromMe = false),
                                    SharedPhoto(R.drawable.img3, fromMe = true)
                                ),
                                onAddPhoto = {}
                            )
                        }
                        composable("kiosk") { KioskScreen() }
                        composable("call") {
                            val viewModel: CallViewModel = viewModel()
                            val shortcuts by viewModel.shortcuts.collectAsState()
                            CallScreen(contacts = shortcuts) { idx ->
                                navController.navigate("call_setup/$idx")
                            }
                        }
                        composable("call_setup/{index}") { backStack ->
                            val viewModel: CallViewModel = viewModel()
                            val idx = backStack.arguments?.getString("index")?.toIntOrNull() ?: 0
                            SetupShortcutScreen(index = idx) {
                                viewModel.loadShortcuts()
                                navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}
