package im.vector.app.features.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.databinding.VectorSettingsPreferencesBinding
import im.vector.app.features.themes.ThemeUtils
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class VectorSettingsPreferencesFragment : Fragment() {

    private var _binding: VectorSettingsPreferencesBinding? = null
    private val binding get() = _binding!!

    @Inject lateinit var vectorLocale: VectorLocale

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = VectorSettingsPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Giao diện và ngôn ngữ"
        setupThemeRadioGroup()
        setupLanguageRadioGroup()
    }

    private fun setupThemeRadioGroup() {
        val currentTheme = ThemeUtils.getApplicationTheme(requireContext())
        val selectedThemeId = when (currentTheme) {
            "light" -> binding.radioThemeLight.id
            "dark" -> binding.radioThemeDark.id
            else -> binding.radioThemeSystem.id
        }
        binding.radioGroupTheme.check(selectedThemeId)

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val theme = when (checkedId) {
                binding.radioThemeLight.id -> "light"
                binding.radioThemeDark.id -> "dark"
                else -> "system"
            }
            ThemeUtils.setApplicationTheme(requireContext().applicationContext, theme)
            activity?.recreate()
        }
    }

    private fun setupLanguageRadioGroup() {
        val currentLanguage = vectorLocale.applicationLocale.language
        val selectedLangId = when (currentLanguage) {
            "lo" -> binding.radioLangLao.id
            "vi" -> binding.radioLangVietnamese.id
            else -> binding.radioLangEnglish.id
        }
        binding.radioGroupLanguage.check(selectedLangId)

        binding.radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            val lang = when (checkedId) {
                binding.radioLangLao.id -> "lo"
                binding.radioLangVietnamese.id -> "vi"
                else -> "en"
            }
            vectorLocale.saveApplicationLocale(Locale(lang))
            activity?.recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
