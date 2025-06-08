package com.example.loopin.newchat

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.loopin.PreferenceManager // PreferenceManager'ınızın yolu
import com.example.loopin.databinding.ActivityCreateGroupBinding // Yeni binding
import com.example.loopin.newchat.CreateGroupViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreateGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateGroupBinding
    private val viewModel: CreateGroupViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Yeni Grup Oluştur"
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupListeners() {
        binding.btnCreateGroup.setOnClickListener {
            val groupName = binding.etGroupName.text.toString().trim()
            val groupDescription = binding.etGroupDescription.text.toString().trim()
            // val groupImage = ... // Eğer resim seçme özelliği eklerseniz buradan alabilirsiniz

            if (groupName.isEmpty()) {
                binding.etGroupName.error = "Grup Adı boş bırakılamaz"
                return@setOnClickListener
            }

            // Grup oluşturma ViewModel metodunu çağır
            viewModel.createGroup(groupName, groupDescription)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.groupCreationStatus.collectLatest { groupId ->
                if (groupId != null) {
                    if (groupId != -1) {
                        Toast.makeText(this@CreateGroupActivity, "Grup başarıyla oluşturuldu! Grup ID: $groupId", Toast.LENGTH_LONG).show()
                        // Grubu başarıyla oluşturduktan sonra bu aktiviteyi kapat
                        finish()
                    } else {
                        Toast.makeText(this@CreateGroupActivity, "Grup oluşturulamadı. Bir hata oluştu.", Toast.LENGTH_LONG).show()
                    }
                    viewModel.resetGroupCreationStatus() // Durumu sıfırla
                }
            }
        }
    }
}