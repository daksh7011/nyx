/*
 * MIT License
 *
 * Copyright (c) 2020. TechnoWolf FOSS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package `in`.technowolf.nyx.ui.decryption

import `in`.technowolf.nyx.R
import `in`.technowolf.nyx.data.AppDatabase
import `in`.technowolf.nyx.databinding.FragmentDecryptionBinding
import `in`.technowolf.nyx.ui.models.ImageModel
import `in`.technowolf.nyx.utils.ImageHelper.deleteImage
import `in`.technowolf.nyx.utils.ImageHelper.retrieveImage
import `in`.technowolf.nyx.utils.alert
import `in`.technowolf.nyx.utils.viewBinding
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class DecryptionFragment : Fragment(R.layout.fragment_decryption) {

    private val binding by viewBinding(FragmentDecryptionBinding::bind)

    private val decryptionViewModel: DecryptionViewModel by viewModels()

    private val imageGalleryAdapter = ImageGalleryAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvEncryptedImages.apply {
            adapter = imageGalleryAdapter
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }

        imageGalleryAdapter.onDecrypt = {
            alert(
                "Decrypt Message",
                "Please enter passphrase below to decrypt the message from the image.",
                true
            ) {
                cancelable = false
                positiveButton("ok") {
                    decryptionViewModel.decryptImage(
                        requireContext().retrieveImage(it.name),
                        passphraseEditText.text.toString()
                    )
                }
                negativeButton("cancel") {

                }
            }
        }

        imageGalleryAdapter.onDelete = { it: ImageModel, position: Int ->
            lifecycleScope.launch(Dispatchers.IO) {
                provideDb().imageDao().delete(it.name)
            }
            requireContext().deleteImage(it.name)
            decryptionViewModel.imageList.remove(it)
            imageGalleryAdapter.submitList(decryptionViewModel.imageList)
            imageGalleryAdapter.notifyItemRemoved(position)
        }

        decryptionViewModel.decryptedText.observe(viewLifecycleOwner, Observer {
            val alertMessage =
                if (it.isNullOrEmpty()) "Wrong passphrase, Please try again!"
                else "Decrypted message: $it"

            alert("Secret Message", alertMessage) {
                positiveButton("Copy to clipboard") {

                }
                negativeButton("Close") {

                }
            }
        })

        lifecycleScope.launch(Dispatchers.IO) {
            provideDb().imageDao().getAllImages().let {
                it.map { imageEntity ->
                    imageEntity.toImageModel()
                }.also { imageModelList ->
                    decryptionViewModel.imageList.addAll(imageModelList)
                    requireActivity().runOnUiThread { imageGalleryAdapter.submitList(imageModelList) }
                }
            }
        }
    }

    private fun provideDb(): AppDatabase {
        return Room.databaseBuilder(
            requireActivity().applicationContext,
            AppDatabase::class.java,
            "Images"
        ).build()
    }

}
