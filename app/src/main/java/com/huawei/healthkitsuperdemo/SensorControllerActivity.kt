package com.huawei.healthkitsuperdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.hihealth.BleController
import com.huawei.hms.hihealth.HiHealthOptions
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.SensorsController
import com.huawei.hms.hihealth.data.*
import com.huawei.hms.hihealth.options.BleScanCallback
import com.huawei.hms.hihealth.options.DataCollectorsOptions
import com.huawei.hms.hihealth.options.OnSamplePointListener
import com.huawei.hms.hihealth.options.SensorOptions
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import java.util.*
import java.util.concurrent.TimeUnit


class SensorControllerActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    private lateinit var getSensorControllerBtn : AppCompatButton
    private lateinit var registerStepsBtn : AppCompatButton
    private lateinit var unregisterStepsBtn : AppCompatButton
    private lateinit var scanHeartRateDevicesBtn : AppCompatButton
    private lateinit var stopScanningBtn : AppCompatButton
    private lateinit var saveHeartRateDeviceBtn : AppCompatButton
    private lateinit var listMatchedDevicesBtn : AppCompatButton
    private lateinit var findDataCollectorsBtn : AppCompatButton
    private lateinit var removeHeartRateDeviceBtn : AppCompatButton
    private lateinit var registerHeartRateBtn : AppCompatButton
    private lateinit var unregisterHeartRateBtn : AppCompatButton

    // Create a SensorsController object to obtain data.
    private var sensorsController: SensorsController? = null

    // Create a BleController object to scan external Bluetooth devices.
    private var bleController: BleController? = null

    // Create a BleDeviceInfo object to temporarily store the scanned devices.
    private var bleDeviceInfo: BleDeviceInfo? = null

    // Create a DataCollector object to temporarily store the external Bluetooth devices.
    private var dataCollector: DataCollector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_controller)

        // Obtain SensorsController and BleControllerObtain first when accessing the UI.
        val options = HiHealthOptions.builder().build()

        // Sign in to the HUAWEI ID.
        val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)

        // Obtain the SensorsController and BleController.
        sensorsController = HuaweiHiHealth.getSensorsController(this, signInHuaweiId)
        bleController = HuaweiHiHealth.getBleController(this, signInHuaweiId)

        initView()
    }

    companion object {

        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, SensorControllerActivity::class.java))
            }
    }

    private fun initView(){

        initToolbar()

        getSensorControllerBtn = findViewById(R.id.getSensorController)
        registerStepsBtn = findViewById(R.id.registerSteps)
        unregisterStepsBtn = findViewById(R.id.unregisterSteps)
        scanHeartRateDevicesBtn = findViewById(R.id.scanHeartRateDevices)
        stopScanningBtn = findViewById(R.id.stopScanning)
        saveHeartRateDeviceBtn = findViewById(R.id.saveHeartRateDevice)
        listMatchedDevicesBtn = findViewById(R.id.listMatchedDevices)
        findDataCollectorsBtn = findViewById(R.id.findDataCollectors)
        removeHeartRateDeviceBtn = findViewById(R.id.removeHeartRateDevice)
        registerHeartRateBtn = findViewById(R.id.registerHeartRate)
        unregisterHeartRateBtn = findViewById(R.id.unregisterHeartRate)

        getSensorControllerBtn.setOnClickListener {
            getSensorController()
        }

        registerStepsBtn.setOnClickListener {
            registerSteps()
        }

        unregisterStepsBtn.setOnClickListener {
            unregisterSteps()
        }

        scanHeartRateDevicesBtn.setOnClickListener {
            scanHeartRateDevices()
        }

        stopScanningBtn.setOnClickListener {
            stopScanning()
        }

        saveHeartRateDeviceBtn.setOnClickListener {
            saveHeartRateDevice()
        }

        listMatchedDevicesBtn.setOnClickListener {
            listMatchedDevices()
        }

        findDataCollectorsBtn.setOnClickListener {
            findDataCollectors()
        }

        removeHeartRateDeviceBtn.setOnClickListener {
            removeHeartRateDevice()
        }

        registerHeartRateBtn.setOnClickListener {
            registerHeartRate()
        }

        unregisterHeartRateBtn.setOnClickListener {
            unregisterHeartRate()
        }
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    /**
     * Sign in to your HUAWEI ID to obtain SensorsController and BleCtronller first.
     */
    private fun getSensorController() {
        val options = HiHealthOptions.builder().build()

        // Sign in to the HUAWEI ID.
        val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)

        // Obtain the SensorsController and BleController.
        sensorsController = HuaweiHiHealth.getSensorsController(this, signInHuaweiId)
        bleController = HuaweiHiHealth.getBleController(this, signInHuaweiId)
    }

    /**
     * Create a listener object to receive step count reports.
     */
    private val onSamplePointListener =
        OnSamplePointListener { samplePoint -> // The step count, time, and type data reported by the pedometer is called back to the app through
            // samplePoint.
            showSamplePoint(samplePoint)
        }

    /**
     * Register a listener to obtain the step count from the phone.
     */
    private fun registerSteps() {
        if (sensorsController == null) {
            Toast.makeText(this, "SensorsController is null", Toast.LENGTH_SHORT).show()
            return
        }

        // Build a SensorsOptions object to pass the data type (which is the total step count in this case).
        val builder = SensorOptions.Builder()
        builder.setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL)

        // Register a listener and adding the callback for registration success and failure.
        sensorsController!!.register(builder.build(), onSamplePointListener)
            .addOnSuccessListener {
                Toast.makeText(this, "registerSteps successed... ", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "registerSteps failed... ", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Unregister the listener for the step count.
     */
    private fun unregisterSteps() {
        if (sensorsController == null) {
            Toast.makeText(this, "SensorsController is null", Toast.LENGTH_SHORT).show()
            return
        }

        // Unregister the listener for the step count.
        sensorsController!!.unregister(onSamplePointListener).addOnSuccessListener {
            Toast.makeText(this, "unregisterSteps successed ...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, "unregisterSteps failed ...", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Enable Bluetooth scanning to scan for external Bluetooth devices capable of monitoring the heart rate.
     */
    private fun scanHeartRateDevices() {
        if (bleController == null) {
            Toast.makeText(this, "BleController is null", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Scanning devices started", Toast.LENGTH_SHORT).show()

        // Pass the heart rate data type as an array. Multiple data types can be passed at a time. The scanning time is
        // set to 15 seconds.
        bleController!!.beginScan(listOf(DataType.DT_INSTANTANEOUS_HEART_RATE), 15, mBleCallback)
    }

    /**
     * Forcibly stop Bluetooth scanning.
     */
    private fun stopScanning() {
        if (bleController == null) {
            Toast.makeText(this, "BleController is null", Toast.LENGTH_SHORT).show()
            return
        }
        bleController!!.endScan(mBleCallback)
    }

    /**
     * Bluetooth scanning callback object
     */
    private val mBleCallback: BleScanCallback = object : BleScanCallback() {
        override fun onDeviceDiscover(bleDeviceInfo: BleDeviceInfo) {
            // Bluetooth devices detected during the scanning will be called back to the bleDeviceInfo object
            Toast.makeText(this@SensorControllerActivity, "onDeviceDiscover : " + bleDeviceInfo.deviceName, Toast.LENGTH_SHORT).show()

            // Save the scanned heart rate devices to the variables for later use.
            this@SensorControllerActivity.bleDeviceInfo = bleDeviceInfo
        }

        override fun onScanEnd() {
            Toast.makeText(this@SensorControllerActivity, "onScanEnd  Scan called", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Save the scanned heart rate devices to the local device for the listener that will be registered later to obtain
     * data.
     */
    private fun saveHeartRateDevice() {
        if (bleController == null || bleDeviceInfo == null) {
            Toast.makeText(this, "BleController or BleDeviceInfo is null", Toast.LENGTH_SHORT).show()
            return
        }
        bleController!!.saveDevice(bleDeviceInfo).addOnSuccessListener {
            Toast.makeText(this, "saveHeartRateDevice successed... ", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "saveHeartRateDevice failed... ", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * List all external Bluetooth devices that have been saved to the local device.
     */
    private fun listMatchedDevices() {
        if (bleController == null) {
            Toast.makeText(this, "SensorsController is null", Toast.LENGTH_SHORT).show()
            return
        }
        val bleDeviceInfoTask = bleController!!.savedDevices

        bleDeviceInfoTask.addOnSuccessListener { bleDeviceInfos -> // bleDeviceInfos contains the list of the saved devices.
            for (bleDeviceInfo in bleDeviceInfos) {
                Toast.makeText(this, "Matched BLE devices:" + bleDeviceInfo.deviceName, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Find available data collectors from the saved devices in the list.
     */
    private fun findDataCollectors() {
        if (sensorsController == null) {
            Toast.makeText(this, "SensorsController is null", Toast.LENGTH_SHORT).show()
            return
        }

        // Build a DataCollectorsOptions object and passing the type of device we are looking for (which is heart rate
        // devices in this case).
        val dataCollectorsOptions = DataCollectorsOptions.Builder().setDataTypes(DataType.DT_INSTANTANEOUS_HEART_RATE).build()

        // Use dataCollectorsOptions as a parameter to return available heart rate devices.
        sensorsController!!.getDataCollectors(dataCollectorsOptions)
            .addOnSuccessListener { dataCollectors ->
                // dataCollectors contains the returned available data collectors.
                for (dataCollector in dataCollectors) {
                    Toast.makeText(this, "Available data collector:" + dataCollector.dataCollectorName, Toast.LENGTH_SHORT).show()

                    // Save the heart rate data collectors for later use when registering the listener.
                    this@SensorControllerActivity.dataCollector = dataCollector
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "findDataCollectors failed... ", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Register a listener for the heart rate device.
     */
    private fun registerHeartRate() {
        if (sensorsController == null) {
            Toast.makeText(this, "SensorsController is null", Toast.LENGTH_SHORT).show()
            return
        }
        sensorsController!!.register( // Build a SensorsOptions object and passing the data type, data collectors, and sampling rate.
            // The data type is mandatory.
            SensorOptions.Builder()
                .setDataType(DataType.DT_INSTANTANEOUS_HEART_RATE)
                .setDataCollector(dataCollector) // Set the sampling rate to 1 second.
                .setCollectionRate(1, TimeUnit.SECONDS)
                .build(),
            heartrateListener
        ).addOnSuccessListener(OnSuccessListener<Void?> {
            Toast.makeText(this, "registerHeartRate successed... ", Toast.LENGTH_SHORT).show()
        }).addOnFailureListener(OnFailureListener {
            Toast.makeText(this, "registerHeartRate failed... ", Toast.LENGTH_SHORT).show()
        })
    }

    /**
     * Create a listener object for heart rate data. The received heart rate data will be called back to onSamplePoint.
     */
    private val heartrateListener =
        OnSamplePointListener { samplePoint ->
            Toast.makeText(this, "Heart rate received " + samplePoint.getFieldValue(Field.FIELD_BPM), Toast.LENGTH_SHORT).show()
        }

    /**
     * Unregister the listener for the heart rate data.
     */
    private fun unregisterHeartRate() {
        if (sensorsController == null) {
            Toast.makeText(this, "SensorsController is null", Toast.LENGTH_SHORT).show()
            return
        }
        sensorsController!!.unregister(heartrateListener)
            .addOnSuccessListener {
                Toast.makeText(this, "unregisterHeartRate successed... ", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "unregisterHeartRate failed... ", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Delete the heart rate device information that has been saved.
     */
    private fun removeHeartRateDevice() {
        if (bleController == null || bleDeviceInfo == null) {
            Toast.makeText(this, "BleController or BleDeviceInfo is null", Toast.LENGTH_SHORT).show()
            return
        }

        // Pass the saved Bluetooth device information object to delete the information.
        bleController!!.deleteDevice(bleDeviceInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "removeHeartRateDevice successed... ", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "removeHeartRateDevice failed... ", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * Print the SamplePoint in the SampleSet object as an output.
     *
     * @param samplePoint Reported data
     */
    private fun showSamplePoint(samplePoint: SamplePoint?) {
        if (samplePoint != null) {

            runOnUiThread(Runnable {
                Toast.makeText(this, "Sample point type: " + samplePoint.dataType.name, Toast.LENGTH_SHORT).show()
            })

            for (field in samplePoint.dataType.fields) {

                runOnUiThread(Runnable {
                    Toast.makeText(this, "Field: " + field.name + " Value: " + samplePoint.getFieldValue(field), Toast.LENGTH_SHORT).show()
                })

            }
        } else {
            runOnUiThread(Runnable {
                Toast.makeText(this, "samplePoint is null!!", Toast.LENGTH_SHORT).show()
            })

        }
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager: InputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
