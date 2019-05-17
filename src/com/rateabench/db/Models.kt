package com.rateabench.db

import com.rateabench.PostBench
import com.rateabench.db.framework.Column
import com.rateabench.db.framework.Entity
import com.rateabench.db.framework.Id
import com.rateabench.db.framework.Table

/**
 * Created by Jonathan Schurmann on 5/8/19.
 */
@Table("coordinates")
data class Coordinate(
    @Id
    val id: Long,
    @Column
    val lat: Double,
    @Column
    val lng: Double
) {
    companion object : Entity<Coordinate>(Coordinate::class) {
        override val TABLE = "coordinates"
        override val PK = "$TABLE.id"

        fun getByPK(id: Long): Coordinate? {
            return prepareStatement("SELECT $PK,$COLS FROM $TABLE WHERE $PK = ?") {
                setLong(1, id)
                execute(connection, this) {
                    getSingle(this)
                }

            }
        }
    }
}

@Table("users")
data class User(
    @Id
    val id: Long,
    @Column(
        "coordinate_id"
    ) val coordinateId: Long,
    @Column("lat", refTable = "coordinates")
    val lat: Double,
    @Column("lat", refTable = "coordinates")
    val lng: Double
) {
    companion object : Entity<User>(User::class) {
        override val TABLE = "users"
        override val PK = "$TABLE.id"

        fun getByPK(id: Long): User? {
            return prepareStatement("SELECT $PK,$COLS FROM $TABLE WHERE $PK = ?") {
                setLong(1, id)
                execute(connection, this) {
                    getSingle(this)
                }

            }
        }
    }
}

@Table("benches")
data class Bench(
    @Id
    val id: Long,
    @Column(
        "creator_id"
    ) val creatorId: Long,
    @Column(
        "coordinate_id"
    ) val coordinateId: Long,
    @Column("image_url")
    val imageUrl: String,
    @Column("lat", refTable = "coordinates")
    val lat: Double,
    @Column("lng", refTable = "coordinates")
    val lng: Double
) {
    companion object : Entity<Bench>(Bench::class) {
        override val TABLE: String
            get() = """
            benches left join coordinates on benches.coordinate_id = coordinates.id
            left join users on benches.creator_id = users.id
            """.trimIndent()
        override val PK = "\"benches\".\"id\""

        fun getByPK(id: Long): Bench? {
            return prepareStatement("SELECT $PK,$COLS FROM $TABLE WHERE $PK = ?") { connection ->
                setLong(1, id)
                execute(connection, this) {
                    if (next()) parse(this) else null
                }
            }
        }

        fun getAll(): List<Bench> {
            return executeSimple("SELECT $PK,$COLS FROM $TABLE") {
                getMultiple(executeQuery())
            }
        }

        fun insert(bench: PostBench): Boolean {
            val res = transaction {
                val stmt = prepareStatement(
                    """
                with ins1 as (
                    insert into ${Coordinate.getTable()} (lat, lng)
                        values (?, ?)
                        returning id as coordinate_id
                )
                insert
                into ${getTable()}(coordinate_id, creator_id)
                select coordinate_id, ?
                from ins1
                """.trimIndent()
                )
                stmt.setDouble(1, bench.lat)
                stmt.setDouble(2, bench.lng)
                stmt.setLong(3, bench.creatorId)
                stmt.executeUpdate()
            }
            return res == 1
        }
    }
}

@Table("rating_types")
data class RatingType(
    @Id
    val name: String,
    @Column
    val description: String,
    @Column
    val min: Int,
    @Column
    val max: Int
) {
    companion object : Entity<RatingType>(RatingType::class) {
        override val TABLE = "rating_types"
        override val PK = "\"$TABLE\".\"name\""

        fun getByPK(name: String): RatingType? {
            return prepareStatement("SELECT $PK,$COLS FROM $TABLE WHERE $PK = ?") { connection ->
                setString(1, name)
                execute(connection, this) {
                    if (next()) parse(this) else null
                }
            }
        }
    }
}

@Table("reviews")
data class Review(
    @Id
    val id: Long,
    @Column
    val text: String,
    @Column(
        "bench_id",
        refTable = "benches",
        refColumn = "id"
    ) val benchId: Long
) {
    companion object : Entity<Review>(Review::class) {
        override val TABLE = "reviews"
        override val PK = "\"$TABLE\".\"id\""

        fun getByPK(id: Long): Review? {
            return prepareStatement("SELECT $PK,$COLS FROM $TABLE WHERE $PK = ?") {
                setLong(1, id)
                execute(connection, this) {
                    if (next()) parse(this) else null
                }
            }
        }
    }
}

@Table("ratings")
data class Rating(
    @Id
    val id: Long,
    @Column(
        "bench_id",
        refTable = "benches",
        refColumn = "id"
    ) val bench: Bench,
    @Column(
        "rating_type_name",
        refTable = "rating_types",
        refColumn = "name"
    ) val ratingTypeName: String,
    @Column val value: Int
) {
    companion object : Entity<Rating>(Rating::class) {
        override val TABLE = "ratings"
        override val PK = "\"$TABLE\".\"id\""
        fun getByPK(id: Long): Rating? {
            return prepareStatement("SELECT $PK,$COLS FROM $TABLE WHERE $PK = ?") {
                setLong(1, id)
                execute(connection, this) {
                    if (next()) parse(this) else null
                }
            }
        }
    }
}

@Table("votes")
data class Vote(
    @Id
    val id: Long,
    @Column(
        "review_id",
        refTable = "reviews",
        refColumn = "id"
    ) val reviewId: Long,
    @Column(
        "user_id",
        refTable = "users",
        refColumn = "id"
    ) val userId: Long,
    @Column val value: Int
) {
    companion object : Entity<Vote>(Vote::class) {
        override val TABLE = "votes"
        override val PK = "\"$TABLE\".\"id\""
        fun getByPK(id: Long): Vote? {
            return prepareStatement("SELECT $PK,$COLS FROM $TABLE WHERE $PK = ?") {
                setLong(1, id)
                execute(connection, this) {
                    if (next()) parse(this) else null
                }
            }
        }
    }
}
