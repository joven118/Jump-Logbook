package com.V2Skydivejump.app.utils

import com.V2Skydivejump.app.TimeUtils
import com.V2Skydivejump.app.database.entities.JumpLogEntity

enum class DeviceBrand {
    SONOALTI, DEKUNU, LNB, FLYSIGHT, X_SHUT
}

object TelemetryParser {

    /**
     * Parses a raw byte array from the SonoAlti 2 altimeter (FDS).
     * Expanded packet format:
     * Bytes 0-1: Jump Number
     * Bytes 2-5: Exit Alt (ft)
     * Bytes 6-9: Deploy Alt (ft)
     * Bytes 10-13: Max Speed (mph)
     * Bytes 14-17: FF Duration (seconds)
     */
    fun parseFdsPacket(data: ByteArray, userId: String): JumpLogEntity? {
        if (data.size < 10) return null

        try {
            val jumpNumber = (data[0].toInt() and 0xFF) or ((data[1].toInt() and 0xFF) shl 8)
            val exitAltitude = extractInt32(data, 2)
            val deploymentAltitude = extractInt32(data, 6)
            
            var maxSpeed = 0
            var ffTime = 0
            
            if (data.size >= 18) {
                maxSpeed = extractInt32(data, 10)
                ffTime = extractInt32(data, 14)
            }

            return JumpLogEntity(
                userId = userId,
                jumpNumber = jumpNumber,
                date = TimeUtils.nowEpochMillis(),
                location = "Sync from Altimeter",
                dzName = "Sync from Altimeter",
                aircraft = "Detected via BLE",
                exitAltitudeAgl = exitAltitude,
                deploymentAltitudeAgl = deploymentAltitude,
                maxSpeedMph = maxSpeed,
                freefallTimeSeconds = ffTime,
                verificationMethod = "HARDWARE_SYNC"
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun extractInt32(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or 
               ((data[offset + 1].toInt() and 0xFF) shl 8) or 
               ((data[offset + 2].toInt() and 0xFF) shl 16) or 
               ((data[offset + 3].toInt() and 0xFF) shl 24)
    }

    /**
     * Parses a CSV string from a compatible altimeter (e.g. FlySight or Dekunu).
     */
    fun parseAltimeterCsv(content: String, userId: String): List<JumpLogEntity> {
        val lines = content.lines().filter { it.isNotBlank() }
        if (lines.size < 2) return emptyList()

        val headers = lines[0].split(",").map { it.trim().lowercase() }
        val jumps = mutableListOf<JumpLogEntity>()

        for (i in 1 until lines.size) {
            val data = lines[i].split(",").map { it.trim() }
            val map = mutableMapOf<String, String>()
            headers.forEachIndexed { index, header ->
                if (index < data.size) map[header] = data[index]
            }

            try {
                jumps.add(JumpLogEntity(
                    userId = userId,
                    jumpNumber = map["jump"]?.toIntOrNull() ?: map["#"]?.toIntOrNull() ?: 0,
                    date = TimeUtils.nowEpochMillis(), // Real app: parse from CSV date
                    exitAltitudeAgl = map["exit"]?.toIntOrNull() ?: map["exit_alt"]?.toIntOrNull() ?: 0,
                    deploymentAltitudeAgl = map["deploy"]?.toIntOrNull() ?: map["deploy_alt"]?.toIntOrNull() ?: 0,
                    maxSpeedMph = map["max_speed"]?.toIntOrNull() ?: map["speed"]?.toIntOrNull() ?: 0,
                    freefallTimeSeconds = map["ff_time"]?.toIntOrNull() ?: map["duration"]?.toIntOrNull() ?: 0,
                    verificationMethod = "CSV_IMPORT"
                ))
            } catch (_: Exception) {}
        }
        return jumps
    }

    /**
     * Extension to convert ByteArray to Hex String for debugging.
     */
    fun ByteArray.toHexString(): String {
        return joinToString("") { it.toUByte().toString(16).padStart(2, '0').uppercase() }
    }
}
