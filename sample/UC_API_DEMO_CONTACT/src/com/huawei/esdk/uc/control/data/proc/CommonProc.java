package com.huawei.esdk.uc.control.data.proc;

import com.huawei.common.LocalBroadcast.LocalBroadcastProc;
import com.huawei.common.LocalBroadcast.ReceiveData;
import com.huawei.common.constant.CustomBroadcastConst;
import com.huawei.common.constant.UCResource;
import com.huawei.data.base.BaseResponseData;

import android.content.Intent;

public class CommonProc implements LocalBroadcastProc
{
	@Override
	public boolean onProc(Intent intent, ReceiveData rd) {
		rd.action = intent.getAction();

		if (CustomBroadcastConst.ACTION_SEND_MESSAGE_RESPONSE.equals(rd.action)
				|| CustomBroadcastConst.ACTION_GROUPSEND_CHAT.equals(rd.action)) {
			rd.result = intent.getIntExtra(UCResource.SERVICE_RESPONSE_RESULT,
					UCResource.REQUEST_OK);

			Object obj = intent
					.getSerializableExtra(UCResource.SERVICE_RESPONSE_DATA);

			if (obj instanceof BaseResponseData) {
				rd.data = (BaseResponseData) obj;
			}

			return true;
		} else {
			return false;
		}

	}

	private boolean onKickedBySVNGateway(Intent it) {
		return false;
	}
}
