package io.qzz.lstudy.novelforge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.qzz.lstudy.novelforge.ui.home.CreateNovelDialog
import io.qzz.lstudy.novelforge.ui.home.HomeScreen
import io.qzz.lstudy.novelforge.ui.home.HomeViewModel
import io.qzz.lstudy.novelforge.ui.settings.SettingsScreen
import io.qzz.lstudy.novelforge.ui.settings.SettingsViewModel
import io.qzz.lstudy.novelforge.ui.theme.NovelForgeTheme
import kotlinx.coroutines.launch

/** 页面导航枚举 */
private enum class Screen { Home, Settings }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovelForgeTheme {
                NovelForgeApp(
                    homeViewModel = homeViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun NovelForgeApp(
    homeViewModel: HomeViewModel,
    settingsViewModel: SettingsViewModel
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // 收集 Flow 状态
    val novels by homeViewModel.novels.collectAsStateWithLifecycle()
    val skills by homeViewModel.skills.collectAsStateWithLifecycle()
    val apiKeys by settingsViewModel.apiKeys.collectAsStateWithLifecycle()
    val exportMode by settingsViewModel.exportMode.collectAsStateWithLifecycle()

    when (currentScreen) {
        Screen.Home -> HomeScreen(
            novels = novels,
            onNovelClick = { /* TODO: 阶段三实现创作页 */ },
            onDeleteNovel = { homeViewModel.deleteNovel(it) },
            onAddClick = { showCreateDialog = true },
            onSettingsClick = { currentScreen = Screen.Settings }
        )
        Screen.Settings -> SettingsScreen(
            apiKeys = apiKeys,
            exportMode = exportMode,
            onBack = { currentScreen = Screen.Home },
            onSetApiKey = { provider, key -> settingsViewModel.setApiKey(provider, key) },
            onSetExportMode = { settingsViewModel.setExportMode(it) }
        )
    }

    // 新建小说对话框
    if (showCreateDialog) {
        CreateNovelDialog(
            skills = skills,
            onDismiss = { showCreateDialog = false },
            onCreate = { title, targetWords, _ ->
                scope.launch {
                    homeViewModel.createNovel(title, targetWords)
                    showCreateDialog = false
                }
            }
        )
    }
}