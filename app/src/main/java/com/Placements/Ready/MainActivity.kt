package com.Placements.Ready

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.Placements.Ready.ui.theme.CampusPlacementTheme
import com.Placements.Ready.ui.theme.LocalThemePreferences
import com.Placements.Ready.ui.theme.ThemePreferences
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.PathInterpolator
import androidx.core.animation.doOnEnd
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.Placements.Ready.ui.CampusPlacementApp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : ComponentActivity() {
    
    private var isAppReady = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        // Hold the splash screen until essential initializations (like session check) are done
        splashScreen.setKeepOnScreenCondition { !isAppReady }
        
        // Simulate a tiny, non-blocking background initialization operation
        lifecycleScope.launch {
            delay(1400) // 1.4s delay + 0.6s animation = 2.0s total duration
            isAppReady = true
        }

        // Add an AWESOME, premium cinematic exit animation
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashScreenView = splashScreenViewProvider.view
            val iconView = splashScreenViewProvider.iconView

            // 1. Icon Animation: Scale UP, rotate, and shoot upwards!
            val iconScaleX = ObjectAnimator.ofFloat(iconView, View.SCALE_X, 1f, 1.5f, 0f)
            val iconScaleY = ObjectAnimator.ofFloat(iconView, View.SCALE_Y, 1f, 1.5f, 0f)
            val iconRotate = ObjectAnimator.ofFloat(iconView, View.ROTATION, 0f, -15f, 90f)
            val iconTranslateY = ObjectAnimator.ofFloat(iconView, View.TRANSLATION_Y, 0f, -600f)
            
            val iconAnimators = listOf(iconScaleX, iconScaleY, iconRotate, iconTranslateY)
            iconAnimators.forEach {
                it.duration = 600L
                it.interpolator = PathInterpolator(0.2f, 0f, 0f, 1f) // Cinematic Ease Out
            }

            // 2. Background Animation: Fade out smoothly revealing the app
            val bgAlpha = ObjectAnimator.ofFloat(splashScreenView, View.ALPHA, 1f, 0f)
            bgAlpha.duration = 400L
            bgAlpha.startDelay = 200L // Wait for icon to start its cool move
            bgAlpha.interpolator = PathInterpolator(0.4f, 0f, 0.2f, 1f)

            // 3. Branding Image Animation (if present on Android 12+)
            val animators = mutableListOf<android.animation.Animator>()
            animators.addAll(iconAnimators)
            animators.add(bgAlpha)

            val animatorSet = AnimatorSet()
            animatorSet.playTogether(animators)
            animatorSet.doOnEnd { splashScreenViewProvider.remove() }
            animatorSet.start()
        }
        
        enableEdgeToEdge()
        setContent {
            val themePreferences = remember { ThemePreferences(this@MainActivity) }
            val isDark = themePreferences.isDarkMode.value

            CompositionLocalProvider(LocalThemePreferences provides themePreferences) {
                CampusPlacementTheme(darkTheme = isDark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CampusPlacementApp()
                    }
                }
            }
        }
    }
}
