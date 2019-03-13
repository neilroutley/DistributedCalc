package com.androidatc.kotlin.distributedcalc

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "KotlinActivity"
    }

    private var url : String? = null
    private val database = FirebaseDatabase.getInstance()
    private val myAPIRef = database.getReference("apiUrl")

    override fun onStart() {
        super.onStart()

        myAPIRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                if (value != null) {
                    url = value
                    Toast.makeText(this@MainActivity, value, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "No/bad val: ".plus(value), Toast.LENGTH_SHORT).show()
                }
                Log.d(TAG, "Value is: $value")
            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Number listeners
        tvOne.setOnClickListener { appendOnExp("1", true) }
        tvTwo.setOnClickListener { appendOnExp("2", true) }
        tvThree.setOnClickListener { appendOnExp("3", true) }
        tvFour.setOnClickListener { appendOnExp("4", true) }
        tvFive.setOnClickListener { appendOnExp("5", true) }
        tvSix.setOnClickListener { appendOnExp("6", true) }
        tvSeven.setOnClickListener { appendOnExp("7", true) }
        tvEight.setOnClickListener { appendOnExp("8", true) }
        tvNine.setOnClickListener { appendOnExp("9", true) }
        tvZero.setOnClickListener { appendOnExp("0", true) }
        tvDot.setOnClickListener { appendOnExp(".", true) }

        // Operator listeners
        tvPlus.setOnClickListener { appendOnExp("+", false) }
        tvMinus.setOnClickListener { appendOnExp("-", false) }
        tvMult.setOnClickListener { appendOnExp("*", false) }
        tvDivide.setOnClickListener { appendOnExp("/", false) }
        tvOpen.setOnClickListener { appendOnExp("(", false) }
        tvClose.setOnClickListener { appendOnExp(")", false) }

        tvClear.setOnClickListener{
            tvExpression.text = ""
            tvResult.text = ""
        }

        tvBack.setOnClickListener {
            var string = tvExpression.text.toString()
            if(string.isNotEmpty()){
                tvExpression.text = string.substring(0, string.length-1)
            }
            tvResult.text = ""
        }

        tvEquals.setOnClickListener {
            try {
                val queue = Volley.newRequestQueue(this)

                // add the expression to the url before passing it to the RESTapi
                val expressionUrl = url.plus(tvExpression.text.toString())

                // new json object get request and setting tvResult with the returned
                // result from the RESTapi
                val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, expressionUrl, null,
                    Response.Listener { response ->
                        tvResult.text = response.getString("result")
                        Toast.makeText(this@MainActivity, "result: ".plus(response.getString("result")), Toast.LENGTH_SHORT).show()
                    },
                    Response.ErrorListener { error ->
                        Toast.makeText(this@MainActivity, "No/bad val: ".plus(error), Toast.LENGTH_SHORT).show()
                    }
                )
                // add to the network pool for processing
                queue.add(jsonObjectRequest)

            } catch (e : Exception) {
                Log.d("Excpetion ", "message: " + e.message)
            }
        }
    }

    fun appendOnExp(string : String, canClear: Boolean){
        if(tvResult.text.isNotEmpty()){
            tvResult.text = ""
        }

        if(canClear) {
            tvResult.text = ""
            tvExpression.append(string)
        } else {
            tvExpression.append(tvResult.text)
            tvExpression.append(string)
            tvResult.text = ""
        }
    }
}
