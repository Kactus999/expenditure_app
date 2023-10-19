package com.example.expenditureapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.expenditureapp.databinding.ActivityWithdrawBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityWithdrawBinding
class WithdrawActivity : AppCompatActivity() {
    private lateinit var calendar: Calendar
    private lateinit var prefs: SharedPreferences
    private lateinit var walletName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWithdrawBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        calendar = Calendar.getInstance()

        walletName = prefs.getString("WalletName","").toString()

        setupTypeSpinner()
        setupDatePicker()
        addDoneButtonEvent()

    }

    private fun addDoneButtonEvent() {
        binding.btnDoneDeposit.setOnClickListener {
            val amount = binding.tiedtMoney.text.toString().toDouble()
            val category = binding.spCategory.text.toString()
            val date = binding.tiedDate.text.toString()
            addTransactionToDB(walletName,amount,category,"Withdraw",date)

        }
    }

    private fun addTransactionToDB(walletName: String, amount: Double, category:String ,type: String, date: String) {
        val helper = UserDBHelper(applicationContext)
        val db = helper.readableDatabase
        val baseAmount: Double
        val tempAmount:Double
        //Add new transaction to db
        val cvInsert = ContentValues()
        cvInsert.put("AMOUNT",amount)
        cvInsert.put("TYPE",type)
        cvInsert.put("CATEGORY",category)
        cvInsert.put("TRANS_DATE",date)
        cvInsert.put("WALLET",walletName)
        db.insert("TRANSACTIONS",null,cvInsert)

        //upadte base amount
        val rs = db.rawQuery("SELECT * FROM USERS WHERE WALLET LIKE '%${walletName}'  ",null)
        if (rs.moveToFirst())
        {
            baseAmount = rs.getString(2).toString().toDouble()
            tempAmount = baseAmount - amount
            val cvUpdate = ContentValues()
            cvUpdate.put("BASE_AMOUNT",tempAmount)
            db.update("USERS",cvUpdate,"USERID = ?", arrayOf(rs.getString(0)))
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun setupDatePicker() {
        val date = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR,year)
            calendar.set(Calendar.MONTH,month)
            calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateLabel()
        }

        binding.tiedDate.setOnClickListener {
            DatePickerDialog(this,date,calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

    }

    private fun updateLabel() {
        val myFormat = "yyyy-MM-dd"       //MM is month and mm is minute
        val dateFormat = SimpleDateFormat(myFormat, Locale.UK)
        binding.tiedDate.setText(dateFormat.format(calendar.time))
    }

    private fun setupTypeSpinner() {
        val list = resources.getStringArray(R.array.category_of_withdraw)
        val adapter = ArrayAdapter(this,R.layout.drop_down_item,list)
        binding.spCategory.setAdapter(adapter)

    }
}