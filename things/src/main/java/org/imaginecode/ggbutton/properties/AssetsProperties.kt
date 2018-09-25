package org.imaginecode.ggbutton.properties

import android.content.Context
import android.util.Log
import java.io.IOException
import java.util.*

class AssetsProperties(private var context: Context) {

    fun getProperties(file: String): Properties {
        val properties = Properties()
        try {
            val asssetManager = context.assets
            val inputStream = asssetManager.open(file)
            properties.load(inputStream)
        }catch (e: IOException){
            Log.d("AssetProperties", "Error loading $file.")
        }

        return properties
    }

}