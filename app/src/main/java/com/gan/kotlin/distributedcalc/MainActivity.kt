package com.gan.kotlin.distributedcalc

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "Distributed Calculator"
    }

    private var url : String? = null
    private val database = FirebaseDatabase.getInstance()
    private val myAPIRef = database.getReference("apiUrl")

    //not sure i can delete, we dont use, but we do...
    private var mAuth: FirebaseAuth? = null

    //describes builders for the signin/register methods
    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build()
    )
    //holds true value
    private val RC_SIGN_IN = 1
    private var loggedIn = false
    private var user = FirebaseAuth.getInstance().currentUser

    override fun onStart() {
        super.onStart()

        myAPIRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val value = dataSnapshot.getValue(String::class.java)
                if (value != null) {
                    url = value
                    //Toast.makeText(this@MainActivity, value, Toast.LENGTH_SHORT).show()
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
                        //Toast.makeText(this@MainActivity, "No/bad val: ".plus(error), Toast.LENGTH_SHORT).show()
                        Log.w(TAG, "bad return :".plus(error))
                    }
                )
                // add to the network pool for processing
                queue.add(jsonObjectRequest)

                //firebase recording
                 if(loggedIn) {
                     //only happens if logged in
                     var map = HashMap<String, String>()
                     map.put("id", "1") //wasnt working with database reference
                     map.put("body", tvExpression.text.toString())
                     map.put("result", tvResult.text.toString())
                    //Toast.makeText(this@MainActivity, map.toString(), Toast.LENGTH_LONG).show()
                     //push map to database, database converts to correct structure
                     database.getReference(user!!.uid)
                         .push()
                         .setValue(map)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                user = FirebaseAuth.getInstance().currentUser
                loggedIn = true

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                Toast.makeText(this, "Sign in Failed, Try again later", Toast.LENGTH_LONG).show()
            }
        }
    }


    /**
     * Creates options menu, is called when app is started
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.calc_menu, menu)
        return true
    }


    /**
     * Recreates options menu, is called when menu is updated, Used to control what options are visible if logged in or not
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        invalidateOptionsMenu()
        if(loggedIn) {
            menu.findItem(R.id.login).isVisible = false
            menu.findItem(R.id.register_user).isVisible = false
            menu.findItem(R.id.logout).isVisible = true
            menu.findItem(R.id.delete_user).isVisible = true
            menu.findItem(R.id.history).isVisible = true
        } else {
            //not logged in
            menu.findItem(R.id.login).isVisible = true
            menu.findItem(R.id.register_user).isVisible = true
            menu.findItem(R.id.logout).isVisible = false
            menu.findItem(R.id.delete_user).isVisible = false
            menu.findItem(R.id.history).isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }

    /**
     * Gives action to menu items
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.register_user -> {
               // Toast.makeText(this, item.itemId.toString(), Toast.LENGTH_SHORT).show()
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    RC_SIGN_IN)
                return true }
            R.id.login -> {


                // Create and launch sign-in intent
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                    RC_SIGN_IN)
                return true }
            R.id.logout -> {
                signOut()
                return true }
            R.id.history -> {

                goToDisplayHistory()
                return true }
            R.id.delete_user -> {

                delete()
                return true }
            else -> return super.onContextItemSelected(item)
        }
    }


    /**
     * Signs user out of firebase
     */
    private fun signOut() {
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // user is now signed out
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
                finish()
            }
    }



    /**
     * Deletes User from firebase
     */
    private fun delete() {
        AuthUI.getInstance()
            .delete(this)
            .addOnCompleteListener {
                //if this isnt here, it loops and fails due to smartlock, probably doesnt happen on real device
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
                finish()
            }
    }


    private fun goToDisplayHistory() {
        if(user == null) {
            Toast.makeText(this,"Must be logged in", Toast.LENGTH_SHORT).show()
        } else {
            //user is logged in
            //Toast.makeText(this, user!!.displayName, Toast.LENGTH_LONG).show()
            //create history activity
            var intent = Intent(this, ChatHistory::class.java)
  /*          intent.putExtra("user", user!!.uid)*/
            startActivity(intent)


        }
    }
}
