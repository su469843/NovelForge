# NovelForge 更新日志

## v1.0.0 - 2026-07-13

### 新增
- 数据层：Room 三表（Novel/Chapter/Skill）+ DAO + AppDatabase
- DataStore 存储各 AI 模型 API Key 及用户偏好设置
- NovelRepository / SettingRepository 封装数据操作
- Hilt 依赖注入 + Application 初始化内置 Skill 模板
- 内置 6 个创作风格 Skill（悬疑、言情、玄幻、历史、科幻、通用小说创作）
- Compose Material3 主题与国际化 strings.xml
- 主页：小说卡片列表 + 空状态 + 创建小说对话框
- 设置页：13 个 AI 供应商 API Key 输入（密码遮罩）+ 导出模式选择
- 全局异常捕获：崩溃时弹窗展示错误详情，支持复制到剪贴板
- GitHub Actions CI：自动构建签名 Release APK
- 打 tag 时自动发布 GitHub Release 并上传 APK

### 修复
- 修复 gradlew 无执行权限导致 CI 构建失败
- 修复 Android SDK 组件安装失败（改用 runner 预装 SDK）
- 修复「无法创建小说」问题（实现 CreateNovelDialog）
- 修复「Hello Android」占位显示（接入实际 UI）