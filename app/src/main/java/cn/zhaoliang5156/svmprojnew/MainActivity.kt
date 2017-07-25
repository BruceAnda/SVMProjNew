package cn.zhaoliang5156.svmprojnew

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import cn.zhaoliang5156.androidsvmlib.adapter.ControllerAdapter
import cn.zhaoliang5156.androidsvmlib.adapter.FeatureAdapter
import cn.zhaoliang5156.androidsvmlib.adapter.LableAdapter
import cn.zhaoliang5156.androidsvmlib.domain.ControllerData
import cn.zhaoliang5156.androidsvmlib.domain.FeatureData
import cn.zhaoliang5156.androidsvmlib.domain.LableData
import cn.zhaoliang5156.androidsvmlib.features.SVM
import kotlinx.android.synthetic.main.activity_main.*

/**
 * <pre>
 *     主界面
 *     @author zhaoliang 2017-7-25 V1
 * </pre>
 */
class MainActivity : AppCompatActivity() {

    // svm 对象，程序的核心
    lateinit var svm: SVM
    // 加速度读数据的频率
    var hz = ((1000 * 1000) / 32.0).toInt()

    // 点击lable的监听器对象
    var onLableItemClickListener = object : LableAdapter.OnLableItemClickListener {
        override fun onControllerItemClick(view: View, position: Int) {
            // 改变图片样式
            lableView.notifyDataSetIcon(position)
            // 改变界面上的lable
            tvLable.text = "Label:${position}"
            // 给svm对象设置label
            svm.trainLable = position
        }

    }

    // 选择feature的监听器对象
    var onFeatureItemClickListener = object : FeatureAdapter.OnFeatureItemClickListener {
        override fun onControllerItemClick(view: View, position: Int) {
            featureView.notifyDataSetChange(position)
            svm.setFeatrues(featureView.getData())
        }
    }

    // 点击控制按钮的监听器对象
    var onControllerItemClickListener = object : ControllerAdapter.OnControllerItemClickListener {
        override fun onControllerItemClick(view: View, position: Int) {
            when (position) {
                0 -> {
                    // 当点击收集数据的时候设置成收集数据模式
                    lableView.setCollectionMode(true)
                    svm.startCollection()
                }
                1 -> {
                    // 训练mode
                    svm.train()
                }
                2 -> {
                    // 当点击测试的时候设置成测试模式
                    lableView.setCollectionMode(false)
                    svm.test()
                }
                3 -> {
                    // 删除文件
                    svm.deleteFile()
                }
            }
            contrllerView.notifyDataSetChange(position)
        }
    }

    var onControllerListener = object : SVM.SVMListener {
        override fun onSensorChanged(acc: Double) {
            tvAcc.text = "ACC:${acc}"
        }

        override fun onTrainFaile() {
            Toast.makeText(this@MainActivity, "样本错误，训练失败，请检查样本文件", Toast.LENGTH_SHORT).show()
        }

        override fun onTestSuccess(code: Int) {
            runOnUiThread {
                tvLable.text = "识别结果${code}"
                lableView.notifyDataSetIcon(code)
            }
        }

        override fun onTrainSuccess(accuracyInfo: String) {
            runOnUiThread {
                tvAccuracy.text = accuracyInfo
                contrllerView.notifyDataSetChange(1)
            }
        }

        override fun deleteFileSuccess() {
            runOnUiThread {
                Toast.makeText(this@MainActivity, "删除文件成功！", Toast.LENGTH_SHORT).show()
                contrllerView.notifyDataSetChange(3)
                lableView.notifyDataSetChange()
            }
        }

        override fun onWriteSuccess() {
            lableView.notifyDataSetLable(svm.trainLable!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        svm = SVM(this@MainActivity, hz, "${filesDir}", onControllerListener)

        // Lable 部分
        val lableDataList = ArrayList<LableData>()
        lableDataList.add(LableData(R.mipmap.gait_walk_off, R.mipmap.gait_walk, R.mipmap.gait_walk_test, 0, 1, "${filesDir}/train0", 0))
        lableDataList.add(LableData(R.mipmap.gait_walk_off, R.mipmap.gait_walk, R.mipmap.gait_walk_test, 0, 0, "${filesDir}/train1", 0))
        lableDataList.add(LableData(R.mipmap.gait_walk_off, R.mipmap.gait_walk, R.mipmap.gait_walk_test, 0, 0, "${filesDir}/train2", 0))
        lableDataList.add(LableData(R.mipmap.gait_walk_off, R.mipmap.gait_walk, R.mipmap.gait_walk_test, 0, 0, "${filesDir}/train3", 0))
        lableDataList.add(LableData(R.mipmap.gait_walk_off, R.mipmap.gait_walk, R.mipmap.gait_walk_test, 0, 0, "${filesDir}/train4", 0))
        lableDataList.add(LableData(R.mipmap.gait_walk_off, R.mipmap.gait_walk, R.mipmap.gait_walk_test, 0, 0, "${filesDir}/train5", 0))
        lableDataList.add(LableData(R.mipmap.gait_walk_off, R.mipmap.gait_walk, R.mipmap.gait_walk_test, 0, 0, "${filesDir}/train6", 0))
        lableDataList.add(LableData(R.mipmap.gait_walk_off, R.mipmap.gait_walk, R.mipmap.gait_walk_test, 0, 0, "${filesDir}/train7", 0))
        lableView.setData(lableDataList, 4, onLableItemClickListener)

        // Feature 部分
        var featureDataList = ArrayList<FeatureData>()
        featureDataList.add(FeatureData(true, "最小值"))
        featureDataList.add(FeatureData(true, "最大值"))
        featureDataList.add(FeatureData(true, "过均值率"))
        featureDataList.add(FeatureData(true, "标准差"))
        featureDataList.add(FeatureData(true, "谱峰位置"))
        featureDataList.add(FeatureData(true, "频域能量"))
        featureDataList.add(FeatureData(true, "熵"))
        featureDataList.add(FeatureData(true, "质心"))
        featureDataList.add(FeatureData(true, "平均值"))
        featureDataList.add(FeatureData(true, "方根平均值"))
        featureDataList.add(FeatureData(true, "幅值面积"))
        featureDataList.add(FeatureData(true, "四分卫距"))
        featureDataList.add(FeatureData(true, "绝对平均差"))
        featureDataList.add(FeatureData(true, "时域能量"))
        featureDataList.add(FeatureData(true, "频域标准备差"))
        featureDataList.add(FeatureData(true, "频域平均值"))
        featureDataList.add(FeatureData(true, "频域偏度"))
        featureDataList.add(FeatureData(true, "频域峰度"))
        featureDataList.add(FeatureData(true, "中位数"))
        featureView.setData(featureDataList, 4, onFeatureItemClickListener)
        svm.setFeatrues(featureView.getData())
        // Controller 部分

        val contrllerDataList = ArrayList<ControllerData>()
        contrllerDataList.add(ControllerData(R.mipmap.sample_off, R.mipmap.sample, false, "采集"))
        contrllerDataList.add(ControllerData(R.mipmap.train_off, R.mipmap.train, false, "训练"))
        contrllerDataList.add(ControllerData(R.mipmap.test_off, R.mipmap.test, false, "测试"))
        contrllerDataList.add(ControllerData(R.mipmap.delete_off, R.mipmap.delete, false, "删除"))
        contrllerView.setData(contrllerDataList, 4, onControllerItemClickListener)
    }
}
