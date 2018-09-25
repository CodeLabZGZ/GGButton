package org.imaginecode.ggbutton

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import okhttp3.*
import org.imaginecode.ggbutton.properties.AssetsProperties
import java.io.IOException


/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class GGButton : Activity() {

    private val TIME = 1000L
    private val SLEEP_THRESHOLD = 1000L

    private val PREFS_NAME = "hello_file"
    private val AMOUNT_CLICKS = "clicks"

    private lateinit var IC_URL_READ:String
    private lateinit var IC_URL_WRITE:String

    private var amount: Int = 0
    private var milestones = arrayOf(0, 100, 500, 666, 1000, 2000, 5000, 6666, 10000, 25000, 50000, 100000, 250000, 500000)
    private lateinit var counterView: TextView

    private val client = OkHttpClient()

    private lateinit var runnable : SaveRunnable
    private var lastClick = System.currentTimeMillis()

    private lateinit var currentMilestoneHolder : TextView
    private lateinit var nextMilestoneHolder : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ggbutton)

        val assetsProps = AssetsProperties(applicationContext)
        val properties = assetsProps.getProperties("production.properties")
        IC_URL_READ = properties.getProperty("icRead")
        IC_URL_WRITE = properties.getProperty("icWrite")

        counterView = findViewById(R.id.counter)

        val request = Request.Builder().url(IC_URL_READ).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    counterView.setTextColor(resources.getColor(R.color.colorRed))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {
                amount = if(response?.body() != null) {
                    Integer.parseInt(response.body()!!.string().trim())
                }else{
                    0
                }

                runOnUiThread {
                    val loadingPanel = findViewById<RelativeLayout>(R.id.loadingPanel)
                    run forEach@ {
                        milestones.forEachIndexed { index, i ->
                            if (amount < i) {
                                updateTextMilestones(milestones[index - 1], i)
                                return@forEach
                            }
                        }
                    }

                    counterView.text = amount.toString()
                    counterView.visibility = View.VISIBLE
                    loadingPanel.visibility = View.GONE
                }
                runnable.handler.postDelayed(runnable, TIME)
            }
        })

        val handler = Handler()
        runnable = SaveRunnable(handler, this)

        currentMilestoneHolder = findViewById(R.id.currentMilestone)
        nextMilestoneHolder = findViewById(R.id.nextMilestone)

    }

    fun updateTextMilestones(current: Int, next: Int) {
        currentMilestoneHolder.text = current.toString()
        nextMilestoneHolder.text = next.toString()
    }

    fun updateMilestones() {
        milestones.forEachIndexed {
            i, x ->
                if(amount == x){
                    updateTextMilestones(x, milestones[i+1])
                }
        }
    }

    fun updateClicks(){
        val request = Request.Builder().url(IC_URL_WRITE + amount).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                runOnUiThread {
                    counterView.setTextColor(resources.getColor(R.color.colorRed))
                }
            }

            override fun onResponse(call: Call?, response: Response?) {

            }
        })
    }

    fun onMainButtonClick(view: View) {
        if(System.currentTimeMillis() - lastClick > SLEEP_THRESHOLD) {
            amount++
            counterView.text = amount.toString()
            updateMilestones()

            lastClick = System.currentTimeMillis()
        }
    }

    class SaveRunnable(var handler: Handler, private var ggButton: GGButton) : Runnable {
        override fun run() {
            try {
                ggButton.updateClicks()
            } catch (e: Exception) {
            } finally {
                handler.postDelayed(this, ggButton.TIME)
            }
        }

    }
}
