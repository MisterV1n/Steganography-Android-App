package com.mister.steganography.fragment

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextEncodingCallback
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextEncoding
import com.mister.steganography.databinding.FragmentEncodeBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.random.Random

class EncodeFragment : Fragment(), TextEncodingCallback  {
    private var _binding: FragmentEncodeBinding? = null
    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    private val SELECT_PICTURE = 100
    private var filepath: Uri? = null
    private var original_image: Bitmap? = null
    private var encoded_image: Bitmap? = null
    private var save: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()
    }

    override fun onCreateView(  inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentEncodeBinding.inflate(inflater, container, false)
        binding.buttonOpenimage.setOnClickListener {
            imagechooser()
        }
        binding.buttonEncode.setOnClickListener {
            val secret_key =binding.edittextKey
            val message= binding.edittextMessage
            if(filepath!=null){
                if (secret_key.text != null){
                    if(message.text != null){
                        val imageSteganography = ImageSteganography(
                            message.text.toString(),
                            secret_key.text.toString(),
                            original_image
                        )
                        val textEncoding = TextEncoding(activity, this)
                        textEncoding.execute(imageSteganography)
                    }
                    else{
                        Toast.makeText(activity,"Masukan Message", Toast.LENGTH_SHORT).show()
                    }
                }
                else{
                    Toast.makeText(activity,"Masukan Key", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(activity,"Masukan Gambar", Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonSave.setOnClickListener {
            if(encoded_image!=null && original_image!=null){
                if (binding.edittextKey.text!=null && binding.edittextMessage.text!=null ){
                    val imgtosave= encoded_image
                    val PerformEncoding = Thread(Runnable {
                        kotlin.run { saveToInternalStorage(imgtosave) }
                    })
                    save = ProgressDialog(activity)
                    save!!.setMessage("Saving, Please Wait...")
                    save!!.setTitle("Saving Image")
                    save!!.setIndeterminate(false)
                    save!!.setCancelable(false)
                    save!!.show()
                    PerformEncoding.start()
                    Toast.makeText(activity,"Selesai di save", Toast.LENGTH_SHORT).show()
                    binding.edittextMessage.text=null
                    binding.edittextKey.text=null
                }
                else{
                    Toast.makeText(activity,"Isi key dan message", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(activity,"Pilih dan Encode gambar", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    private fun saveToInternalStorage(bitmapImage: Bitmap?) {
        val number1 = Random.nextInt(1,1000)
        val number2 = Random.nextInt(1,1000)
        val fOut: OutputStream
        val nama = "Encoded" + number1 + number2 + ".PNG"
        val file = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), nama
        ) // the File to save ,

        fOut = FileOutputStream(file)
        bitmapImage?.compress(
            Bitmap.CompressFormat.PNG,
            10,
            fOut
        ) // saving the Bitmap to a file
        fOut.flush() // Not really required
        fOut.close() // do not forget to close the stream
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = file
            mediaScanIntent.data = Uri.fromFile(f)
            activity?.sendBroadcast(mediaScanIntent)
        }
        save!!.dismiss()
    }

    private fun imagechooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    override fun onStartTextEncoding() {
        Toast.makeText(activity,"Lagi Encoded", Toast.LENGTH_SHORT).show()
    }

    override fun onCompleteTextEncoding(p0: ImageSteganography?) {
        if (p0 != null && p0.isEncoded()) {
            encoded_image = p0.getEncoded_image()
            binding.imageEncript.setImageBitmap(encoded_image)
            Toast.makeText(activity,"Encoded", Toast.LENGTH_SHORT).show()
        }
        else{
            Toast.makeText(activity,"Not Encoded", Toast.LENGTH_SHORT).show()
        }
    }
    private fun checkAndRequestPermissions() {
        val permissionWriteStorage = activity?.let {
            ContextCompat.checkSelfPermission(
                it,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val ReadPermission =
            activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) }
        val listPermissionsNeeded: MutableList<String> = ArrayList()
        if (ReadPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            activity?.let {
                ActivityCompat.requestPermissions(
                    it,
                    listPermissionsNeeded.toTypedArray(),
                    1
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === SELECT_PICTURE && resultCode === Activity.RESULT_OK && android.R.attr.data != null) {
            filepath = data?.data
            try {
                original_image = MediaStore.Images.Media.getBitmap(activity?.contentResolver,filepath!!)
                binding.imageEncript.setImageBitmap(original_image)
            } catch (e: IOException) {
                Log.d("MainActivity", "Error : $e")
            }
        }
    }
}