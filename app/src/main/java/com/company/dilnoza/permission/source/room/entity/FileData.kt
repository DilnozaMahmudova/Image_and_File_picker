package com.company.dilnoza.permission.source.room.entity

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FileData(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,
    var path: String="",
    var type: Int= MEDIA,
    var hide: Boolean=false
){
    companion object{
        const val FILE=0
        const val MEDIA=1
        val ITEM_CALLBACK=object :DiffUtil.ItemCallback<FileData>(){
            override fun areItemsTheSame(oldItem: FileData, newItem: FileData)=oldItem.id==newItem.id

            override fun areContentsTheSame(oldItem: FileData, newItem: FileData)=oldItem.path==newItem.path && oldItem.hide==newItem.hide

        }
    }
}