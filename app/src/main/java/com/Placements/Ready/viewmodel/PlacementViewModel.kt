package com.Placements.Ready.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.Placements.Ready.data.Company
import com.Placements.Ready.data.PlacementRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlacementViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PlacementRepository(application)

    private val _years = MutableStateFlow<List<String>>(emptyList())
    val years: StateFlow<List<String>> = _years.asStateFlow()

    private val _selectedYear = MutableStateFlow("")
    val selectedYear: StateFlow<String> = _selectedYear.asStateFlow()

    private val _months = MutableStateFlow<List<String>>(emptyList())
    val months: StateFlow<List<String>> = _months.asStateFlow()

    private val _selectedMonth = MutableStateFlow("")
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _companies = MutableStateFlow<List<Company>>(emptyList())
    val companies: StateFlow<List<Company>> = _companies.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filteredCompanies = MutableStateFlow<List<Company>>(emptyList())
    val filteredCompanies: StateFlow<List<Company>> = _filteredCompanies.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _totalCompanies = MutableStateFlow(0)
    val totalCompanies: StateFlow<Int> = _totalCompanies.asStateFlow()

    private val _roleFilter = MutableStateFlow("All")
    val roleFilter: StateFlow<String> = _roleFilter.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Fetch Years
            repository.getYears().onSuccess { yearsList ->
                _years.value = yearsList
                if (yearsList.isNotEmpty()) {
                    selectYear(yearsList.first())
                }
            }.onFailure {
                Log.e("PlacementViewModel", "Failed to fetch years", it)
            }
            
            // Fetch Total Count in background
            launch(Dispatchers.IO) {
                val total = repository.getTotalCompanyCount()
                _totalCompanies.value = total
            }
            
            _isLoading.value = false
        }
    }

    fun selectYear(year: String) {
        _selectedYear.value = year
        viewModelScope.launch {
            repository.getMonths(year).onSuccess { monthsList ->
                _months.value = monthsList
                if (monthsList.isNotEmpty()) {
                    selectMonth(monthsList.first())
                } else {
                    _selectedMonth.value = ""
                    _companies.value = emptyList()
                    applySearch()
                }
            }.onFailure {
                Log.e("PlacementViewModel", "Failed to fetch months for $year", it)
                _months.value = emptyList()
            }
        }
    }

    fun selectMonth(month: String) {
        _selectedMonth.value = month
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCompanies(_selectedYear.value, month).onSuccess { companiesList ->
                _companies.value = companiesList
                applySearch()
            }.onFailure {
                Log.e("PlacementViewModel", "Failed to fetch companies for $month", it)
                _companies.value = emptyList()
                applySearch()
            }
            _isLoading.value = false
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
             applySearch()
        }
    }

    fun setRoleFilter(filter: String) {
        _roleFilter.value = filter
        applySearch()
    }

    private fun applySearch() {
        val query = _searchQuery.value.lowercase()
        val role = _roleFilter.value

        val filteredByRole = if (role == "All") {
            _companies.value
        } else {
            _companies.value.filter { it.roleCategory == role }
        }

        _filteredCompanies.value = if (query.isBlank()) {
            filteredByRole
        } else {
            filteredByRole.filter {
                it.companyName.lowercase().contains(query) ||
                        (it.driveType?.lowercase()?.contains(query) == true) ||
                        (it.rolesOffered?.any { r -> r.lowercase().contains(query) } == true)
            }
        }
    }

    fun getCompanyDetails(companyName: String): List<Company> {
        // Since this is called from UI synchronously in the current implementation, 
        // we use the current loaded state. 
        // NOTE: Ideally details screen should fetch its own data via ID/Name in ViewModel.
        return _companies.value.filter { it.companyName == companyName }
    }
}
