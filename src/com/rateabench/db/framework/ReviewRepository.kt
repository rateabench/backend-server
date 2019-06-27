package com.rateabench.db.framework

import com.rateabench.db.Review

/**
 * Created by Jonathan Schurmann on 5/22/19.
 */
class ReviewRepository {
    companion object : Entity<Review>(Review::class) {
        override val TABLE = """
            reviews r join benches b on r.bench_id = b.id
        """.trimIndent()

        fun getReviewsByBenchId(id: Long): List<Review> {
            return prepareStatement("SELECT r.id, r.text, r.created_at, r.updated_at from $TABLE where b.id = ?") {
                setLong(1, id)
                executeQuery(connection, this) {
                    getMultiple(this)
                }
            }
        }
    }

}