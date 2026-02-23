package com.Placements.Ready.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.reflect.Type

/**
 * Repository responsible for loading placement data from assets.
 * Follows Clean Architecture and handles data fetching with Coroutines.
 */
class PlacementRepository(private val context: Context) {

    private val gson = Gson()
    // In-memory cache to store loaded companies for (Year + Month) key
    private val monthCache = mutableMapOf<String, List<Company>>()
    
    // Cache for total count to avoid re-calculating
    private var cachedTotalCount: Int? = null

    companion object {
        private const val TAG = "PlacementRepository"
        private const val ASSET_ROOT = "placement_data"
    }

    /**
     * Retrieves a list of available years from the assets folder.
     * @return Result containing list of year strings (e.g., ["2026", "2025"]) or error.
     */
    suspend fun getYears(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val list = context.assets.list(ASSET_ROOT)?.filter {
                it.matches(Regex("\\d{4}"))
            }?.sortedDescending() ?: emptyList()
            Result.success(list)
        } catch (e: IOException) {
            Log.e(TAG, "Error listing years", e)
            Result.failure(e)
        }
    }

    /**
     * Retrieves a list of months for a given year, sorted chronologically.
     */
    suspend fun getMonths(year: String): Result<List<String>> = withContext(Dispatchers.IO) {
        val monthOrder = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        try {
            val list = context.assets.list("$ASSET_ROOT/$year")?.sortedBy {
                val index = monthOrder.indexOf(it)
                if (index == -1) 99 else index
            } ?: emptyList()
            Result.success(list)
        } catch (e: IOException) {
            Log.e(TAG, "Error listing months for year $year", e)
            Result.failure(e)
        }
    }

    /**
     * Loads companies for a specific year and month.
     * Uses caching to prevent redundant disk I/O.
     */
    suspend fun getCompanies(year: String, month: String): Result<List<Company>> = withContext(Dispatchers.IO) {
        val cacheKey = "$year/$month"
        synchronized(monthCache) {
            if (monthCache.containsKey(cacheKey)) {
                return@withContext Result.success(monthCache[cacheKey]!!)
            }
        }

        try {
            val companies = mutableListOf<Company>()
            val startPath = "$ASSET_ROOT/$year/$month"
            recursiveLoad(startPath, companies)
            
            // Log if no companies found, helpful for debugging
            if (companies.isEmpty()) {
                Log.w(TAG, "No companies found for $year/$month")
            }

            // Deduplicate: Group by company name, keep latest by email date
            val uniqueCompanies = companies
                .groupBy { it.companyName.trim() }
                .map { (_, list) ->
                    list.maxByOrNull { it.emailDate ?: "" } ?: list.first()
                }
                .sortedBy { it.companyName }

            synchronized(monthCache) {
                monthCache[cacheKey] = uniqueCompanies
            }
            Result.success(uniqueCompanies)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading companies for $year/$month", e)
            Result.failure(e)
        }
    }

    /**
     * Recursively walks through directories to find and parse JSON files.
     */
    private fun recursiveLoad(path: String, resultList: MutableList<Company>) {
        try {
            val items = context.assets.list(path)
            if (items == null) {
                Log.w(TAG, "recursiveLoad: path '$path' returns null list")
                return
            }
            Log.d(TAG, "recursiveLoad: Found ${items.size} items in '$path'")
            for (item in items) {
                val fullPath = "$path/$item"
                if (item.endsWith(".json", ignoreCase = true)) {
                    processFile(fullPath, resultList)
                } else {
                    val subItems = context.assets.list(fullPath)
                    if (subItems != null && subItems.isNotEmpty()) {
                        recursiveLoad(fullPath, resultList)
                    } else if (subItems != null) {
                        // Empty directory
                        recursiveLoad(fullPath, resultList)
                    } else {
                         Log.d(TAG, "Skipping item '$fullPath' (not JSON, not dir)")
                    }
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Failed to list assets at $path: ${e.message}")
        }
    }

    private fun processFile(path: String, resultList: MutableList<Company>) {
        try {
            Log.d(TAG, "Processing file: $path")
            context.assets.open(path).bufferedReader().use { reader ->
                val json = reader.readText()
                val type = object : TypeToken<List<Company>>() {}.type
                val companies: List<Company> = gson.fromJson(json, type) ?: return

                // Determine category from path structure
                // Path format expected: .../tech_roles/... or .../non_tech_roles/...
                val category = when {
                    path.contains("non_tech", ignoreCase = true) -> "Non-Tech"
                    path.contains("tech", ignoreCase = true) -> "Tech"
                    else -> "General"
                }
                
                Log.d(TAG, "Loaded ${companies.size} companies from $path as $category")
                
                // Copy with assigned category
                resultList.addAll(companies.map { it.copy(roleCategory = category) })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing file $path", e)
        }
    }

    suspend fun getCompanyDetails(companyName: String, year: String?, month: String?): Company? {
        // If year/month provided, search there first (fast)
        if (year != null && month != null) {
            val result = getCompanies(year, month)
            result.getOrNull()?.find { it.companyName == companyName }?.let { return it }
        }
        
        // Fallback: This would require searching everywhere, which is expensive.
        // For now, return null or implement global search if strictly needed.
        return null
    }
    
    /**
     * Calculates total number of companies across all years/months.
     * This is an expensive operation, so it should be used sparingly or cached.
     */
    suspend fun getTotalCompanyCount(): Int = withContext(Dispatchers.IO) {
        cachedTotalCount?.let { return@withContext it }

        var count = 0
        getYears().getOrNull()?.forEach { year ->
            getMonths(year).getOrNull()?.forEach { month ->
                val companies = getCompanies(year, month).getOrDefault(emptyList())
                count += companies.size
            }
        }
        cachedTotalCount = count
        count
    }
}
