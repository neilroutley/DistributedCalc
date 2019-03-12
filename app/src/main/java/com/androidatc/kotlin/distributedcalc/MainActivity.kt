package com.androidatc.kotlin.distributedcalc

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import net.objecthunter.exp4j.ExpressionBuilder
import java.lang.Exception
import kotlin.math.exp

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "KotlinActivity"
    }
    // Write a message to the database
//    val database = FirebaseDatabase.getInstance()
//    val myRef = database.getReference("message")
//
//    myRef.setValue("Hello, World!")

//    private val mRootRef : DatabaseReference = FirebaseDatabase.getInstance().reference
//    private val mConditionRef : DatabaseReference = mRootRef.child("condition")

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("condition")

    override fun onStart() {
        super.onStart()

        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                if (value != null) {
                    tvResult.text = value
                    Toast.makeText(this@MainActivity, value, Toast.LENGTH_SHORT).show()
                } else {
                    tvResult.text = "bad value"
                    Toast.makeText(this@MainActivity, value, Toast.LENGTH_SHORT).show()
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
                val expression = ExpressionBuilder(tvExpression.text.toString()).build()
                val result = expression.evaluate()
                val longResult = result.toLong()
                if(result == longResult.toDouble()){
                    tvResult.text = longResult.toString()
                } else {
                    tvResult.text = result.toString()
                }

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
