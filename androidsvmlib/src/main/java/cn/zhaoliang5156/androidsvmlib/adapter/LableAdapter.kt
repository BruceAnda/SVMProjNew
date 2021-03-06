package cn.zhaoliang5156.androidsvmlib.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.zhaoliang5156.androidsvmlib.R
import cn.zhaoliang5156.androidsvmlib.domain.LableData
import kotlinx.android.synthetic.main.lable_layout.view.*

/**
 * Created by zhaoliang on 2017/7/22.
 */
class LableAdapter(var context: Context, var dataList: ArrayList<LableData>) : RecyclerView.Adapter<LableAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.lable_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (onLabelClickListener != null) {
            holder.itemView.setOnClickListener {
                onLabelClickListener.onControllerItemClick(holder.itemView, position)
            }
        }
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        // 判断传过来的列表数据集是否为空，如果为空返回0，如果不为空返回列表数量
        if (dataList == null) {
            return 0
        } else {
            return dataList.size
        }
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: LableData) {
            when (item.status) {
                0 -> {
                    itemView.imageView.setImageResource(item.iconIDDefault)
                }
                1 -> {
                    itemView.imageView.setImageResource(item.iconIDLableSelect)
                }
                2 -> {
                    itemView.imageView.setImageResource(item.iconIDLableTest)
                }
            }
            itemView.lableCount.text = item.labelCount.toString()
        }
    }

    lateinit var onLabelClickListener: OnLableItemClickListener

    interface OnLableItemClickListener {
        fun onControllerItemClick(view: View, position: Int)
    }
}