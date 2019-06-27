package com.rateabench.db

import com.rateabench.db.framework.Column
import com.rateabench.db.framework.Id
import com.rateabench.db.framework.Table
import java.sql.Timestamp

/**
 * Created by Jonathan Schurmann on 5/8/19.
 */

open class TimeStamps {
    @Column("created_at")
    lateinit var createdAt: Timestamp
    @Column("updated_at")
    lateinit var updatedAt: Timestamp
}


@Table("coordinates")
data class Coordinate(
    @Id
    val id: Long,
    @Column
    val lat: Double,
    @Column
    val lng: Double
)

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
)

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
    lateinit var ratings: List<Rating>
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
)

@Table("reviews")
data class Review(
    @Id
    val id: Long,
    @Column
    val text: String
)

@Table("ratings")
data class Rating(
    @Column("rating_type_name") val ratingTypeName: String,
    @Column val value: Int
)

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
)
