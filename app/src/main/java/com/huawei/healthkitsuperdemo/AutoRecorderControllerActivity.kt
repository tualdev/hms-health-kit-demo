package com.huawei.healthkitsuperdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.Toolbar
import com.huawei.hmf.tasks.OnCompleteListener
import com.huawei.hms.hihealth.AutoRecorderController
import com.huawei.hms.hihealth.HiHealthOptions
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.data.DataCollector
import com.huawei.hms.hihealth.data.DataType
import com.huawei.hms.support.hwid.HuaweiIdAuthManager

class AutoRecorderControllerActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    private val SPLIT = "*******************************" + System.lineSeparator()

    private lateinit var startRecordDataTypeBtn : AppCompatButton
    private lateinit var stopRecordDataTypeBtn : AppCompatButton
    private lateinit var startRecordDataCollectorBtn : AppCompatButton
    private lateinit var stopRecordDataCollectorBtn : AppCompatButton
    private lateinit var stopRecordByRecordBtn : AppCompatButton
    private lateinit var getAllRecordsBtn : AppCompatButton
    private lateinit var getRecordsByDataTypeBtn : AppCompatButton

    private lateinit var logInfoView : TextView

    private var autoRecorderController: AutoRecorderController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auto_recorder)

        val options = HiHealthOptions.builder().build()
        val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)
        autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId)

        initView()
    }

    companion object {

        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, AutoRecorderControllerActivity::class.java))
            }
    }

    private fun initView(){

        initToolbar()

        startRecordDataTypeBtn = findViewById(R.id.startRecordDataTypeBtn)
        stopRecordDataTypeBtn = findViewById(R.id.stopRecordDataTypeBtn)
        startRecordDataCollectorBtn = findViewById(R.id.startRecordDataCollectorBtn)
        stopRecordDataCollectorBtn = findViewById(R.id.stopRecordDataCollectorBtn)
        stopRecordByRecordBtn = findViewById(R.id.stopRecordByRecordBtn)
        getAllRecordsBtn = findViewById(R.id.getAllRecordsBtn)
        getRecordsByDataTypeBtn = findViewById(R.id.getRecordsByDataTypeBtn)

        logInfoView = findViewById(R.id.autoRecorderLogInfoTv)
        logInfoView.movementMethod = ScrollingMovementMethod.getInstance()

        startRecordDataTypeBtn.setOnClickListener {
            startRecordByType()
        }

        stopRecordDataTypeBtn.setOnClickListener {
            stopRecordByType()
        }

        startRecordDataCollectorBtn.setOnClickListener {
            startRecordByCollector()
        }

        stopRecordDataCollectorBtn.setOnClickListener {
            stopRecordByCollector()
        }

        stopRecordByRecordBtn.setOnClickListener {
            stopRecordByRecord()
        }

        getAllRecordsBtn.setOnClickListener {
            getAllRecords()
        }

        getRecordsByDataTypeBtn.setOnClickListener {
            getRecordsByType()
        }
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    /**
     * start record By DataType, the data from sensor will be inserted into database automatically until call Stop
     * Interface
     */

    private fun startRecordByType() {
        logger("startRecordByType")
        if (autoRecorderController == null) {
            val options = HiHealthOptions.builder().build()
            val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId)
        }

        // DT_CONTINUOUS_STEPS_TOTAL as sample, after startRecord this type, the total steps will be inserted into
        // database when u shake ur handset
        val dataType = DataType.DT_CONTINUOUS_STEPS_TOTAL
        autoRecorderController!!.startRecord(dataType)
            .addOnCompleteListener { taskResult -> // the interface won't always success, if u use the onComplete interface, u should add the judgement of
                // result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                if (taskResult.isSuccessful) {
                    logger("onComplete startRecordByType Successful")
                } else {
                    logger("onComplete startRecordByType Failed")
                }
            }.addOnSuccessListener { // u could call addOnSuccessListener to print something
                logger("onSuccess startRecordByType Successful")
                logger(SPLIT)
            }
            .addOnFailureListener { e -> // otherwise u could call addOnFailureListener to catch the fail result
                logger("onFailure startRecordByType Failed: " + e.message)
                logger(SPLIT)
            }
    }

    /**
     * start record By DataCollector, the data from sensor will be inserted into database automatically until call Stop
     * Interface
     */
    private fun startRecordByCollector() {
        logger("startRecordByCollector")
        if (autoRecorderController == null) {
            val options = HiHealthOptions.builder().build()
            val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId)
        }

        // When record data from a data collector, you must specify the data type and collector type (for example, raw
        // data or derived data).
        // You do not need to add other information (such as device information and data stream information) for
        // data-collector-based recording.
        // The app will start data recording by assembling the data collector based on the data type , collect type and
        // packageName.
        val dataCollector: DataCollector =
            DataCollector.Builder()
                .setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL)
                .setPackageName(this)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()
        autoRecorderController!!.startRecord(dataCollector)
            .addOnCompleteListener { taskResult -> // the interface won't always success, if u use the onComplete interface, u should add the judgement of
                // result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                if (taskResult.isSuccessful) {
                    logger("onComplete startRecordByCollector Successful")
                } else {
                    logger("onComplete startRecordByCollector Failed")
                }
            }.addOnSuccessListener { // u could call addOnSuccessListener to print something
                logger("onSuccess startRecordByCollector Successful")
                logger(SPLIT)
            }
            .addOnFailureListener { e -> // otherwise u could call addOnFailureListener to catch the fail result
                logger("onFailure startRecordByCollector Failed: " + e.message)
                logger(SPLIT)
            }
    }

    /**
     * stop record By DataType, the data from sensor will NOT be inserted into database automatically
     */
    private fun stopRecordByType() {
        logger("stopRecordByType")
        if (autoRecorderController == null) {
            val options = HiHealthOptions.builder().build()
            val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId)
        }

        // DT_CONTINUOUS_STEPS_TOTAL as sample, after stopRecord this type, the total steps will NOT be inserted into
        // database when u shake ur handset
        autoRecorderController!!.stopRecord(DataType.DT_CONTINUOUS_STEPS_TOTAL)
            .addOnCompleteListener { taskResult -> // the interface won't always success, if u use the onComplete interface, u should add the judgement
                // of result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                if (taskResult.isSuccessful) {
                    logger("onComplete stopRecordByType Successful")
                } else {
                    logger("onComplete stopRecordByType Failed")
                }
            }
            .addOnSuccessListener { // u could call addOnSuccessListener to print something
                logger("onSuccess stopRecordByType Successful")
                logger(SPLIT)
            }
            .addOnFailureListener { e -> // otherwise u could call addOnFailureListener to catch the fail result
                logger("onFailure stopRecordByType Failed: " + e.message)
                logger(SPLIT)
            }
    }

    /**
     * stop record By DataCollector, the data from sensor will NOT be inserted into database automatically
     */
    private fun stopRecordByCollector() {
        logger("stopRecordByCollector")
        if (autoRecorderController == null) {
            val options = HiHealthOptions.builder().build()
            val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId)
        }
        val dataCollector: DataCollector =
            DataCollector.Builder()
                .setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL)
                .setPackageName(this)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        // if u want to stop record by DataCollector, using the collector which exits in startRecord should be better
        autoRecorderController!!.stopRecord(dataCollector)
            .addOnCompleteListener { taskResult -> // the interface won't always success, if u use the onComplete interface, u should add the judgement
                // of result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                if (taskResult.isSuccessful) {
                    logger("onComplete stopRecordByCollector Successful")
                } else {
                    logger("onComplete stopRecordByCollector Failed")
                }
            }.addOnSuccessListener { // u could call addOnSuccessListener to print something
                logger("onSuccess stopRecordByCollector Successful")
                logger(SPLIT)
            }
            .addOnFailureListener { e -> // otherwise u could call addOnFailureListener to catch the fail result
                logger("onFailure stopRecordByCollector Failed: " + e.message)
                logger(SPLIT)
            }
    }

    /**
     * stop record By Record, the data from sensor will NOT be inserted into database automatically
     */
    private fun stopRecordByRecord() {
        logger("stopRecordByRecord")
        if (autoRecorderController == null) {
            val options = HiHealthOptions.builder().build()
            val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId)
        }

        // Although HMS's Record can be constructed directly, it is still recommended that third-party developers
        // use the getRecords interface to obtain the record, and then stop recording data through stopRecordByRecord
        autoRecorderController!!.records.addOnCompleteListener(OnCompleteListener { task ->
            logger("stopRecordByRecord getRecords firstly")
            if (task.isSuccessful) {
                logger("stopRecordByRecord getRecords Successful")
                val result =
                    task.result ?: return@OnCompleteListener
                if (result.size == 0) {
                    logger("stopRecordByRecord there is no any record exits")
                    logger(SPLIT)
                    return@OnCompleteListener
                }
                for (record in result) {
                    autoRecorderController!!.stopRecord(record).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            logger("stopRecordByRecord Successful")
                            logger(SPLIT)
                        } else {
                            logger("stopRecordByRecord Failed")
                            logger(SPLIT)
                        }
                    }
                }
            }
        })
    }

    /**
     * get all record info of this application
     */
    private fun getAllRecords() {
        logger("getAllRecords")
        if (autoRecorderController == null) {
            val options = HiHealthOptions.builder().build()
            val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId)
        }
        autoRecorderController!!.records.addOnCompleteListener(OnCompleteListener { task -> // the interface won't always success, if u use the onComplete interface, u should add the judgement
            // of result is successful or not. the fail reason include:
            // 1.the app hasn't been granted the scropes
            // 2.this type is not supported so far
            logger("getAllRecords:onComplete")
            if (task.isSuccessful) {
                logger("getAllRecords Successfully")
                val result =
                    task.result ?: return@OnCompleteListener
                if (result.size == 0) {
                    logger("getAllRecords there is no any record exits")
                    logger(SPLIT)
                    return@OnCompleteListener
                }
                for (record in result) {
                    logger("getAllRecords Record : $record")
                }
                logger(SPLIT)
            }
        }).addOnFailureListener { e ->
            logger("getAllRecords Failed: " + e.message)
            logger(SPLIT)
        }
    }

    /**
     * get record info of this application base on the dataType
     */
    private fun getRecordsByType() {
        logger("getRecordsByType")
        if (autoRecorderController == null) {
            val options = HiHealthOptions.builder().build()
            val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options)
            autoRecorderController = HuaweiHiHealth.getAutoRecorderController(this, signInHuaweiId)
        }

        // Get the record information through datatype. In addition to the records started with datatype, the records
        // started with DataCollector will also be obtained (if the datatype in this DataCollector is the same as the
        // datatype in the getrecords input parameter)
        val dataType = DataType.DT_CONTINUOUS_STEPS_TOTAL

        autoRecorderController!!.getRecords(dataType)
            .addOnCompleteListener(OnCompleteListener { task -> // the interface won't always success, if u use the onComplete interface, u should add the judgement
                // of result is successful or not. the fail reason include:
                // 1.the app hasn't been granted the scropes
                // 2.this type is not supported so far
                logger("getRecordsByType:onComplete")
                if (task.isSuccessful) {
                    val result =
                        task.result ?: return@OnCompleteListener
                    if (result.size == 0) {
                        logger("getRecordsByType there is no record with this type exits")
                        logger(SPLIT)
                        return@OnCompleteListener
                    }
                    for (record in result) {
                        logger("getRecordsByType Record : $record")
                    }
                    logger(SPLIT)
                }
            }).addOnFailureListener { e ->
                logger("getRecordsByType Failed: " + e.message)
                logger(SPLIT)
            }
    }

    private fun logger(string: String) {
        Log.i(TAG, string)
        logInfoView.append(string + System.lineSeparator())
        val offset: Int = logInfoView.lineCount * logInfoView.lineHeight
        if (offset > logInfoView.height) {
            logInfoView.scrollTo(0, offset - logInfoView.height)
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
