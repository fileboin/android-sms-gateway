package com.smspeer.app.modules.logs.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.smspeer.app.modules.logs.db.LogEntry
import com.smspeer.app.modules.logs.repositories.LogsRepository

class LogsViewModel(
    logs: LogsRepository
) : ViewModel() {
    val lastEntries: LiveData<List<LogEntry>> = logs.lastEntries
}