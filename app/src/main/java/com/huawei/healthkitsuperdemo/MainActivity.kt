package com.huawei.healthkitsuperdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.common.ApiException
import com.huawei.hms.hihealth.data.Scopes
import com.huawei.hms.support.api.entity.auth.Scope
import com.huawei.hms.support.api.entity.hwid.HwIDConstant
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val HUAWEIID_SIGNIN = 1001

    private lateinit var huaweiIdAuthParamsHelper: HuaweiIdAuthParamsHelper
    private lateinit var authParams: HuaweiIdAuthParams
    private lateinit var authService : HuaweiIdAuthService

    private lateinit var loginWithHuaweiId : HuaweiIdAuthButton
    private lateinit var dataControllerTv : TextView
    private lateinit var sensorsControllerTv : TextView
    private lateinit var autoRecorderControllerTv : TextView
    private lateinit var activityRecordsControllerTv : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        huaweiIdAuthParamsHelper = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
        val scopeList = listOf(
            Scope(HwIDConstant.SCOPE.SCOPE_ACCOUNT_EMAIL),
            Scope(HwIDConstant.SCOPE.ACCOUNT_BASEPROFILE),
            Scope(Scopes.HEALTHKIT_STEP_BOTH),
            Scope(Scopes.HEALTHKIT_HEIGHTWEIGHT_BOTH),
            Scope(Scopes.HEALTHKIT_HEARTRATE_BOTH)
        )
        huaweiIdAuthParamsHelper.setScopeList(scopeList)
        authParams = huaweiIdAuthParamsHelper.setAccessToken().createParams()
        authService = HuaweiIdAuthManager.getService(this, authParams)

        loginWithHuaweiId = findViewById(R.id.loginWithHuaweiId)
        dataControllerTv = findViewById(R.id.dataControllerTv)
        sensorsControllerTv = findViewById(R.id.sensorsControllerTv)
        autoRecorderControllerTv = findViewById(R.id.autoRecorderControllerTv)
        activityRecordsControllerTv = findViewById(R.id.activityRecordsControllerTv)

        initView()
    }

    private fun initView() {

        loginWithHuaweiId.setOnClickListener(View.OnClickListener {
            signIn()
        })

        dataControllerTv.setOnClickListener(View.OnClickListener {
            DataControllerActivity.launch(this)
        })

        sensorsControllerTv.setOnClickListener(View.OnClickListener {
            SensorControllerActivity.launch(this)
        })

        autoRecorderControllerTv.setOnClickListener(View.OnClickListener {
            AutoRecorderControllerActivity.launch(this)
        })

        activityRecordsControllerTv.setOnClickListener {
            ActivityRecordsControllerActivity.launch(this)
        }
    }

    private fun signIn() {

        val authHuaweiIdTask = authService.silentSignIn()

        authHuaweiIdTask.addOnSuccessListener {

            Toast.makeText(this, "Sign in success", Toast.LENGTH_LONG).show()
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {

                val signInIntent = authService.signInIntent

                startActivityForResult(signInIntent, HUAWEIID_SIGNIN)
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == HUAWEIID_SIGNIN) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)

            if (authHuaweiIdTask.isSuccessful) {

                val huaweiAccount = authHuaweiIdTask.result
                val accessToken = huaweiAccount.accessToken
                val credential = HwIdAuthProvider.credentialWithToken(accessToken)

                AGConnectAuth.getInstance().signIn(credential)
                    .addOnSuccessListener { signInResult -> // onSuccess

                        val user = signInResult.user


                    }.addOnFailureListener {
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    }

            } else {
                Toast.makeText(this, "HwID signIn failed: " + authHuaweiIdTask.exception.message, Toast.LENGTH_LONG).show()
            }

        }
    }
}
