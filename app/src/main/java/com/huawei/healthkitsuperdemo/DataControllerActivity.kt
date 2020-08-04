package com.huawei.healthkitsuperdemo

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import com.huawei.hmf.tasks.Task
import com.huawei.hms.hihealth.DataController
import com.huawei.hms.hihealth.HiHealthOptions
import com.huawei.hms.hihealth.HuaweiHiHealth
import com.huawei.hms.hihealth.data.DataCollector
import com.huawei.hms.hihealth.data.DataType
import com.huawei.hms.hihealth.data.Field
import com.huawei.hms.hihealth.data.SampleSet
import com.huawei.hms.hihealth.options.DeleteOptions
import com.huawei.hms.hihealth.options.ModifyDataMonitorOptions
import com.huawei.hms.hihealth.options.ReadOptions
import com.huawei.hms.hihealth.options.UpdateOptions
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class DataControllerActivity : AppCompatActivity() {

    private val TAG = javaClass.simpleName

    private lateinit var dataController: DataController

    private lateinit var weightEditText : AppCompatEditText
    private lateinit var heightEditText : AppCompatEditText
    private lateinit var stepsEditText : AppCompatEditText
    private lateinit var startTimeEditText : AppCompatEditText
    private lateinit var endTimeEditText : AppCompatEditText

    private lateinit var insertBtn : AppCompatButton
    private lateinit var readBtn : AppCompatButton
    private lateinit var updateBtn : AppCompatButton
    private lateinit var deleteBtn : AppCompatButton
    private lateinit var registerListenerBtn : AppCompatButton
    private lateinit var syncBtn : AppCompatButton
    private lateinit var clearCloudBtn : AppCompatButton

    private var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_controller)

        initDataController()
        initView()
    }

    companion object {

        fun launch(activity: AppCompatActivity) =
            activity.apply {
                startActivity(Intent(this, DataControllerActivity::class.java))
            }
    }

    private fun initView(){

        initToolbar()

        weightEditText = findViewById(R.id.weightEditText)
        heightEditText = findViewById(R.id.heightEditText)
        stepsEditText = findViewById(R.id.stepsEditText)
        startTimeEditText = findViewById(R.id.startTimeEditText)
        endTimeEditText = findViewById(R.id.endTimeEditText)

        insertBtn = findViewById(R.id.insertBtn)
        readBtn = findViewById(R.id.readBtn)
        updateBtn = findViewById(R.id.updateBtn)
        deleteBtn = findViewById(R.id.deleteBtn)
        registerListenerBtn = findViewById(R.id.registerListenerBtn)
        syncBtn = findViewById(R.id.syncBtn)
        clearCloudBtn = findViewById(R.id.clearCloudBtn)

        startTimeEditText.setOnClickListener(View.OnClickListener {
            chooseStartDate()
        })

        endTimeEditText.setOnClickListener(View.OnClickListener {
            chooseEndDate()
        })

        insertBtn.setOnClickListener{

            val weight = weightEditText.text.toString()
            val height = heightEditText.text.toString()
            val steps = stepsEditText.text.toString()

            if(weight.isNotEmpty()){
                addWeightData(dataController, weight.toFloat())
            }

            if(height.isNotEmpty()){
                addHeightData(dataController, height.toFloat())
            }

            if(steps.isNotEmpty()){
                addStepsData(dataController, steps.toInt())
            }
        }

        readBtn.setOnClickListener{

            val listItems = arrayOf(
                "Read step with dates",
                "Read weight with dates",
                "Read height with dates",
                "Read step today",
                "Read weight today",
                "Read height today",
                "Read step with device",
                "Read weight with device",
                "Read height with device"
            )
            val mBuilder = AlertDialog.Builder(this)
            //mBuilder.setTitle("Choose an item")
            mBuilder.setSingleChoiceItems(listItems, -1) { dialogInterface, i ->

                when (i) {
                    0 -> readStepData(dataController)
                    1 -> readWeightData(dataController)
                    2 -> readHeightData(dataController)
                    3 -> readTodayStep(dataController)
                    4 -> readTodayWeight(dataController)
                    5 -> readTodayHeight(dataController)
                    6 -> readTodayDeviceStep(dataController)
                    7 -> readTodayDeviceWeight(dataController)
                    8 -> readTodayDeviceHeight(dataController)
                }

                dialogInterface.dismiss()
            }
            // Set the neutral/cancel button click listener
            mBuilder.setNeutralButton("Cancel") { dialog, which ->
                // Do something when click the neutral button
                dialog.cancel()
            }

            val mDialog = mBuilder.create()
            mDialog.show()

        }

        updateBtn.setOnClickListener{

            val weight = weightEditText.text.toString()
            val height = heightEditText.text.toString()
            val steps = stepsEditText.text.toString()

            if(weight.isNotEmpty()){
                updateWeightData(dataController, weight.toFloat())
            }

            if(height.isNotEmpty()){
                updateHeightData(dataController, height.toFloat())
            }

            if(steps.isNotEmpty()){
                updateStepData(dataController, steps.toInt())
            }
        }

        deleteBtn.setOnClickListener{
            val listItems = arrayOf(
                "Delete step data",
                "Delete weight data",
                "Delete height data"
            )
            val mBuilder = AlertDialog.Builder(this)
            //mBuilder.setTitle("Choose an item")
            mBuilder.setSingleChoiceItems(listItems, -1) { dialogInterface, i ->

                when (i) {
                    0 -> deleteStepData(dataController)
                    1 -> deleteWeightData(dataController)
                    2 -> deleteHeightData(dataController)
                }

                dialogInterface.dismiss()
            }
            // Set the neutral/cancel button click listener
            mBuilder.setNeutralButton("Cancel") { dialog, which ->
                // Do something when click the neutral button
                dialog.cancel()
            }

            val mDialog = mBuilder.create()
            mDialog.show()
        }

        registerListenerBtn.setOnClickListener{

            val listItems = arrayOf(
                "Register step data",
                "Unregister step data",
                "Test Register data"
            )
            val mBuilder = AlertDialog.Builder(this)
            //mBuilder.setTitle("Choose an item")
            mBuilder.setSingleChoiceItems(listItems, -1) { dialogInterface, i ->

                when (i) {
                    0 -> registerListener(dataController)
                    1 -> unregisterListener(dataController)
                    2 -> insertListenerData(dataController, 72.0f)
                }

                dialogInterface.dismiss()
            }
            // Set the neutral/cancel button click listener
            mBuilder.setNeutralButton("Cancel") { dialog, which ->
                // Do something when click the neutral button
                dialog.cancel()
            }

            val mDialog = mBuilder.create()
            mDialog.show()
        }

        syncBtn.setOnClickListener {
            syncCloudData(dataController)
        }

        clearCloudBtn.setOnClickListener {
            clearCloudData(dataController)
        }

        hideKeyboardWhenFocusGoes()

    }

    private fun hideKeyboardWhenFocusGoes() {
        startTimeEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        }

        endTimeEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        }

        weightEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        }

        heightEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        }

        stepsEditText.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) {
                hideKeyboard(v)
            }
        }
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun initDataController() {
        val hiHealthOptions = HiHealthOptions.builder()
            .addDataType(DataType.DT_CONTINUOUS_STEPS_DELTA, HiHealthOptions.ACCESS_READ)
            .addDataType(DataType.DT_CONTINUOUS_STEPS_DELTA, HiHealthOptions.ACCESS_WRITE)
            .addDataType(DataType.DT_INSTANTANEOUS_HEIGHT, HiHealthOptions.ACCESS_READ)
            .addDataType(DataType.DT_INSTANTANEOUS_HEIGHT, HiHealthOptions.ACCESS_WRITE)
            .addDataType(DataType.DT_INSTANTANEOUS_BODY_WEIGHT, HiHealthOptions.ACCESS_READ)
            .addDataType(DataType.DT_INSTANTANEOUS_BODY_WEIGHT, HiHealthOptions.ACCESS_WRITE)
            .build()

        val signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(hiHealthOptions)
        dataController = HuaweiHiHealth.getDataController(this, signInHuaweiId)
    }

    private fun addWeightData(dataController: DataController, weight: Float){

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
            .setDataType(DataType.DT_INSTANTANEOUS_BODY_WEIGHT)
            .setDataStreamName("BODY_WEIGHT_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build()

        val sampleSet = SampleSet.create(dataCollector)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val currentDate = dateFormat.format(Date())

        val startDate: Date = dateFormat.parse(currentDate)
        val endDate: Date = dateFormat.parse(currentDate)

        val samplePoint = sampleSet.createSamplePoint().setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
        samplePoint.getFieldValue(Field.FIELD_BODY_WEIGHT).setFloatValue(weight)

        sampleSet.addSample(samplePoint)

        val insertTask: Task<Void> = dataController.insert(sampleSet)

        insertTask.addOnSuccessListener {
            Log.i("HomeFragment","Success insert a SampleSet into HMS core")

            showSampleData(sampleSet, "Insert Weight")

        }.addOnFailureListener { e ->
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun addHeightData(dataController: DataController, height: Float){

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
                .setDataStreamName("HEIGHT_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        val sampleSet = SampleSet.create(dataCollector)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val currentDate = dateFormat.format(Date())

        val startDate: Date = dateFormat.parse(currentDate)
        val endDate: Date = dateFormat.parse(currentDate)

        val samplePoint = sampleSet.createSamplePoint().setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
        samplePoint.getFieldValue(Field.FIELD_HEIGHT).setFloatValue(height)

        sampleSet.addSample(samplePoint)

        val insertTask: Task<Void> = dataController.insert(sampleSet)

        insertTask.addOnSuccessListener {
            Log.i("HomeFragment","Success insert a SampleSet into HMS core")

            showSampleData(sampleSet, "Insert Height")

        }.addOnFailureListener { e ->
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun addStepsData(dataController: DataController, steps: Int){

        if (startTimeEditText.text.toString().isEmpty() || endTimeEditText.text.toString().isEmpty()){
            Toast.makeText(this, "Start time or end time cannot be blank.", Toast.LENGTH_LONG).show()
            return
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
                .setDataStreamName("STEPS_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        val sampleSet = SampleSet.create(dataCollector)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val startDate: Date = dateFormat.parse(startTimeEditText.text.toString())
        val endDate: Date = dateFormat.parse(endTimeEditText.text.toString())

        val samplePoint = sampleSet.createSamplePoint().setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
        samplePoint.getFieldValue(Field.FIELD_STEPS_DELTA).setIntValue(steps)

        sampleSet.addSample(samplePoint)

        val insertTask: Task<Void> = dataController.insert(sampleSet)

        insertTask.addOnSuccessListener {
            Log.i("HomeFragment","Success insert a SampleSet into HMS core")

            showSampleData(sampleSet, "Insert Step")

        }.addOnFailureListener { e ->
            Toast.makeText(this, e.message.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun deleteStepData(dataController: DataController) {

        if (startTimeEditText.text.toString().isEmpty() || endTimeEditText.text.toString().isEmpty()){
            Toast.makeText(this, "Start time or end time cannot be blank.", Toast.LENGTH_LONG).show()
            return
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
                .setDataStreamName("STEPS_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val startDate: Date = dateFormat.parse(startTimeEditText.text.toString())
        val endDate: Date = dateFormat.parse(endTimeEditText.text.toString())

        val deleteOptions = DeleteOptions.Builder()
            .addDataCollector(dataCollector)
            .setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
            .build()

        val deleteTask = dataController.delete(deleteOptions)

        deleteTask.addOnSuccessListener {
            Log.i("HomeFragment","Success delete steps data")
            Toast.makeText(this@DataControllerActivity, "Delete success", Toast.LENGTH_LONG).show()

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun deleteHeightData(dataController: DataController) {

        if (startTimeEditText.text.toString().isEmpty() || endTimeEditText.text.toString().isEmpty()){
            Toast.makeText(this, "Start time or end time cannot be blank.", Toast.LENGTH_LONG).show()
            return
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
                .setDataStreamName("HEIGHT_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val startDate: Date = dateFormat.parse(startTimeEditText.text.toString())
        val endDate: Date = dateFormat.parse(endTimeEditText.text.toString())

        val deleteOptions = DeleteOptions.Builder()
            .addDataCollector(dataCollector)
            .setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
            .build()

        val deleteTask = dataController.delete(deleteOptions)

        deleteTask.addOnSuccessListener {
            Log.i("HomeFragment","Success delete steps data")
            Toast.makeText(this@DataControllerActivity, "Delete success", Toast.LENGTH_LONG).show()

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun deleteWeightData(dataController: DataController) {

        if (startTimeEditText.text.toString().isEmpty() || endTimeEditText.text.toString().isEmpty()){
            Toast.makeText(this, "Start time or end time cannot be blank.", Toast.LENGTH_LONG).show()
            return
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_INSTANTANEOUS_BODY_WEIGHT)
                .setDataStreamName("BODY_WEIGHT_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val startDate: Date = dateFormat.parse(startTimeEditText.text.toString())
        val endDate: Date = dateFormat.parse(endTimeEditText.text.toString())

        val deleteOptions = DeleteOptions.Builder()
            .addDataCollector(dataCollector)
            .setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
            .build()

        val deleteTask = dataController.delete(deleteOptions)

        deleteTask.addOnSuccessListener {
            Log.i("HomeFragment","Success delete steps data")
            Toast.makeText(this@DataControllerActivity, "Delete success", Toast.LENGTH_LONG).show()

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun updateStepData(dataController: DataController, steps: Int) {

        if (startTimeEditText.text.toString().isEmpty() || endTimeEditText.text.toString().isEmpty()){
            Toast.makeText(this, "Start time or end time cannot be blank.", Toast.LENGTH_LONG).show()
            return
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
                .setDataStreamName("STEPS_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        val sampleSet = SampleSet.create(dataCollector)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val startDate: Date = dateFormat.parse(startTimeEditText.text.toString())
        val endDate: Date = dateFormat.parse(endTimeEditText.text.toString())

        val samplePoint = sampleSet.createSamplePoint().setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
        samplePoint.getFieldValue(Field.FIELD_STEPS_DELTA).setIntValue(steps)

        sampleSet.addSample(samplePoint)

        val updateOptions = UpdateOptions.Builder().setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
            .setSampleSet(sampleSet)
            .build()

        val updateTask = dataController.update(updateOptions)

        updateTask.addOnSuccessListener {

            showSampleData(sampleSet, "Update Step: ")

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }

    }

    private fun updateWeightData(dataController: DataController, weight: Float) {

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_INSTANTANEOUS_BODY_WEIGHT)
                .setDataStreamName("BODY_WEIGHT_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        val sampleSet = SampleSet.create(dataCollector)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val currentDate = dateFormat.format(Date())

        val startDate: Date = dateFormat.parse(currentDate)
        val endDate: Date = dateFormat.parse(currentDate)

        val samplePoint = sampleSet.createSamplePoint().setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
        samplePoint.getFieldValue(Field.FIELD_BODY_WEIGHT).setFloatValue(weight)

        sampleSet.addSample(samplePoint)

        val updateOptions = UpdateOptions.Builder().setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
            .setSampleSet(sampleSet)
            .build()

        val updateTask = dataController.update(updateOptions)

        updateTask.addOnSuccessListener {

            showSampleData(sampleSet, "Update Weight: ")

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }

    }

    private fun updateHeightData(dataController: DataController, height: Float) {

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
                .setDataStreamName("HEIGHT_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        val sampleSet = SampleSet.create(dataCollector)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val currentDate = dateFormat.format(Date())

        val startDate: Date = dateFormat.parse(currentDate)
        val endDate: Date = dateFormat.parse(currentDate)

        val samplePoint = sampleSet.createSamplePoint().setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
        samplePoint.getFieldValue(Field.FIELD_HEIGHT).setFloatValue(height)

        sampleSet.addSample(samplePoint)

        val updateOptions = UpdateOptions.Builder().setTimeInterval(startDate.time, endDate.time, TimeUnit.MILLISECONDS)
            .setSampleSet(sampleSet)
            .build()

        val updateTask = dataController.update(updateOptions)

        updateTask.addOnSuccessListener {

            showSampleData(sampleSet, "Update Height: ")

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }

    }

    private fun readStepData(dataController: DataController) {

        if (startTimeEditText.text.toString().isEmpty() || endTimeEditText.text.toString().isEmpty()){
            Toast.makeText(this, "Start time or end time cannot be blank.", Toast.LENGTH_LONG).show()
            return
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
            .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
            .setDataStreamName("STEPS_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build()


        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val startDate: Date = dateFormat.parse(startTimeEditText.text.toString())
        val endDate: Date = dateFormat.parse(endTimeEditText.text.toString())

        val readOptions = ReadOptions.Builder().read(dataCollector).setTimeRange(startDate.time, endDate.time, TimeUnit.MILLISECONDS).build()

        val readReplyTask = dataController.read(readOptions)

        readReplyTask.addOnSuccessListener { readReply ->

            for (sampleSet in readReply.sampleSets) {
                showSampleData(sampleSet, "Read Step: ")
            }

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }

    }

    private fun readHeightData(dataController: DataController) {

        if (startTimeEditText.text.toString().isEmpty() || endTimeEditText.text.toString().isEmpty()){
            Toast.makeText(this, "Start time or end time cannot be blank.", Toast.LENGTH_LONG).show()
            return
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
            .setDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
            .setDataStreamName("HEIGHT_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val startDate: Date = dateFormat.parse(startTimeEditText.text.toString())
        val endDate: Date = dateFormat.parse(endTimeEditText.text.toString())

        val readOptions = ReadOptions.Builder().read(dataCollector).setTimeRange(startDate.time, endDate.time, TimeUnit.MILLISECONDS).build()

        val readReplyTask = dataController.read(readOptions)

        readReplyTask.addOnSuccessListener { readReply ->

            for (sampleSet in readReply.sampleSets) {
                showSampleData(sampleSet, "Read Height: ")
            }

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }

    }

    private fun readWeightData(dataController: DataController) {

        if (startTimeEditText.text.toString().isEmpty() || endTimeEditText.text.toString().isEmpty()){
            Toast.makeText(this, "Start time or end time cannot be blank.", Toast.LENGTH_LONG).show()
            return
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
            .setDataType(DataType.DT_INSTANTANEOUS_BODY_WEIGHT)
            .setDataStreamName("BODY_WEIGHT_DELTA")
            .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
            .build()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

        val startDate: Date = dateFormat.parse(startTimeEditText.text.toString())
        val endDate: Date = dateFormat.parse(endTimeEditText.text.toString())

        val readOptions = ReadOptions.Builder().read(dataCollector).setTimeRange(startDate.time, endDate.time, TimeUnit.MILLISECONDS).build()

        val readReplyTask = dataController.read(readOptions)

        readReplyTask.addOnSuccessListener { readReply ->

            for (sampleSet in readReply.sampleSets) {
                showSampleData(sampleSet, "Read Weight: ")
            }

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }

    }

    private fun readTodayStep(dataController: DataController) {

        val todaySummationTask = dataController.readTodaySummation(DataType.DT_CONTINUOUS_STEPS_DELTA)

        todaySummationTask.addOnSuccessListener { sampleSet ->

            showSampleData(sampleSet, "Read Today Step: ")

        }.addOnFailureListener {

        }
    }

    private fun readTodayWeight(dataController: DataController) {

        val todaySummationTask = dataController.readTodaySummation(DataType.DT_INSTANTANEOUS_BODY_WEIGHT)

        todaySummationTask.addOnSuccessListener { sampleSet ->

            showSampleData(sampleSet, "Read Today Weight: ")

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun readTodayHeight(dataController: DataController) {

        val todaySummationTask = dataController.readTodaySummation(DataType.DT_INSTANTANEOUS_HEIGHT)

        todaySummationTask.addOnSuccessListener { sampleSet ->

            showSampleData(sampleSet, "Read Today Height: ")

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun readTodayDeviceStep(dataController: DataController) {

        val todaySummationTask = dataController.readTodaySummationFromDevice(DataType.DT_CONTINUOUS_STEPS_DELTA)

        todaySummationTask.addOnSuccessListener { sampleSet ->

            showSampleData(sampleSet, "Read Today Device Step: ")

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun readTodayDeviceWeight(dataController: DataController) {

        val todaySummationTask = dataController.readTodaySummationFromDevice(DataType.DT_INSTANTANEOUS_BODY_WEIGHT)

        todaySummationTask.addOnSuccessListener { sampleSet ->

            showSampleData(sampleSet, "Read Today Device Weight: ")

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun readTodayDeviceHeight(dataController: DataController) {

        val todaySummationTask = dataController.readTodaySummationFromDevice(DataType.DT_INSTANTANEOUS_HEIGHT)

        todaySummationTask.addOnSuccessListener { sampleSet ->

            showSampleData(sampleSet, "Read Today Device Height: ")

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun registerListener(dataController: DataController) {
        if (pendingIntent != null) {
            Toast.makeText(this, "There is already an listener, no need to re listener", Toast.LENGTH_LONG).show()
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
                .setDataStreamName("STEPS_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        pendingIntent = PendingIntent.getBroadcast(this, 0, Intent(this, DataRegisterReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT)

        val dataMonitorOptions = ModifyDataMonitorOptions.Builder()
            .setModifyDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
            .setModifyDataCollector(dataCollector)
            .setModifyDataIntent(pendingIntent)
            .build()

        val registerTask = dataController.registerModifyDataMonitor(dataMonitorOptions)

        registerTask.addOnSuccessListener {

            Toast.makeText(this, "Register Data Update Listener Success", Toast.LENGTH_LONG).show()

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }


    private fun unregisterListener(dataController: DataController) {
        if (pendingIntent == null) {
            Toast.makeText(this, "There is no listener, no need to un listener", Toast.LENGTH_LONG).show()
            return
        }

        val task = dataController.unregisterModifyDataMonitor(pendingIntent)

        task.addOnSuccessListener {
            Toast.makeText(this, "Unregister Data Update Listener Success: ", Toast.LENGTH_LONG).show()
            pendingIntent = null
        }
        task.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun syncCloudData(dataController: DataController) {
        val syncTask = dataController.syncAll()

        syncTask.addOnSuccessListener {

            Toast.makeText(this, "synAll success", Toast.LENGTH_LONG).show()

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun clearCloudData(dataController: DataController) {
        val clearTask = dataController.clearAll()

        clearTask.addOnSuccessListener {

            Toast.makeText(this, "clearAll success ", Toast.LENGTH_LONG).show()

        }.addOnFailureListener {
            Log.v(TAG, it.message.toString())
        }
    }

    private fun insertListenerData(dataController: DataController, height: Float) {
        if (pendingIntent == null) {
            Toast.makeText(this, "There is no listener, no need to insert sample point", Toast.LENGTH_LONG).show()
            return
        }

        val dataCollector: DataCollector = DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_INSTANTANEOUS_HEIGHT)
                .setDataStreamName("HEIGHT_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build()

        val sampleSet = SampleSet.create(dataCollector)
        val samplePoint = sampleSet.createSamplePoint().setTimeInterval(System.currentTimeMillis(), System.currentTimeMillis(), TimeUnit.MILLISECONDS)

        samplePoint.getFieldValue(Field.FIELD_HEIGHT).setFloatValue(height)

        sampleSet.addSample(samplePoint)

        dataController.insert(sampleSet)
    }

    private fun showSampleData(sampleSet: SampleSet, desc: String) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        for (samplePoint in sampleSet.samplePoints) {
            Log.i("HomeFragment","Sample point type: " + samplePoint.dataType.name)
            Log.i("HomeFragment", "Start: " + dateFormat.format(Date(samplePoint.getStartTime(TimeUnit.MILLISECONDS))))
            Log.i("HomeFragment", "End: " + dateFormat.format(Date(samplePoint.getEndTime(TimeUnit.MILLISECONDS))))
            for (field in samplePoint.dataType.fields) {
                Log.i("HomeFragment", "Field: " + field.name + " Value: " + samplePoint.getFieldValue(field))
                Toast.makeText(this, desc + samplePoint.getFieldValue(field), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun chooseStartDate(){

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            val cal = Calendar.getInstance()
            cal.set(year, monthOfYear, dayOfMonth)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

            val startDate = dateFormat.format(cal.time)

            startTimeEditText.setText(startDate)

        }, year, month, day)

        dpd.show()

    }

    private fun chooseEndDate(){

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->

            val cal = Calendar.getInstance()
            cal.set(year, monthOfYear, dayOfMonth)

            val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())

            val startDate = dateFormat.format(cal.time)

            endTimeEditText.setText(startDate)

        }, year, month, day)

        dpd.show()

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
