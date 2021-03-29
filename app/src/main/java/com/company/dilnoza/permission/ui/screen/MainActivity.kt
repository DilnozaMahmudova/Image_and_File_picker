package com.company.dilnoza.permission.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.company.dilnoza.permission.R
import com.company.dilnoza.permission.extension.PathUtil
import com.company.dilnoza.permission.extension.checkPermission
import com.company.dilnoza.permission.source.local.LocalStorage
import com.company.dilnoza.permission.source.room.AppDatabase
import com.company.dilnoza.permission.source.room.dao.FileDao
import com.company.dilnoza.permission.source.room.entity.FileData
import com.company.dilnoza.permission.source.room.entity.FileData.Companion.FILE
import com.company.dilnoza.permission.ui.adapters.FileAdapter
import com.company.dilnoza.permission.ui.adapters.MediaAdapter
import com.company.dilnoza.permission.ui.dialog.FileDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.DecimalFormat
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private val storage = LocalStorage.instance
    private lateinit var dao: FileDao
    private lateinit var adapterMedia: MediaAdapter
    private lateinit var adapterFile: FileAdapter
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dao = AppDatabase.getDatabase(this).fileDao()
        loadViews()
    }

    private fun loadViews() {
        settingsM.setOnClickListener {
            val dialog =
                FileDialog(
                    context = this,
                    MediaCount.text.split("/")[0].toInt(),
                    MediaSize.text.split(" ")[0].toFloat()
                )
            dialog.setOnClickListener { i, fl ->
                storage.countMedia = i
                storage.sizMedia = fl
                changeMedia()
                runOnWorkerThread {
                    val ls = dao.getAllMedia()
                    runOnUiThread {
                        val l = ls.toMutableList()
                        l.add(FileData(hide = storage.countMedia == ls.size))
                        adapterMedia.submitList(l)
                    }
                }
            }
            dialog.show()
        }

        settingsF.setOnClickListener {
            val dialog =
                FileDialog(
                    context = this,
                    FileCount.text.split("/")[0].toInt(),
                    FileSize.text.split(" ")[0].toFloat()
                )
            dialog.setOnClickListener { i, fl ->
                storage.countFile = i
                storage.sizeFile = fl
                changeFile()
            }
            dialog.show()
        }

        adapterMedia = MediaAdapter()
        runOnWorkerThread {
            val ls = dao.getAllMedia()
            runOnUiThread {
                setTextCountMedia(ls.size)
                getTotalSizeString(ls) {
                    setTextSizeMedia(it)
                }
                val l = ls.toMutableList()
                l.add(FileData(hide = storage.countMedia == ls.size))
                adapterMedia.submitList(l)
            }
        }

        adapterFile = FileAdapter()
        runOnWorkerThread {
            val ls = dao.getAllFile()
            runOnUiThread {
                adapterFile.submitList(ls)
                setTextCountFile(ls.size)
                getTotalSizeString(ls) {
                    setTextSizeFile(it)
                }
            }
        }

        adapterMedia.setOnAddListener {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                openGallery()
            }
        }

        download.setOnClickListener {
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) {
                openFileChooser()
            }
        }

        adapterMedia.setOnItemRemoveListener {
            runOnWorkerThread {
                dao.delete(it)
                val ls = dao.getAllMedia()
                runOnUiThread {
                    setTextCountMedia(ls.size)
                    getTotalSizeString(ls) {
                        setTextSizeMedia(it)
                    }
                    val l = ls.toMutableList()
                    l.add(FileData(hide = storage.countMedia == ls.size))
                    adapterMedia.submitList(l)
                }
            }
        }

        adapterFile.setOnItemRemoveListener {
            runOnWorkerThread {
                dao.delete(it)
                val ls = dao.getAllFile()
                runOnUiThread {
                    setTextCountFile(ls.size)
                    getTotalSizeString(ls) {
                        setTextSizeFile(it)
                    }
                }
            }
        }

        list_Media.adapter = adapterMedia
        list_Media.layoutManager = GridLayoutManager(this, 3)

        list_File.adapter = adapterFile
        list_File.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "*/*"
        startActivityForResult(intent, 1)
    }

    @SuppressLint("InlinedApi")
    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val path = PathUtil.getPath(this, uri)

            runOnWorkerThread {
                var ls = dao.getAllMedia()
                val currentCount = ls.size
                getTotalSizeInMb(ls) {
                    val currentSize = it
                    val itemSize = getSizeInMb(path)
                    if (currentSize + itemSize <= storage.sizMedia) {
                        if (currentCount + 1 <= storage.countMedia) {
                            val fd = FileData(path = path)
                            val id = dao.insert(fd)
                            fd.id = id
                            ls = dao.getAllMedia()
                            runOnUiThread {
                                setTextCountMedia(currentCount + 1)
                                getTotalSizeString(ls) { s ->
                                    setTextSizeMedia(s)
                                }
                                val l = ls.toMutableList()
                                l.add(FileData(hide = storage.countMedia == ls.size))
                                adapterMedia.submitList(l)
                            }
                        } else {
                            runOnUiThread { showMessage("Tanlangan file galereyaga sig'maydi: max count: ${storage.countMedia}!") }
                        }
                    } else {
                        runOnUiThread { showMessage("Tanlangan file galereyaga sig'maydi: max MB: ${storage.sizMedia}!") }
                    }
                }
            }
        }

        if (requestCode == 2 && resultCode == RESULT_OK) {
            val uri = data?.data ?: return
            val path =uri.path.toString()
            runOnWorkerThread {
                var ls = dao.getAllFile()
                val currentCount = ls.size
                getTotalSizeInMb(ls) {
                    val currentSize = it
                    val itemSize = getSizeInMb(path)
                    if (currentSize + itemSize <= storage.sizeFile) {
                        if (currentCount + 1 <= storage.countFile) {
                            val fd = FileData(path = path, type = FILE)
                            val id = dao.insert(fd)
                            fd.id = id
                            ls = dao.getAllFile()
                            runOnUiThread {
                                adapterFile.insert(fd)
                                setTextCountFile(currentCount + 1)
                                getTotalSizeString(ls) { s ->
                                    setTextSizeFile(s)
                                }
                            }
                        } else {
                            runOnUiThread { showMessage("Tanlangan file storage ga sig'maydi: max count: ${storage.countFile}!") }
                        }
                    } else {
                        runOnUiThread { showMessage("Tanlangan file storage ga sig'maydi: max MB: ${storage.sizeFile}!") }
                    }
                }
            }
        }
    }

    private fun runOnWorkerThread(f: () -> Unit) {
        executor.execute(f)
    }

    private fun showMessage(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

    @SuppressLint("SetTextI18n")
    private fun setTextCountMedia(count: Int) {
        MediaCount.text = "$count/${storage.countMedia}"
    }

    @SuppressLint("SetTextI18n")
    private fun setTextSizeMedia(size: String) {
        MediaSize.text = "$size/${storage.sizMedia} MB"
    }

    @SuppressLint("SetTextI18n")
    private fun setTextCountFile(count: Int) {
        FileCount.text = "$count/${storage.countFile}"
    }

    @SuppressLint("SetTextI18n")
    private fun setTextSizeFile(size: String) {
        FileSize.text = "$size/${storage.sizeFile} MB"
    }

    private fun getTotalSizeString(ls: List<FileData>, block: (String) -> Unit) {
        runOnWorkerThread {
            var size = 0f
            ls.forEach {
                val path = it.path
                val file = File(path)
                size += file.length()
            }

            val m = (size / 1024) / 1024
            val dec = DecimalFormat("0.00")
            runOnUiThread {
                val s = if (m > 1) {
                    dec.format(m).plus(" MB")
                } else {
                    dec.format(size / 1024).plus(" KB")
                }
                block(s)
            }
        }
    }

    private fun getTotalSizeInMb(ls: List<FileData>, block: (Float) -> Unit) {
        runOnWorkerThread {
            var size = 0f
            ls.forEach {
                val path = it.path
                val file = File(path)
                size += file.length()
            }
            block((size / 1024) / 1024f)
        }

    }

    private fun getSizeInMb(path: String): Float {
        val file = File(path)
        val size = file.length()
        return (size / 1024) / 1024f
    }

    private fun changeMedia() {
        runOnWorkerThread {
            val ls = dao.getAllMedia()
            runOnUiThread {
                setTextCountMedia(ls.size)
                getTotalSizeString(ls) {
                    setTextSizeMedia(it)
                }
            }
        }
    }

    private fun changeFile() {
        runOnWorkerThread {
            val ls = dao.getAllFile()
            runOnUiThread {
                setTextCountFile(ls.size)
                getTotalSizeString(ls) {
                    setTextSizeFile(it)
                }
            }
        }
    }
}