package com.V2Skydivejump.app.data

object SyncMarkers {
    fun contains(serializedIds: String, id: Long): Boolean {
        if (id == 0L) return false
        return parse(serializedIds).contains(id)
    }

    fun add(serializedIds: String, id: Long): String {
        if (id == 0L) return serializedIds
        return (parse(serializedIds) + id).sorted().joinToString(",")
    }

    private fun parse(serializedIds: String): Set<Long> {
        return serializedIds
            .split(",")
            .mapNotNull { it.trim().toLongOrNull() }
            .toSet()
    }
}
