package io.qzz.lstudy.novelforge.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * 根据主题名称构建对应的 ColorScheme
 * 支持 6 套预设主题：purple / blue / green / orange / red / gray
 */
private fun createColorScheme(themeName: String, darkTheme: Boolean): ColorScheme {
    val colors: AppColors = when (themeName) {
        "blue" -> BlueColors
        "green" -> GreenColors
        "orange" -> OrangeColors
        "red" -> RedColors
        "gray" -> GrayColors
        else -> PurpleColors
    }
    val error = ThemeError
    return if (darkTheme) {
        darkColorScheme(
            primary = colors.DarkPrimary,
            onPrimary = colors.DarkOnPrimary,
            primaryContainer = colors.DarkPrimaryContainer,
            onPrimaryContainer = colors.DarkOnPrimaryContainer,
            secondary = colors.DarkSecondary,
            onSecondary = colors.DarkOnSecondary,
            secondaryContainer = colors.DarkSecondaryContainer,
            onSecondaryContainer = colors.DarkOnSecondaryContainer,
            tertiary = colors.DarkTertiary,
            onTertiary = colors.DarkOnTertiary,
            tertiaryContainer = colors.DarkTertiaryContainer,
            onTertiaryContainer = colors.DarkOnTertiaryContainer,
            background = colors.DarkBackground,
            onBackground = colors.DarkOnBackground,
            surface = colors.DarkSurface,
            onSurface = colors.DarkOnSurface,
            surfaceVariant = colors.DarkSurfaceVariant,
            onSurfaceVariant = colors.DarkOnSurfaceVariant,
            error = error.Dark,
            onError = error.DarkOn,
            errorContainer = error.DarkContainer,
            onErrorContainer = error.DarkOnContainer
        )
    } else {
        lightColorScheme(
            primary = colors.LightPrimary,
            onPrimary = colors.LightOnPrimary,
            primaryContainer = colors.LightPrimaryContainer,
            onPrimaryContainer = colors.LightOnPrimaryContainer,
            secondary = colors.LightSecondary,
            onSecondary = colors.LightOnSecondary,
            secondaryContainer = colors.LightSecondaryContainer,
            onSecondaryContainer = colors.LightOnSecondaryContainer,
            tertiary = colors.LightTertiary,
            onTertiary = colors.LightOnTertiary,
            tertiaryContainer = colors.LightTertiaryContainer,
            onTertiaryContainer = colors.LightOnTertiaryContainer,
            background = colors.LightBackground,
            onBackground = colors.LightOnBackground,
            surface = colors.LightSurface,
            onSurface = colors.LightOnSurface,
            surfaceVariant = colors.LightSurfaceVariant,
            onSurfaceVariant = colors.LightOnSurfaceVariant,
            error = error.Light,
            onError = error.LightOn,
            errorContainer = error.LightContainer,
            onErrorContainer = error.LightOnContainer
        )
    }
}

/**
 * 应用主题
 *
 * @param themeName 主题名称（purple / blue / green / orange / red / gray），默认 purple
 * @param darkTheme 是否使用深色主题，默认跟随系统
 * @param dynamicColor 是否启用 Android 12+ 动态取色（Material You），优先级最高
 */
@Composable
fun NovelForgeTheme(
    themeName: String = "purple",
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> createColorScheme(themeName, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}