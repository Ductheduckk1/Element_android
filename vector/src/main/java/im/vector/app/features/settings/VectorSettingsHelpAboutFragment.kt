package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.core.platform.VectorBaseActivity
import im.vector.app.core.resources.BuildMeta
import im.vector.app.core.utils.copyToClipboard
import im.vector.app.core.utils.openAppSettingsPage
import im.vector.app.core.utils.openUrlInChromeCustomTab
import im.vector.app.databinding.VectorSettingsHelpAboutBinding
import im.vector.app.features.analytics.plan.MobileScreen
import im.vector.app.features.version.VersionProvider
import im.vector.lib.strings.CommonStrings
import org.matrix.android.sdk.api.Matrix
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsHelpAboutFragment : Fragment() {

    @Inject lateinit var versionProvider: VersionProvider
    @Inject lateinit var buildMeta: BuildMeta

    private var _binding: VectorSettingsHelpAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsHelpAboutBinding.inflate(inflater, container, false)
        (activity as? VectorBaseActivity<*>)?.analyticsScreenName = MobileScreen.ScreenName.SettingsHelp
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Help & About"

        // Help
        binding.itemHelp.setOnClickListener {
            openUrlInChromeCustomTab(requireContext(), null, VectorSettingsUrls.HELP)
        }

        // App info
        binding.itemAppInfo.setOnClickListener {
            activity?.let { openAppSettingsPage(it) }
        }

        // App version
        binding.appVersion.text = getString(CommonStrings.settings_version)
        binding.summaryAppVersion.text = buildString {
            append(versionProvider.getVersion(longFormat = false))
            if (buildMeta.isDebug) {
                append(" ")
                append(buildMeta.gitBranchName)
                append(" ")
                append(buildMeta.gitRevision)
            }
        }
        binding.summaryAppVersion.setOnClickListener {
            copyToClipboard(requireContext(), binding.summaryAppVersion.text.toString())
        }

        // SDK version
        binding.SDKVersion.text = getString(CommonStrings.settings_sdk_version)
        binding.summarySdkVersion.text = Matrix.getSdkVersion()
        binding.summarySdkVersion.setOnClickListener {
            copyToClipboard(requireContext(), binding.summarySdkVersion.text.toString())
        }

        // Crypto version
        binding.titleCryptoVersion.text = getString(CommonStrings.settings_crypto_version)
        binding.summaryVersionCrypto.text = Matrix.getCryptoVersion(true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
