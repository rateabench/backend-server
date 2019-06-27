package com.rateabench.db.framework

import com.rateabench.PostBench
import com.rateabench.db.Bench

/**
 * Created by Jonathan Schurmann on 5/22/19.
 */
class BenchRepository {
    companion object : Entity<Bench>(Bench::class) {
        override val TABLE: String
            get() = """
            benches left join coordinates on benches.coordinate_id = coordinates.id
            left join users on benches.creator_id = users.id
            """.trimIndent()
        private const val PK = "\"benches\".\"id\""

        fun getByPK(id: Long): Bench? {
            val bench = prepareStatement("SELECT $PK,$COLS FROM $TABLE WHERE $PK = ?") { connection ->
                setLong(1, id)
                executeQuery(connection, this, ::getSingle)
            }
            bench?.let {
                it.ratings = RatingRepository.getRatingsByBenchId(it.id)
            }
            return bench
        }

        fun getAll(): List<Bench> {
            val benches = executeSimple("SELECT $PK,$COLS FROM $TABLE ORDER BY $PK", ::getMultiple)
            benches.map { bench ->
                bench.ratings = RatingRepository.getRatingsByBenchId(bench.id)
            }
            return benches
        }

        fun updateImageURL(id: Long, url: String): Boolean {
            val res = transaction {
                prepareStatement("UPDATE ${getTable()} SET image_url = ? WHERE $PK = ?") { conn ->
                    setString(1, url)
                    setLong(2, id)
                    executeUpdate(conn, this)
                }
            }
            return res == 1
        }

        fun insert(bench: PostBench): Boolean {
            val res = transaction {
                prepareStatement(
                    """
                with ins1 as (
                    insert into coordinates(lat, lng)
                        values (?, ?)
                        returning id as coordinate_id
                )
                insert
                into ${getTable()}(coordinate_id, creator_id)
                select coordinate_id, ?
                from ins1
                """.trimIndent()
                ) { conn ->
                    setDouble(1, bench.lat)
                    setDouble(2, bench.lng)
                    setLong(3, bench.creatorId)
                    executeUpdate(conn, this)
                }
            }
            return res == 1
        }
    }
}