package com.harjot.realtimedatabase

import android.app.Dialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.harjot.realtimedatabase.databinding.CustomDialogLayoutBinding
import com.harjot.realtimedatabase.databinding.FragmentStudentBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StudentFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StudentFragment : Fragment(),StudentInterface {
    lateinit var binding: FragmentStudentBinding
    lateinit var mainActivity: MainActivity
    var dbReference: DatabaseReference = FirebaseDatabase.getInstance().reference  //database declaration and initialization
    var arrayList = ArrayList<StudentInfo>()
    var studentAdapter = StudentAdapter(arrayList,this)
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity = activity as MainActivity
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        dbReference.addChildEventListener(object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val studentInfo: StudentInfo? = snapshot.getValue(StudentInfo::class.java)
                studentInfo?.id = snapshot.key.toString()
                if (studentInfo!=null){
                    arrayList.add(studentInfo)
                    studentAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val studentInfo: StudentInfo? = snapshot.getValue(StudentInfo::class.java)
                studentInfo?.id = snapshot.key.toString()
                if (studentInfo!=null){
                    arrayList.forEachIndexed { index, studentInfoData ->
                        if (studentInfoData.id == studentInfo.id){
                            arrayList[index] = studentInfo
                            return@forEachIndexed
                        }
                    }
                    studentAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val studentInfo: StudentInfo? = snapshot.getValue(StudentInfo::class.java)
                studentInfo?.id = snapshot.key.toString()
                if(studentInfo!=null){
                    arrayList.remove(studentInfo)
                    studentAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStudentBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rv.layoutManager = LinearLayoutManager(mainActivity)
        binding.rv.adapter = studentAdapter

        binding.fabAdd.setOnClickListener {
            dialog()
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StudentFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StudentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    override fun listClick(position: Int) {
        dialog(position)
    }

    override fun onDeleteClick(studentInfo: StudentInfo, position: Int) {
        var alertDialog = AlertDialog.Builder(mainActivity)
        alertDialog.setTitle("Delete Item")
        alertDialog.setMessage("Do you want to delete the item?")
        alertDialog.setCancelable(false)
        alertDialog.setNegativeButton("No") { _, _ ->
            alertDialog.setCancelable(true)
        }
        alertDialog.setPositiveButton("Yes") { _, _ ->
            if (arrayList.size == 0){
                Toast.makeText(mainActivity, "List Is Empty", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(
                    mainActivity,
                    "The item is  deleted",
                    Toast.LENGTH_SHORT
                ).show()
                        dbReference.child(studentInfo.id?:"").removeValue()
            }
        }
        alertDialog.show()
    }

    fun dialog(position: Int = -1){
        var dialogBinding = CustomDialogLayoutBinding.inflate(layoutInflater)
        var dialog = Dialog(mainActivity).apply {
            setContentView(dialogBinding.root)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            if (position == -1){
                dialogBinding.btnAdd.setText("Add")
                dialogBinding.btnDelete.visibility = View.GONE
            }else{
                dialogBinding.btnAdd.setText("Update")
                dialogBinding.btnDelete.visibility = View.VISIBLE
                dialogBinding.etName.setText(arrayList[position].name)
                dialogBinding.etRollNo.setText(arrayList[position].rollNo.toString())
                dialogBinding.etDepartment.setText(arrayList[position].department)
            }
            dialogBinding.btnAdd.setOnClickListener {
                val studentInfo = StudentInfo("",
                    dialogBinding.etName.text.toString(),
                    dialogBinding.etDepartment.text.toString(),
                    dialogBinding.etRollNo.text.toString().toInt())

                if (dialogBinding.etName.text.toString().trim().isNullOrEmpty()){
                    dialogBinding.etName.error = "Enter Name"
                }else  if (dialogBinding.etRollNo.text.toString().trim().isNullOrEmpty()){
                    dialogBinding.etRollNo.error = "Enter RollNo"
                }else  if (dialogBinding.etDepartment.text.toString().trim().isNullOrEmpty()){
                    dialogBinding.etDepartment.error = "Enter Department"
                }else{
                    if (position > -1){
//                        arrayList[position] = StudentInfo(
//                            "",
//                            dialogBinding.etName.text.toString(),
//                            dialogBinding.etDepartment.text.toString(),
//                            dialogBinding.etRollNo.text.toString().toInt(),
//                        )
                        val key = arrayList[position].id
                        val data = StudentInfo(
                            "",
                            dialogBinding.etName.text.toString(),
                            dialogBinding.etDepartment.text.toString(),
                            dialogBinding.etRollNo.text.toString().toInt()
                        )
                        val update = hashMapOf<String,Any>(
                            "$key" to data
                        )
                        dbReference.updateChildren(update)
                    }else{
                        Log.e(TAG, "dialog: fabbutton", )
//                        arrayList.add(StudentInfo(
//                            null,
//                            dialogBinding.etName.text.toString(),
//                            dialogBinding.etDepartment.text.toString(),
//                            dialogBinding.etRollNo.text.toString().toString().toInt())
//                        )
                        dbReference.push().setValue(studentInfo)
                            .addOnCompleteListener {
                                Toast.makeText(mainActivity, "Menu add", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(mainActivity, "Failed", Toast.LENGTH_SHORT).show()
                            }
                    }
                    dismiss()
                }
            }
            dialogBinding.btnDelete.setOnClickListener {
                var alertDialog = AlertDialog.Builder(mainActivity)
                alertDialog.setTitle("Delete Item")
                alertDialog.setMessage("Do you want to delete the item?")
                alertDialog.setCancelable(false)
                alertDialog.setNegativeButton("No") { _, _ ->
                    alertDialog.setCancelable(true)
                }
                alertDialog.setPositiveButton("Yes") { _, _ ->
                    if (arrayList.size == 0){
                        Toast.makeText(mainActivity, "List Is Empty", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        Toast.makeText(
                            mainActivity,
                            "The item is  deleted",
                            Toast.LENGTH_SHORT
                        ).show()
//                        dbReference.child(studentInfo.id?:").removeValue()
                        dismiss()
                    }
                }
                alertDialog.show()
            }
            show()
        }
    }
}