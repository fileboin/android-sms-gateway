package com.smspeer.app.modules.logs.repositories

import androidx.lifecycle.distinctUntilChanged
import com.smspeer.app.modules.logs.db.LogEntriesDao

class LogsRepository(
    private val dao: LogEntriesDao
) {
    val lastEntries = dao.selectLast().distinctUntilChanged()
}