package com.androidauth.app.ui

enum class AppLanguage(val displayName: String, val code: String) {
    RU("Русский", "ru"),
    EN("English", "en")
}

class Strings(val lang: AppLanguage) {

    val appName: String get() = "AndroidAuth"

    fun sessionsCount(count: Int): String = when (lang) {
        AppLanguage.RU -> when (count) {
            0 -> "0 сессий"
            1 -> "1 сессия"
            in 2..4 -> "$count сессии"
            else -> "$count сессий"
        }
        AppLanguage.EN -> when (count) {
            0 -> "0 sessions"
            1 -> "1 session"
            else -> "$count sessions"
        }
    }

    val syncTimeStart: String get() = if (lang == AppLanguage.RU) "Синхронизация времени Steam..." else "Syncing Steam server time..."
    fun syncTimeDone(offset: Long): String = if (lang == AppLanguage.RU) "Время синхронизировано (смещение: ${offset}s)" else "Time synced (offset: ${offset}s)"

    val countdownLabel: String get() = if (lang == AppLanguage.RU) "Смена кода 2FA" else "2FA Code Refresh"
    val searchPlaceholder: String get() = if (lang == AppLanguage.RU) "Поиск по логину, почте, SteamID..." else "Search by login, email, SteamID..."

    val allAccounts: String get() = if (lang == AppLanguage.RU) "Все аккаунты" else "All accounts"
    val favorites: String get() = if (lang == AppLanguage.RU) "Избранные" else "Favorites"

    val noSessionsTitle: String get() = if (lang == AppLanguage.RU) "Нет добавленных сессий" else "No active sessions"
    val noSessionsDesc: String get() = if (lang == AppLanguage.RU) "Загрузите .maFile из Files by Google или создайте тестовую сессию" else "Import .maFile from Files by Google or generate a test session"
    val addMaFileBtn: String get() = if (lang == AppLanguage.RU) "Добавить maFile" else "Add maFile"

    val notFound: String get() = if (lang == AppLanguage.RU) "Ничего не найдено по запросу" else "Nothing found for query"

    // Account Card
    val codeHeader: String get() = if (lang == AppLanguage.RU) "Код Steam Guard 2FA" else "Steam Guard 2FA Code"
    val activeSession: String get() = if (lang == AppLanguage.RU) "Активен" else "Active"
    val detailsBtn: String get() = if (lang == AppLanguage.RU) "Подробнее" else "Details"
    val loginLabel: String get() = if (lang == AppLanguage.RU) "Логин:" else "Login:"
    val passwordLabel: String get() = if (lang == AppLanguage.RU) "Пароль:" else "Password:"
    val emailLabel: String get() = if (lang == AppLanguage.RU) "Почта:" else "Email:"

    fun copiedToast(label: String, text: String): String = if (lang == AppLanguage.RU) "$label скопирован: $text" else "$label copied: $text"

    // Details Sheet
    val detailsSheetTitle: String get() = if (lang == AppLanguage.RU) "Параметры сессии" else "Session Parameters"
    val sessionNameLabel: String get() = if (lang == AppLanguage.RU) "Название сессии" else "Session Name"
    val sessionNamePlaceholder: String get() = if (lang == AppLanguage.RU) "Введите название..." else "Enter name..."
    val steamLoginFullLabel: String get() = if (lang == AppLanguage.RU) "Логин аккаунта Steam" else "Steam Account Login"
    val rcodeLabel: String get() = if (lang == AppLanguage.RU) "R-Code (Код восстановления)" else "R-Code (Revocation Code)"
    val notSpecified: String get() = if (lang == AppLanguage.RU) "Не указан" else "Not specified"
    val saveNameBtn: String get() = if (lang == AppLanguage.RU) "Сохранить название" else "Save Name"
    val exportMaFileBtn: String get() = if (lang == AppLanguage.RU) "Экспортировать .maFile" else "Export .maFile"
    val deleteAccountBtn: String get() = if (lang == AppLanguage.RU) "Удалить аккаунт" else "Delete Account"
    val changesSaved: String get() = if (lang == AppLanguage.RU) "Изменения сохранены" else "Changes saved"
    val accountDeleted: String get() = if (lang == AppLanguage.RU) "Аккаунт удален" else "Account deleted"

    // Add Sheet
    val addSheetTitle: String get() = if (lang == AppLanguage.RU) "Добавить сессию" else "Add Session"
    val addSheetSubtitle: String get() = if (lang == AppLanguage.RU) "Импорт .maFile или ручной ввод" else "Import .maFile or manual entry"
    val selectMaFilesBtn: String get() = if (lang == AppLanguage.RU) "Выбрать файлы .maFile" else "Select .maFile files"
    val selectMaFilesDesc: String get() = if (lang == AppLanguage.RU) "Files by Google / Проводник (поддерживает мультивыбор)" else "Files by Google / Explorer (multi-select supported)"
    val pasteTextBtn: String get() = if (lang == AppLanguage.RU) "Вставить текст" else "Paste Text"
    val demoAccountBtn: String get() = if (lang == AppLanguage.RU) "Демо-аккаунт" else "Demo Account"
    val clipboardEmpty: String get() = if (lang == AppLanguage.RU) "Буфер обмена пуст" else "Clipboard is empty"
    val maFileContentLabel: String get() = if (lang == AppLanguage.RU) "Содержимое .maFile (JSON)" else "maFile Content (JSON)"
    val optionalFieldsToggle: String get() = if (lang == AppLanguage.RU) "Дополнительные данные (пароли, почта)" else "Additional Credentials (passwords, email)"
    val emailPasswordLabel: String get() = if (lang == AppLanguage.RU) "Пароль от почты" else "Email Password"
    val addSessionConfirmBtn: String get() = if (lang == AppLanguage.RU) "Добавить сессию" else "Add Session"
    val promptPasteOrSelect: String get() = if (lang == AppLanguage.RU) "Пожалуйста, вставьте текст .maFile или выберите файл" else "Please paste .maFile content or select file"
    fun sessionSaved(name: String): String = if (lang == AppLanguage.RU) "Сессия $name сохранена!" else "Session $name saved!"
    fun demoCreated(name: String): String = if (lang == AppLanguage.RU) "Демо-аккаунт $name создан!" else "Demo account $name created!"

    // Settings Sheet
    val settingsTitle: String get() = if (lang == AppLanguage.RU) "Настройки" else "Settings"
    val accentThemeTitle: String get() = if (lang == AppLanguage.RU) "Тема акцента" else "Accent Theme"
    val displayModeTitle: String get() = if (lang == AppLanguage.RU) "Режим отображения" else "Display Mode"
    val themeSystem: String get() = if (lang == AppLanguage.RU) "Системный" else "System"
    val themeLight: String get() = if (lang == AppLanguage.RU) "Светлый" else "Light"
    val themeDark: String get() = if (lang == AppLanguage.RU) "Темный" else "Dark"
    val languageTitle: String get() = if (lang == AppLanguage.RU) "Язык приложения" else "App Language"
    val autoCopyTitle: String get() = if (lang == AppLanguage.RU) "Копирование в 1 тап" else "1-Tap Copy"
    val autoCopyDesc: String get() = if (lang == AppLanguage.RU) "Быстрое копирование кода при нажатии на плашку" else "Quick code copy when tapping the box"
    val syncSteamServerTimeBtn: String get() = if (lang == AppLanguage.RU) "Синхронизировать время Steam API" else "Sync Steam API Server Time"

    // Backup Sheet
    val backupTitle: String get() = if (lang == AppLanguage.RU) "Резервное копирование" else "Backup & Restore"
    fun exportBackupBtn(count: Int): String = if (lang == AppLanguage.RU) "Экспортировать бэкап ($count сессий)" else "Export backup ($count sessions)"
    val restoreBackupBtn: String get() = if (lang == AppLanguage.RU) "Восстановить из файла бэкапа" else "Restore from backup file"
    val clearAllSessionsBtn: String get() = if (lang == AppLanguage.RU) "Очистить все сессии" else "Clear all sessions"
    val deleteConfirmTitle: String get() = if (lang == AppLanguage.RU) "Удалить все сессии?" else "Delete all sessions?"
    fun deleteConfirmDesc(count: Int): String = if (lang == AppLanguage.RU) "Это действие удалит все $count сохраненных аккаунтов из AndroidAuth без возможности восстановления." else "This action will permanently delete all $count saved accounts from AndroidAuth."
    val deleteAllConfirmBtn: String get() = if (lang == AppLanguage.RU) "Удалить всё" else "Delete All"
    val cancelBtn: String get() = if (lang == AppLanguage.RU) "Отмена" else "Cancel"
    val allSessionsCleared: String get() = if (lang == AppLanguage.RU) "Все сессии удалены" else "All sessions cleared"
    val closeBtn: String get() = if (lang == AppLanguage.RU) "Закрыть" else "Close"
    val copyBtn: String get() = if (lang == AppLanguage.RU) "Скопировать" else "Copy"
}
