# Bluetooth
# 蓝牙传输数据（经典蓝牙、BLE蓝牙）代码里面分别有客户端和服务端，两端配合使用。BLE蓝牙需要注意：手机硬件得支持这个功能才行哟
# 依赖方式
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
  
	dependencies {
	        implementation 'com.github.kezhangzhao:Bluetooth:1.0.0'
	}

  下载代码看吧，有备注。
  以前参考别人代码写的，自己封装一下方便自己使用。
