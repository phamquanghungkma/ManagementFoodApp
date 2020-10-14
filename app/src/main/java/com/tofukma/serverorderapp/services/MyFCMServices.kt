package com.tofukma.serverorderapp.services

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.tofukma.serverorderapp.MainActivity
import com.tofukma.serverorderapp.common.Common
import java.util.*

class MyFCMServices : FirebaseMessagingService(){
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Common.updateToken(this,p0,true,false)
        // Khi một thiết bị cài đặt ứng dụng thì nó sẽ tạo ra một device_token, ta sẽ gửi device_token đó lên Firebase
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val dataRev = remoteMessage.data
        if(dataRev != null ){
            if (dataRev[Common.NOTI_TITLE]!!.equals(" Đơn mới "))
            {
                var intent = Intent(this, MainActivity::class.java)
                intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER, true)
                Common.showNotification(this, Random().nextInt(),
                    dataRev[Common.NOTI_TITLE],
                    dataRev[Common.NOTI_CONTENT], intent)

            }
            else
                Common.showNotification(this,
                    Random().nextInt(),dataRev[Common.NOTI_TITLE],dataRev[Common.NOTI_CONTENT],null)
        }
    }


}