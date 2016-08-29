package com.huawei.push.ipc;

import com.huawei.push.ipc.IPushConfig;

interface Ipush{

       void startPush(in IPushConfig config);
       void resume();
       void end();
    }
