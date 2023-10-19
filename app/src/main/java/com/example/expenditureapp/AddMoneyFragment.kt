package com.example.expenditureapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.Cursor
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.CursorAdapter
import android.widget.Toast
import com.example.expenditureapp.databinding.FragmentAddMoneyBinding


/**
 * A simple [Fragment] subclass.
 * Use the [AddMoneyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddMoneyFragment : Fragment() {

    private var binding:FragmentAddMoneyBinding? = null
    private lateinit var prefs:SharedPreferences
    private lateinit var appContext:Context
    private lateinit var walletName:String
    private lateinit var rs: Cursor
    private lateinit var adapterDeposit:CustomSimpleAdapter
    private lateinit var adapterWithDraw : CustomSimpleAdapter
    private val deposit = "Deposit"
    private val withdraw = "Withdraw"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddMoneyBinding.inflate(inflater,container,false)
        appContext = requireContext().applicationContext
        val helper = UserDBHelper(requireContext().applicationContext)
        val db = helper.readableDatabase
        prefs = appContext.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
        walletName = prefs.getString("WalletName","").toString()
        rs = db.rawQuery("SELECT * FROM USERS WHERE WALLET LIKE '%${walletName}'  ",null)

        updateBaseMoney()

        binding?.tvGreeting?.text =  "${binding?.tvGreeting?.text} ${prefs.getString("WalletName","")}"
        binding?.btnDeposit?.setOnClickListener {
            val intent = Intent(appContext,DepositActivity::class.java)
            startActivity(intent)
        }
        setupLVDeposit()

        binding?.btnWithdraw?.setOnClickListener {
            val intent = Intent(appContext,WithdrawActivity::class.java)
            startActivity(intent)
        }

        setupLVWithdraw()



        return binding?.root
    }


    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }


    override fun onResume() {
        super.onResume()
        val helper = UserDBHelper(requireContext().applicationContext)
        val db = helper.readableDatabase
        rs = db.rawQuery("SELECT * FROM USERS WHERE WALLET LIKE '%${walletName}'  ",null)
        updateBaseMoney()

        val rsDeposit = db.rawQuery("SELECT * FROM TRANSACTIONS WHERE WALLET LIKE '%${walletName}' and TYPE LIKE '%${deposit}' ORDER BY _Id DESC  ",null)

        if (this::adapterDeposit.isInitialized)
        {
            adapterDeposit.changeCursor(rsDeposit)
            adapterDeposit.notifyDataSetChanged()
        }

        val rsWithdraw = db.rawQuery("SELECT * FROM TRANSACTIONS WHERE WALLET LIKE '%${walletName}' and TYPE LIKE '%${withdraw}' ORDER BY _Id DESC  ",null)

        if ( this::adapterWithDraw.isInitialized)
        {
            adapterWithDraw.changeCursor(rsWithdraw)
            adapterWithDraw.notifyDataSetChanged()
        }

    }

    private fun updateBaseMoney()
    {
        if (rs.moveToFirst()){
            binding?.tvTotalMoney?.text = rs.getString(2) + " VNĐ"
        }
    }

    private fun setupLVDeposit()
    {
        val helper = UserDBHelper(requireContext().applicationContext)
        val db = helper.readableDatabase
        val type = "Deposit"
        val rs2 = db.rawQuery("SELECT * FROM TRANSACTIONS WHERE WALLET LIKE '%${walletName}' and TYPE LIKE '%${type}' ORDER BY _Id DESC  ",null)

        if (rs2.moveToFirst())
        {
            adapterDeposit = CustomSimpleAdapter(appContext, R.layout.deposit_list_item, rs2, arrayOf("AMOUNT","CATEGORY","TRANS_DATE"),
                intArrayOf(R.id.listAmount,R.id.listCategory,R.id.listTime),0
            )
            binding?.lvDeposit?.adapter = adapterDeposit

           binding?.lvDeposit?.onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    val intent = Intent(appContext, DetailDepositActivity::class.java)
                    startActivity(intent)
                }
        }


    }

    private fun setupLVWithdraw()
    {
        val helper = UserDBHelper(requireContext().applicationContext)
        val db = helper.readableDatabase
        val type = "Withdraw"
        val rs2 = db.rawQuery("SELECT * FROM TRANSACTIONS WHERE WALLET LIKE '%${walletName}' and TYPE LIKE '%${type}' ORDER BY _Id DESC  ",null)

        if (rs2.moveToFirst())
        {
            adapterWithDraw = CustomSimpleAdapter(appContext, R.layout.deposit_list_item, rs2, arrayOf("AMOUNT","CATEGORY","TRANS_DATE"),
                intArrayOf(R.id.listAmount,R.id.listCategory,R.id.listTime),0
            )
            binding?.lvWithdrawal?.adapter = adapterWithDraw

            binding?.lvWithdrawal?.onItemClickListener =
                OnItemClickListener { parent, view, position, id ->
                    val intent = Intent(appContext, DetailWithdrawActivity::class.java)
                    startActivity(intent)
                }
        }
    }
}