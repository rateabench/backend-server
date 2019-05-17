package com.rateabench

import com.rateabench.db.DataSource
import kotlin.test.Test
import kotlin.test.assertTrue


/**
 * Created by Jonathan Schurmann on 5/8/19.
 */
class DatabaseTest {
    @Test
    fun testConnection() {
        val conn = DataSource.getConnection()
        assertTrue(conn.isValid(3))
    }
}