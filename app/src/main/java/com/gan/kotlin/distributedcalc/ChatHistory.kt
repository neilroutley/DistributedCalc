package com.gan.kotlin.distributedcalc

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.gan.kotlin.distributedcalc.model.Expression
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.gan.kotlin.distributedcalc.viewholder.ExpressionViewHolder
import com.google.firebase.database.*
import android.support.v7.widget.RecyclerView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.database.FirebaseRecyclerOptions


class ChatHistory : AppCompatActivity() {
    //tag for logging
    companion object {
        private const val TAG = "ChatHistory"
    }

    //holds user
    private var user: FirebaseUser? = null

    //database
    private var mDatabase: DatabaseReference? = null
    //database for spcific user
    private var mUserHistory: DatabaseReference? = null

    //for layout
    private var recyclerView: RecyclerView? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private lateinit var mAdapter: FirebaseRecyclerAdapter<Expression, ExpressionViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat__history)

        recyclerView = findViewById(R.id.chat_history)
        mDatabase = FirebaseDatabase.getInstance().reference
        user = FirebaseAuth.getInstance().currentUser
        mUserHistory = FirebaseDatabase.getInstance().getReference(user!!.uid).child("")

        linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager!!.reverseLayout = true
        linearLayoutManager!!.setStackFromEnd(true);
        recyclerView!!.setLayoutManager(linearLayoutManager)
        recyclerView!!.setHasFixedSize(true)
        getHistory()


    }

    private fun getHistory() {
        //create a place to query from, in this case the child of the userid reference, each one a unique transaction key
        val query = FirebaseDatabase.getInstance().getReference(user!!.uid).child("")

        //create a buider to fill out recycler ciew on the fly
        val options = FirebaseRecyclerOptions.Builder<Expression>()
            .setQuery(query) { snapshot ->
                Expression(
                    snapshot.child("id").value!!.toString(),
                    snapshot.child("body").value!!.toString(),
                    snapshot.child("result").value!!.toString()
                )
            }
            .build()

        mAdapter = object : FirebaseRecyclerAdapter<Expression, ExpressionViewHolder>(options) {

            //creates the holder
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpressionViewHolder {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.card_layout, parent, false)

                return ExpressionViewHolder(view)
            }


            //binds data to holder
            override fun onBindViewHolder(holder: ExpressionViewHolder, position: Int, expression: Expression) {
                holder.bindExpression(expression)

                //function for clicking on the history items
                holder.itemView.setOnClickListener { Toast.makeText(this@ChatHistory, expression.toString(), Toast.LENGTH_SHORT).show() }
            }

        }
        //set the view to use the adapter
        recyclerView!!.setAdapter(mAdapter)
    }

    //start listening to database
    override fun onStart() {
        super.onStart()
        mAdapter.startListening()
    }

    //stop listening to database
    override fun onStop() {
        super.onStop()
        mAdapter.stopListening()
    }
}
