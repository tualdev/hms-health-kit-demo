package com.huawei.healthkitsuperdemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.huawei.hms.hihealth.data.DataModifyInfo
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class DataRegisterReceiver: BroadcastReceiver(){

    private val TAG = "DataRegisterReceiver"

    private val SPLIT = System.lineSeparator()

    override fun onReceive(context: Context?, intent: Intent?) {

        val updateNotification = DataModifyInfo.getModifyInfo(intent)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        var msg = "detected a data has been modified:$SPLIT"
        if (updateNotification != null) {
            msg += if (updateNotification.dataType != null) {
                "data type：" + updateNotification.dataType.name + SPLIT
            } else {
                "data type：$SPLIT"
            }
            msg += if (updateNotification.dataCollector != null) {
                "data collector：" + updateNotification.dataCollector.toString() + SPLIT
            } else {
                "data collector：$SPLIT"
            }
            msg += ("start time："
                    + dateFormat.format(Date(updateNotification.getModifyStartTime(TimeUnit.MILLISECONDS))) + SPLIT)
            msg += ("end time：" + dateFormat.format(
                Date(
                    updateNotification.getModifyEndTime(
                        TimeUnit.MILLISECONDS
                    )
                )
            )
                    + SPLIT)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

}