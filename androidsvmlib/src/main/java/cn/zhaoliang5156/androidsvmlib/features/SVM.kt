package cn.zhaoliang5156.androidsvmlib.features

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import cn.zhaoliang5156.androidsvmlib.domain.FeatureData
import cn.zhaoliang5156.androidsvmlib.svmlib.svm_predict
import cn.zhaoliang5156.androidsvmlib.svmlib.svm_scale
import cn.zhaoliang5156.androidsvmlib.svmlib.svm_train
import libsvm.svm
import libsvm.svm_model
import libsvm.svm_node
import org.jetbrains.anko.doAsync
import java.io.*

/**
 * <pre>
 *     封装好的SVM类
 *     功能：
 *          1. 收集样本文件
 *          2. 训练Model
 *          3. 识别结果
 *     使用说明：
 *          1. 创建SVM对象
 *          2. 设置lable 和 features
 *          3. 收集样本
 *          4. 训练model
 *          5. 测试
 *     @author zhaoliang 2017-7-25 V1
 * </pre>
 *
 */
class SVM {

    // 传感器管理类
    var mSensorManager: SensorManager
    // 传感器
    var mSensor: Sensor
    // 频率
    var mHz: Int? = 0
    // 存放文件的目录
    var mDir: String? = null
    // 标记的lable
    var trainLable: Int = 0

    /**
     * 构造方法
     * @param context 上下文对象
     * @param hz 拿数据的频率
     * @param dir 存放文件的目录
     * @param svmListener SVM的监听器
     */
    constructor(context: Context, hz: Int, dir: String, svmListener: SVMListener) {
        mSensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        mHz = hz
        mDir = dir
        mSVMListener = svmListener
    }

    /**
     * 收集数据加速度监听器对象
     */
    var mCollectionSensorEventListener = object : SensorEventListener {

        // 存放加速度值的数组
        var accArr = DoubleArray(128)
        // 存放加速度的索引
        var currentIndex = 0

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

        }

        /**
         * 从这里那到加速度的值
         */
        override fun onSensorChanged(p0: SensorEvent) {
            // x轴方向上的加速度
            var x = p0.values[0]
            // y轴方向上的加速度
            var y = p0.values[1]
            // x轴方向上的加速度
            var z = p0.values[2]
            // 合成加速度
            val acc = Math.sqrt((x * x + y * y + z * z).toDouble())
            // 调用监听器的回调，？表示mSVMListener对象为空的时候就不会执行这行代码，不为空的时候执行
            mSVMListener?.onSensorChanged(acc)
            // 判断当前数据是否足够128个数据，够128个数据了就取特征值存起来
            if (currentIndex >= 128) {
                // 把数组转换成特征值
                val features = dataToFeatures(accArr, mHz!!)
                // 写入到文件
                writeToFile("${mDir}/train${trainLable}", trainLable, features)
                // 当前的索引重置为0
                currentIndex = 0
                // 写入文件成功
                mSVMListener?.onWriteSuccess()
            } else {
                // 继续往数组里放数据
                accArr[currentIndex++] = acc
            }
        }
    }

    /**
     * 测试的加速度监听器对象
     */
    var mTestSensorEventListener = object : SensorEventListener {

        // 存放加速度值的数组
        var accArr = DoubleArray(128)
        // 存放加速度的索引
        var currentIndex = 0

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

        }

        /**
         * 从这里那到加速度的值
         */
        override fun onSensorChanged(p0: SensorEvent) {
            // x轴方向上的加速度
            var x = p0.values[0]
            // y轴方向上的加速度
            var y = p0.values[1]
            // x轴方向上的加速度
            var z = p0.values[2]
            // 合成加速度
            val acc = Math.sqrt((x * x + y * y + z * z).toDouble())
            // 调用监听器的回调，？表示mSVMListener对象为空的时候就不会执行这行代码，不为空的时候执行
            mSVMListener?.onSensorChanged(acc)
            // 判断当前数据是否足够128个数据，够128个数据了就取特征值存起来
            if (currentIndex >= 128) {
                // 把数组转换成特征值
                val features = dataToFeatures(accArr, mHz!!)
                // svm识别结果
                val code = predictUnScaleData(features)
                // 调用监听器对象的回调
                mSVMListener?.onTestSuccess(code.toInt())
                // 当前的索引重置为0
                currentIndex = 0
            } else {
                // 继续往数组里放数据
                accArr[currentIndex++] = acc
            }
        }
    }

    // 是否开始记录数据的标记
    var isStartCollection: Boolean = false

    /**
     * 收集数据
     */
    fun startCollection() {
        if (!isStartCollection) {
            mSensorManager.registerListener(mCollectionSensorEventListener, mSensor, mHz!!)
        } else {
            mSensorManager.unregisterListener(mCollectionSensorEventListener)
        }
        isStartCollection = !isStartCollection
    }

    /**
     * 训练
     */
    fun train() {
        doAsync {
            // 把train样本组合到一个文件中
            try {
                var tempTrainFile = BufferedWriter(OutputStreamWriter(FileOutputStream("${mDir}/tempTrain", false)))
                for (index in 0..7) {
                    var trainFile = File("${mDir}/train${index}")
                    if (trainFile.exists()) {
                        // 把数据拿出来写到一个文件中
                        val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(trainFile)))
                        var line = bufferedReader.readLine()
                        while (line != null) {
                            tempTrainFile.write(line + "\n")
                            line = bufferedReader.readLine()
                        }
                        bufferedReader.close()
                    }
                }
                tempTrainFile.close()

                createScaleFile(arrayOf("-l", "0", "-u", "1", "-s", "${mDir}/range", "${mDir}/tempTrain"))
                createModelFile(arrayOf("-s", "0", "-c", "128.0", "-t", "2", "-g", "8.0", "-e", "0.1", "${mDir}/scale", "${mDir}/model"))
                //createPredictFile(arrayOf("${mDir}/scale", "${mDir}/model", "${mDir}/predict"))
                createAccuracyFile(arrayOf("-v", "5", "${mDir}/scale"))

                var reader: BufferedReader = BufferedReader(InputStreamReader(FileInputStream("${mDir}/accuracy")))
                var line = reader.readLine()
                while (line != null) {
                    line = reader.readLine()
                    if (line.contains("Cross Validation Accuracy")) {
                        mSVMListener?.onTrainSuccess(line.replace("Cross Validation ", ""))
                    }
                }
            } catch (e: Exception) {

            }
        }
    }

    /**
     * 创建归一化文件
     */
    fun createScaleFile(args: Array<String>) {
        val out = System.out
        var outScaleFile = PrintStream("${mDir}/scale")
        System.setOut(outScaleFile)
        svm_scale.main(args)
        System.setOut(out)
    }

    /**
     * 创建model文件
     */
    fun createModelFile(args: Array<String>) {
        svm_train.main(args)
    }

    fun createAccuracyFile(args: Array<String>) {
        val out = System.out
        var outScaleFile = PrintStream("${mDir}/accuracy")
        System.setOut(outScaleFile)
        svm_train.main(args)
        System.setOut(out)
    }

    /**
     * 创建Predict文件
     */
    fun createPredictFile(args: Array<String>) {
        val out = System.out
        var outAccuracy = PrintStream("${mDir}/accuracy")
        System.setOut(outAccuracy)
        svm_predict.main(args)
        System.setOut(out)
    }

    var isStartTest: Boolean = false
    /**
     * 测试
     */
    fun test() {
        if (!isStartTest) {
            loadFile("${mDir}/range", "${mDir}/model")
            mSensorManager.registerListener(mTestSensorEventListener, mSensor, mHz!!)
        } else {
            mSensorManager.unregisterListener(mTestSensorEventListener)
        }
        isStartTest = !isStartTest
    }

    /**
     * 删除文件
     */
    fun deleteFile() {
        doAsync {
            val file = File("${mDir}")
            for (item in file.list()) {
                File("${mDir}/${item}").delete()
            }
            if (mSVMListener != null) {
                mSVMListener.deleteFileSuccess()
            }
        }
    }

    lateinit var mFeatrues: ArrayList<FeatureData>

    fun setFeatrues(data: ArrayList<FeatureData>) {
        mFeatrues = data
    }

    /**
     * 特征lable
     */
    private val FUN_1_MINIMUM_LABLE = 1
    private val FUN_2_MAXIMUM_LABLE = 2
    private val FUN_3_MEANCROSSINGSRATE_LABLE = 3
    private val FUN_4_STANDARDDEVIATION_LABLE = 4
    private val FUN_5_SPP_LABLE = 5
    private val FUN_6_ENERGY_LABLE = 6
    private val FUN_7_ENTROPY_LABLE = 7
    private val FUN_8_CENTROID_LABLE = 8
    private val FUN_9_MEAN_LABLE = 9
    private val FUN_10_RMS_LABLE = 10
    private val FUN_11_SMA_LABLE = 11
    private val FUN_12_IQR_LABLE = 12
    private val FUN_13_MAD_LABLE = 13
    private val FUN_14_TENERGY_LABLE = 14
    private val FUN_15_FDEV_LABLE = 15
    private val FUN_16_FMEAN_LABLE = 16
    private val FUN_17_SKEW_LABLE = 17
    private val FUN_18_KURT_LABLE = 18
    private val FUN_19_MEDIAN_LABLE = 19

    /**
     * 把double数组转换成特征字符串数组
     * @param doubleArr 数据
     * *
     * @param sinter    采样间隔(毫秒数)
     * *
     * @return
     */
    fun dataToFeatures(doubleArr: DoubleArray, sinter: Int): Array<String?> {
        val featuresList = java.util.ArrayList<String>()
        val fft = Features.fft(doubleArr.clone())
        if (mFeatrues.get(0).isSelect) {
            featuresList.add(FUN_1_MINIMUM_LABLE.toString() + ":" + Features.minimum(doubleArr.clone()))
        }
        if (mFeatrues.get(1).isSelect) {
            featuresList.add(FUN_2_MAXIMUM_LABLE.toString() + ":" + Features.maximum(doubleArr.clone()))
        }
        if (mFeatrues.get(2).isSelect) {
            featuresList.add(FUN_3_MEANCROSSINGSRATE_LABLE.toString() + ":" + Features.meanCrossingsRate(doubleArr.clone()))
        }
        if (mFeatrues.get(3).isSelect) {
            featuresList.add(FUN_4_STANDARDDEVIATION_LABLE.toString() + ":" + Features.standardDeviation(doubleArr.clone()))
        }
        if (mFeatrues.get(4).isSelect) {
            featuresList.add(FUN_5_SPP_LABLE.toString() + ":" + Features.spp(fft.clone()))
        }
        if (mFeatrues.get(5).isSelect) {
            featuresList.add(FUN_6_ENERGY_LABLE.toString() + ":" + Features.energy(fft.clone()))
        }
        if (mFeatrues.get(6).isSelect) {
            featuresList.add(FUN_7_ENTROPY_LABLE.toString() + ":" + Features.entropy(fft.clone()))
        }
        if (mFeatrues.get(7).isSelect) {
            featuresList.add(FUN_8_CENTROID_LABLE.toString() + ":" + Features.centroid(fft.clone()))
        }
        if (mFeatrues.get(8).isSelect) {
            featuresList.add(FUN_9_MEAN_LABLE.toString() + ":" + Features.mean(doubleArr.clone()))
        }
        if (mFeatrues.get(9).isSelect) {
            featuresList.add(FUN_10_RMS_LABLE.toString() + ":" + Features.rms(doubleArr.clone()))
        }
        if (mFeatrues.get(10).isSelect) {
            featuresList.add(FUN_11_SMA_LABLE.toString() + ":" + Features.sma(doubleArr.clone(), sinter / 1000.0))
        }
        if (mFeatrues.get(11).isSelect) {
            featuresList.add(FUN_12_IQR_LABLE.toString() + ":" + Features.iqr(doubleArr.clone()))
        }
        if (mFeatrues.get(12).isSelect) {
            featuresList.add(FUN_13_MAD_LABLE.toString() + ":" + Features.mad(doubleArr.clone()))
        }
        if (mFeatrues.get(13).isSelect) {
            featuresList.add(FUN_14_TENERGY_LABLE.toString() + ":" + Features.tenergy(doubleArr.clone()))
        }
        if (mFeatrues.get(14).isSelect) {
            featuresList.add(FUN_15_FDEV_LABLE.toString() + ":" + Features.fdev(fft.clone()))
        }
        if (mFeatrues.get(15).isSelect) {
            featuresList.add(FUN_16_FMEAN_LABLE.toString() + ":" + Features.fmean(fft.clone()))
        }
        if (mFeatrues.get(16).isSelect) {
            featuresList.add(FUN_17_SKEW_LABLE.toString() + ":" + Features.skew(fft.clone()))
        }
        if (mFeatrues.get(17).isSelect) {
            featuresList.add(FUN_18_KURT_LABLE.toString() + ":" + Features.kurt(fft.clone()))
        }
        if (mFeatrues.get(18).isSelect) {
            featuresList.add(FUN_19_MEDIAN_LABLE.toString() + ":" + Features.median(doubleArr.clone()))
        }
        return listToString(featuresList)
    }

    private fun listToString(featuresList: List<String>): Array<String?> {
        val strings = arrayOfNulls<String>(featuresList.size)
        for (i in featuresList.indices) {
            strings[i] = featuresList[i]
        }
        return strings
    }

    /**
     * 把数据写入到文件
     * @param path
     * *
     * @param lable
     * *
     * @param features
     */
    fun writeToFile(path: String, lable: Int?, features: Array<String?>) {
        var bufferedWriter: BufferedWriter? = null
        try {
            bufferedWriter = BufferedWriter(OutputStreamWriter(FileOutputStream(path, true)))
            val stringBuilder = StringBuilder()
            stringBuilder.append(lable)
            for (feature in features) {
                stringBuilder.append(" " + feature)
            }
            stringBuilder.append("\n")
            bufferedWriter.write(stringBuilder.toString())
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                bufferedWriter!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    lateinit var mFeatures: Array<DoubleArray>
    var mScaleLower: Double = 0.0
    var mScaleUpper: Double = 0.0
    var mFeatureCount = 0
    lateinit var svm_load_model: svm_model

    /**
     * 读入range文件
     */
    fun readRange(path: String) {
        val bufferedReader = BufferedReader(InputStreamReader(FileInputStream(path)))
        var feature = bufferedReader.readLine()
        val range = java.util.ArrayList<String>()
        while (feature != null) {
            range.add(feature)
            feature = bufferedReader.readLine()
        }
        mFeatureCount = range.size - 2
        mFeatures = Array(mFeatureCount) { kotlin.DoubleArray(2) }
        var lowerAndUpper = range[1]
        val split = lowerAndUpper.split(" ")
        mScaleLower = split[0].toDouble()
        mScaleUpper = split[1].toDouble()
        for (i in 0..mFeatureCount - 1) {
            val featuresLowerAndUpper = range[i + 2].split(" ")
            mFeatures[i][0] = featuresLowerAndUpper[1].toDouble()
            mFeatures[i][1] = featuresLowerAndUpper[2].toDouble()
        }
    }

    /**
     * 加载model文件
     */
    fun loadModel(path: String) {
        svm_load_model = svm.svm_load_model(path)
    }

    /**
     * 加载文件
     */
    fun loadFile(rangeFile: String, modelFile: String) {
        readRange(rangeFile)
        loadModel(modelFile)
    }

    /**
     * 预测没有归一化的文件
     */
    fun predictUnScaleData(features: Array<String?>): Double {
        var px = arrayOfNulls<svm_node>(mFeatureCount)
        var p: svm_node
        for (i in 0..mFeatureCount - 1) {
            val tempNode = features[i]?.split(":")
            p = svm_node()
            p.index = tempNode!![0].toInt()
            p.value = Features.zeroOneLibSvm(mScaleLower, mScaleUpper, tempNode[1].toDouble(), mFeatures[i][0], mFeatures[i][1])
            px[i] = p
        }
        return svm.svm_predict(svm_load_model, px)
    }


    // svm监听器对象
    var mSVMListener: SVMListener

    /**
     * SVM监听器类，监听SVM状态。
     */
    interface SVMListener {

        /**
         * 加速度数据改变
         * @param acc 合成加速度 Math.sqrt((x * x + y * y + z * z).toDouble())
         */
        fun onSensorChanged(acc: Double)

        /**
         * 写入一个样本数据成功调用
         */
        fun onWriteSuccess()

        /**
         * 删除文件成功调用
         */
        fun deleteFileSuccess()

        /**
         * svm识别出结果调用
         */
        fun onTestSuccess(code: Int)

        /**
         * 训练模型成功调用
         * @param accuracyInfo 精度信息
         */
        fun onTrainSuccess(accuracyInfo: String)

        /**
         * 训练模型失败调用
         */
        fun onTrainFaile()
    }
}