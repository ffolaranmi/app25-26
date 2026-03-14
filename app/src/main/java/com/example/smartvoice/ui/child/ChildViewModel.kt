package com.example.smartvoice.ui.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.ChildTable
import com.example.smartvoice.data.SmartVoiceDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChildViewModel(private val db: SmartVoiceDatabase) : ViewModel() {

    private val _children = MutableStateFlow<List<ChildTable>>(emptyList())
    val children: StateFlow<List<ChildTable>> = _children

    private val _selectedChild = MutableStateFlow<ChildTable?>(null)
    val selectedChild: StateFlow<ChildTable?> = _selectedChild

    fun loadChildren(userId: Long) {
        viewModelScope.launch {
            _children.value = withContext(Dispatchers.IO) {
                db.childDao().getChildrenForUser(userId)
            }
        }
    }

    fun loadChildById(id: Long) {
        viewModelScope.launch {
            _selectedChild.value = withContext(Dispatchers.IO) {
                db.childDao().getChildById(id)
            }
        }
    }

    fun addChild(child: ChildTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.childDao().insertChild(child) }
            loadChildren(child.userId)
        }
    }

    fun updateChild(child: ChildTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.childDao().updateChild(child) }
            loadChildren(child.userId)
            _selectedChild.value = child
        }
    }

    fun deleteChild(child: ChildTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.childDao().deleteChild(child) }
            loadChildren(child.userId)
        }
    }

    suspend fun getRecordingCountForChild(childName: String, userId: Long): Int {
        return withContext(Dispatchers.IO) {
            db.diagnosisDao().getRecordingCountForChild(childName, userId)
        }
    }
}