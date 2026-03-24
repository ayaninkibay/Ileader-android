package com.ileader.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ileader.app.data.DeepLinkTarget
import com.ileader.app.ui.screens.auth.*
import com.ileader.app.ui.screens.main.MainScreen
import com.ileader.app.ui.theme.ILeaderColors

sealed class Screen(val route: String) {
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object ForgotPassword : Screen("forgot_password")
    data object Main : Screen("main")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    deepLinkTarget: DeepLinkTarget? = null,
    onDeepLinkConsumed: () -> Unit = {}
) {
    val authState by authViewModel.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Welcome.route,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                state = authState,
                onSignIn = { email, password ->
                    authViewModel.signIn(email, password)
                },
                onDemoLogin = { role ->
                    authViewModel.demoLogin(role)
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Screen.ForgotPassword.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClearError = { authViewModel.clearError() }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                state = authState,
                onSignUp = { data ->
                    authViewModel.signUp(data)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onClearError = { authViewModel.clearError() }
            )
        }

        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                state = authState,
                onResetPassword = { email ->
                    authViewModel.resetPassword(email)
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLogin = {
                    authViewModel.clearPasswordResetSent()
                    navController.popBackStack()
                },
                onClearError = { authViewModel.clearError() }
            )
        }

        composable(Screen.Main.route) {
            val user = authState.currentUser
            if (user != null) {
                MainScreen(
                    user = user,
                    onSignOut = {
                        authViewModel.signOut()
                    },
                    deepLinkTarget = deepLinkTarget,
                    onDeepLinkConsumed = onDeepLinkConsumed
                )
            } else {
                // Loading state while currentUser is being fetched
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ILeaderColors.PrimaryRed)
                }
            }
        }
    }

    // Navigate when auth state changes
    LaunchedEffect(authState.isAuthenticated) {
        if (authState.isAuthenticated && navController.currentDestination?.route != Screen.Main.route) {
            navController.navigate(Screen.Main.route) {
                popUpTo(0) { inclusive = true }
            }
        } else if (!authState.isAuthenticated && navController.currentDestination?.route == Screen.Main.route) {
            navController.navigate(Screen.Welcome.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
}
