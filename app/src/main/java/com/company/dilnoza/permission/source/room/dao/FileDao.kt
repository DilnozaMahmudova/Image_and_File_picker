package com.company.dilnoza.permission.source.room.dao

import androidx.room.Dao
import androidx.room.Query
import com.company.dilnoza.permission.source.room.entity.FileData
@Dao
interface FileDao: BaseDao<FileData> {
    @Query("Select * From FileData where type=1")
    fun getAllMedia(): List<FileData>
    @Query("Select * From FileData where type=0")
    fun getAllFile(): List<FileData>
}