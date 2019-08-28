package com.juan.currencyconverter

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayout
import com.juan.currencyconverter.fragments.CalculatorFragment
import com.juan.currencyconverter.fragments.OneToOneFragment
import kotlinx.android.synthetic.main.activity_main.*

private const val FRAGMENT_INFO_KEY = "MyBundle"

class MainActivity : AppCompatActivity() {

    private var tabItem1: TabLayout.Tab? = null
    private var tabItem2: TabLayout.Tab? = null
    private var tabLayout: TabLayout? = null
    private var fragment1 = OneToOneFragment()
    private var fragment2 = CalculatorFragment()
    private val fragmentLayoutId = R.id.fragment_base_layout
    private var selectedTab = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configureTitle()
        selectInitialTab(savedInstanceState)
        configurateTabLayout()
    }

    override fun onResume() {
        super.onResume()
        if (selectedTab == 0) {
            tabItem1!!.select()
            replaceFragment(fragment1, fragmentLayoutId)
        } else {
            tabItem2!!.select()
            replaceFragment(fragment2, fragmentLayoutId)
        }
    }
    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.savedInstance = selectedTab
    }

    private fun configureTitle()
    {
        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            supportActionBar!!.title = getString(R.string.app_name)
        else
            supportActionBar!!.title = getString(R.string.app_name_land)
    }

    private fun selectInitialTab(bundle: Bundle?)
    {
        selectedTab = bundle?.savedInstance ?: 0
    }

    private fun configurateTabLayout()
    {
        tabLayout = tablayout
        tabItem1 = tabLayout?.newTab()
        tabItem1?.icon = getDrawable(R.drawable.dollar_button_state)
        tabItem1?.view!!.marginBottom.minus(3)
        tabItem1?.text = getString(R.string.current_value_string)
        tabLayout?.addTab(tabItem1!!)

        tabItem2 = tabLayout?.newTab()
        tabItem2?.icon = getDrawable(R.drawable.calculator_button_state)
        tabItem2?.text = getString(R.string.currency_calculator_text)
        tabLayout?.addTab(tabItem2!!)
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(p0: TabLayout.Tab?) { }
            override fun onTabUnselected(p0: TabLayout.Tab?) {}
            override fun onTabSelected(p0: TabLayout.Tab?) {
                when (tabLayout?.selectedTabPosition) {
                    tabItem1?.position -> {
                        selectedTab = 0
                        replaceFragment(fragment1, fragmentLayoutId)
                    }
                    tabItem2?.position -> {
                        selectedTab = 1
                        replaceFragment(fragment2, fragmentLayoutId)
                    }
                }
            }
        })
    }

    private var Bundle.savedInstance
        get() = getInt(FRAGMENT_INFO_KEY, 0)
        set(value) = putInt(FRAGMENT_INFO_KEY, value)

    private fun FragmentManager.inTransacion(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    private fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int)
    {
        supportFragmentManager.inTransacion { replace(frameId, fragment) }
    }
}
