package com.harjot.realtimedatabase

interface StudentInterface {
    fun listClick(position:Int)
    fun onDeleteClick(studentInfo: StudentInfo,position: Int)
}