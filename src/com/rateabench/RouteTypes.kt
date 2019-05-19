package com.rateabench

import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredMemberProperties

/**
 * Created by Jonathan Schurmann on 5/16/19.
 */
open class PostValidatable {
    fun isValid(): Boolean {
        return !this::class.declaredMemberProperties.filter { it.visibility == KVisibility.PUBLIC }.any {
            when (val value = it.getter.call(this)) {
                is Number -> value.toDouble() == 0.0
                is String -> value.isEmpty()
                else -> true
            }
        }
    }
}

data class PostBench(val lat: Double, val lng: Double, val creatorId: Long) : PostValidatable()
data class PostImage(val benchId: Long) : PostValidatable()

