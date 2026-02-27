package com.example.smartvoice.ui.child

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartvoice.data.ChildTable
import com.example.smartvoice.data.SmartVoiceDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.smartvoice.ui.child.ChildViewModelFactory

class ChildViewModel(private val db: SmartVoiceDatabase) : ViewModel() {

    private val _children = MutableStateFlow<List<ChildTable>>(emptyList())
    val children: StateFlow<List<ChildTable>> = _children

    private val _selectedChild = MutableStateFlow<ChildTable?>(null)
    val selectedChild: StateFlow<ChildTable?> = _selectedChild

    fun loadChildren() {
        viewModelScope.launch {
            _children.value = withContext(Dispatchers.IO) { db.childDao().getAllChildren() }
        }
    }

    fun loadChildById(id: Long) {
        viewModelScope.launch {
            _selectedChild.value = withContext(Dispatchers.IO) { db.childDao().getChildById(id) }
        }
    }

    fun deleteChild(child: ChildTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.childDao().deleteChild(child) }
            loadChildren()
        }
    }

    fun addChild(child: ChildTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.childDao().insertChild(child) }
            loadChildren()
        }
    }

    fun updateChild(child: ChildTable) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { db.childDao().updateChild(child) }
            loadChildren()
            _selectedChild.value = child
        }
    }
}