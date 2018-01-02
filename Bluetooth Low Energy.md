

Note:
由于笔者在做蓝牙开发，但国内未搭梯子的相关博客比较少，所以会出一系列的蓝牙相关的博客和demos。

更多请参考：
[Android 6.0 蓝牙ble 官方demo简化版](http://blog.csdn.net/sparkleyn/article/details/57481321)

----------


这一篇是android developer官方有关低功耗蓝牙的译文，希望分享给大家，能为大家提供一些帮助。

官方demos：
App作为中心设备：[BluetoothLeGatt](https://github.com/googlesamples/android-BluetoothLeGatt/)
App作为外围设备：[BluetoothAdvertisements](https://github.com/googlesamples/android-BluetoothAdvertisements/)

----------


Andriod4.3(API 18)提供了内置BLE平台支持作为中心角色，并且提供了API接口，可以用来查找设备，查询服务，发送信息。
1.与附近设备交换少量数据
2.和近距离传感器（比如GoogleBeacon）交互，根据实时定位给用户提供定制服务

相对经典蓝牙，Ble（低功耗蓝牙）使用很少的电量，这就允许android app和更高要求的ble设备相连使用，比如近距离传感器，心率检测器，智能穿戴设备。

###**重要定义和概念**

----------


这是一些总要的定义和概念：
1.**Generic Attribute Profile(GATT)**:GATT协议通常被认为是Ble连接中的属性，用于接收发送数据。现在所有的低功耗蓝牙协议都是基于GATT的。

Bluetooth SIG给低功耗设备定义了许多协议，一个协议就是一个设备在特定的app上是如何工作的。要注意的是一个设备可以有多个协议。
比如说一个设备可以有心率检测器和电池检测器。

2.**Attribute Protocol(ATT)**：GATT是基于ATT的，因此这也被成为GATT/ATT。ATT在BLE设备上是优化运行的。在这个方面，它会使用最少的byte字节。
每个属性都被独一无二的UUID（Universally Unique Identifier）标识，UUID是标准的128位的字符串组成。被ATT传输的属性一般被称为characteristic和
services

**Characteristic-**-一个characteristic包含了单个值和0-n描述这个值的描述信息块（description）。一个characteristic可以被看成是一个类似class的类型。

**Descriptor-**-Descriptors是描述characteristic值的属性。例如，一个Descriptor可以指定一个人类可以看懂的描述，只要是在Characteristic的值可接受范围之内，或者是Characteristic的值
的测量单元。

**Service**—一个Service是Characteristic的集合。例如，心率检测器这个Service可以包括心率测量这个Characteristic。可以在https://www.bluetooth.com/specifications上找到一系列已存在的GATT-based协议和服务。

###**角色和责任**

----------


以下是android设备在与ble设备交互的时候应该承担的责任和角色：

**1.中心（Central）VS 外围（peripheral）**

这是应用于BLE连接本身的。处于中心角色的设备搜索，查找广告(Advertisement)。而外围设备则是专门发送广告（Advertisement）的。

**2.GATT 服务器（server） VS GATT客户（client）**
这决定了两个蓝牙设备一旦连接之后，如何交流。

为了明白这之间的区别，想象一下你有一个Android手机和一个BLE设备的活动跟踪器。手机充当中心角色，活动跟踪器充当外围设备。
（如果想要建立蓝牙连接，必须要中心角色和外围角色都有，同一个角色无法建立连接）

一旦手机和活动跟踪器建立了连接，他们就开始传输GATT的元数据了。根据他们传输的数据，他们中的一个会充当服务器（Server）的角色。
例如，如果活动追踪器想要发送传感器数据给手机，那么活动追踪器充当的就是服务器Server的角色。如果活动追踪器想要接收手机的更新，那么
手机就是充当GATT 服务器角色。

在上述例子中，这个Android APP是一个GATT 客户（client），这个APP从GATT服务器中获取支持心率协议的BLE心率数据。当然你也可以设置
Android APP作为GATT 服务器角色存在，详情可以看[BluetoothGattServer](https://developer.android.com/reference/android/bluetooth/BluetoothGattServer.html)


###**BLE  权限**

----------


为了可以在APP上使用蓝牙功能，你必须要声明BLUETOOTH权限。在使用任何蓝牙操作的时候，都需要使用这个权限，比如说申请连接，接受连接，传输数据等。

如果你希望你的APP可以发现或者操作一个蓝牙设置，你还要生命BLUETOOTH_ADMIN权限。注意：声明BLUETTOOTH_ADMIN之前必须要声明BLUETOOTH权限。

在Manifest文件中声明权限：

```
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
```


如果你希望你的APP只在拥有BLE功能的设备上执行，还需要
```
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
```

想要在不支持BLE的设备上执行，你仍然要声明上一句，但是设置成required="false",在执行的时候需要使用PackageManager.hasSystemFeature

```
// Use this check to determine whether BLE is supported on the device. Then
// you can selectively disable BLE-related features.
if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
    Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
    finish();
}
```
Beacons经常和位置相联系。为了使用BluetoothLeScanner不用filter，你最好还在Manifest中声明ACCESS_COARSE_LOCATION和ACCESS_FINE_LOCATION的权限。
如果没有这些权限，扫描不会由任何返回结果。

###**设置BLE**

----------


在使用BLE的之前，你需要确认BLE是否在设备上支持，如果支持的话，那么要确认是否打开了BLE功能。注意，这只有在 `<uses-feature.../>`  设置成false的时候是有必要的。

如果不支持BLE功能，你最好优雅的关掉所有的BLE特性。如果支持BLE，但是没有打开，那么一可以向用户申请在APP内部打开蓝牙功能。这需要使用两个步骤完成：

**1.Get the BluetoothAdpter**
BluetoothAdpter是所有蓝牙活动中需要的，BluetoothAdpter代表着当前设备的蓝牙适配器（蓝牙无线）。在整个系统中有一个蓝牙适配器，你的APP可以使用对象来交流。下面的片段是怎么获取这个适配器的。
注意：getSystemService()返回的是用于获取适配器的BluetoothManager实例。Android4.3（API 18）引入了BluetoothManager

```
private BluetoothAdapter mBluetoothAdapter;
...
// Initializes Bluetooth adapter.
final BluetoothManager bluetoothManager =
        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
mBluetoothAdapter = bluetoothManager.getAdapter();
```

**2.Enable Bluetooth**
接下来，你需要确认蓝牙功能被打开了。调用isEnabled()方法来确认当前蓝牙是否打开。如果这个方法返回了false，那么蓝牙就没有被打开。以下片段就是确认蓝牙功能是否被打开。
如果没有被打开，那么这个片段就会催促用户去设置中打开蓝牙功能。

```
// Ensures Bluetooth is available on the device and it is enabled. If not,
// displays a dialog requesting user permission to enable Bluetooth.
if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
}
```

注意: REQUEST_ENABLE_BT 是一个startActivityForResult中的参数并且会在onActivityResult中返回，是本地定义的integar常量。

###**寻找BLE设备**

----------


寻找BLE设备，需要使用startScan()方法，这个方法需要BluetoothAdapter.LeScanCallback作为参数，你最好补充上这个callback，因为那是搜索结果返回的方法。

因为寻找设备很耗电，所以最好按照以下规则来执行：
1.一旦查找到了蓝牙设备，立马停止扫描
2.不要循环反复查找设备，要给查找设备的功能设定一个时间限定。一个之前连接过的设备可能已经在连接范围之外了，如果继续扫描只会很耗电。

接下来的片段是展示如何开始和结束一个搜索设备的：

```
/**
 * Activity for scanning and displaying available BLE devices.
 */
public class DeviceScanActivity extends ListActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
    ...
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        ...
    }
...
}
```

如果你只想搜索到特定类型的外围设备，那么你可以调用startLeScan(UUID[],BluetoothAdapter,LeScanCallback),提供一个你的app特有的GATT服务的UUID数组对象。

这是BluetoothAdpter.LeScanCallback用于返回搜索结果的代码：

```
private LeDeviceListAdapter mLeDeviceListAdapter;
...
// Device scan callback.
private BluetoothAdapter.LeScanCallback mLeScanCallback =
        new BluetoothAdapter.LeScanCallback() {
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi,
            byte[] scanRecord) {
        runOnUiThread(new Runnable() {
           @Override
           public void run() {
               mLeDeviceListAdapter.addDevice(device);
               mLeDeviceListAdapter.notifyDataSetChanged();
           }
       });
   }
};
```

注意：你只能搜索ble设备或者标准蓝牙设备，搜索蓝牙设备是不能同时搜索两种蓝牙的。

###**连接到GATT Server**

----------


和蓝牙设备交流的第一步就是与它相连接，更准确的讲，是连接到设备上的GATT server.你需要使用**connectGatt()**方法来连接到GATT server。

这个方法需要三个参数，一个是**Context**,**autoConnect**(表示是否需要发现这个设备就自动连接的布尔变量)，以及一个**BluetoothGattCallback**的调用。
```
mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
```

这可以连接到BLE设备山的GATT server，并且返回一个BluetoothGatt实例，这个实例可以用来进行一些GATT client的操作。当前的APP是GATT client。

BluetoothGattCallback可以用来把结果传输给GATT client,比如连接状态或者其他的GATT client操作。

在这个例子中，BLE app提供一个DeviceControlActivity用于连接，展示数据，展示蓝牙设备的GATT services和characteristics。根据用户的选择，这个activity会和一个叫BluetoothService的service互相交互，从而实现通过android API来和BLE设备交流。

```
// A service that interacts with the BLE device via the Android BLE API.
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    // Various callback methods defined by the BLE API.
    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server.");
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        // Result of a characteristic read operation
        public void onCharacteristicRead(BluetoothGatt gatt,
                BluetoothGattCharacteristic characteristic,
                int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }
     ...
    };
...
}
```

如果出发了一个特定的callback,它就会调用合适的broadcastUpdate()辅助方法来传递一个行为。
注意在此处调用的数据解析是根据[Bluetooth Heart Rate Measurement](http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml)
```
private void broadcastUpdate(final String action) {
    final Intent intent = new Intent(action);
    sendBroadcast(intent);
}

private void broadcastUpdate(final String action,
                             final BluetoothGattCharacteristic characteristic) {
    final Intent intent = new Intent(action);

    // This is special handling for the Heart Rate Measurement profile. Data
    // parsing is carried out as per profile specifications.
    if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
        int flag = characteristic.getProperties();
        int format = -1;
        if ((flag & 0x01) != 0) {
            format = BluetoothGattCharacteristic.FORMAT_UINT16;
            Log.d(TAG, "Heart rate format UINT16.");
        } else {
            format = BluetoothGattCharacteristic.FORMAT_UINT8;
            Log.d(TAG, "Heart rate format UINT8.");
        }
        final int heartRate = characteristic.getIntValue(format, 1);
        Log.d(TAG, String.format("Received heart rate: %d", heartRate));
        intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
    } else {
        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                    stringBuilder.toString());
        }
    }
    sendBroadcast(intent);
}
```

再返回DeviceControlActivity,这些事件都会被BroadcastReceiver处理

```
// Handles various events fired by the Service.
// ACTION_GATT_CONNECTED: connected to a GATT server.
// ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
// ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
// ACTION_DATA_AVAILABLE: received data from the device. This can be a
// result of read or notification operations.
private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
            mConnected = true;
            updateConnectionState(R.string.connected);
            invalidateOptionsMenu();
        } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            mConnected = false;
            updateConnectionState(R.string.disconnected);
            invalidateOptionsMenu();
            clearUI();
        } else if (BluetoothLeService.
                ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            // Show all the supported services and characteristics on the
            // user interface.
            displayGattServices(mBluetoothLeService.getSupportedGattServices());
        } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
            displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
        }
    }
};
```

###**Reading BLE Attributes**

----------


一旦你的APP连接到了GATT server，并且发现了一些services，他就可以读写其中支持的属性了。比如说，以下片段迭代了server的services和characteristics并且显示在UI界面上

```
public class DeviceControlActivity extends Activity {
    ...
    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
           // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
         }
    ...
    }
...
}
```


###**Receiving GATT Notification**

----------


在蓝爷设备上一些characteristic发生改变，BLE app会被提醒是一件很常见的事情。下面的片段就是为其中一个characteristic设置提醒，使用**setCharacteristicNotification()**方法：

```
private BluetoothGatt mBluetoothGatt;
BluetoothGattCharacteristic characteristic;
boolean enabled;
...
mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
...
BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
        UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
mBluetoothGatt.writeDescriptor(descriptor);
```

一旦characteristic被设置了提醒，那么**onCharacteristicChanged()**回调方法就在远程设备上的characteristic发生改变时候会被触发。

```
@Override
// Characteristic notification
public void onCharacteristicChanged(BluetoothGatt gatt,
        BluetoothGattCharacteristic characteristic) {
    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
}
```

###**Closing the Client APP**

----------


一旦你的app结束连接BLE设备，它应该使用close()方法来释放系统资源

```
public void close() {
    if (mBluetoothGatt == null) {
        return;
    }
    mBluetoothGatt.close();
    mBluetoothGatt = null;
}
```

















