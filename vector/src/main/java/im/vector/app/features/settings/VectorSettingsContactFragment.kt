package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsContactBinding
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsContactFragment : Fragment() {

    private var _binding: VectorSettingsContactBinding? = null
    private val binding get() = _binding!!

//    @Inject lateinit var viewModel: SettingsContactViewModel // Nếu bạn dùng ViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Danh bạ"
        setupSwitch()
        setupCountrySelector()
    }

    private fun setupSwitch() {
        binding.switchDiscoverContacts.setOnCheckedChangeListener { _: CompoundButton, _: Boolean ->
            // TODO: Lưu hoặc cập nhật trạng thái tìm danh bạ
            // Ví dụ: viewModel.setContactDiscoveryEnabled(isChecked)
        }
    }

    private fun setupCountrySelector() {
        binding.layoutContactCountry.setOnClickListener {
            // TODO: Mở dialog hoặc activity chọn quốc gia
            // Sau khi chọn xong, cập nhật UI:
            // binding.tvCountry.text = "Nhật Bản" hoặc quốc gia tương ứng
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
