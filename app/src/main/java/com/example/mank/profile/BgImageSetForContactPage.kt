package com.example.mank.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.mank.MainActivity
import com.example.mank.R

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class BgImageSetForContactPage() : Activity() {
	private var bgImageView: ImageView? = null
	private var progressBar: ProgressBar? = null
	private var bitmap: Bitmap? = null
	private var imageData: ByteArray? = null
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_bg_image_set_for_contact_page)

		findViewById<ImageButton>(R.id.ABGSBackActivity).setOnClickListener { finish() }
		bgImageView = findViewById(R.id.bgImageView)
		progressBar = findViewById(R.id.ABGSProgressBar)
		progressBar?.visibility = View.GONE
		setBackgroundImage()
	}

	fun setBackgroundImage() {
		val ti = Thread {
			val imagePath = "/storage/emulated/0/Android/media/com.massenger.mank.main/bg/bgImages/" + MainActivity.user_login_id + ".png"
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
				runOnUiThread(Runnable { bgImageView!!.setImageBitmap(selfImage) })
			}
		}
		ti.start()
	}

	@SuppressLint("IntentReset")
	fun bgSelectImageButtonOnClick(view: View?) {
		Log.d("log-BgImageSetForContactPage", "bgSelectImageButtonOnClick method start")
		Log.d("log-BgImageSetForContactPage", "bgSelectImageButtonOnClick method before startActivityForResult")
		@SuppressLint("IntentReset") val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
		pickIntent.type = "image/*"
		val chooserIntent = Intent.createChooser(pickIntent, "Select Image")
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
		startActivityForResult(chooserIntent, IMAGE_RESULT)
	}

	fun bgSaveButtonOnClick(view: View?) {
		if (!blockSaveButton) {
			Log.d("log-BgImageSetForContactPage", "bgSaveButtonOnClick blockSaveButton$blockSaveButton")
			if (passForSave) {
				progressBar!!.visibility = View.VISIBLE
				val ts = Thread(object : Runnable {
					override fun run() {
						saveImageToInternalStorage(bitmap)
					}
				})
				ts.start()
			} else {
				Toast.makeText(this, "please select Image", Toast.LENGTH_SHORT).show()
			}
		} else {
			Toast.makeText(this, "please wait while we save background", Toast.LENGTH_SHORT).show()
		}
	}

	private fun saveImageToInternalStorage(bitmapImage: Bitmap?) { // Get the application-specific directory path
		val directory = File(Environment.getExternalStorageDirectory(), "Android/media/com.massenger.mank.main/bg/bgImages")
		if (!directory.exists()) {
			val x = directory.mkdirs()
			if (!x) {
				Log.d("log-saveImageToInternalStorage", "Saved failed dir creation failed")
				return
			}
		}

		// Create the file path
		val imagePath = File(directory, MainActivity.user_login_id + ".png")

		// Save the bitmap image to the file
		try {
			FileOutputStream(imagePath).use { outputStream ->
				bitmapImage!!.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
				outputStream.flush()
				Log.d("log-saveImageToInternalStorage", "Saved image path: " + imagePath.getAbsolutePath())
				runOnUiThread(object : Runnable {
					override fun run() {
						Toast.makeText(this@BgImageSetForContactPage, "background image is updated", Toast.LENGTH_SHORT).show()
						progressBar!!.setVisibility(View.GONE)
					}
				})
			}
		} catch (e: IOException) {
			e.printStackTrace()
			Log.d("log-saveImageToInternalStorage", "Image Save failed $e")
			runOnUiThread(object : Runnable {
				override fun run() {
					Toast.makeText(this@BgImageSetForContactPage, "background image is update failed", Toast.LENGTH_SHORT).show()
					progressBar!!.visibility = View.GONE
				}
			})
		}
	}

	private var passForSave = false
	private val blockSaveButton = false
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		Log.d("log-BgImageSetForContactPage", "onActivityResult resultCode = $resultCode")
		if (resultCode == RESULT_OK) {
			if (requestCode == IMAGE_RESULT) {
				progressBar!!.visibility = View.VISIBLE
				val filePath = getImageFilePath(data)
				if (filePath != null) {
					Log.d("log-UserProfileActivity", "onActivityResult filepath not null : $filePath")
					val Ti = Thread { //                            bitmap = BitmapFactory.decodeFile(filePath);
						passForSave = true
						val options = BitmapFactory.Options()
						options.inJustDecodeBounds = true
						BitmapFactory.decodeFile(filePath, options)
						val imageWidth = options.outWidth
						val imageHeight = options.outHeight
						val displayMetrics = DisplayMetrics()
						windowManager.defaultDisplay.getMetrics(displayMetrics)
						val TARGET_RESOLUTION_X = displayMetrics.widthPixels
						val TARGET_RESOLUTION_Y = displayMetrics.heightPixels
						var scaleFactor = 1
						if (imageWidth > TARGET_RESOLUTION_X || imageHeight > TARGET_RESOLUTION_Y) {
							val scaleX = imageWidth / TARGET_RESOLUTION_X
							val scaleY = imageHeight / TARGET_RESOLUTION_Y
							scaleFactor = Math.max(scaleX, scaleY)
						}
						options.inJustDecodeBounds = false
						options.inSampleSize = scaleFactor
						bitmap = BitmapFactory.decodeFile(filePath, options)
						if (bitmap?.getWidth() != TARGET_RESOLUTION_X || bitmap?.getHeight() != TARGET_RESOLUTION_Y) {
							val scaledBitmap = Bitmap.createScaledBitmap(bitmap!!, TARGET_RESOLUTION_X, TARGET_RESOLUTION_Y, true)

							bitmap?.recycle()
							bitmap = scaledBitmap
						}
						val stream = ByteArrayOutputStream()
						bitmap!!.compress(Bitmap.CompressFormat.PNG, JPEG_QUALITY, stream)
						imageData = stream.toByteArray()
						runOnUiThread(object : Runnable {
							override fun run() {
								bgImageView!!.setImageBitmap(bitmap)
								progressBar!!.visibility = View.GONE
							}
						})
					}
					Ti.start()
				}
			}
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

	private fun getImageFromFilePath(data: Intent?): String? {
		val isCamera = data == null || data.data == null
		return if (isCamera) {
			captureImageOutputUri!!.path
		} else {
			getPathFromURI(data!!.data)
		}
	}

	//            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
	private val captureImageOutputUri: Uri?
		private get() {
			var outputFileUri: Uri? = null
			val getImage = getExternalFilesDir("")
			if (getImage != null) { //            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "profile.png"));
				outputFileUri = FileProvider.getUriForFile(this, this.applicationContext.packageName + ".provider", File(getImage.path, "profile.png"))
			}
			return outputFileUri
		}

	companion object {
		private val IMAGE_RESULT = 230
		private val TARGET_RESOLUTION = 1024
		private val JPEG_QUALITY = 100
	}
}