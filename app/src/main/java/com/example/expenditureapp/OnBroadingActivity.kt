package com.example.expenditureapp

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.expenditureapp.databinding.ActivityOnBroadingBinding

@SuppressLint("StaticFieldLeak")
private lateinit var binding: ActivityOnBroadingBinding
class OnBroadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnBroadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preference: SharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
        val firstTime = preference.getString("FirstTimeInstall","")

        if (firstTime.equals("Yes"))
        {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        else{
            createWallet()
        }
    }

    private fun createWallet()
    {
        binding.btnNext.setOnClickListener {
            takeUserInput()
        }
    }

    private fun takeUserInput() {
        val walletName = binding.edtWalletName.text.toString()
        val baseAmount = binding.edtBaseAmount.text.toString()
        val preference: SharedPreferences = getSharedPreferences("PREFERENCE", MODE_PRIVATE)

        if (walletName.isNotEmpty() && baseAmount.isNotEmpty())
        {
            val editor = preference.edit()
            editor.putString("FirstTimeInstall","Yes")
            editor.putString("WalletName",walletName)
            editor.apply()
            //Create then database to store users and theirs money
            createUserDB(walletName, baseAmount.toDouble())
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        else
        {
            Toast.makeText(this, "Please enter the wallet name and base amount to continue", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createUserDB(wallet: String, baseAmount : Double)
    {
        val helper = UserDBHelper(applicationContext)
        val db = helper.readableDatabase
        var rs = db.rawQuery("SELECT * FROM USERS ",null)
        val cv = ContentValues()
        cv.put("WALLET",wallet)
        cv.put("BASE_AMOUNT",baseAmount)
        db.insert("USERS",null,cv)
    }
}