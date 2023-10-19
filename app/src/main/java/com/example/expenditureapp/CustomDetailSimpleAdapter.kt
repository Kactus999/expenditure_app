package com.example.expenditureapp

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleCursorAdapter
import com.example.expenditureapp.databinding.DepositListItemBinding
import java.text.SimpleDateFormat

class CustomDetailSimpleAdapter(context: Context, layout: Int, c : Cursor,
                                 from : Array<String>, to : IntArray, flag : Int ) : SimpleCursorAdapter(context,layout,c,from,to,flag)
{

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return super.getView(position, convertView, parent)
    }

    override fun newView(context: Context, cursor: Cursor, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val binding = DepositListItemBinding.inflate(inflater,parent,false)
        binding.root.tag = binding
        return binding.root
    }

    override fun bindView(view: View, context: Context, cursor: Cursor) {
        val binding = view.tag as DepositListItemBinding
        val amount = cursor.getString(1)
        val catagory = cursor.getString(3)
        var inputDate = cursor.getString(4)
        val type = cursor.getString(2)
        val inputFormat = SimpleDateFormat("yyyy-MM-dd")
        val outputFormat = SimpleDateFormat("dd/MM/yyyy")
        val date = inputFormat.parse(inputDate)
        val formattedDate = outputFormat.format(date)

        if (type.equals("Deposit"))
        {
            binding.listAmount.text = "+ " + amount
            binding.listAmount.setTextColor(Color.parseColor("#598C58"))
            binding.lvImage.setColorFilter(Color.parseColor("#D6BD68"))
        }
        else{
            binding.listAmount.text = "- " + amount
            binding.listAmount.setTextColor(Color.parseColor("#ffff4444"))
            binding.lvImage.setImageResource(R.drawable.icon_withdraw)
        }

        binding.listCategory.text = catagory
        binding.listTime.text = formattedDate
    }


}