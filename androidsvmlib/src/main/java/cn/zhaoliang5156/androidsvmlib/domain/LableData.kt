package cn.zhaoliang5156.androidsvmlib.domain

import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

/**
 *
 * Created by zhaoliang on 2017/7/22.
 */
data class LableData(var iconIDDefault: Int, var iconIDLableSelect: Int, var iconIDLableTest: Int, var lable: Int, var status: Int, var filePath: String, var labelCount: Int) {

    init {
        getLableCount()
    }

    fun getLableCount() {
        val file = File(filePath)
        if (file.exists()) {
            val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(file)))
            var line = bufferedReader.readLine()
            while (line != null) {
                labelCount++
                line = bufferedReader.readLine()
            }
        }
    }
}