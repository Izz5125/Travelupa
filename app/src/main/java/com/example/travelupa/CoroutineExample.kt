package com.example.travelupa

import kotlinx.coroutines.*

/**
 * Contoh penggunaan Coroutines seperti di modul
 * SESUAI MODUL HAL 35: "Kotlin coroutines sendiri sudah diterapkan pada bab sebelumnya"
 */
object CoroutineExample {

    /**
     * Contoh fungsi dengan coroutines seperti di modul
     * SESUAI MODUL HAL 35
     */
    suspend fun contohCoroutineModul() {
        // Pattern yang sama dengan modul
        val result = withContext(Dispatchers.IO) {
            // Simulasi operasi network/database
            delay(1000)
            "Data loaded"
        }
        println(result)
    }

    /**
     * Contoh lain untuk upload data
     */
    suspend fun uploadDataWithCoroutine(data: String): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                // Simulasi upload
                delay(2000)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Multiple coroutines seperti dijelaskan di modul
     */
    suspend fun multipleCoroutinesExample() {
        coroutineScope {
            val job1 = launch {
                delay(1000)
                println("Job 1 completed")
            }

            val job2 = async {
                delay(500)
                "Result from job 2"
            }

            job1.join()
            val result = job2.await()
            println("Both jobs completed: $result")
        }
    }
}