package io.qzz.lstudy.novelforge.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 主题色板公共接口
 * 所有色板 object 实现此接口，确保 Theme.kt 的 when 表达式能正确推断类型
 */
interface AppColors {
    val LightPrimary: Color
    val LightOnPrimary: Color
    val LightPrimaryContainer: Color
    val LightOnPrimaryContainer: Color
    val LightSecondary: Color
    val LightOnSecondary: Color
    val LightSecondaryContainer: Color
    val LightOnSecondaryContainer: Color
    val LightTertiary: Color
    val LightOnTertiary: Color
    val LightTertiaryContainer: Color
    val LightOnTertiaryContainer: Color
    val LightBackground: Color
    val LightOnBackground: Color
    val LightSurface: Color
    val LightOnSurface: Color
    val LightSurfaceVariant: Color
    val LightOnSurfaceVariant: Color

    val DarkPrimary: Color
    val DarkOnPrimary: Color
    val DarkPrimaryContainer: Color
    val DarkOnPrimaryContainer: Color
    val DarkSecondary: Color
    val DarkOnSecondary: Color
    val DarkSecondaryContainer: Color
    val DarkOnSecondaryContainer: Color
    val DarkTertiary: Color
    val DarkOnTertiary: Color
    val DarkTertiaryContainer: Color
    val DarkOnTertiaryContainer: Color
    val DarkBackground: Color
    val DarkOnBackground: Color
    val DarkSurface: Color
    val DarkOnSurface: Color
    val DarkSurfaceVariant: Color
    val DarkOnSurfaceVariant: Color
}

// ============================
//  紫色（默认）
// ============================
object PurpleColors : AppColors {
    override val LightPrimary = Color(0xFF6750A4)
    override val LightOnPrimary = Color(0xFFFFFFFF)
    override val LightPrimaryContainer = Color(0xFFEADDFF)
    override val LightOnPrimaryContainer = Color(0xFF21005D)
    override val LightSecondary = Color(0xFF625B71)
    override val LightOnSecondary = Color(0xFFFFFFFF)
    override val LightSecondaryContainer = Color(0xFFE8DEF8)
    override val LightOnSecondaryContainer = Color(0xFF1D192B)
    override val LightTertiary = Color(0xFF7D5260)
    override val LightOnTertiary = Color(0xFFFFFFFF)
    override val LightTertiaryContainer = Color(0xFFFFD8E4)
    override val LightOnTertiaryContainer = Color(0xFF31111D)
    override val LightBackground = Color(0xFFFFFBFE)
    override val LightOnBackground = Color(0xFF1C1B1F)
    override val LightSurface = Color(0xFFFFFBFE)
    override val LightOnSurface = Color(0xFF1C1B1F)
    override val LightSurfaceVariant = Color(0xFFE7E0EC)
    override val LightOnSurfaceVariant = Color(0xFF49454F)

    override val DarkPrimary = Color(0xFFD0BCFF)
    override val DarkOnPrimary = Color(0xFF381E72)
    override val DarkPrimaryContainer = Color(0xFF4F378B)
    override val DarkOnPrimaryContainer = Color(0xFFEADDFF)
    override val DarkSecondary = Color(0xFFCCC2DC)
    override val DarkOnSecondary = Color(0xFF332D41)
    override val DarkSecondaryContainer = Color(0xFF4A4458)
    override val DarkOnSecondaryContainer = Color(0xFFE8DEF8)
    override val DarkTertiary = Color(0xFFEFB8C8)
    override val DarkOnTertiary = Color(0xFF492532)
    override val DarkTertiaryContainer = Color(0xFF633B48)
    override val DarkOnTertiaryContainer = Color(0xFFFFD8E4)
    override val DarkBackground = Color(0xFF1C1B1F)
    override val DarkOnBackground = Color(0xFFE6E1E5)
    override val DarkSurface = Color(0xFF1C1B1F)
    override val DarkOnSurface = Color(0xFFE6E1E5)
    override val DarkSurfaceVariant = Color(0xFF49454F)
    override val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
}

// ============================
//  蓝色
// ============================
object BlueColors : AppColors {
    override val LightPrimary = Color(0xFF0061A4)
    override val LightOnPrimary = Color(0xFFFFFFFF)
    override val LightPrimaryContainer = Color(0xFFD1E4FF)
    override val LightOnPrimaryContainer = Color(0xFF001D36)
    override val LightSecondary = Color(0xFF535F70)
    override val LightOnSecondary = Color(0xFFFFFFFF)
    override val LightSecondaryContainer = Color(0xFFD7E3F7)
    override val LightOnSecondaryContainer = Color(0xFF101C2B)
    override val LightTertiary = Color(0xFF6B5778)
    override val LightOnTertiary = Color(0xFFFFFFFF)
    override val LightTertiaryContainer = Color(0xFFF2DAFF)
    override val LightOnTertiaryContainer = Color(0xFF251432)
    override val LightBackground = Color(0xFFFDFCFF)
    override val LightOnBackground = Color(0xFF191C20)
    override val LightSurface = Color(0xFFFDFCFF)
    override val LightOnSurface = Color(0xFF191C20)
    override val LightSurfaceVariant = Color(0xFFDFE2EB)
    override val LightOnSurfaceVariant = Color(0xFF43474E)

    override val DarkPrimary = Color(0xFF9ECAFF)
    override val DarkOnPrimary = Color(0xFF003258)
    override val DarkPrimaryContainer = Color(0xFF00497D)
    override val DarkOnPrimaryContainer = Color(0xFFD1E4FF)
    override val DarkSecondary = Color(0xFFBBC7DB)
    override val DarkOnSecondary = Color(0xFF253140)
    override val DarkSecondaryContainer = Color(0xFF3B4858)
    override val DarkOnSecondaryContainer = Color(0xFFD7E3F7)
    override val DarkTertiary = Color(0xFFD7BDE4)
    override val DarkOnTertiary = Color(0xFF3B2948)
    override val DarkTertiaryContainer = Color(0xFF523F5F)
    override val DarkOnTertiaryContainer = Color(0xFFF2DAFF)
    override val DarkBackground = Color(0xFF191C20)
    override val DarkOnBackground = Color(0xFFE2E2E6)
    override val DarkSurface = Color(0xFF191C20)
    override val DarkOnSurface = Color(0xFFE2E2E6)
    override val DarkSurfaceVariant = Color(0xFF43474E)
    override val DarkOnSurfaceVariant = Color(0xFFC3C7CF)
}

// ============================
//  绿色
// ============================
object GreenColors : AppColors {
    override val LightPrimary = Color(0xFF006B57)
    override val LightOnPrimary = Color(0xFFFFFFFF)
    override val LightPrimaryContainer = Color(0xFF7FF8D9)
    override val LightOnPrimaryContainer = Color(0xFF002118)
    override val LightSecondary = Color(0xFF4C635A)
    override val LightOnSecondary = Color(0xFFFFFFFF)
    override val LightSecondaryContainer = Color(0xFFCFE9DC)
    override val LightOnSecondaryContainer = Color(0xFF092018)
    override val LightTertiary = Color(0xFF3E6374)
    override val LightOnTertiary = Color(0xFFFFFFFF)
    override val LightTertiaryContainer = Color(0xFFC2E8FC)
    override val LightOnTertiaryContainer = Color(0xFF001F2A)
    override val LightBackground = Color(0xFFFBFDF9)
    override val LightOnBackground = Color(0xFF191C1A)
    override val LightSurface = Color(0xFFFBFDF9)
    override val LightOnSurface = Color(0xFF191C1A)
    override val LightSurfaceVariant = Color(0xFFDBE5DE)
    override val LightOnSurfaceVariant = Color(0xFF404944)

    override val DarkPrimary = Color(0xFF60DBBE)
    override val DarkOnPrimary = Color(0xFF00382C)
    override val DarkPrimaryContainer = Color(0xFF005141)
    override val DarkOnPrimaryContainer = Color(0xFF7FF8D9)
    override val DarkSecondary = Color(0xFFB3CCC1)
    override val DarkOnSecondary = Color(0xFF1F352D)
    override val DarkSecondaryContainer = Color(0xFF354B43)
    override val DarkOnSecondaryContainer = Color(0xFFCFE9DC)
    override val DarkTertiary = Color(0xFFA6CCE0)
    override val DarkOnTertiary = Color(0xFF093544)
    override val DarkTertiaryContainer = Color(0xFF254B5B)
    override val DarkOnTertiaryContainer = Color(0xFFC2E8FC)
    override val DarkBackground = Color(0xFF191C1A)
    override val DarkOnBackground = Color(0xFFE1E3DF)
    override val DarkSurface = Color(0xFF191C1A)
    override val DarkOnSurface = Color(0xFFE1E3DF)
    override val DarkSurfaceVariant = Color(0xFF404944)
    override val DarkOnSurfaceVariant = Color(0xFFBFC9C2)
}

// ============================
//  橙色（暖色调）
// ============================
object OrangeColors : AppColors {
    override val LightPrimary = Color(0xFF904A00)
    override val LightOnPrimary = Color(0xFFFFFFFF)
    override val LightPrimaryContainer = Color(0xFFFFDCC3)
    override val LightOnPrimaryContainer = Color(0xFF2E1400)
    override val LightSecondary = Color(0xFF735943)
    override val LightOnSecondary = Color(0xFFFFFFFF)
    override val LightSecondaryContainer = Color(0xFFFFDCC3)
    override val LightOnSecondaryContainer = Color(0xFF281805)
    override val LightTertiary = Color(0xFF5E6135)
    override val LightOnTertiary = Color(0xFFFFFFFF)
    override val LightTertiaryContainer = Color(0xFFE4E6AE)
    override val LightOnTertiaryContainer = Color(0xFF1B1D00)
    override val LightBackground = Color(0xFFFFFBFF)
    override val LightOnBackground = Color(0xFF1F1B16)
    override val LightSurface = Color(0xFFFFFBFF)
    override val LightOnSurface = Color(0xFF1F1B16)
    override val LightSurfaceVariant = Color(0xFFF3DFD1)
    override val LightOnSurfaceVariant = Color(0xFF52443A)

    override val DarkPrimary = Color(0xFFFFB77C)
    override val DarkOnPrimary = Color(0xFF4D2500)
    override val DarkPrimaryContainer = Color(0xFF6D3700)
    override val DarkOnPrimaryContainer = Color(0xFFFFDCC3)
    override val DarkSecondary = Color(0xFFE2BFA8)
    override val DarkOnSecondary = Color(0xFF412C19)
    override val DarkSecondaryContainer = Color(0xFF5A422E)
    override val DarkOnSecondaryContainer = Color(0xFFFFDCC3)
    override val DarkTertiary = Color(0xFFC8CA94)
    override val DarkOnTertiary = Color(0xFF30320B)
    override val DarkTertiaryContainer = Color(0xFF474920)
    override val DarkOnTertiaryContainer = Color(0xFFE4E6AE)
    override val DarkBackground = Color(0xFF1F1B16)
    override val DarkOnBackground = Color(0xFFEAE1D9)
    override val DarkSurface = Color(0xFF1F1B16)
    override val DarkOnSurface = Color(0xFFEAE1D9)
    override val DarkSurfaceVariant = Color(0xFF52443A)
    override val DarkOnSurfaceVariant = Color(0xFFD7C3B5)
}

// ============================
//  红色
// ============================
object RedColors : AppColors {
    override val LightPrimary = Color(0xFFBA1A1A)
    override val LightOnPrimary = Color(0xFFFFFFFF)
    override val LightPrimaryContainer = Color(0xFFFFDAD6)
    override val LightOnPrimaryContainer = Color(0xFF410002)
    override val LightSecondary = Color(0xFF775652)
    override val LightOnSecondary = Color(0xFFFFFFFF)
    override val LightSecondaryContainer = Color(0xFFFFDAD6)
    override val LightOnSecondaryContainer = Color(0xFF2C1512)
    override val LightTertiary = Color(0xFF725B2E)
    override val LightOnTertiary = Color(0xFFFFFFFF)
    override val LightTertiaryContainer = Color(0xFFFEDFA5)
    override val LightOnTertiaryContainer = Color(0xFF261900)
    override val LightBackground = Color(0xFFFFFBFF)
    override val LightOnBackground = Color(0xFF201A19)
    override val LightSurface = Color(0xFFFFFBFF)
    override val LightOnSurface = Color(0xFF201A19)
    override val LightSurfaceVariant = Color(0xFFF5DDDA)
    override val LightOnSurfaceVariant = Color(0xFF534341)

    override val DarkPrimary = Color(0xFFFFB4AB)
    override val DarkOnPrimary = Color(0xFF690005)
    override val DarkPrimaryContainer = Color(0xFF93000A)
    override val DarkOnPrimaryContainer = Color(0xFFFFDAD6)
    override val DarkSecondary = Color(0xFFE7BDB8)
    override val DarkOnSecondary = Color(0xFF442926)
    override val DarkSecondaryContainer = Color(0xFF5D3F3B)
    override val DarkOnSecondaryContainer = Color(0xFFFFDAD6)
    override val DarkTertiary = Color(0xFFE1C38C)
    override val DarkOnTertiary = Color(0xFF3F2D04)
    override val DarkTertiaryContainer = Color(0xFF584419)
    override val DarkOnTertiaryContainer = Color(0xFFFEDFA5)
    override val DarkBackground = Color(0xFF201A19)
    override val DarkOnBackground = Color(0xFFEDE0DD)
    override val DarkSurface = Color(0xFF201A19)
    override val DarkOnSurface = Color(0xFFEDE0DD)
    override val DarkSurfaceVariant = Color(0xFF534341)
    override val DarkOnSurfaceVariant = Color(0xFFD8C2BE)
}

// ============================
//  灰色（极简）
// ============================
object GrayColors : AppColors {
    override val LightPrimary = Color(0xFF5E5E5E)
    override val LightOnPrimary = Color(0xFFFFFFFF)
    override val LightPrimaryContainer = Color(0xFFE3E3E3)
    override val LightOnPrimaryContainer = Color(0xFF1B1B1B)
    override val LightSecondary = Color(0xFF5F5F5F)
    override val LightOnSecondary = Color(0xFFFFFFFF)
    override val LightSecondaryContainer = Color(0xFFE4E4E4)
    override val LightOnSecondaryContainer = Color(0xFF1C1C1C)
    override val LightTertiary = Color(0xFF6D6D6D)
    override val LightOnTertiary = Color(0xFFFFFFFF)
    override val LightTertiaryContainer = Color(0xFFF5F5F5)
    override val LightOnTertiaryContainer = Color(0xFF252525)
    override val LightBackground = Color(0xFFFCFCFC)
    override val LightOnBackground = Color(0xFF1B1B1B)
    override val LightSurface = Color(0xFFFCFCFC)
    override val LightOnSurface = Color(0xFF1B1B1B)
    override val LightSurfaceVariant = Color(0xFFE2E2E2)
    override val LightOnSurfaceVariant = Color(0xFF454545)

    override val DarkPrimary = Color(0xFFC7C7C7)
    override val DarkOnPrimary = Color(0xFF303030)
    override val DarkPrimaryContainer = Color(0xFF464646)
    override val DarkOnPrimaryContainer = Color(0xFFE3E3E3)
    override val DarkSecondary = Color(0xFFC8C8C8)
    override val DarkOnSecondary = Color(0xFF313131)
    override val DarkSecondaryContainer = Color(0xFF474747)
    override val DarkOnSecondaryContainer = Color(0xFFE4E4E4)
    override val DarkTertiary = Color(0xFFD9D9D9)
    override val DarkOnTertiary = Color(0xFF3B3B3B)
    override val DarkTertiaryContainer = Color(0xFF525252)
    override val DarkOnTertiaryContainer = Color(0xFFF5F5F5)
    override val DarkBackground = Color(0xFF1B1B1B)
    override val DarkOnBackground = Color(0xFFE4E4E4)
    override val DarkSurface = Color(0xFF1B1B1B)
    override val DarkOnSurface = Color(0xFFE4E4E4)
    override val DarkSurfaceVariant = Color(0xFF454545)
    override val DarkOnSurfaceVariant = Color(0xFFC6C6C6)
}

/** 通用 Error 色 */
object ThemeError {
    val Light = Color(0xFFBA1A1A)
    val LightOn = Color(0xFFFFFFFF)
    val LightContainer = Color(0xFFFFDAD6)
    val LightOnContainer = Color(0xFF410002)
    val Dark = Color(0xFFFFB4AB)
    val DarkOn = Color(0xFF690005)
    val DarkContainer = Color(0xFF93000A)
    val DarkOnContainer = Color(0xFFFFDAD6)
}

/** 主题元数据（用于 UI 选择器） */
data class ThemeInfo(
    val key: String,
    val displayName: String,
    val description: String
)

val ALL_THEMES = listOf(
    ThemeInfo("purple", "紫色", "经典 Material 色调，沉稳优雅"),
    ThemeInfo("blue", "蓝色", "专业冷静，适合长时间阅读"),
    ThemeInfo("green", "绿色", "护眼舒适，自然清新"),
    ThemeInfo("orange", "橙色", "暖色活力，激发创作灵感"),
    ThemeInfo("red", "红色", "热情奔放，视觉冲击力强"),
    ThemeInfo("gray", "灰色", "极简克制，专注内容本身")
)