package com.example.volleywebservices

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.example.volleywebservices.APIvoley.VolleySingleton
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    val wsInsertar = "http://192.168.137.212/servicios/InsertarAlumno.php"
    val wsActualizar="http://192.168.137.212/servicios/ActualizarAlumno.php"
    val wsEliminar="http://192.168.137.212/servicios/BorrarAlumno.php"
    val wsConusltabyID="http://192.168.137.212/servicios/MostrarAlumno.php"
    val wsConsulta="http://192.168.137.212/servicios/MostrarAlumnos.php"
    val wsrespalda="http://192.168.137.212/servicios/RespaldaAlumno.php"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun consulta(v:View){
        getAlumnos()
    }
    fun insertarAlumno(v:View){
        if (etcontrol.text.isEmpty() || etcarrera.text.isEmpty() || ettelefono.text.isEmpty() || etnombre.text.isEmpty()) {
            Toast.makeText(this, "Falta enviar informacion", Toast.LENGTH_SHORT).show();
        }else {
           val control = etcontrol.text.toString()
            val nom = etnombre.text.toString()
            val carrera=etcarrera.text.toString()
            val tel=ettelefono.text.toString()
            var jsonEntrada=JSONObject()

            jsonEntrada.put("nocontrol", control)
            jsonEntrada.put("nombre", nom)
            jsonEntrada.put("carrera", carrera)
            jsonEntrada.put("telefono", tel)

            sendRequest(wsInsertar,jsonEntrada)

            limpiar()
        }
}
    fun ActualizaAlumno(v:View){
        if (etcontrol.text.isEmpty() || etcarrera.text.isEmpty() || ettelefono.text.isEmpty() || etnombre.text.isEmpty()) {
            Toast.makeText(this, "Falta enviar informcaion", Toast.LENGTH_SHORT).show();
        }else {
            val control = etcontrol.text.toString()
            val nom = etnombre.text.toString()
            val carrera=etcarrera.text.toString()
            val tel=ettelefono.text.toString()
            var jsonEntrada=JSONObject()

            jsonEntrada.put("nocontrol", control)
            jsonEntrada.put("nombre", nom)
            jsonEntrada.put("carrera", carrera)
            jsonEntrada.put("telefono", tel)

            sendRequest(wsActualizar,jsonEntrada)

            limpiar()
        }
    }
    fun EliminarAlumno(v:View){
        if (etcontrol.text.isEmpty()) {
            Toast.makeText(this, "Ingrese numero de control", Toast.LENGTH_SHORT).show();
        }else {
            val control = etcontrol.text.toString()
            var jsonEntrada=JSONObject()

            jsonEntrada.put("nocontrol", control)
            sendRequest(wsEliminar,jsonEntrada)
            limpiar()
        }
    }

    fun respaldaAlumnos(v:View){
        val admin= adminBD(this)

        var alumnoJson:JSONObject
        var jsonArray= JSONArray()
        var jsonParam=JSONObject() //JSON FINAL

        jsonParam.put("usr","vlz")
        jsonParam.put("pwd","hola")

        val cur = admin.Consulta("SELECT nocontrol, nombre, carrera, telefono FROM alumno")
       if(cur!!.moveToFirst()) {
           do {
               alumnoJson = JSONObject()
               alumnoJson.put("nocontrol", cur!!.getString(0))
               alumnoJson.put("carrera", cur!!.getString(1))
               alumnoJson.put("nombre", cur!!.getString(2))
               alumnoJson.put("telefono", cur!!.getString(3))
               jsonArray.put(alumnoJson)
           } while (cur!!.moveToNext())
           cur.close()
       }
        jsonParam.put("alumno",jsonArray)
        sendRequest(wsrespalda,jsonParam)
    }


    fun buscarAlumno (v : View) {
        if (etcontrol.text.isEmpty()) {
            Toast.makeText(this, "ERROR: SE NECESITA EL NUMERO DE CONTROL PARA ESTA OPERACION", Toast.LENGTH_SHORT).show();
            etcontrol.requestFocus()
        }else {//PREPRARA JSON DE ENTRADA A NUESTRO WEB SERVICES
            val no = etcontrol.text.toString()
            var jsonEntrada = JSONObject()
            jsonEntrada.put("nocontrol",no)
            getAlumno(jsonEntrada)
        }
    }
    fun limpiar(){
        etcontrol.setText("")
        etnombre.setText("")
        etcarrera.setText("")
        ettelefono.setText("")

    }
   fun getAlumno(jsonEntrada: JSONObject){
        val url= wsConusltabyID
        val jsonArrayRequest= JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonEntrada,
            Response.Listener {response->
                val succ = response["success"]
                val msg = response["message"]
                val alumnoJSON = response.getJSONArray("alumno")

                if (alumnoJSON.length() >= 1) {//No es un arreglo, por eso empieza en la posicion 1
                    //Todos son el elemento 0 porque estan en el arreglo en la posicion 0
                    val no = alumnoJSON.getJSONObject(0).getString("nocontrol")
                     val carr = alumnoJSON.getJSONObject(0).getString("carrera")
                     val nom = alumnoJSON.getJSONObject(0).getString("nombre")
                    val tel = alumnoJSON.getJSONObject(0).getString("telefono")
                    etcontrol.setText(no)
                    etcarrera.setText(carr)
                    etnombre.setText(nom)
                    ettelefono.setText(tel)
                    etcontrol.requestFocus()
                }
            },
            Response.ErrorListener { error->
                Toast.makeText(this, "${error.message}", Toast.LENGTH_LONG).show();
                Log.d("ERROR","${error.message}")
                Toast.makeText(this, "API: Error de capa 8 WS):", Toast.LENGTH_LONG).show();
            })
        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest)
    }

    fun sendRequest(wsUrl:String,jsonEntrada: JSONObject){

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, wsUrl, jsonEntrada,
            Response.Listener { response ->
                val succ = response["success"]
                val msg = response["message"]
                Toast.makeText(this, "Success: ${succ} Message:${msg} ", Toast.LENGTH_LONG).show();
            },
            Response.ErrorListener { error ->
                Toast.makeText(this, "${error.message}", Toast.LENGTH_LONG).show();
                Log.d("ERROR","${error.message}")
                Toast.makeText(this, "API: Error de capa 8 WS):", Toast.LENGTH_LONG).show();
            }
        )
        //Es para agregar las peticiones a la cola
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    fun getAlumnos(){
        val url= wsConsulta
        val jsonArrayRequest= JsonObjectRequest(
            Request.Method.POST,
            url,
            null,
            Response.Listener {response->
                val succ = response["success"]
                val msg = response["message"]
                val alumnoJSON = response.getJSONArray("alumno")

                for (i in 0 until alumnoJSON.length()) {//No es un arreglo, por eso empieza en la posicion 1
                    //Todos son el elemento 0 porque estan en el arreglo en la posicion 0
                    val no = alumnoJSON.getJSONObject(i).getString("nocontrol")
                    val carr = alumnoJSON.getJSONObject(i).getString("carrera")
                    val nom = alumnoJSON.getJSONObject(i).getString("nombre")
                    val tel = alumnoJSON.getJSONObject(i).getString("telefono")
                    val admin=adminBD(this)
                    val sentencia = "INSERT INTO alumno (nocontrol, nombre, carrera, telefono)VALUES('${no}','${nom}','${carr}','${tel}')"
                    admin.Ejecuta(sentencia)
                    etcontrol.setText(no)
                    etcarrera.setText(carr)
                    etnombre.setText(nom)
                    ettelefono.setText(tel)
                    etcontrol.requestFocus()
                }

                Toast.makeText(this, "Alumnos guardados en SQLITE", Toast.LENGTH_SHORT).show();
            },
            Response.ErrorListener { error->
                Toast.makeText(this, "${error.message}", Toast.LENGTH_LONG).show();
                Log.d("ERROR","${error.message}")
                Toast.makeText(this, "API: Error de capa 8 WS):", Toast.LENGTH_LONG).show();
            })
        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest)
    }
}
