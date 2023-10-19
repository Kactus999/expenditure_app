package com.example.expenditureapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.SimpleCursorAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.expenditureapp.databinding.ActivityDetailDepositBinding
import com.example.expenditureapp.databinding.CustomUpdateDialogBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityDetailDepositBinding
class DetailDepositActivity : AppCompatActivity() {
    private lateinit var calendar: Calendar
    private lateinit var adapter : SimpleCursorAdapter
    private lateinit var prefs : SharedPreferences
    private lateinit var walletName:String
    private lateinit var cursor: Cursor
    private lateinit var dialog : AlertDialog
    private lateinit var helper: SQLiteOpenHelper
    private lateinit var db: SQLiteDatabase
    private val deposit = "Deposit"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailDepositBinding.inflate(layoutInflater)
        setContentView(binding.root)
        calendar = Calendar.getInstance()
        helper = UserDBHelper(applicationContext)
        db = helper.readableDatabase
        prefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        walletName = prefs.getString("WALLET","").toString()
        setupDetailDepositLV()

    }

    override fun onResume() {
        super.onResume()
    }

    private fun setupDetailDepositLV()
    {
        helper = UserDBHelper(applicationContext)
        db = helper.readableDatabase
        val rsTrans = db.rawQuery("SELECT * FROM TRANSACTIONS WHERE WALLET LIKE '%${walletName}' and TYPE LIKE '%${deposit}' ORDER BY date(TRANS_DATE) DESC, _Id DESC  ",null)

        if (rsTrans.moveToLast())
        {
            adapter = CustomDetailSimpleAdapter(applicationContext, R.layout.deposit_list_item, rsTrans, arrayOf("AMOUNT","CATEGORY","TRANS_DATE"),
                intArrayOf(R.id.listAmount,R.id.listCategory,R.id.listTime),0,
            )
            binding.lvDetailDeposit.adapter = adapter
        }

        binding.lvDetailDeposit.setOnItemLongClickListener { parent, view, position, id ->
            if(rsTrans.moveToPosition(position))
            {
                showDeleteDialog(rsTrans)

            }

            return@setOnItemLongClickListener true
        }

        binding.lvDetailDeposit.setOnItemClickListener { parent, view, position, id ->
            if (rsTrans.moveToPosition(position))
            {
                showUpdateDialog(rsTrans,rsTrans.getString(1),rsTrans.getString(3),rsTrans.getString(4))
            }
        }

    }

    private fun showDeleteDialog(c : Cursor) {
        val dialog = AlertDialog.Builder(this)
        dialog.apply {
            setTitle("Delete confirmation")
            setMessage("Do you want to delete this deposit")
            setNegativeButton("No"){ dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
            setPositiveButton("Yes"){ dialogInterface: DialogInterface, i: Int ->
                db.delete("TRANSACTIONS","_id = ?", arrayOf(c.getString(0)))

                val hisDepAmount = c.getString(1).toString().toDouble()

                val rsUser = db.rawQuery("SELECT * FROM USERS WHERE WALLET LIKE '%${walletName}'  ",null)
                if (rsUser.moveToFirst())
                {
                    var baseAmount = rsUser.getString(2).toString().toDouble()
                    baseAmount -= hisDepAmount
                    val cvUpdate = ContentValues()
                    cvUpdate.put("BASE_AMOUNT",baseAmount)
                    db.update("USERS",cvUpdate,"USERID = ?", arrayOf(rsUser.getString(0)))
                    Toast.makeText(this@DetailDepositActivity, "deleted", Toast.LENGTH_SHORT).show()
                    updateAdapterCursor()
                }
                dialogInterface.dismiss()
            }

        }
        dialog.create()
        dialog.show()
    }

    private fun updateAdapterCursor() {
        val rsTrans = db.rawQuery("SELECT * FROM TRANSACTIONS WHERE WALLET LIKE '%${walletName}' and TYPE LIKE '%${deposit}' ORDER BY date(TRANS_DATE) DESC, _Id DESC ",null)
        adapter.swapCursor(rsTrans)
        adapter.notifyDataSetChanged()
    }

    private fun showUpdateDialog(c : Cursor,amount : String, category : String, date : String) {
        val list = resources.getStringArray(R.array.type_of_deposit)
        val adapter = ArrayAdapter(this,R.layout.drop_down_item,list)
        val build = AlertDialog.Builder(this)
        val dialogBinding = CustomUpdateDialogBinding.inflate(LayoutInflater.from(this))
        build.setView(dialogBinding.root)
        dialogBinding.tiedtMoney.setText(amount)
        dialogBinding.spCategory.setText(category)
        dialogBinding.spCategory.setAdapter(adapter)
        dialogBinding.tiedDate.setText(date)
        setupDatePicker(dialogBinding)

        dialogBinding.btnDoneDeposit.setOnClickListener {
            if (dialogBinding.tiedtMoney.text.toString() == amount && dialogBinding.spCategory.text.toString() == category && dialogBinding.tiedDate.text.toString() == date)
            {
                Toast.makeText(this, "You didn't edit anything bro", Toast.LENGTH_SHORT).show()
            }
            else
            {
                val cvUpdateTrans = ContentValues()
                cvUpdateTrans.put("AMOUNT",dialogBinding.tiedtMoney.text.toString().toDouble())
                cvUpdateTrans.put("CATEGORY",dialogBinding.spCategory.text.toString())
                cvUpdateTrans.put("TRANS_DATE", dialogBinding.tiedDate.text.toString())
                db.update("TRANSACTIONS",cvUpdateTrans,"_id = ?" , arrayOf(c.getString(0)))
                var baseAmount:Double
                val rsUser = db.rawQuery("SELECT * FROM USERS WHERE WALLET LIKE '%${walletName}'  ",null)
                if (rsUser.moveToFirst())
                {
                    baseAmount = rsUser.getString(2).toString().toDouble()
                    baseAmount -= amount.toDouble()
                    baseAmount += dialogBinding.tiedtMoney.text.toString().toDouble()
                    val cvUpdate = ContentValues()
                    cvUpdate.put("BASE_AMOUNT",baseAmount)
                    db.update("USERS",cvUpdate,"USERID = ?", arrayOf(rsUser.getString(0)))
                    setupDetailDepositLV()
                }
                dialog.dismiss()
            }
        }
        dialog = build.create()
        dialog.show()
    }

    private fun setupDatePicker(dialogBinding: CustomUpdateDialogBinding) {
        val dateCal = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR,year)
            calendar.set(Calendar.MONTH,month)
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            val myFormat = "yyyy-MM-dd"       //MM is month and mm is minute
            val dateFormat = SimpleDateFormat(myFormat, Locale.UK)
            dialogBinding.tiedDate.setText(dateFormat.format(calendar.time))
        }

        dialogBinding.tiedDate.setOnClickListener {
            DatePickerDialog(this,dateCal,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(
                Calendar.DAY_OF_MONTH)).show()
        }

    }
}