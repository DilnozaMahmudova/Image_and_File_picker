package com.company.dilnoza.permission.ui.adapters

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.company.dilnoza.permission.R
import com.company.dilnoza.permission.extension.SingleBlock
import com.company.dilnoza.permission.extension.bindItem
import com.company.dilnoza.permission.extension.inflate
import com.company.dilnoza.permission.source.room.entity.FileData
import kotlinx.android.synthetic.main.item_file.view.*
import java.io.File

class FileAdapter : ListAdapter<FileData, FileAdapter.VH>(FileData.ITEM_CALLBACK) {
    private var listenerRemove: SingleBlock<FileData>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(parent.inflate(R.layout.item_file))
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind()

    override fun getItemCount(): Int = currentList.size

    fun setOnItemRemoveListener(block: SingleBlock<FileData>) {
        listenerRemove = block
    }

    fun insert(data: FileData) {
        val ls = currentList.toMutableList()
        ls.add(data)
        submitList(ls)
    }

    fun delete(data: FileData) {
        val ls = currentList.toMutableList()
        val pos = ls.indexOfFirst { data.id == it.id }
        ls.removeAt(pos)
        submitList(ls)
    }

    @Suppress("DEPRECATION")
    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        init {
            itemView.btn_file_remove?.apply {
                setOnClickListener {
                    listenerRemove?.invoke(currentList[adapterPosition])
                    delete(currentList[adapterPosition])
                }
            }
        }

        fun bind() = bindItem {
            val d = currentList[adapterPosition]
            val f = File(d.path)
            text_file_name.text = f.name

            val mimeTypeMap = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(d.path))
                    mimeTypeMap.let { Log.d("bbb",mimeTypeMap.toString()) }
            val image = when {
                (mimeTypeMap ?: "").contains("application/pdf") -> R.drawable.pdf
                (mimeTypeMap
                    ?: "").startsWith("video") -> R.drawable.mp4
                (mimeTypeMap ?: "").startsWith("image") -> R.drawable.jpg
                d.path.contains("pdf") -> R.drawable.word
                d.path.endsWith("docx") -> R.drawable.word
                d.path.endsWith("txt") -> R.drawable.txt
                d.path.endsWith("apk") -> R.drawable.android
                d.path.endsWith("mp3") -> R.drawable.mp3
                d.path.endsWith("m4p") -> R.drawable.mp4
                else -> R.drawable.file
            }

            btn_file_type.setImageResource(image)
        }
    }
}