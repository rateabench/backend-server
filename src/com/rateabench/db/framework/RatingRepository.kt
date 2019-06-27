package com.rateabench.db.framework

import com.rateabench.db.Rating

/**
 * Created by Jonathan Schurmann on 5/22/19.
 */
class RatingRepository {
    companion object : Entity<Rating>(Rating::class) {
        override val TABLE = """
            ratings r join benches b on r.bench_id = b.id
            join coordinates c on b.coordinate_id = c.id
        """.trimIndent()

        fun getRatingsByBenchId(id: Long): List<Rating> {
            return prepareStatement("SELECT r.rating_type_name, avg(r.value) as value FROM $TABLE where b.id = ? group by (b.id, c.id, r.rating_type_name)") {
                setLong(1, id)
                executeQuery(connection, this) {
                    getMultiple(this)
                }
            }
        }
    }
}