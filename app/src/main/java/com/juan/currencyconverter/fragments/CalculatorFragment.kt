package com.juan.currencyconverter.fragments

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.TypedArray
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.juan.currencyconverter.R
import kotlinx.android.synthetic.main.current_value_layout.*
import kotlinx.android.synthetic.main.spinner_layout.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import kotlin.coroutines.CoroutineContext
import android.view.*

private const val API_KEY = "USE YOUR OWN KEY"
private const val FRAGMENT_CALCULATOR_INFO_KEY = "calculator"

class CalculatorFragment : Fragment() {

    private var v: View? = null
    private var localCurrencySpinner = local_currency_spinner
    private var foreignCurrencySpinner = foreign_currency_spinner
    private var localSpinnerAdapter: MyArrayAdapter? = null
    private var foreignSpinnerAdapter: MyArrayAdapter? = null
    private var countryNameResource:Array<String>? = null
    private var flagResource: TypedArray? = null
    private var button: Button? = null
    private var parentJob: Job? = null
    private var coroutineContext: CoroutineContext? = null
    private var coroutineScope: CoroutineScope? = null
    private var resultCalculatorTextView: TextView? = null
    private var localCurrency = "AED"
    private var foreignCurrency = "AED"
    private var editText:EditText? = null
    private var multiplier = 1.0
    private var mMyData:String? = null
    private var localPos:Int = 0
    private var foreignPos:Int = 0
    private var queryData:String? = null
    private var sharedPreferences: SharedPreferences? = null
    private var editor:SharedPreferences.Editor? = null
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        v = inflater.inflate(R.layout.calculator_layout, container, false)
        configCoroutine()
        configSharedPreferences()
        configTexts()
        configButton()
        configSpinners()
        return v
    }

    override fun onResume() {
        super.onResume()
        mMyData = sharedPreferences!!.getString(FRAGMENT_CALCULATOR_INFO_KEY,"")

        if (!mMyData.equals(""))
        {
            localCurrency = mMyData!!.split("-")[0]
            foreignCurrency = mMyData!!.split("-")[1]
            multiplier = mMyData!!.split("-")[2].toDouble()
            localPos = mMyData!!.split("-")[3].toInt()
            foreignPos = mMyData!!.split("-")[4].toInt()
            queryData = mMyData!!.split("-")[5]

            localCurrencySpinner.setSelection(getCountry(countryNameResource!![localPos]))
            foreignCurrencySpinner.setSelection(getCountry(countryNameResource!![foreignPos]))
            editText!!.run { setText(multiplier.toString()) }

            resultCalculatorTextView!!.text = queryData
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mMyData = "$localCurrency-$foreignCurrency-$multiplier-$localPos-$foreignPos-$queryData"
        editor!!.putString(FRAGMENT_CALCULATOR_INFO_KEY,mMyData)
        editor!!.apply()
    }

    private fun configCoroutine()
    {
        parentJob = Job()
        coroutineContext = parentJob!!+ Dispatchers.IO
        coroutineScope = CoroutineScope(coroutineContext!!)
    }

    private fun configSharedPreferences()
    {
        sharedPreferences = context!!.getSharedPreferences(FRAGMENT_CALCULATOR_INFO_KEY,Context.MODE_PRIVATE)
        editor = sharedPreferences!!.edit()
    }

    private fun configTexts()
    {

        editText = v!!.findViewById(R.id.calculator_amount_edit_text)
        resultCalculatorTextView = v!!.findViewById(R.id.result_calculator_text_view)
    }

    private fun configButton()
    {
        button = v!!.findViewById(R.id.calculator_convert_button)
        button!!.setOnClickListener {
            coroutineScope!!.launch {
                queryData = getOneToOneCurrency("https://free.currconv.com/api/v7/convert?q=${localCurrency}_$foreignCurrency&compact=ultra&apiKey=$API_KEY")
                GlobalScope.launch(Dispatchers.Main) {
                    resultCalculatorTextView!!.text = queryData
                    (activity!!.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(view!!.rootView.windowToken,0)
                }
            }
        }
    }

    private fun configSpinners()
    {
        countryNameResource = context!!.resources!!.getStringArray(R.array.countries_and_currency_name_array)
        flagResource = context!!.resources.obtainTypedArray(R.array.flag_resources_array)

        localCurrencySpinner = v!!.findViewById(R.id.calculator_origin_currency)
        localCurrencySpinner.isHorizontalScrollBarEnabled = true
        localSpinnerAdapter = MyArrayAdapter(context!!,countryNameResource!!,flagResource!!)
        localCurrencySpinner.adapter = localSpinnerAdapter

        foreignCurrencySpinner = v!!.findViewById(R.id.calculator_destination_currency)
        foreignCurrencySpinner.isHorizontalScrollBarEnabled = true
        foreignSpinnerAdapter = MyArrayAdapter(context!!,countryNameResource!!,flagResource!!)
        foreignCurrencySpinner.adapter = foreignSpinnerAdapter

        localCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                localCurrency = localSpinnerAdapter!!.getView(position,view,parent).text_spinner_item.text.toString().split(" - ")[0]
                localPos=position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
        foreignCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                foreignCurrency = foreignSpinnerAdapter!!.getView(position,view,parent).text_spinner_item.text.toString().split(" - ")[0]
                foreignPos=position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun getCountry(stri: String) : Int
    {
        var pos = 0
        for (i in 0 until countryNameResource!!.size-1)
        {
            if (countryNameResource!![i] == stri)
            {
                pos = i
            }
        }
        return pos
    }

    private fun getOneToOneCurrency(queryString:String) : String
    {

        val jsonAnswer= URL(queryString).readText()
        var inter = editText!!.run { text.toString() }
        if (inter.split(".").size == 1)
            inter =inter.plus(".00")

        multiplier = if (inter.equals("") || inter.toDouble() <= 0.0)
                        1.0
                    else
                    {
                        (inter.split(".")[0]
                            .plus(".")
                            .plus(inter.split(".")[1].let {
                                when (it.length)
                                {
                                    0 ->it.plus("00")
                                    1 ->it.plus("0")
                                    else -> it
                                }
                            })).toDouble()
                    }


        return "$multiplier $localCurrency\n=\n${String.format("%.2f",jsonToText(jsonAnswer,multiplier).toFloat())} $foreignCurrency"
    }

    private fun jsonToText(json:String, _multiplier:Double) = (_multiplier * JSONObject(json).getString("${localCurrency}_$foreignCurrency").toDouble()).toString()



}