package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.utils.copyToClipboard
import im.vector.app.databinding.VectorSettingsEncryptionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.matrix.android.sdk.api.extensions.getFingerprintHumanReadable
import org.matrix.android.sdk.api.session.Session
import org.matrix.android.sdk.api.session.crypto.crosssigning.isVerified
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsEncryptionFragment : Fragment() {

    private var _binding: VectorSettingsEncryptionBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var session: Session

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsEncryptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Mã hóa tin nhắn"

        // Copy device info when clicking
        binding.itemDeviceName.setOnClickListener {
            copyToClipboard(requireContext(), binding.summaryDeviceName.text.toString())
        }
        binding.itemDeviceId.setOnClickListener {
            copyToClipboard(requireContext(), binding.summaryDeviceId.text.toString())
        }
        binding.itemDeviceKey.setOnClickListener {
            copyToClipboard(requireContext(), binding.summaryDeviceKey.text.toString())
        }
        binding.itemCrossSigning.setOnClickListener {
            lifecycleScope.launch {
                val trustResult = session.cryptoService().crossSigningService().checkUserTrust(session.myUserId)
                val canCrossSign = session.cryptoService().crossSigningService().canCrossSign()
//                val crossSigningKeys = session.cryptoService().crossSigningService().getMyCrossSigningKeys()

                // Nếu chưa setup đủ cross-signing
                if (!canCrossSign || !trustResult.isVerified()) {
                    (requireActivity() as? VectorSettingsActivity)?.let {
                        it.navigator.requestSelfSessionVerification(it)
                    }
                } else {
                    // Đã đầy đủ thì copy chuỗi như trước
                    copyToClipboard(requireContext(), binding.summaryCrossSigning.text.toString())
                }
            }
        }


        // Switch: Never send to unverified devices
        binding.switchNeverSendUnverified.setOnCheckedChangeListener { _, isChecked ->
            session.cryptoService().setGlobalBlacklistUnverifiedDevices(isChecked)
        }

        // Load data
        lifecycleScope.launch {
            updateCryptographyInfo()
        }
    }

    private suspend fun updateCryptographyInfo() {
        val userId = session.myUserId
        val deviceId = session.sessionParams.deviceId
        val devices = session.cryptoService().getUserDevices(userId)
        val currentDevice = devices.find { it.deviceId == deviceId }

        val deviceInfo = session.cryptoService().getCryptoDeviceInfo(userId, deviceId)
//        val fingerprint = deviceInfo?.fingerprint()
        val humanFingerprint = deviceInfo?.getFingerprintHumanReadable()

        val trustResult = session.cryptoService().crossSigningService().checkUserTrust(userId)
        val crossSigningKeys = session.cryptoService().crossSigningService().getMyCrossSigningKeys()

        withContext(Dispatchers.Main) {
            binding.summaryDeviceName.text = currentDevice?.displayName() ?: "-"
            binding.summaryDeviceId.text = deviceId
            binding.summaryDeviceKey.text = humanFingerprint ?: "-"

            // Cross-signing status
            val canSign = session.cryptoService().crossSigningService().canCrossSign()
            binding.summaryCrossSigning.text = when {
                canSign -> getString(im.vector.lib.strings.R.string.encryption_information_dg_xsigning_complete)
                trustResult.isVerified() -> getString(im.vector.lib.strings.R.string.encryption_information_dg_xsigning_trusted)
                crossSigningKeys != null -> getString(im.vector.lib.strings.R.string.encryption_information_dg_xsigning_not_trusted)
                else -> getString(im.vector.lib.strings.R.string.encryption_information_dg_xsigning_disabled)
            }

            // Switch checked state
            binding.switchNeverSendUnverified.isChecked =
                    session.cryptoService().getGlobalBlacklistUnverifiedDevices()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
