package com.devanshu.simpleapicalldemo

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        CallAPILoginAsyncTask("devanshu","123456").startApiCall()//execute the background process
    }

    private inner class CallAPILoginAsyncTask(val username: String, val password: String){

        private lateinit var customProgressDialog: Dialog

        fun startApiCall() {
            showProgressDialog()
            lifecycleScope.launch(Dispatchers.IO) {
//                delay(5000L)
                val stringResult=makeApiCall()
                afterCallFinish(stringResult)
            }
        }

        //like do in background
        fun makeApiCall(): String {
            var result:String
            var connection: HttpURLConnection?=null

            try{
                val url= URL("https://run.mocky.io/v3/1f2d726e-f8d0-4974-aca0-b66690e12e7a")
                connection= url.openConnection() as HttpURLConnection?   //Returns a URLConnection instance that represents a connection to the remote object referred to by the URL.
                connection!!.doInput=true  //doInput tells if we get any data(by default doInput will be true and doOutput false)
                connection.doOutput=true //doOutput tells if we send any data with the api call

                connection.instanceFollowRedirects = false

                //POST request
                //START
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type","application/json")
                connection.setRequestProperty("charset","utf-8")
                connection.setRequestProperty("Accept","application/json")

                connection.useCaches = false

                val writeDataOutputStream = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                jsonRequest.put("username",username)
                jsonRequest.put("password",password)


                writeDataOutputStream.writeBytes(jsonRequest.toString())
                writeDataOutputStream.flush()
                writeDataOutputStream.close()

                //END

                val httpResult:Int=connection.responseCode
                if(httpResult==HttpURLConnection.HTTP_OK){
                    //now once we have established a successful connection, we want to read the data.

                    //Returns an input stream that reads from this open connection. A SocketTimeoutException can be thrown when
                    // reading from the returned input stream if the read timeout expires before data is available for read.
                    val inputStream=connection.inputStream

                    val reader= BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder:StringBuilder=StringBuilder()
                    var line:String?
                    try{
                        while (reader.readLine().also { line=it }!=null) {
                            stringBuilder.append(line+"\n")
                            Log.i("TAG", "doInBackground: $line\n")
                        }
                    }
                    catch (e: IOException){
                        e.printStackTrace()
                    }
                    finally {
                        try {  //there could be some error while closing the inputStream
                            inputStream.close()
                        }
                        catch (e:IOException){
                            e.printStackTrace()
                        }
                    }
                    result=stringBuilder.toString()
                }
                else{  //if the response code is not OK
                    result=connection.responseMessage
                }
            }
            catch (e: SocketTimeoutException){
                result="Connection Timeout"
            }
            catch (e:Exception){
                result="Error + ${e.message}"
            }
            finally {
                connection?.disconnect()
            }

            return result
        }

        fun afterCallFinish(result: String?) {
            cancelProgressDialog()

            Log.i("JSON RESPONSE RESULT", result.toString())

            //for getting data using gson

            val responseData = Gson().fromJson(result, ResponseData::class.java)
            Log.i("Message",responseData.Message)

            Log.i("User Id","${responseData.User_id}")
            Log.i("Name",responseData.Name)
            Log.i("Email",responseData.Email)
            Log.i("Mobile","${responseData.Phone}")

            Log.i("Is Profile Completed","${responseData.Profile_details.is_profile_completed}")
            Log.i("Rating","${responseData.Profile_details.rating}")

            Log.i("Data List Size","${responseData.data_list.size}")

            for (item in responseData.data_list.indices){
                Log.i("Value $item", "${responseData.data_list[item]}")

                Log.i("ID","${responseData.data_list[item].id}")
                Log.i("Value", responseData.data_list[item].name)

            }




            /*
            //for getting data from json
            val jsonObject = JSONObject(result)
            val message = jsonObject.optString("Message")
            Log.i("Message", message)                       // return message from json

            val userID = jsonObject.optInt("User_id")
            Log.i("User Id", "$userID")

            val name = jsonObject.optString("Name")
            Log.i("Name", name)

            //for getting json inside json
            val profileDetailObject = jsonObject.optJSONObject("Profile_details")
            val isProfileCompleted = profileDetailObject.optBoolean("is_profile_completed")
            Log.i("is_profile_completed","$isProfileCompleted")


            //for getting list data from json
            val dataListArray = jsonObject.optJSONArray("data_list")
            Log.i("Data list size", "${dataListArray.length()}")

            for (item in 0 until dataListArray.length()){
                Log.i("Value $item","${dataListArray[item]}")

                val dataItemObject: JSONObject = dataListArray[item] as JSONObject

                val id = dataItemObject.optInt("id")
                Log.i("id","$id")

                val value = dataItemObject.optString("name")
                Log.i("value","$value")
            }

             */


        }



        private fun showProgressDialog(){
            customProgressDialog= Dialog(this@MainActivity)
            customProgressDialog.setContentView(R.layout.dialog_custom_progress)
            customProgressDialog.setCancelable(false)
            customProgressDialog.show()
        }

        private fun cancelProgressDialog(){
            customProgressDialog.dismiss()
        }

    }
}