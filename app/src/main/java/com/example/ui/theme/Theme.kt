package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = CourtroomGold,
    secondary = RoyalBlue,
    tertiary = EmpathyGreen,
    background = MidnightSlate,
    surface = DeepCharcoal,
    onPrimary = MidnightSlate,
    onSecondary = PureIce,
    onTertiary = PureIce,
    onBackground = PureIce,
    onSurface = PureIce,
    error = AngerRed
)

private val LightColorScheme = lightColorScheme(
    primary = CourtroomGold,
    secondary = RoyalBlue,
    tertiary = EmpathyGreen,
    background = MidnightSlate, // Keep the dark, moody courtroom aesthetic even in "light" mode for premium brand consistency
    surface = DeepCharcoal,
    onPrimary = MidnightSlate,
    onSecondary = PureIce,
    onTertiary = PureIce,
    onBackground = PureIce,
    onSurface = PureIce,
    error = AngerRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable Android 12+ wallpaper dynamic colors to enforce the signature Courtroom Gold aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            try {
                var context = view.context
                var limit = 20
                while (context is android.content.ContextWrapper && context !is Activity && limit > 0) {
                    val nextContext = context.baseContext
                    if (nextContext == context || nextContext == null) {
                        break
                    }
                    context = nextContext
                    limit--
                }
                if (context is Activity) {
                    val window = context.window
                    if (window != null) {
                        window.statusBarColor = colorScheme.background.toArgb()
                        window.navigationBarColor = colorScheme.background.toArgb()
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Theme", "Failed to style status/navigation bars", e)
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
