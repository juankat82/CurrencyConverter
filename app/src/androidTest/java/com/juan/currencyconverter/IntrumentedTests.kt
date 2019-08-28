package com.juan.currencyconverter

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.filters.LargeTest
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.GrantPermissionRule
import com.google.android.material.tabs.TabLayout
import com.juan.currencyconverter.fragments.CalculatorFragment
import com.juan.currencyconverter.fragments.OneToOneFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.core.AllOf
import org.jetbrains.annotations.NotNull
import org.json.JSONObject
import org.junit.After
import org.junit.Test
import java.net.URL

@RunWith(AndroidJUnit4ClassRunner::class)
@LargeTest
class IntrumentedTests {

    private var mContext: Context? = null
    private var mSharedPreferences:SharedPreferences? = null
    private var fragment1 = OneToOneFragment()
    private var fragment2 = CalculatorFragment()
    private val fragmentLayoutId = R.id.fragment_base_layout
    private var transaction:FragmentTransaction? = null

    @get:Rule
    val mMainActivity = ActivityTestRule(MainActivity::class.java,false,false)


    class EspressoTestingIdlingResources {

        companion object {

            val RESOURCE = "GLOBAL"
            val mCountingIdlingResource = CountingIdlingResource(RESOURCE)
            fun getIdlingResources() = mCountingIdlingResource

            const val FRAGMENT_INFO_KEY = "MyBundle"
            const val API_KEY = "GET_YOUR_OWN_KEY"
            const val FRAGMENT_CALCULATOR_INFO_KEY = "calculator"
            const val ONETOONE_FRAGMENT_KEY = "one_to_one"
        }
    }

    @Before
    fun init()
    {
        mContext = InstrumentationRegistry.getInstrumentation().context
        mSharedPreferences=mContext!!.getSharedPreferences("com.juan.currencyconverter", Context.MODE_PRIVATE)
        mMainActivity.launchActivity(null)
        transaction = mMainActivity.activity.supportFragmentManager.beginTransaction()
    }

    @Before
    fun registerEspressoIdlingResources()
    {
        IdlingRegistry.getInstance().register(EspressoTestingIdlingResources.getIdlingResources())
    }

    @Before
    fun grantPermissionsTest()
    {
        GrantPermissionRule.grant(android.Manifest.permission.INTERNET)
        GrantPermissionRule.grant(android.Manifest.permission.ACCESS_NETWORK_STATE)
    }


    @After
    fun unregisterEspressoIdlingResources()
    {
        IdlingRegistry.getInstance().unregister(EspressoTestingIdlingResources.getIdlingResources())
    }

    @Test
    fun testAppOrientation()
    {
        mMainActivity.activity.requestedOrientation = Configuration.ORIENTATION_PORTRAIT
        Thread.sleep(200)
        mMainActivity.activity.requestedOrientation = Configuration.ORIENTATION_LANDSCAPE
        Thread.sleep(200)
    }

    @Test
    fun testReplaceFragments()
    {


        mMainActivity.activity.replaceFragment(fragment1,fragmentLayoutId)
        Thread.sleep(200)
        mMainActivity.activity.replaceFragment(fragment2,fragmentLayoutId)
        Thread.sleep(200)
    }

    @Test
    fun testTabLayout()
    {
        val tabLayout0:TabLayout.Tab = mMainActivity.activity.tabLayout!!.getTabAt(0) as TabLayout.Tab
        val tabLayout1:TabLayout.Tab = mMainActivity.activity.tabLayout!!.getTabAt(0) as TabLayout.Tab
        tabLayout0.select()
        tabLayout1.select()

    }

    private fun FragmentManager.inTransacion(func: FragmentTransaction.() -> Unit) {
        val fragmentTransaction = beginTransaction()
        fragmentTransaction.func()
        fragmentTransaction.commit()
    }

    private fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int)
    {
        supportFragmentManager.inTransacion { replace(frameId, fragment) }
    }

    @Test
    fun testCurrentExchange()
    {
        onView(withId(R.id.tablayout)).check(matches(isDisplayed()))
        onView(withId(R.id.fragment_base_layout)).check(matches(isDisplayed()))

        onView(withId(R.id.check_your_price_title)).check(matches(withText(mMainActivity.activity.getString(R.string.get_value_title_text))))
        onView(withId(R.id.local_currency_text_view)).check(matches(withText(mMainActivity.activity.getString(R.string.local_currency_text))))
        onView(withId(R.id.convert_button)).perform(click())
        Thread.sleep(1000)
    }

    private fun writeText(text: Any) : ViewAction
    {
        return object: ViewAction {
            override fun getDescription() = "Writing to textview"

            override fun getConstraints(): Matcher<View> {
                return allOf(isDisplayed(), isAssignableFrom(TextView::class.java))
            }

            override fun perform(uiController: UiController?, view: View?) {
                (view as TextView).text = text.toString()
            }
        }
    }

    @Test
    fun testConnectivity()
    {
        Log.i("Conversion is: ",getOneToOneCurrency())
    }

    private fun getOneToOneCurrency() : String
    {
        val url = "https://free.currconv.com/api/v7/convert?q=AED_GNF&compact=ultra&apiKey=GET YOUR OWN KEY"
        Log.i("URL is: ",url)
        val jsonAnswer= URL(url).readText()
        return "100 AED\n=\n${String.format("%.2f",jsonToText(jsonAnswer).toFloat())} GNF"
    }

    private fun jsonToText(json:String) = (100 * JSONObject(json).getString("AED_GNF").toDouble()).toString()
}
