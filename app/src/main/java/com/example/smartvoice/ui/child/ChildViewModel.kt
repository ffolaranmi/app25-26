package com.example.smartvoice.ui.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.ChildTable
import com.example.smartvoice.data.SmartVoiceDatabase
import com.example.smartvoice.data.supabase.SupabaseChildRemoteRepository
import com.example.smartvoice.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChildViewModel(private val db: SmartVoiceDatabase) : ViewModel() {

    private val remoteRepo = SupabaseChildRemoteRepository()

    private val _children = MutableStateFlow<List<ChildTable>>(emptyList())
    val children: StateFlow<List<ChildTable>> = _children

    private val _selectedChild = MutableStateFlow<ChildTable?>(null)
    val selectedChild: StateFlow<ChildTable?> = _selectedChild

    fun loadChildren(userId: Long) {
        viewModelScope.launch {
            reloadChildrenFromSupabase()
        }
    }

    fun loadChildById(id: Long) {
        viewModelScope.launch {
            if (_children.value.isEmpty()) {
                reloadChildrenFromSupabase()
            }
            _selectedChild.value = _children.value.firstOrNull { it.id == id }
        }
    }

    fun addChild(child: ChildTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val supabaseUserId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id?.toString()
                if (supabaseUserId != null) {
                    remoteRepo.syncChildInsert(child, supabaseUserId)
                }
            }
            reloadChildrenFromSupabase()
        }
    }

    fun updateChild(child: ChildTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val supabaseUserId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id?.toString()
                if (supabaseUserId != null) {
                    remoteRepo.syncChildUpdate(child, supabaseUserId)
                }
            }
            reloadChildrenFromSupabase()
            _selectedChild.value = child
        }
    }

    fun deleteChild(child: ChildTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                remoteRepo.syncChildDelete(child.id)
            }
            reloadChildrenFromSupabase()
        }
    }

    private suspend fun reloadChildrenFromSupabase() {
        val supabaseUserId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id?.toString()
        if (supabaseUserId == null) {
            _children.value = emptyList()
            _selectedChild.value = null
            return
        }

        val remoteChildren = withContext(Dispatchers.IO) {
            remoteRepo.fetchChildrenForUser(supabaseUserId)
        }

        _children.value = remoteChildren.map { row ->
            ChildTable(
                id = row.id ?: 0L,
                userId = 0L,
                firstName = row.firstName,
                lastName = row.lastName,
                gender = row.gender,
                birthMonth = row.birthMonth,
                birthYear = row.birthYear,
                hospitalId = row.hospitalId ?: ""
            )
        }
    }

    suspend fun getRecordingCountForChild(childName: String): Int {
        // Recording count is now derived from remote diagnoses; this helper
        // should be refactored in a later batch. For now, return 0 to avoid
        // relying on the local Room diagnosis table.
        return 0
    }
}