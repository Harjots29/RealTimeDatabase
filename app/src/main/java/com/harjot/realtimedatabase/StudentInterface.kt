package com.harjot.realtimedatabase

interface StudentInterface {
    fun listClick(studentInfo: StudentInfo,position:Int)
    fun onUpdateClick(position: Int)
    fun onDeleteClick(studentInfo: StudentInfo,position: Int)
}