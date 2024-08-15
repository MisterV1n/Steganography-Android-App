package com.mister.steganography.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.ayush.imagesteganographylibrary.Text.AsyncTaskCallback.TextDecodingCallback
import com.ayush.imagesteganographylibrary.Text.ImageSteganography
import com.ayush.imagesteganographylibrary.Text.TextDecoding
import com.mister.steganography.databinding.FragmentDecodeBinding
import java.io.IOException

class DecodeFragment : Fragment(), TextDecodingCallback {
    private var _binding: FragmentDecodeBinding? = null
    private val binding get() = _binding!!
    private val SELECT_PICTURE = 100
    private var pathfile: Uri? = null
    private var original_image: Bitmap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        _binding = FragmentDecodeBinding.inflate(inflater, container, false)
        binding.buttonOpenimageDecode.setOnClickListener {
            image_chooser()
        }
        binding.buttonDecode.setOnClickListener {
            if(pathfile!=null){
                if(binding.edittextKeyDecode.text!=null){
                    val imagesteganographydecode = ImageSteganography(binding.edittextKeyDecode.text.toString(),original_image)
                    val textDecoding =  TextDecoding(activity, this)
                    textDecoding.execute(imagesteganographydecode)
                }
                else{
                    Toast.makeText(activity,"Masukan Secret Key", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(activity,"Masukan Gambar", Toast.LENGTH_SHORT).show()
            }
        }
        return  binding.root
    }

    private fun image_chooser() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE)
    }

    override fun onStartTextEncoding() {

    }

    override fun onCompleteTextEncoding(p0: ImageSteganography?) {
        if(p0!=null && p0.isDecoded) {
            if (!p0.isSecretKeyWrong){
                binding.edittextMessageDecode.setText(p0.message)
                Toast.makeText(activity,"Decoded", Toast.LENGTH_SHORT).show()
            }
            else{
                binding.edittextMessageDecode.setText("")
                Toast.makeText(activity,"Secret Key Wrong",Toast.LENGTH_SHORT).show()
            }
        }
        else{
            binding.edittextMessageDecode.setText("")
            Toast.makeText(activity,"Not Decode",Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === SELECT_PICTURE && resultCode === Activity.RESULT_OK && android.R.attr.data != null) {
            pathfile = data?.data
            try {
                original_image = MediaStore.Images.Media.getBitmap(activity?.contentResolver,pathfile!!)
                binding.imageDecode.setImageBitmap(original_image)
            } catch (e: IOException) {
                Log.d("MainActivity", "Error : $e")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
