package com.gan.kotlin.distributedcalc.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.gan.kotlin.distributedcalc.model.Expression
import kotlinx.android.synthetic.main.card_layout.view.*

class ExpressionViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {

    //binds data appropriately
    fun bindExpression(Expression: Expression?) {
        with(Expression!!) {
            itemView.card_body.text = "Expression: " + body
            itemView.card_result.text = "Result: " + result
        }
    }
}