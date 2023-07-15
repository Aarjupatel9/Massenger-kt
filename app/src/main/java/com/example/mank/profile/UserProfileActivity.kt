package com.example.mank.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.mank.LocalDatabaseFiles.DAoFiles.MassegeDao
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.holdLoginData
import com.example.mank.MainActivity
import com.example.mank.configuration.GlobalVariables
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.mank.R
import io.socket.emitter.Emitter
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Boolean
import kotlin.Any
import kotlin.Array
import kotlin.ByteArray
import kotlin.Int
import kotlin.IntArray
import kotlin.String
import kotlin.also
import kotlin.arrayOf

class UserProfileActivity() : Activity() {
    private val url = GlobalVariables.URL_MAIN
    private var picUri: Uri? = null
    private var massegeDao: MassegeDao? = null
    private val permissionsToRequest: ArrayList<String>? = null
    private val permissionsRejected = ArrayList<String>()
    private val permissions = ArrayList<String>()
    var progress: ProgressDialog? = null
    private var ProfileUploadProgressBar: ProgressBar? = null
    private val fabCamera: FloatingActionButton? = null
    private var fabUpload: FloatingActionButton? = null
    private var user_Profile_photo: ImageView? = null
    private var bitmap: Bitmap? = null
    private var user_name_EditField: EditText? = null
    private var user_about_in_profile_page: EditText? = null

    //    private RadioGroup radioGroupForSelectingPrivacy;
    //    private RadioButton radioButton;
    var username: String? = null
    var aboutInfo: String? = null
    private var imageData: ByteArray? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_page)
        user_name_EditField = findViewById<View>(R.id.user_name_in_profile_page) as EditText
        user_about_in_profile_page = findViewById<View>(R.id.user_about_in_profile_page) as EditText
        //        radioGroupForSelectingPrivacy = (RadioGroup) findViewById(R.id.radioGroupForSelectingPrivacy);
//        //by default set to allow to all we will edit this according to database later
//        findViewById(R.id.onlineStatusAllowToAll).setSelected(true);
        val hold_LoginData = holdLoginData()
        val dataFromDatabase = hold_LoginData.data
        massegeDao = MainActivity.db!!.massegeDao()
        if (dataFromDatabase != null) {
            if (dataFromDatabase.displayUserName != null) {
                val userNameLocal = dataFromDatabase.displayUserName.toString()
                val aboutInfolocal = dataFromDatabase.about.toString()
                Log.d("log-oncreate-username", "onCreate: username is $userNameLocal")
                user_name_EditField!!.setText(userNameLocal, TextView.BufferType.EDITABLE)
                user_about_in_profile_page!!.setText(aboutInfolocal)
                username = userNameLocal
                aboutInfo = aboutInfolocal
            }
        }
        fabUpload = findViewById(R.id.fabUpload)
        user_Profile_photo = findViewById(R.id.userProfilePhoto)
        setUserImage()
        ProfileUploadProgressBar = findViewById(R.id.ProfileUploadProgressBar)
        progress = ProgressDialog(this)
        progress!!.setTitle("Loading")
        progress!!.setMessage("Wait while loading...")
        progress!!.setCancelable(false)
    }

    fun setUserImage() {
        val ti = Thread(object : Runnable {
            override fun run() {
//                byte[] selfUserImageData = massegeDao.getSelfUserImage(user_login_id, user_login_id);
//                if (selfUserImageData != null) {
//                    Bitmap selfImage = BitmapFactory.decodeByteArray(selfUserImageData, 0, selfUserImageData.length);
//                    Log.d("log-ContactListAdapter", "setUserImage : after fetch image form db : " + selfUserImageData.length);
//                    user_Profile_photo.setImageBitmap(selfImage);
//                }
                val imagePath =
                    "/storage/emulated/0/Android/media/com.massenger.mank.main/Pictures/profiles/" + MainActivity.user_login_id + MainActivity.user_login_id + ".png"
                var byteArray: ByteArray? = null
                try {
                    val imageFile = File(imagePath)
                    val fis = FileInputStream(imageFile)
                    val bos = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while ((fis.read(buffer).also { bytesRead = it }) != -1) {
                        bos.write(buffer, 0, bytesRead)
                    }
                    fis.close()
                    bos.close()
                    byteArray = bos.toByteArray()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (byteArray != null) {
                    val selfImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    Log.d(
                        "log-ContactListAdapter",
                        "setUserImage : after fetch image form file system : " + byteArray.size
                    )
                    runOnUiThread(Runnable { user_Profile_photo!!.setImageBitmap(selfImage) })
                }
            }
        })
        ti.start()
    }

    private val onUpdateUserDisplayName_return: Emitter.Listener = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.d(
                "log-onUpdateUserDisplayName_return",
                "call: onUpdateUserDisplayName_return enter "
            )
            runOnUiThread(object : Runnable {
                override fun run() {
                    username = user_name_EditField!!.text.toString()
                    Toast.makeText(
                        this@UserProfileActivity,
                        "Display name is updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }
    }
    private val onUpdateUserAboutInfo_return: Emitter.Listener = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.d("log-onUpdateUserAboutInfo_return", "call: onUpdateUserAboutInfo_return enter ")
            runOnUiThread(object : Runnable {
                override fun run() {
                    aboutInfo = user_about_in_profile_page!!.text.toString()
                    Toast.makeText(applicationContext, "About is updated", Toast.LENGTH_SHORT)
                        .show()
                }
            })
        }
    }
    private val onUpdateUserProfileImage_return: Emitter.Listener = object : Emitter.Listener {
        override fun call(vararg args: Any) {
            Log.d("log-onChangeUserProfileImage_return", "start")
            runOnUiThread(object : Runnable {
                override fun run() {
                    Toast.makeText(applicationContext, "Image is updated", Toast.LENGTH_SHORT)
                        .show()
                    ProfileUploadProgressBar!!.visibility = View.GONE
                }
            })
        }
    }


    //            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
    private val captureImageOutputUri: Uri?
        private get() {
            var outputFileUri: Uri? = null
            val getImage = getExternalFilesDir("")
            if (getImage != null) {
//            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
                outputFileUri = FileProvider.getUriForFile(
                    this,
                    this.applicationContext.packageName + ".provider",
                    File(getImage.path, "profile.png")
                )
            }
            return outputFileUri
        }
    private var savePass = false
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            Log.d(
                "log-UserProfileActivity",
                "onActivityResult r(resultCode == Activity.RESULT_OK start"
            )
            if (requestCode == IMAGE_RESULT) {
                fabUpload!!.visibility = View.VISIBLE
                val filePath = getImageFilePath(data)
                if (filePath != null) {
                    Log.d(
                        "log-UserProfileActivity",
                        "onActivityResult filepath not null : $filePath"
                    )
                    val Ti = Thread(object : Runnable {
                        override fun run() {

//                            bitmap = BitmapFactory.decodeFile(filePath);
                            savePass = true
                            val options = BitmapFactory.Options()
                            options.inJustDecodeBounds = true
                            BitmapFactory.decodeFile(filePath, options)
                            val imageWidth = options.outWidth
                            val imageHeight = options.outHeight
                            var scaleFactor = 1
                            if (imageWidth > TARGET_RESOLUTION || imageHeight > TARGET_RESOLUTION) {
                                scaleFactor = Math.pow(
                                    2.0,
                                    Math.ceil(
                                        Math.log(
                                            Math.max(
                                                imageWidth,
                                                imageHeight
                                            ) / TARGET_RESOLUTION.toDouble()
                                        ) / Math.log(0.5)
                                    ).toInt().toDouble()
                                ).toInt()
                            }
                            options.inJustDecodeBounds = false
                            options.inSampleSize = scaleFactor
                            bitmap = BitmapFactory.decodeFile(filePath, options)
                            if (bitmap?.getWidth() != TARGET_RESOLUTION || bitmap?.getHeight() != TARGET_RESOLUTION) {
                                val scaledBitmap = bitmap?.let {
                                    Bitmap.createScaledBitmap(it, TARGET_RESOLUTION, TARGET_RESOLUTION, true)
                                } ?: bitmap

                                bitmap?.recycle()
                                bitmap = scaledBitmap
                            }
                            val stream = ByteArrayOutputStream()
                            bitmap!!.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
                            runOnUiThread(object : Runnable {
                                override fun run() {
//                                    Toast.makeText(UserProfileActivity.this, "image resolution is : " + bitmap.getHeight() + "*" + bitmap.getWidth(), Toast.LENGTH_LONG).show();
                                }
                            })
                            imageData = stream.toByteArray()
                            runOnUiThread(object : Runnable {
                                override fun run() {
                                    val compressedImageLength = imageData?.size
                                    if (compressedImageLength != null) {
                                        if (compressedImageLength > (200 * 1024)) {
                                            Toast.makeText(
                                                this@UserProfileActivity,
                                                "image size is two large for store into database",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            user_Profile_photo!!.setImageBitmap(bitmap)
                                        }
                                    }
                                }
                            })
                        }
                    })
                    Ti.start()
                }
            }
        }
    }

    private fun getImageFromFilePath(data: Intent?): String? {
        val isCamera = data == null || data.data == null
        return if (isCamera) {
            captureImageOutputUri!!.path
        } else {
            getPathFromURI(data!!.data)
        }
    }

    fun getImageFilePath(data: Intent?): String? {
        return getImageFromFilePath(data)
    }

    private fun getPathFromURI(contentUri: Uri?): String {
        val proj = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor = contentResolver.query((contentUri)!!, proj, null, null, null)
        val column_index = cursor!!.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable("pic_uri", picUri)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // get the file url
        picUri = savedInstanceState.getParcelable("pic_uri")
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ALL_PERMISSIONS_RESULT -> {
                for (perms: String in permissionsToRequest!!) {
//                    if (!hasPermission(perms)) {
                    permissionsRejected.add(perms)
                    //                    }
                }
                if (permissionsRejected.size > 0) {
                    if (shouldShowRequestPermissionRationale(permissionsRejected[0])) {
                        showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                            object : DialogInterface.OnClickListener {
                                override fun onClick(dialog: DialogInterface, which: Int) {
                                    requestPermissions(
                                        permissionsRejected.toTypedArray(),
                                        ALL_PERMISSIONS_RESULT
                                    )
                                }
                            })
                        return
                    }
                }
            }
        }
    }

    @SuppressLint("IntentReset")
    fun ProfilePhotoOnClick(view: View?) {
        Log.d("log-UserProfileActivity", "ProfilePhotoOnClick method start")
        fabUpload!!.visibility = View.GONE
        fabUpload!!.isEnabled = Boolean.TRUE
        Log.d("log-UserProfileActivity", "ProfilePhotoOnClick method before startActivityForResult")
        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"
        val chooserIntent = Intent.createChooser(pickIntent, "Select Image")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        startActivityForResult(chooserIntent, IMAGE_RESULT)
    }

    fun FabUploadOnClick(view: View?) {
        ProfileUploadProgressBar!!.visibility = View.VISIBLE
        if (imageData != null) {
            if (MainActivity.socket != null) {
                MainActivity.contactListAdapter!!.updateSelfUserImage(imageData!!)
                MainActivity.socket!!.on(
                    "updateUserProfileImage_return",
                    onUpdateUserProfileImage_return
                )
                MainActivity.socket!!.emit(
                    "updateUserProfileImage",
                    MainActivity.user_login_id,
                    imageData
                )
                saveImageToInternalStorage(bitmap)
            } else {
                Toast.makeText(
                    applicationContext,
                    "profile photo can not be update right now, please try again after soe time",
                    Toast.LENGTH_SHORT
                ).show()
                ProfileUploadProgressBar!!.visibility = View.GONE
            }
        } else {
            Toast.makeText(applicationContext, "Bitmap is null. Try again", Toast.LENGTH_SHORT)
                .show()
            ProfileUploadProgressBar!!.visibility = View.GONE
        }
    }

    fun UpdateUserProfileDetails(view: View?) {
        val set_display_name = user_name_EditField!!.text.toString()
        val set_about_name = user_about_in_profile_page!!.text.toString()
        if (set_display_name != username) {
            if (MainActivity.socket != null) {
                val success_status =
                    massegeDao!!.updateDisplayUserName(set_display_name, MainActivity.user_login_id)
                Log.d("log-success_status", "UpdateUserDisplayName: status is $success_status")
                MainActivity.socket!!.on(
                    "updateUserDisplayName_return",
                    onUpdateUserDisplayName_return
                )
                MainActivity.socket!!.emit(
                    "updateUserDisplayName",
                    MainActivity.user_login_id,
                    set_display_name
                )
            } else {
                Toast.makeText(
                    applicationContext,
                    "username can not be update right now, please try again after soe time",
                    Toast.LENGTH_SHORT
                ).show()
                ProfileUploadProgressBar!!.visibility = View.GONE
            }
        }
        if (set_about_name != aboutInfo) {
            if ((set_about_name == "")) {
                Toast.makeText(this, "About Can not be empty", Toast.LENGTH_SHORT).show()
            } else {
                if (MainActivity.socket != null) {
                    MainActivity.socket!!.on(
                        "updateUserAboutInfo_return",
                        onUpdateUserAboutInfo_return
                    )
                    MainActivity.socket!!.emit(
                        "updateUserAboutInfo",
                        MainActivity.user_login_id,
                        set_about_name
                    )
                    val success_status =
                        massegeDao!!.updateAboutUserName(set_about_name, MainActivity.user_login_id)
                    Log.d("log-success_status", "UpdateUserAboutInfo: status is $success_status")
                } else {
                    Toast.makeText(
                        applicationContext,
                        "about status can not be update right now, please try again after soe time",
                        Toast.LENGTH_SHORT
                    ).show()
                    ProfileUploadProgressBar!!.visibility = View.GONE
                }
            }
        }
    }

    //    public void UpdateUserOnlineStatusPrivacy(View view) {
    //        int selectedId = radioGroupForSelectingPrivacy.getCheckedRadioButtonId();
    //
    //        // find the radiobutton by returned id
    //        radioButton = (RadioButton) findViewById(selectedId);
    //        Log.d("log-radio", "radio select : " + radioButton.getText());
    ////        int success_status = MassegeDao.updateAboutUserName(set_about_name, user_login_id);
    ////        Log.d("log-success_status", "UpdateUserDisplayName: status is " + success_status);
    //    }
    fun userProfilePhotoLabelOnClick(view: View?) {
        Log.d("log-UserProfileActivity", "userProfilePhotoLabelOnClick method start")
        writeFileOnInternalStorage(this, "try.txt", "testing")
        Log.d("log-UserProfileActivity", "userProfilePhotoLabelOnClick method end")
    }

    fun writeFileOnInternalStorage(mcoContext: Context, sFileName: String?, sBody: String) {
        val file = File(this.filesDir, "try.txt")
        Log.d(
            "log-UserProfileActivity",
            "writeFileOnInternalStorage method before if cond. dir:$file"
        )
        try {
            mcoContext.openFileOutput(sFileName, MODE_PRIVATE)
                .use { fos -> fos.write(sBody.toByteArray()) }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(
                "log-UserProfileActivity-IOException",
                "writeFileOnInternalStorage method IOException:$e"
            )
        }

//        try {
//            Log.d("log-UserProfileActivity", "writeFileOnInternalStorage method try enter");
//            File gpxfile = new File(dir, sFileName);
//            FileWriter writer = new FileWriter(gpxfile);
//            writer.append(sBody);
//            writer.flush();
//            writer.close();
//            Log.d("log-UserProfileActivity", "writeFileOnInternalStorage method before try end");
//        } catch (Exception e){
//            Log.d("log-UserProfileActivity-Exception", "userProfilePhotoLabelOnClick method Exception:"+e);
//            e.printStackTrace();
//        }
    }

    private fun saveImageToInternalStorage(bitmapImage: Bitmap?) {
        // Get the application-specific directory path
//        File directory = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "profiles");
        val directory = File(
            Environment.getExternalStorageDirectory(),
            "Android/media/com.massenger.mank.main/Pictures/Profiles"
        )

        // Create the directory if it doesn't exist
        if (!directory.exists()) {
            val x = directory.mkdirs()
        }

        // Create the file path
        val imagePath =
            File(directory, "" + MainActivity.user_login_id + MainActivity.user_login_id + ".png")

        // Save the bitmap image to the file
        try {
            FileOutputStream(imagePath).use { outputStream ->
                bitmapImage!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("log-saveImageToInternalStorage", "Image Save failed $e")
        }
        // Print the absolute path of the saved image
        Log.d("log-saveImageToInternalStorage", "Saved image path: " + imagePath.absolutePath)
    }

    //    ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
    //            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
    //                // Callback is invoked after the user selects a media item or closes the
    //                // photo picker.
    //                if (uri != null) {
    //                    Log.d("PhotoPicker", "Selected URI: " + uri);
    //                } else {
    //                    Log.d("PhotoPicker", "No media selected");
    //                }
    //            });
    fun FinishAUPPActivity(view: View?) {
        finish()
    }

    companion object {
        private val ALL_PERMISSIONS_RESULT = 107
        private val IMAGE_RESULT = 200
        private val TARGET_RESOLUTION = 1024
        private val JPEG_QUALITY = 80
    }
}