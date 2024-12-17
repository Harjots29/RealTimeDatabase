package com.harjot.realtimedatabase

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StudentAdapter(var arrayList:ArrayList<StudentInfo>,var studentInterface: StudentInterface):
    RecyclerView.Adapter<StudentAdapter.ViewHolder>() {
    class ViewHolder(var view: View):RecyclerView.ViewHolder(view) {
        var name = view.findViewById<TextView>(R.id.tvName)
        var rollno = view.findViewById<TextView>(R.id.tvRollNo)
        var department = view.findViewById<TextView>(R.id.tvDepartment)
        var lv = view.findViewById<LinearLayout>(R.id.lv)
        var delete = view.findViewById<Button>(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        var view = LayoutInflater.from(parent.context).inflate(R.layout.student_list_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.setText(arrayList[position].name)
        holder.department.setText(arrayList[position].department)
        holder.rollno.setText(arrayList[position].rollNo.toString())
        holder.lv.setOnClickListener {
            studentInterface.listClick(position)
        }
        holder.delete.setOnClickListener {
            studentInterface.onDeleteClick(arrayList[position],position)
        }
    }
}