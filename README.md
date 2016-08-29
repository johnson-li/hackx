## eSDK\_EC\_SDK\_Android  ##
eSDK EC Android支持Andorid 4.0.3到6.0之间的版本（包含6.0）,开放eSpace客户端所有基础业务接口。eSDK提供的基础业务接口方法为本地API，将eSDK提供的本地库文件加入其开发工程，即可实现eSpace Mobile基础功能。 
eSpace Mobile API接口无需安装eSpace移动客户端，可直接供第三方应用调用EC能力。 
eSDK EC将eSpace EC移动终端的能力以接口形式开放，ISV只需要调用相应接口就能完成如呼叫、会议、即时消息等功能，ISV在此基础上可以进行灵活的界面定制，融入自己的特色业务。


## 版本更新 ##
eSDK EC最新版本v1.5.70

## 开发环境 ##

- 操作系统： Windows7
- 开发工具：Android Studio1.5.0及以上版本
- JDK：1.6及以上版本

## 文件指引 ##

- src文件夹：eSDK EC Android依赖库
- sample文件夹：eSDK EC全量及场景化样例
- doc：eSDK EC SDK的接口参考、开发指南

## 入门指导 ##

- 下载工程：下载提供的工程文件
- 编译工程：打开Android Studio，菜单栏“File > New > Import Project...”，指定到下载工程中“sample”目录下具体工程，点击"OK"，工程自动引入eSpaceSDK库并编译；
- 用户需要根据安装的gradle版本和SDK版本更新全量和场景化工程gradle文件的版本信息；
- 详细的开发指南请参考doc中的开发指南

###登录EC平台###

要体验华为EC系统的能力，首先要登录EC系统，doc中的开发指南将详细描述如何登录EC系统

## 获取帮助 ##

在开发过程中，您有任何问题均可以至[DevCenter](https://devcenter.huawei.com)中提单跟踪。也可以在[华为开发者社区](http://bbs.csdn.net/forums/hwucdeveloper)中查找或提问。另外，华为技术支持热线电话：400-822-9999（转二次开发）