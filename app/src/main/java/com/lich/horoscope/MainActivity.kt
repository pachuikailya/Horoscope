package com.lich.horoscope

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{


    var sunSign = "Aries"
    var day="Today"
    var resultView: TextView? = null

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var buttonView: Button = findViewById(R.id.button)
        buttonView.setOnClickListener {
            GlobalScope.async {
                getPredictions(buttonView)
            }
        }
        val spinner = findViewById<Spinner>(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.sunsigns,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = this

        resultView = findViewById(R.id.resultView)

        val switchCompat:SwitchCompat= findViewById(R.id.switch2)

        switchCompat.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                day="Tomorrow"
            } else {
                day="Today"
            }

        }
    }




    override fun onNothingSelected(parent: AdapterView<*>?) {
        sunSign = "Aries"

    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (parent != null) {
            sunSign = parent.getItemAtPosition(position).toString()

        }
    }
    suspend fun getPredictions(view: android.view.View) {
        try {
            val result = withContext(Dispatchers.Default) {
                callAztroAPI("https://sameer-kumar-aztro-v1.p.rapidapi.com/?sign=" + sunSign +"&day=" +day)
            }
            onResponse(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun callAztroAPI(apiUrl:String ):String?{
        var result: String? = ""
        val url: URL;
        var connection: HttpURLConnection? = null
        try {
            url = URL(apiUrl)
            connection = url.openConnection() as HttpURLConnection
            // set headers for the request
            // set host name
            connection.setRequestProperty("x-rapidapi-host", "sameer-kumar-aztro-v1.p.rapidapi.com")
            // set the rapid-api key
            connection.setRequestProperty("x-rapidapi-key", "2c4262e88amshaec6457acd17d67p16ba39jsn462fcaf6ae90")
            connection.setRequestProperty("content-type", "application/x-www-form-urlencoded")
            // set the request method - POST
            connection.requestMethod = "POST"
            val `in` = connection.inputStream
            val reader = InputStreamReader(`in`)
            // read the response data
            var data = reader.read()
            while (data != -1) {
                val current = data.toChar()
                result += current
                data = reader.read()
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // if not able to retrieve data return null
        return null
    }
    private fun onResponse(result: String?) {
        try {
            // convert the string to JSON object for better reading
            val resultJson = JSONObject(result)
            // Initialize prediction text
            var prediction =this.day+"'s prediction \n\n"
            prediction += this.sunSign+"\n"
            // Update text with various fields from response
            prediction += resultJson.getString("date_range")+"\n\n"
            prediction += resultJson.getString("description")
            //Update the prediction to the view
            setText(this.resultView,prediction)
        } catch (e: Exception) {
            e.printStackTrace()
            this.resultView!!.text = "Oops!! something went wrong, please try again"
        }
    }
    private fun setText(text: TextView?, value: String) {
        runOnUiThread { text!!.text = value }
    }
}