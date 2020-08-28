package com.huawei.healthkitsuperdemo

import android.app.Activity
import android.app.PendingIntent
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
import com.huawei.hms.hihealth.*
import com.huawei.hms.hihealth.data.*
import com.huawei.hms.hihealth.options.ActivityRecordInsertOptions
import com.huawei.hms.hihealth.options.ActivityRecordReadOptions
import com.huawei.hms.hihealth.options.DeleteOptions
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class ActivityRecordsControllerActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName
    private val SPLIT = "*******************************" + System.lineSeparator()

    private lateinit var beginActivityRecordBtn : AppCompatButton
    private lateinit var endActivityRecordBtn : AppCompatButton
    private lateinit var addActivityRecordBtn : AppCompatButton
    private lateinit var getActivityRecordBtn : AppCompatButton
    private lateinit var addActivityRecordsMonitorBtn : AppCompatButton
    private lateinit var removeActivityRecordsMonitor : AppCompatButton
    private lateinit var deleteActivityRecord : AppCompatButton

    private lateinit var logInfoView : TextView

    // ActivityRecordsController for managing activity records
    private var activityRecordsController: ActivityRecordsController? = null

    // DataController for deleting activity records
    private var dataController: DataController? = null

    // PendingIntent
    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activity_records)

        val hiHealthOptions = HiHealthOptions.builder().build()
        val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(hiHealthOptions)

        dataController = HuaweiHiHealth.getDataController(this, signInHuaweiId)
        activityRecordsController = HuaweiHiHealth.getActivityRecordsController(this, signInHuaweiId)

        initView()
    }

    companion object {

        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, ActivityRecordsControllerActivity::class.java))
            }
    }

    private fun initView(){

        initToolbar()

        beginActivityRecordBtn = findViewById(R.id.beginActivityRecordBtn)
        endActivityRecordBtn = findViewById(R.id.endActivityRecordBtn)
        addActivityRecordBtn = findViewById(R.id.addActivityRecordBtn)
        getActivityRecordBtn = findViewById(R.id.getActivityRecordBtn)
        addActivityRecordsMonitorBtn = findViewById(R.id.addActivityRecordsMonitorBtn)
        removeActivityRecordsMonitor = findViewById(R.id.removeActivityRecordsMonitor)
        deleteActivityRecord = findViewById(R.id.deleteActivityRecord)

        logInfoView = findViewById(R.id.autoRecorderLogInfoTv)
        logInfoView.movementMethod = ScrollingMovementMethod.getInstance()

        beginActivityRecordBtn.setOnClickListener {
            beginActivityRecord()
        }

        endActivityRecordBtn.setOnClickListener {
            endActivityRecord()
        }

        addActivityRecordBtn.setOnClickListener {
            addActivityRecord()
        }

        getActivityRecordBtn.setOnClickListener {
            getActivityRecord()
        }

        addActivityRecordsMonitorBtn.setOnClickListener {
            addActivityRecordsMonitor()
        }

        removeActivityRecordsMonitor.setOnClickListener {
            removeActivityRecordsMonitor()
        }

        deleteActivityRecord.setOnClickListener {
            deleteActivityRecord()
        }
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    /**
     * Start an activity record
     */
    private fun beginActivityRecord() {
        logger(SPLIT + "this is MyActivityRecord Begin")
        val startTime = Calendar.getInstance().timeInMillis

        // Build an ActivityRecord object
        val activityRecord =
            ActivityRecord.Builder().setId("MyBeginActivityRecordId")
                .setName("BeginActivityRecord")
                .setDesc("This is ActivityRecord begin test!")
                .setActivityTypeId(HiHealthActivities.RUNNING)
                .setStartTime(startTime, TimeUnit.MILLISECONDS)
                .build()
        checkConnect()

        // Add a listener for the ActivityRecord start success
        val beginTask =
            activityRecordsController!!.beginActivityRecord(activityRecord)

        // Add a listener for the ActivityRecord start failure
        beginTask.addOnSuccessListener { logger("MyActivityRecord begin success") }
            .addOnFailureListener { e -> printFailureMessage(e, "beginActivityRecord") }
    }

    /**
     * Stop an activity record
     */
    private fun endActivityRecord() {
        logger(SPLIT + "this is MyActivityRecord End")

        // Call the related method of ActivityRecordsController to stop activity records.
        // The input parameter can be the ID string of ActivityRecord or null
        // Stop an activity record of the current app by specifying the ID string as the input parameter
        // Stop activity records of the current app by specifying null as the input parameter
        val endTask = activityRecordsController!!.endActivityRecord("MyBeginActivityRecordId")
        endTask.addOnSuccessListener { activityRecords ->
            logger("MyActivityRecord End success")
            // Return the list of activity records that have stopped
            if (activityRecords.size > 0) {
                for (activityRecord in activityRecords) {
                    dumpActivityRecord(activityRecord)
                }
            } else {
                // Null will be returnded if none of the activity records has stopped
                logger("MyActivityRecord End response is null")
            }
        }.addOnFailureListener { e -> printFailureMessage(e, "endActivityRecord") }
    }

    /**
     * Add an activity record to the Health platform
     */
    private fun addActivityRecord() {
        logger("this is MyActivityRecord Add")

        // Build the time range of the request object: start time and end time
        val cal = Calendar.getInstance()
        val now = Date()
        cal.time = now
        val endTime = cal.timeInMillis
        cal.add(Calendar.HOUR_OF_DAY, -1)
        val startTime = cal.timeInMillis

        // Build the activity record request object
        val activityRecord = ActivityRecord.Builder().setName("AddActivityRecord")
            .setDesc("This is ActivityRecord add test!")
            .setId("MyAddActivityRecordId")
            .setActivityTypeId(HiHealthActivities.RUNNING)
            .setStartTime(startTime, TimeUnit.MILLISECONDS)
            .setEndTime(endTime, TimeUnit.MILLISECONDS)
            .build()

        // Build the dataCollector object
        val dataCollector: DataCollector =
            DataCollector.Builder()
                .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .setPackageName(this)
                .setDataCollectorName("AddActivityRecord")
                .build()

        // Build the sampling sampleSet based on the dataCollector
        val sampleSet = SampleSet.create(dataCollector)

        // Build the (DT_CONTINUOUS_STEPS_DELTA) sampling data object and add it to the sampling dataSet
        val samplePoint = sampleSet.createSamplePoint()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
        samplePoint.getFieldValue(Field.FIELD_STEPS_DELTA)
            .setIntValue(1024)
        sampleSet.addSample(samplePoint)

        // Build the activity record addition request object
        val insertRequest =
            ActivityRecordInsertOptions.Builder().setActivityRecord(activityRecord)
                .addSampleSet(sampleSet).build()
        checkConnect()

        // Call the related method in the ActivityRecordsController to add activity records
        val addTask =
            activityRecordsController!!.addActivityRecord(insertRequest)
        addTask.addOnSuccessListener { logger("ActivityRecord add was successful!") }
            .addOnFailureListener { e -> printFailureMessage(e, "addActivityRecord") }
    }

    /**
     * Read historical activity records
     */
    private fun getActivityRecord() {
        logger(SPLIT + "this is MyActivityRecord Get")

        // Build the time range of the request object: start time and end time
        val cal = Calendar.getInstance()
        val now = Date()
        cal.time = now
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = cal.timeInMillis

        // Build the request body for reading activity records
        val readRequest = ActivityRecordReadOptions.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .readActivityRecordsFromAllApps()
            .read(DataType.DT_CONTINUOUS_STEPS_DELTA)
            .build()
        checkConnect()

        // Call the read method of the ActivityRecordsController to obtain activity records
        // from the Health platform based on the conditions in the request body
        val getTask =
            activityRecordsController!!.getActivityRecord(readRequest)
        getTask.addOnSuccessListener { activityRecordReply ->
            logger("Get ActivityRecord was successful!")
            // Print ActivityRecord and corresponding activity data in the result
            val activityRecordList =
                activityRecordReply.activityRecords
            for (activityRecord in activityRecordList) {
                dumpActivityRecord(activityRecord)
                for (sampleSet in activityRecordReply.getSampleSet(activityRecord)) {
                    dumpSampleSet(sampleSet)
                }
            }
        }.addOnFailureListener { e -> printFailureMessage(e, "getActivityRecord") }
    }

    /**
     * Delete activity record
     */
    private fun deleteActivityRecord() {
        logger("this is MyActivityRecord Delete")

        // Build the time range of the request object: start time and end time
        val cal = Calendar.getInstance()
        val now = Date()
        cal.time = now
        val endTime = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, -2)
        val startTime = cal.timeInMillis

        // Build the request body for reading activity records
        val readRequest = ActivityRecordReadOptions.Builder()
            .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
            .readActivityRecordsFromAllApps()
            .read(DataType.DT_CONTINUOUS_STEPS_DELTA)
            .build()

        // Call the read method of the ActivityRecordsController to obtain activity records
        // from the Health platform based on the conditions in the request body
        val getTask =
            activityRecordsController!!.getActivityRecord(readRequest)
        getTask.addOnSuccessListener { activityRecordReply ->
            Log.i(TAG, "Reading ActivityRecord  response status " + activityRecordReply.status)
            val activityRecords = activityRecordReply.activityRecords

            // Get ActivityRecord and corresponding activity data in the result
            for (activityRecord in activityRecords) {
                val deleteOptions =
                    DeleteOptions.Builder().addActivityRecord(activityRecord)
                        .setTimeInterval(
                            activityRecord.getStartTime(TimeUnit.MILLISECONDS),
                            activityRecord.getEndTime(TimeUnit.MILLISECONDS),
                            TimeUnit.MILLISECONDS
                        )
                        .build()
                logger("begin delete ActivitiRecord is :" + activityRecord.id)

                // Delete ActivityRecord
                val deleteTask =
                    dataController!!.delete(deleteOptions)
                deleteTask.addOnSuccessListener { logger("delete ActivitiRecord is Success:" + activityRecord.id) }
                    .addOnFailureListener { e -> printFailureMessage(e, "delete") }
            }
        }.addOnFailureListener { e -> printFailureMessage(e, "delete") }
    }

    /**
     * Register a listener for monitoring the activity record status
     */
    private fun addActivityRecordsMonitor() {
        logger(SPLIT + "this is MyActivityRecord Add Monitor")
        if (pendingIntent != null) {
            logger("There is already a Monitor, no need to add Monitor")
            return
        }

        // Build the pendingIntent request body.
        // ActivityRecordsMonitorReceiver is the broadcast receiving class created in advance
        pendingIntent = PendingIntent.getBroadcast(
            this, 0, Intent(this, ActivityRecordsMonitorReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Call the related method in the ActivityRecordsController to register a listener
        val addMonitorTask =
            activityRecordsController!!.addActivityRecordsMonitor(pendingIntent)
        addMonitorTask.addOnSuccessListener { logger("addActivityRecordsMonitor is successful") }
            .addOnFailureListener { e ->
                printFailureMessage(e, "addActivityRecordsMonitor")
                pendingIntent = null
            }
    }

    /**
     * Unregister a listener for monitoring the activity record status
     */
    private fun removeActivityRecordsMonitor() {
        logger(SPLIT + "this is MyActivityRecord Remove Monitor")
        if (pendingIntent == null) {
            logger("There is no Monitor, no need to remove Monitor")
            return
        }

        // Build the pendingIntent request body.
        // ActivityRecordsMonitorReceiver is the broadcast receiving class created in advance
        pendingIntent = PendingIntent.getBroadcast(
            this, 0, Intent(this, ActivityRecordsMonitorReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Call the related method in the ActivityRecordsController to unregister a listener
        val removeMonitorTask =
            activityRecordsController!!.removeActivityRecordsMonitor(pendingIntent)
        removeMonitorTask.addOnSuccessListener {
            logger("removeActivityRecordsMonitor is successful")
            pendingIntent = null
        }.addOnFailureListener { e ->
            printFailureMessage(e, "removeActivityRecordsMonitor")
            pendingIntent = null
        }
    }

    /**
     * Print the SamplePoint in the SampleSet object as an output.
     *
     * @param sampleSet indicating the sampling dataset)
     */
    private fun dumpSampleSet(sampleSet: SampleSet) {
        logger("Returned for SamplePoint and Data type: " + sampleSet.dataType.name)
        for (dp in sampleSet.samplePoints) {
            val dateFormat = DateFormat.getTimeInstance()
            logger("SamplePoint:")
            logger("DataCollector:" + dp.dataCollector.toString())
            logger("\tType: " + dp.dataType.name)
            logger("\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)))
            logger("\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)))
            for (field in dp.dataType.fields) {
                logger("\tField: " + field.toString() + " Value: " + dp.getFieldValue(field))
            }
        }
    }

    /**
     * Print the ActivityRecord object as an output.
     *
     * @param activityRecord indicating an activity record
     */
    private fun dumpActivityRecord(activityRecord: ActivityRecord) {
        val dateFormat = DateFormat.getDateInstance()
        val timeFormat = DateFormat.getTimeInstance()
        logger(
            """Returned for ActivityRecord: ${activityRecord.name}
	ActivityRecord Identifier is ${activityRecord.id}
	ActivityRecord created by app is ${activityRecord.packageName}
	Description: ${activityRecord.desc}
	Start: ${dateFormat.format(activityRecord.getStartTime(TimeUnit.MILLISECONDS))} ${timeFormat.format(
                activityRecord.getStartTime(TimeUnit.MILLISECONDS)
            )}
	End: ${dateFormat.format(activityRecord.getEndTime(TimeUnit.MILLISECONDS))} ${timeFormat.format(
                activityRecord.getEndTime(TimeUnit.MILLISECONDS)
            )}
	Activity:${activityRecord.activityType}"""
        )
    }

    /**
     * Check the object connection
     */
    private fun checkConnect() {
        if (activityRecordsController == null) {
            val hiHealthOptions = HiHealthOptions.builder().build()
            val signInHuaweiId =
                HuaweiIdAuthManager.getExtendedAuthResult(hiHealthOptions)
            activityRecordsController =
                HuaweiHiHealth.getActivityRecordsController(this, signInHuaweiId)
        }
    }

    /**
     * Print error code and error information for an exception.
     *
     * @param exception indicating an exception object
     * @param api api name
     */
    private fun printFailureMessage(
        exception: Exception,
        api: String
    ) {
        val errorCode = exception.message
        val pattern = Pattern.compile("[0-9]*")
        val isNum = pattern.matcher(errorCode)
        if (isNum.matches()) {
            val errorMsg =
                HiHealthStatusCodes.getStatusCodeMessage(errorCode!!.toInt())
            logger("$api failure $errorCode:$errorMsg")
        } else {
            logger("$api failure $errorCode")
        }
        logger(SPLIT)
    }

    private fun logger(string: String) {
        Log.i(TAG, string)
        logInfoView.append(string + System.lineSeparator())
        val offset: Int = logInfoView.getLineCount() * logInfoView.getLineHeight()
        if (offset > logInfoView.getHeight()) {
            logInfoView.scrollTo(0, offset - logInfoView.getHeight())
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
