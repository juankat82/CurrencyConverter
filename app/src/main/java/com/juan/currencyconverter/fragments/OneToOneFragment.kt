package com.juan.currencyconverter.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.TypedArray
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.juan.currencyconverter.R
import kotlinx.android.synthetic.main.current_value_layout.*
import kotlinx.android.synthetic.main.spinner_layout.view.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL
import kotlin.coroutines.CoroutineContext

private const val API_KEY = "USE YOUR OWN KEY"
private const val ONETOONE_FRAGMENT_KEY="one_to_one"

class OneToOneFragment : Fragment(){

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
    private var resultOneToOneTextView: TextView? = null
    private var localCurrency = "AED"
    private var foreignCurrency = "AED"
    private var queryData: String? = null
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private var mMyData:String? = null
    private var localPos:Int = 0
    private var foreignPos:Int = 0
    private var orientation = 0 //0 means portrait and 1 means landscape

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        v = inflater.inflate(R.layout.current_value_layout, container, false)
        configCoroutine()
        configSharedPreferences()
        configTexts()
        configButton()
        configSpinners()
        return v
    }

    override fun onResume() {
        super.onResume()

        mMyData = sharedPreferences!!.getString(ONETOONE_FRAGMENT_KEY,"")
        if (!mMyData.equals(""))
        {
            localCurrency = mMyData!!.split("-")[0]
            foreignCurrency = mMyData!!.split("-")[1]
            localPos = mMyData!!.split("-")[2].toInt()
            foreignPos = mMyData!!.split("-")[3].toInt()
            queryData = if (mMyData!!.split("-")[4].equals("null"))
                "1 $localCurrency\n=\n1.00 $foreignCurrency"
            else
                mMyData!!.split("-")[4]

            localCurrencySpinner.setSelection(getCountry(countryNameResource!![localPos]))
            foreignCurrencySpinner.setSelection(getCountry(countryNameResource!![foreignPos]))
            val testToTextView = "${queryData!!.split("=")[0]}=${queryData!!.split("=")[1]}"
            configTexts()
            resultOneToOneTextView!!.text = testToTextView
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mMyData = "$localCurrency-$foreignCurrency-$localPos-$foreignPos-$queryData"
        editor!!.putString(ONETOONE_FRAGMENT_KEY,mMyData)
        editor!!.apply()
    }

    private fun  configCoroutine()
    {
        parentJob = Job()
        coroutineContext = parentJob!!+Dispatchers.IO
        coroutineScope = CoroutineScope(coroutineContext!!)
    }

    private fun configSharedPreferences()
    {
        sharedPreferences = context!!.getSharedPreferences(ONETOONE_FRAGMENT_KEY,Context.MODE_PRIVATE)
        editor = sharedPreferences!!.edit()
    }

    private fun configTexts()
    {
        resultOneToOneTextView =
            if (orientation ==0) {
                v!!.findViewById(R.id.result_one_to_one_text_view)
            }
            else {
                v!!.findViewById(R.id.result_one_to_one_text_view_land)
            }
    }

    private fun configButton()
    {
        orientation = if (activity!!.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            0
        else
            1

        button = if (orientation ==0)
            v!!.findViewById(R.id.convert_button)
        else
            v!!.findViewById(R.id.convert_button_land)

        button!!.setOnClickListener {
            coroutineScope!!.launch {
                getOneToOneCurrency("https://free.currconv.com/api/v7/convert?q=${localCurrency}_$foreignCurrency&compact=ultra&apiKey=$API_KEY")
            }
        }
    }

    private fun configSpinners()
    {
        countryNameResource = context!!.resources!!.getStringArray(R.array.countries_and_currency_name_array)
        flagResource = context!!.resources.obtainTypedArray(R.array.flag_resources_array)

        localCurrencySpinner = if (orientation ==0)
                v!!.findViewById(R.id.local_currency_spinner)
            else
                v!!.findViewById(R.id.local_currency_spinner_land)

        localCurrencySpinner.isHorizontalScrollBarEnabled = true
        localSpinnerAdapter = MyArrayAdapter(context!!,countryNameResource!!,flagResource!!)
        localCurrencySpinner.adapter = localSpinnerAdapter
        localCurrencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                localCurrency = localSpinnerAdapter!!.getView(position,view,parent).text_spinner_item.text.toString().split(" - ")[0]
                localPos=position
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        foreignCurrencySpinner =
            if (orientation ==0)
                v!!.findViewById(R.id.foreign_currency_spinner)
            else
                v!!.findViewById(R.id.foreign_currency_spinner_land)//= v!!.findViewById<Spinner>(R.id.foreign_currency_spinner)
        foreignCurrencySpinner.isHorizontalScrollBarEnabled = true
        foreignSpinnerAdapter = MyArrayAdapter(context!!,countryNameResource!!,flagResource!!)
        foreignSpinnerAdapter!!.notifyDataSetChanged()
        foreignCurrencySpinner.adapter = foreignSpinnerAdapter
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

    private fun getOneToOneCurrency(queryString:String)
    {
        val jsonAnswer= URL(queryString).readText()
        queryData = "1 $localCurrency\n=\n${String.format("%.2f",jsonToText(jsonAnswer).toFloat())} $foreignCurrency"
        resultOneToOneTextView
        GlobalScope.launch(Dispatchers.Main) {
            configTexts()
            resultOneToOneTextView!!.text = queryData
        }
    }

    private fun jsonToText(json:String) = JSONObject(json).getString("${localCurrency}_$foreignCurrency")
}
