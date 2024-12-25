package com.harjot.realtimedatabase

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.window.application
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.harjot.realtimedatabase.databinding.CustomDialogLayoutBinding
import com.harjot.realtimedatabase.databinding.FragmentStudentBinding
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.UploadStatus
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.uploadAsFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val pickImageRequest = 1
    val permissionRequestCode = 100
    val externalStorageRequestCode = 101
    lateinit var binding: FragmentStudentBinding
    lateinit var mainActivity: MainActivity
    lateinit var navController: NavController
    var dbReference: DatabaseReference = FirebaseDatabase.getInstance().reference  //database declaration and initialization
    var arrayList = ArrayList<StudentInfo>()
    var studentAdapter = StudentAdapter(arrayList,this,this)
    lateinit var supabaseClient: SupabaseClient
    var imgUri: Uri? = null
    lateinit var dialogBinding : CustomDialogLayoutBinding
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
        navController = findNavController()
        binding = FragmentStudentBinding.inflate(layoutInflater)
        dialogBinding = CustomDialogLayoutBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment

        supabaseClient = (mainActivity.application as MyApplication).supabaseClient
        checkAndRequestPermission()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rv.layoutManager = LinearLayoutManager(mainActivity)
        binding.rv.adapter = studentAdapter

//        dialogBinding.btnAdd.setOnClickListener {
//            Toast.makeText(mainActivity, "in add click", Toast.LENGTH_SHORT).show()
////            uploadImageToSupabase(imgUri!!)
//
//        }
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
    override fun listClick(studentInfo: StudentInfo, position: Int) {
        navController.navigate(R.id.studentDetailsFragment,
            bundleOf("name" to studentInfo.name,
                "department" to studentInfo.department,
                "rollNo" to studentInfo.rollNo,
                "image" to studentInfo.image))
    }

    override fun onUpdateClick(position: Int) {
        dialog(position)
    }

    override fun onDeleteClick(studentInfo: StudentInfo,position: Int) {
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
        dialogBinding = CustomDialogLayoutBinding.inflate(layoutInflater)
        var dialog = Dialog(mainActivity).apply {
            setContentView(dialogBinding.root)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            if (position > -1){
                dialogBinding.btnAdd.setText("Update")
                dialogBinding.etName.setText(arrayList[position].name)
                dialogBinding.etRollNo.setText(arrayList[position].rollNo.toString())
                dialogBinding.etDepartment.setText(arrayList[position].department)
                Glide.with(this@StudentFragment)
                    .load(arrayList[position].image)
                    .centerCrop()
                    .into(dialogBinding.ivImage)
            }else{
                dialogBinding.btnAdd.setText("Add")
            }
            dialogBinding.ivImage.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent,pickImageRequest)
            }
            dialogBinding.btnAdd.setOnClickListener {
                Toast.makeText(mainActivity, "in add click", Toast.LENGTH_SHORT).show()
                if (dialogBinding.etName.text.toString().trim().isNullOrEmpty()){
                    dialogBinding.etName.error = "Enter Name"
                }else  if (dialogBinding.etRollNo.text.toString().trim().isNullOrEmpty()){
                    dialogBinding.etRollNo.error = "Enter RollNo"
                }else  if (dialogBinding.etDepartment.text.toString().trim().isNullOrEmpty()){
                    dialogBinding.etDepartment.error = "Enter Department"
                }else{
                    val studentInfo = StudentInfo("",
                        dialogBinding.etName.text.toString(),
                        dialogBinding.etDepartment.text.toString(),
                        dialogBinding.etRollNo.text.toString().toInt(),
                        imgUri.toString())
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
                            dialogBinding.etRollNo.text.toString().toInt(),
                            imgUri.toString()
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
//                        uploadImageToSupabase(dialogBinding.ivImage.toString().toUri())
                        uploadImageToSupabase(imgUri!!)
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
            show()
        }
    }
    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()){
                    //permission granted, proceed
                }else{
                    //ask for permission
                    requestManageExternalStoragePermission()
                }
            }else{
                if(ContextCompat.checkSelfPermission(mainActivity,
                        android.Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    requestManageExternalStoragePermission()
                }
            }
        }else{
            if (ContextCompat.checkSelfPermission(mainActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(mainActivity,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), permissionRequestCode)
            }
        }
    }
    private fun requestManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            try{
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                startActivityForResult(intent,permissionRequestCode)
            }catch (e: ActivityNotFoundException){
                Toast.makeText(mainActivity, "Activity not Found", Toast.LENGTH_SHORT).show()
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            permissionRequestCode->{
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(mainActivity, "granted", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(mainActivity, "denied", Toast.LENGTH_SHORT).show()
                }
            }
            externalStorageRequestCode ->{
                if (Environment.isExternalStorageManager()){
                    Toast.makeText(mainActivity, "full storage access granted", Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(mainActivity, "permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImageRequest){
            data?.data?.let { uri->
                dialogBinding.ivImage.setImageURI(uri)
                imgUri = uri
//                uploadImageToSupabase(uri)
            }
        }
    }
    private fun uploadImageToSupabase(uri: Uri) {
        Toast.makeText(mainActivity, "inUploadImage", Toast.LENGTH_SHORT).show()
        val byteArray = uriToByteArray(mainActivity,uri)
        val filename = "images/${System.currentTimeMillis()}.jpg"

        val bucket = supabaseClient.storage.from("student_details_realtime")

        lifecycleScope.launch(Dispatchers.IO) {
            try{
                bucket.uploadAsFlow(filename,byteArray).collect{status->
                    withContext(Dispatchers.Main){
                        when(status){
                            is UploadStatus.Progress->{
                                println("InProgress")
                                Toast.makeText(mainActivity, "progress", Toast.LENGTH_SHORT).show()
                            }
                            is UploadStatus.Success->{
                                println("InSuccess")
                                Toast.makeText(mainActivity, "Upload Success", Toast.LENGTH_SHORT).show()
                                val imgUrl = bucket.publicUrl(filename)
                                val img = dialogBinding.ivImage

                                Glide.with(mainActivity)
                                    .load(imgUrl)
                                    .placeholder(R.drawable.ic_img)
                                    .into(img)

                                Toast.makeText(mainActivity, "upload Success", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }catch (e: Exception){
                val TAG = "Upload"
                Log.e(TAG, "uploadImageToSupabase: ${e.message}", )
//                Toast.makeText(this@MainActivity, "${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun uriToByteArray(context: Context, uri: Uri): ByteArray {
        val inputStream = context.contentResolver.openInputStream(uri)
        return inputStream?.readBytes() ?: ByteArray(0)
    }
}