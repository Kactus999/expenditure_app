package com.example.expenditureapp

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.expenditureapp.databinding.FragmentSummaryBinding
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter


/**
 * A simple [Fragment] subclass.
 * Use the [SummaryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SummaryFragment : Fragment() {
    private var binding:FragmentSummaryBinding? = null
    private lateinit var prefs: SharedPreferences
    private lateinit var appContext: Context
    private lateinit var walletName:String
    private val deposit = "Deposit"
    private val withdraw = "Withdraw"
    private lateinit var helper: SQLiteOpenHelper
    private lateinit var db : SQLiteDatabase
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSummaryBinding.inflate(inflater,container,false)
        appContext = requireContext().applicationContext
        helper = UserDBHelper(requireContext().applicationContext)
        db = helper.readableDatabase
        prefs = appContext.getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE)
        walletName = prefs.getString("WalletName","").toString()

        getMonthYearSetSpinner()

        return binding?.root

    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    /*private fun getMonthYearSetSpinner()
    {
        val monthYearList = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT strftime('%m', TRANS_DATE) AS month, strftime('%Y', TRANS_DATE) AS year FROM TRANSACTIONS WHERE WALLET LIKE '%${walletName}' ", null)
        if (cursor.moveToFirst()) {
            val monthIndex = cursor.getColumnIndex("month")
            val yearIndex = cursor.getColumnIndex("year")

            while (!cursor.isAfterLast) {
                val month = if (monthIndex != -1) cursor.getString(monthIndex) else ""
                val year = if (yearIndex != -1) cursor.getString(yearIndex) else ""

                // Do something with the month and year values
                Toast.makeText(appContext, month, Toast.LENGTH_SHORT).show()
                cursor.moveToNext()
            }
        }

    }*/

    private fun getMonthYearSetSpinner()
    {
        data class MonthYear(val month: String, val year: String)
        val cursor = db.rawQuery("SELECT strftime('%m', TRANS_DATE) AS month, strftime('%Y', TRANS_DATE) AS year FROM TRANSACTIONS WHERE WALLET LIKE '%${walletName}' ", null)
        val monthYearList = mutableListOf<MonthYear>()
        val uniqueMonthYearList = mutableListOf<MonthYear>()
        val uniqueMonths = HashSet<String>()

        if (cursor.moveToFirst()) {
            val monthIndex = cursor.getColumnIndex("month")
            val yearIndex = cursor.getColumnIndex("year")

            while (!cursor.isAfterLast) {
                val month = if (monthIndex != -1) cursor.getString(monthIndex) else ""
                val year = if (yearIndex != -1) cursor.getString(yearIndex) else ""

                val monthYear = MonthYear(month, year)
                monthYearList.add(monthYear)

                cursor.moveToNext()
            }
        }

        for (monthYear in monthYearList) {
            val month = monthYear.month
            val year = monthYear.year
            if (!uniqueMonths.contains(month)) {
                uniqueMonths.add(month)
                uniqueMonthYearList.add(monthYear)
            }
        }

        val list = mutableListOf<String>()
        for (item in uniqueMonthYearList)
        {
            list.add("${item.month}/${item.year}")
        }

        val spinnerAdapter = ArrayAdapter(appContext,R.layout.drop_down_item,list)
        binding?.spDate?.setAdapter(spinnerAdapter)

        binding?.spDate?.setOnItemClickListener { parent, view, position, id ->
            val monthYear = binding?.spDate?.text.toString()
            loadDepositPieChartData(monthYear)
            loadWithdrawPieChartData(monthYear)
        }


    }

    private fun loadWithdrawPieChartData(monthYear: String) {
        val wallet = walletName
        val type = "Withdraw"
        val parts = monthYear.split("/")
        val year = parts[1]
        val month = parts[0]
        val convertedMonthYear = "${year}-${month}"
        val monthInt = month.toInt()
        val whereClause = "strftime('%Y-%m', TRANS_DATE) = ? AND WALLET = ? AND TYPE = ?"

        val cursor = db.query(
            "TRANSACTIONS",
            null,
            whereClause,
            arrayOf(convertedMonthYear, wallet, type),
            "CATEGORY",
            null,
            null
        )

        val entries = mutableListOf<PieEntry>()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val category = cursor.getString(3)
                    val sum = cursor.getFloat(1)
                    entries.add(PieEntry(sum, category))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }

        if (entries.isNotEmpty())
        {
            val dataSet = PieDataSet(entries, "Withdraw Pie Chart of month $month")
            dataSet.colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
            dataSet.valueTextColor = Color.BLACK
            dataSet.valueTextSize = 14f
            dataSet.valueFormatter = VNDValueFormatter()

            val data = PieData(dataSet)
            binding?.pieChartWithdraw?.data = data
            binding?.pieChartWithdraw?.description?.isEnabled = false
            binding?.pieChartWithdraw?.setUsePercentValues(false)
            binding?.pieChartWithdraw?.animateY(1000)
            binding?.pieChartWithdraw?.centerText = "Withdraw"
            binding?.pieChartWithdraw?.visibility = View.VISIBLE
            binding?.pieChartWithdraw?.setHoleColor(Color.parseColor("#060D0D"))
            binding?.pieChartWithdraw?.setCenterTextSize(24f)
            binding?.pieChartWithdraw?.setCenterTextColor(Color.parseColor("#D6BD68"))
            binding?.pieChartWithdraw?.legend?.textSize = 12f
            val customTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            binding?.pieChartWithdraw?.setCenterTextTypeface(customTypeface)
        }
    }

    private fun loadDepositPieChartData(monthYear: String) {
        val wallet = walletName
        val type = "Deposit"
        val parts = monthYear.split("/")
        val year = parts[1]
        val month = parts[0]
        val convertedMonthYear = "${year}-${month}"
        val monthInt = month.toInt()
        val whereClause = "strftime('%Y-%m', TRANS_DATE) = ? AND WALLET = ? AND TYPE = ?"

        val cursor = db.query(
            "TRANSACTIONS",
            null,
            whereClause,
            arrayOf(convertedMonthYear, wallet, type),
            "CATEGORY",
            null,
            null
        )

        val entries = mutableListOf<PieEntry>()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val category = cursor.getString(3)
                    val sum = cursor.getFloat(1)
                    entries.add(PieEntry(sum, category))
                } while (cursor.moveToNext())
            }
            cursor.close()
        }


        if (entries.isNotEmpty())
        {
            val dataSet = PieDataSet(entries, "Deposit Pie Chart of month $month")
            dataSet.colors = listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW)
            dataSet.valueTextColor = Color.BLACK
            dataSet.valueTextSize = 14f
            dataSet.valueFormatter = VNDValueFormatter()

            val data = PieData(dataSet)
            binding?.pieChartDeposit?.data = data
            binding?.pieChartDeposit?.description?.isEnabled = false
            binding?.pieChartDeposit?.setUsePercentValues(false)
            binding?.pieChartDeposit?.animateY(1000)
            binding?.pieChartDeposit?.centerText = "DEPOSIT"
            binding?.pieChartDeposit?.setCenterTextColor(Color.parseColor("#D6BD68"))
            binding?.pieChartDeposit?.setCenterTextSize(24f)
            binding?.pieChartDeposit?.visibility = View.VISIBLE
            binding?.pieChartDeposit?.setHoleColor(Color.parseColor("#060D0D"))
            binding?.pieChartDeposit?.legend?.textSize = 12f
            val customTypeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            binding?.pieChartDeposit?.setCenterTextTypeface(customTypeface)
        }
    }

    class VNDValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return "${value}VNĐ"
        }
    }


}