package com.example.mank.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.room.Room
import com.example.mank.LocalDatabaseFiles.DAoFiles.UserDao
import com.example.mank.LocalDatabaseFiles.DataContainerClasses.holdLoginData
import com.example.mank.LocalDatabaseFiles.MainDatabaseClass
import com.example.mank.MainActivity
import com.example.mank.MainActivity.Companion.user_login_id
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
	private var picUri: Uri? = null
	private var userDao: UserDao? = null
	private val permissionsToRequest: ArrayList<String>? = null
	private val permissionsRejected = ArrayList<String>()
	private var ProfileUploadProgressBar: ProgressBar? = null
	private var user_Profile_photo: ImageView? = null
	private var bitmap: Bitmap? = null
	private var user_name_EditField: EditText? = null
	private var user_about_in_profile_page: EditText? = null
	var username: String? = null
	var aboutInfo: String? = null
	private var imageData: ByteArray? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_user_profile_page)

		findViewById<ImageButton>(R.id.AUPPBackActivity).setOnClickListener { this.finish() }

		findViewById<Button>(R.id.AUPPSaveButton).setOnClickListener { updateUserProfileDetails() }

		user_name_EditField = findViewById<EditText>(R.id.user_name_in_profile_page)
		user_about_in_profile_page = findViewById<EditText>(R.id.user_about_in_profile_page)
		ProfileUploadProgressBar = findViewById<ProgressBar>(R.id.ProfileUploadProgressBar)

		val holdLoginData = holdLoginData()
		val dataFromDatabase = holdLoginData.data

		MainActivity.db?.let {
			userDao = MainActivity.db?.userDao()
		} ?: run {
			MainActivity.db = Room.databaseBuilder(applicationContext, MainDatabaseClass::class.java, "MassengerDatabase").fallbackToDestructiveMigration().allowMainThreadQueries().build()
			userDao = MainActivity.db?.userDao()
		}

		if (dataFromDatabase != null) {
			if (dataFromDatabase.displayUserName != null) {
				val userNameLocal = dataFromDatabase.displayUserName.toString()
				val aboutInfoLocal = dataFromDatabase.about.toString()
				Log.d("log-onCreate-username", "onCreate: username is $userNameLocal")
				user_name_EditField?.setText(userNameLocal, TextView.BufferType.EDITABLE)
				user_about_in_profile_page?.setText(aboutInfoLocal)
				username = userNameLocal
				aboutInfo = aboutInfoLocal
			}
		}

		user_Profile_photo = findViewById(R.id.userProfilePhoto)
		setUserImage()

	}

	private fun setUserImage() {
		val ti = Thread {
			val imagePath = "/storage/emulated/0/Android/media/com.massenger.mank.main/Pictures/profiles/" + MainActivity.user_login_id + MainActivity.user_login_id + ".png"
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
				Log.d("log-ContactListAdapter", "setUserImage : after fetch image form file system : " + byteArray.size)
				runOnUiThread(Runnable { user_Profile_photo?.setImageBitmap(selfImage) })
			}
		}
		ti.start()
	}

	private val onUpdateUserDisplayNameReturn: Emitter.Listener = Emitter.Listener {
		Log.d("log-onUpdateUserDisplayName_return", "call: onUpdateUserDisplayName_return enter ")
		runOnUiThread {
			ProfileUploadProgressBar?.visibility = View.GONE
			username = user_name_EditField?.text.toString()
			Toast.makeText(this@UserProfileActivity, "Display name is updated", Toast.LENGTH_SHORT).show()
		}
	}
	private val onUpdateUserAboutInfoReturn: Emitter.Listener = Emitter.Listener {
		Log.d("log-onUpdateUserAboutInfo_return", "call: onUpdateUserAboutInfo_return enter ")
		runOnUiThread {
			ProfileUploadProgressBar?.visibility = View.GONE
			aboutInfo = user_about_in_profile_page?.text.toString()
			Toast.makeText(applicationContext, "About is updated", Toast.LENGTH_SHORT).show()
		}
	}
	private val onUpdateUserProfileImageReturn: Emitter.Listener = Emitter.Listener {
		Log.d("log-onChangeUserProfileImage_return", "start")
		runOnUiThread {
			Toast.makeText(applicationContext, "Image is updated", Toast.LENGTH_SHORT).show()
			ProfileUploadProgressBar?.visibility = View.GONE
		}
	}

	private val captureImageOutputUri: Uri?
		get() {
			var outputFileUri: Uri? = null
			val getImage = getExternalFilesDir("")
			if (getImage != null) { //            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
				outputFileUri = FileProvider.getUriForFile(this, this.applicationContext.packageName + ".provider", File(getImage.path, "profile.png"))
			}
			return outputFileUri
		}
	private var savePass = false
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (resultCode == RESULT_OK) {
			Log.d("log-UserProfileActivity", "onActivityResult r(resultCode == Activity.RESULT_OK start")
			if (requestCode == IMAGE_RESULT) {
				runOnUiThread {
					ProfileUploadProgressBar?.visibility = View.VISIBLE
				}
				val Ti = Thread {
					val filePath = getImageFilePath(data)
					if (filePath != null) {
						Log.d("log-UserProfileActivity", "onActivityResult filepath not null : $filePath")
						savePass = true
						val options = BitmapFactory.Options()
						options.inJustDecodeBounds = true
						BitmapFactory.decodeFile(filePath, options)
						val imageWidth = options.outWidth
						val imageHeight = options.outHeight
						var scaleFactor = 1
						if (imageWidth > TARGET_RESOLUTION || imageHeight > TARGET_RESOLUTION) {
							scaleFactor = Math.pow(2.0, Math.ceil(Math.log(Math.max(imageWidth, imageHeight) / TARGET_RESOLUTION.toDouble()) / Math.log(0.5)).toInt().toDouble()).toInt()
						}
						options.inJustDecodeBounds = false
						options.inSampleSize = scaleFactor
						bitmap = BitmapFactory.decodeFile(filePath, options)
						if (bitmap?.width != TARGET_RESOLUTION || bitmap?.height != TARGET_RESOLUTION) {
							val scaledBitmap = bitmap?.let {
								Bitmap.createScaledBitmap(it, TARGET_RESOLUTION, TARGET_RESOLUTION, true)
							} ?: bitmap

							bitmap?.recycle()
							bitmap = scaledBitmap
						}
						val stream = ByteArrayOutputStream()
						bitmap?.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
						imageData = stream.toByteArray()
						runOnUiThread {
							val compressedImageLength = imageData?.size
							if (compressedImageLength != null) {
								if (compressedImageLength > (200 * 1024)) {
									Toast.makeText(this@UserProfileActivity, "image size is two large for store into database", Toast.LENGTH_LONG).show()
								} else {
									user_Profile_photo?.setImageBitmap(bitmap)
								}
							}
						}
					}

					runOnUiThread { ProfileUploadProgressBar?.visibility = View.GONE }
				}
				Ti.start()
			}
		}
	}

	//	@SuppressLint("Recycle")
	private fun getImageFilePath(data: Intent?): String? {
		val isCamera = data == null || data.data == null
		if (isCamera) {
			return captureImageOutputUri?.path
		} else {
			val proj = arrayOf(MediaStore.Audio.Media.DATA)
			val cursor = contentResolver.query(data?.data!!, proj, null, null, null)
			val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
			cursor?.moveToFirst()
			return columnIndex?.let { cursor.getString(it) }
		}
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelable("pic_uri", picUri)
	}

	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState) // get the file url
		picUri = savedInstanceState.getParcelable("pic_uri")
	}

	private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
		AlertDialog.Builder(this).setMessage(message).setPositiveButton("OK", okListener).setNegativeButton("Cancel", null).create().show()
	}

	override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		when (requestCode) {
			ALL_PERMISSIONS_RESULT -> {
				for (perms: String in permissionsToRequest!!) {
					permissionsRejected.add(perms)
				}
				if (permissionsRejected.size > 0) {
					if (shouldShowRequestPermissionRationale(permissionsRejected[0])) {
						showMessageOKCancel("These permissions are mandatory for the application. Please allow access.", object : DialogInterface.OnClickListener {
							override fun onClick(dialog: DialogInterface, which: Int) {
								requestPermissions(permissionsRejected.toTypedArray(), ALL_PERMISSIONS_RESULT)
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
		val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
		pickIntent.type = "image/*"
		val chooserIntent = Intent.createChooser(pickIntent, "Select Image")
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
		startActivityForResult(chooserIntent, IMAGE_RESULT)
	}

	private fun updateUserProfileDetails() {
		val set_display_name = user_name_EditField?.text.toString()
		val set_about_name = user_about_in_profile_page?.text.toString()

		if (MainActivity.socket == null || MainActivity.socket?.connected() == false) {
			Toast.makeText(this, "please connect with internet first", Toast.LENGTH_SHORT).show()
			return
		}
		if (set_display_name != username) {
			if (MainActivity.socket != null) {
				val successStatus = userDao?.updateDisplayUserName(set_display_name, MainActivity.user_login_id)
				Log.d("log-success_status", "UpdateUserDisplayName: status is $successStatus")
				MainActivity.socket?.on("updateUserDisplayName_return", onUpdateUserDisplayNameReturn)
				ProfileUploadProgressBar?.visibility = View.VISIBLE
				MainActivity.socket?.emit("updateUserDisplayName", MainActivity.user_login_id, set_display_name)
			} else {
				Toast.makeText(applicationContext, "username can not be update right now, please try again after soe time", Toast.LENGTH_SHORT).show()
				ProfileUploadProgressBar?.visibility = View.GONE
			}
		}
		if (set_about_name != aboutInfo) {
			if ((set_about_name == "")) {
				Toast.makeText(this, "About Can not be empty", Toast.LENGTH_SHORT).show()
			} else {
				if (MainActivity.socket != null) {
					MainActivity.socket?.on("updateUserAboutInfo_return", onUpdateUserAboutInfoReturn)
					MainActivity.socket?.emit("updateUserAboutInfo", MainActivity.user_login_id, set_about_name)
					ProfileUploadProgressBar?.visibility = View.VISIBLE
					val successStatus = userDao?.updateAboutUserName(set_about_name, MainActivity.user_login_id)
					Log.d("log-success_status", "UpdateUserAboutInfo: status is $successStatus")
				} else {
					Toast.makeText(applicationContext, "about status can not be update right now, please try again after soe time", Toast.LENGTH_SHORT).show()
					ProfileUploadProgressBar?.visibility = View.GONE
				}
			}
		}

		if (savePass) {
			if (imageData != null) {
				if (MainActivity.socket != null && MainActivity.socket?.connected() == true) {
					ProfileUploadProgressBar?.visibility = View.VISIBLE
					Log.d("log-UserProfileActivity", "ProfileUploadProgressBar || after visible")

					MainActivity.contactListAdapter?.updateSelfUserImage(imageData!!)
					MainActivity.socket?.on("updateUserProfileImage_return", onUpdateUserProfileImageReturn)
					MainActivity.socket?.emit("updateUserProfileImage", MainActivity.user_login_id, imageData)

					Thread {
						var  version = userDao?.getProfileImageVersion(user_login_id);
						userDao?.updateProfileImageVersion(user_login_id, version?.plus(1) ?: 1);

						saveImageToInternalStorage(bitmap)
					}.start()
					savePass = false
				} else {
					Toast.makeText(this, "please connect with internet first", Toast.LENGTH_SHORT).show()
				}
			} else {
				Toast.makeText(this, "Bitmap is null. Try again", Toast.LENGTH_SHORT).show()
			}
		}
	}

	private fun writeFileOnInternalStorage(mcoContext: Context, sFileName: String?, sBody: String) {
		val file = File(this.filesDir, "try.txt")
		Log.d("log-UserProfileActivity", "writeFileOnInternalStorage method before if cond. dir:$file")
		try {
			mcoContext.openFileOutput(sFileName, MODE_PRIVATE).use { fos -> fos.write(sBody.toByteArray()) }
		} catch (e: IOException) {
			e.printStackTrace()
			Log.d("log-UserProfileActivity-IOException", "writeFileOnInternalStorage method IOException:$e")
		}
	}

	private fun saveImageToInternalStorage(bitmapImage: Bitmap?) { // Get the application-specific directory path
		val directory = File(Environment.getExternalStorageDirectory(), "Android/media/com.massenger.mank.main/Pictures/Profiles")
		if (!directory.exists()) {
			val x = directory.mkdirs()
		}

		val imagePath = File(directory, "" + MainActivity.user_login_id + MainActivity.user_login_id + ".png")        // Save the bitmap image to the file
		try {
			FileOutputStream(imagePath).use { outputStream ->
				bitmapImage?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
				outputStream.flush()
			}
		} catch (e: IOException) {
			e.printStackTrace()
			Log.d("log-saveImageToInternalStorage", "Image Save failed $e")
		} // Print the absolute path of the saved image
		Log.d("log-saveImageToInternalStorage", "Saved image path: " + imagePath.absolutePath)
	}


	companion object {
		private const val ALL_PERMISSIONS_RESULT = 107
		private const val IMAGE_RESULT = 200
		private const val TARGET_RESOLUTION = 1024
		private const val JPEG_QUALITY = 80
	}
}