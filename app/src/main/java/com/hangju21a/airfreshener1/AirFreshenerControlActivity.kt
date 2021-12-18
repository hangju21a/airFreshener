package com.hangju21a.airfreshener1


import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import retrofit2.Call
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_led_control.*
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.util.*


public class LedControlActivity : AppCompatActivity() {

    companion object{
        var BaseUrl = "http://api.openweathermap.org/"
        var AppId = "659228011946e40bc157c1e77e20f897"
        var lat = "37.445293"
        var lon = "126.785823"
    }

    //default timer time
    var timer = 15

    //    UI Components
    lateinit var btnTurnOn: Button
    lateinit var btnTurnOff: Button
    lateinit var btnDisconnect: Button

    lateinit var tvLEDstatus: TextView

    private lateinit  var progressDialog: ProgressDialog

    //    Program Variables
    lateinit var btAddress: String

    //    Bluetooth Realted Variables and Values
    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var bluetoothSocket: BluetoothSocket
    var isBluetoothConnected: Boolean = false

    val myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    var filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)

    override fun registerReceiver(receiver: BroadcastReceiver?, filter: IntentFilter?): Intent? {
        return super.registerReceiver(receiver, filter)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_led_control)

//toolbar

        /*
        supportActionBar?.setDisplayShowCustomEnabled(true)

        val topBar = layoutInflater.inflate(R.layout.custom_actionbar,null)
        supportActionBar?.customView=topBar

        topBar.run{
            textView.setTextColor(Color.WHITE)
        }

        //action bar
*/
        Toast.makeText(applicationContext, "My ID: $myUUID", Toast.LENGTH_LONG).show()
        // Initialize the UI Components/ Bind UI Components to the Application Logic


        btnDisconnect = findViewById(R.id.btnDisconnect)
        tvLEDstatus = findViewById(R.id.tvLEDstatus)



        timerButton.setOnClickListener {
            timer = Integer.parseInt(editTextTime.text.toString())
            if(timer!=0) {
                Toast.makeText(
                    this@LedControlActivity,
                    "반복 시간을 " + timer + "분으로 설정했습니다.",
                    Toast.LENGTH_LONG
                ).show()
                tvLEDstatus.append("반복 시간을 " + timer + " 분으로 설정했습니다. \n")
            }else {
                Toast.makeText(
                    this@LedControlActivity,
                    "반복 시간을 최소 1분으로 설정해주십시오.",
                    Toast.LENGTH_LONG
                ).show()
            }

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editTextTime.windowToken,0)

            editTextTime.clearFocus()
        }



        //receive the address of the bluetooth device
        val newIntent = intent
        btAddress = newIntent.getStringExtra("currDeviceBluetoothAddress").toString()


        // To Check Whether the Current Bluetooth Device's Address is Received
        Toast.makeText(applicationContext, "BT Address: $btAddress", Toast.LENGTH_LONG).show()

        ConnectBluetooth().execute()


        //weather Retorfit

        val retrofit = Retrofit.Builder()
            .baseUrl(BaseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)
        val call = service.getCurrentWeatherData(lat, lon, AppId)
        call.enqueue(object : Callback<WeatherResponse> {
            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Log.d("LedControlActivity", "result :" + t.message)
            }

            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if(response.code() == 200){
                    val weatherResponse = response.body()
                    Log.d("LedControlActivity", "result: " + weatherResponse.toString())
                    var nowWeather_main = weatherResponse!!.weather!!.get(0).main
                    var cTemp =  weatherResponse!!.main!!.temp - 273.15  //켈빈을 섭씨로 변환
                    var minTemp = weatherResponse!!.main!!.temp_min - 273.15
                    var maxTemp = weatherResponse!!.main!!.temp_max - 273.15
                    val stringBuilder =
                        "지역: " + weatherResponse!!.sys!!.country + "\n" +
                                "날씨 : " + nowWeather_main + "\n" +
                                "현재기온: " + cTemp.toInt() + "\n" +
                                "최저기온: " + minTemp.toInt() + "\n" +
                                "최고기온: " + maxTemp.toInt() + "\n"


                    weatherText.text = stringBuilder

                    weatherButton.setOnClickListener{
                        if(nowWeather_main=="Clouds")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Tornado")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Squall")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Ash")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Dust")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Mist")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Smoke")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Haze")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Dust")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Fog")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Sand")
                        {
                            Toast.makeText(this@LedControlActivity,"날씨는 흐림입니다. 1번향을 분사합니다.", Toast.LENGTH_LONG).show()
                            //bluetooth data send
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 1번향을 분사합니다. \n")
                            writeToBluetooth("1#"+timer)


                        }
                        else if(nowWeather_main=="Clear") {
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 2번향을 분사합니다. \n")
                            writeToBluetooth("2#"+timer)


                            Toast.makeText(
                                this@LedControlActivity,
                                "날씨는 맑음입니다. 2번향을 분사합니다.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else{
                            tvLEDstatus.append("날씨는 "+nowWeather_main+" 입니다. 3번향을 분사합니다. \n")
                            writeToBluetooth("3#"+timer)


                            Toast.makeText(this@LedControlActivity,"비(눈)이 내립니다. 3번향을 분사합니다.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }

        })






    }

    fun push1(view: View){
        writeToBluetooth("1#"+timer)
        tvLEDstatus.append("1번향을 분사합니다. \n")
    }

    fun push2(view: View){
        writeToBluetooth("2#"+timer)
        tvLEDstatus.append("2번향을 분사합니다. \n")
    }
    fun push3(view: View){
        writeToBluetooth("3#"+timer)
        tvLEDstatus.append("3번향을 분사합니다. \n")
    }

    fun disconnectBluetooth(view: View){
        bluetoothSocket.close();


    }

    fun readFromBluetooth(){

        val buffer = ByteArray(256) // buffer store for the stream
        var bytes: Int // bytes returned from read()
        try {
            var socketInputStream = bluetoothSocket.inputStream


            // Read from the InputStream
            bytes = socketInputStream.read(buffer)
            val readMessage = String(buffer, 0, bytes)
            // Send the obtained bytes to the UI Activity
        } catch (e: Exception) {

        }






    }

    fun writeToBluetooth(str: String){

        bluetoothSocket.outputStream.write(str.toByteArray())
        Toast.makeText(applicationContext, "성공적으로 데이터를 전송하였습니다.", Toast.LENGTH_LONG).show()
    }

    // --------------------------------------- Inner Class --------------------------------------------------
    inner class ConnectBluetooth: AsyncTask<Void, Void, Void>() {
        private var ConnectSuccess = true //if it's here, it's almost connected

        override fun onPreExecute() {
            super.onPreExecute()
            progressDialog = ProgressDialog.show(this@LedControlActivity, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        override fun doInBackground(vararg params: Void?): Void? {

            try {
                if ( !::bluetoothSocket.isInitialized || !isBluetoothConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() //get the mobile bluetooth device
                    val dispositivo: BluetoothDevice = bluetoothAdapter.getRemoteDevice(btAddress) //connects to the device's address and checks if it's available
                    bluetoothSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID) //create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    bluetoothSocket.connect() //start connection
//                    tvLEDstatus.append("Device Connected \n")
                }
            } catch (e: IOException) {
                ConnectSuccess = false //if the try failed, you can check the exception here
//                tvLEDstatus.append("Connection Failed \n")
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            if (!ConnectSuccess)
            {
                Toast.makeText(applicationContext,"Connection Failed. Is it a SPP Bluetooth? Try again.",Toast.LENGTH_LONG).show();
                tvLEDstatus.append("Unable to Connect :( \n")
                finish();
            }
            else
            {
                Toast.makeText(applicationContext,"Connected",Toast.LENGTH_LONG).show();
                tvLEDstatus.append("Device Connected :) \n")
                isBluetoothConnected = true;
            }
            progressDialog.dismiss();
        }

    }



}

interface WeatherService{

    @GET("data/2.5/weather")
    fun getCurrentWeatherData(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String) :
            Call<WeatherResponse>
}

class WeatherResponse(){
    @SerializedName("weather") var weather = ArrayList<Weather>()
    @SerializedName("main") var main: Main? = null
    @SerializedName("wind") var wind : Wind? = null
    @SerializedName("sys") var sys: Sys? = null
}

class Weather {
    @SerializedName("id") var id: Int = 0
    @SerializedName("main") var main : String? = null
    @SerializedName("description") var description: String? = null
    @SerializedName("icon") var icon : String? = null
}

class Main {
    @SerializedName("temp")
    var temp: Float = 0.toFloat()
    @SerializedName("humidity")
    var humidity: Float = 0.toFloat()
    @SerializedName("pressure")
    var pressure: Float = 0.toFloat()
    @SerializedName("temp_min")
    var temp_min: Float = 0.toFloat()
    @SerializedName("temp_max")
    var temp_max: Float = 0.toFloat()

}

class Wind {
    @SerializedName("speed")
    var speed: Float = 0.toFloat()
    @SerializedName("deg")
    var deg: Float = 0.toFloat()
}

class Sys {
    @SerializedName("country")
    var country: String? = null
    @SerializedName("sunrise")
    var sunrise: Long = 0
    @SerializedName("sunset")
    var sunset: Long = 0
}

