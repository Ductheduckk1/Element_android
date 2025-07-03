/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package im.vector.app.features.pin.lockscreen.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.core.view.isVisible
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import dagger.hilt.android.AndroidEntryPoint
import im.vector.app.R
import im.vector.app.core.hardware.vibrate
import im.vector.app.core.platform.VectorBaseFragment
import im.vector.app.databinding.FragmentLockScreenBinding
import im.vector.app.features.pin.lockscreen.configuration.LockScreenConfiguration
import im.vector.app.features.pin.lockscreen.configuration.LockScreenMode

@AndroidEntryPoint
class LockScreenFragment :
        VectorBaseFragment<FragmentLockScreenBinding>() {

    var lockScreenListener: LockScreenListener? = null
    var onLeftButtonClickedListener: View.OnClickListener? = null

    private val viewModel: LockScreenViewModel by fragmentViewModel()

    override fun getBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentLockScreenBinding =
            FragmentLockScreenBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBindings(views)
        views.btnCreatePin.setOnClickListener {
            val pinCode = listOf(
                    views.pinDigit1.text,
                    views.pinDigit2.text,
                    views.pinDigit3.text,
                    views.pinDigit4.text
            ).joinToString(separator = "") { it?.toString().orEmpty() }

            if (pinCode.length == 4) {
                viewModel.handle(LockScreenAction.PinCodeEntered(pinCode))
            } else {
                // Có thể rung cảnh báo hoặc hiển thị lỗi nếu chưa đủ 4 chữ số
                vibrate(requireContext(), 200)
            }
        }


        viewModel.observeViewEvents {
            handleEvent(it)
        }

        viewModel.handle(LockScreenAction.OnUIReady)
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            when (state.pinCodeState) {
                is PinCodeState.FirstCodeEntered -> {
                    setupTitleView(views.titleTextView, true, state.lockScreenConfiguration)
                    lockScreenListener?.onFirstCodeEntered()
                }
                is PinCodeState.Idle -> {
                    setupTitleView(views.titleTextView, false, state.lockScreenConfiguration)
                }
            }
        }
    }

    private fun onAuthFailure(method: AuthMethod) {
        lockScreenListener?.onAuthenticationFailure(method)

        val configuration = withState(viewModel) { it.lockScreenConfiguration }
        if (configuration.vibrateOnError) {
            vibrate(requireContext(), 400)
        }

        if (configuration.animateOnError) {
            context?.let {
                val animation = AnimationUtils.loadAnimation(it, R.anim.lockscreen_shake_animation)
                listOf(
                        views.pinDigit1,
                        views.pinDigit2,
                        views.pinDigit3,
                        views.pinDigit4
                ).forEach { it.startAnimation(animation) }
            }
        }
    }

    private fun onAuthError(authMethod: AuthMethod, throwable: Throwable) {
        lockScreenListener?.onAuthenticationError(authMethod, throwable)
        withState(viewModel) { state ->
            if (state.lockScreenConfiguration.clearCodeOnError) {
                clearPinInputs()
            }
        }
    }

    private fun handleEvent(viewEvent: LockScreenViewEvent) {
        when (viewEvent) {
            is LockScreenViewEvent.CodeCreationComplete -> lockScreenListener?.onPinCodeCreated()
            is LockScreenViewEvent.ClearPinCode -> {
                if (viewEvent.confirmationFailed) {
                    lockScreenListener?.onNewCodeValidationFailed()
                }
                clearPinInputs()
            }
            is LockScreenViewEvent.AuthSuccessful -> lockScreenListener?.onAuthenticationSuccess(viewEvent.method)
            is LockScreenViewEvent.AuthFailure -> onAuthFailure(viewEvent.method)
            is LockScreenViewEvent.AuthError -> onAuthError(viewEvent.method, viewEvent.throwable)
            is LockScreenViewEvent.ShowBiometricKeyInvalidatedMessage -> lockScreenListener?.onBiometricKeyInvalidated()
            is LockScreenViewEvent.ShowBiometricPromptAutomatically -> showBiometricPrompt()
        }
    }

    private fun setupBindings(binding: FragmentLockScreenBinding) = with(binding) {
        val configuration = withState(viewModel) { it.lockScreenConfiguration }
        val lockScreenMode = configuration.mode

        configuration.title?.let { titleTextView.text = it }
        configuration.subtitle?.let {
            subtitleTextView.text = it
            subtitleTextView.isVisible = true
        }

        setupTitleView(titleTextView, false, configuration)
        setupPinEditTexts(binding)
//        setupFingerprintButton(buttonFingerPrint)
        setupLeftButton(buttonLeft, lockScreenMode, configuration)
        when (configuration.mode) {
            LockScreenMode.CREATE -> views.btnCreatePin.text = "Tạo mã"
            LockScreenMode.VERIFY -> views.btnCreatePin.text = "Nhập mã"
        }
    }

    private fun setupTitleView(titleView: TextView, isConfirmation: Boolean, configuration: LockScreenConfiguration) = with(titleView) {
        text = if (isConfirmation) {
            configuration.newCodeConfirmationTitle ?: getString(im.vector.lib.ui.styles.R.string.lockscreen_confirm_pin)
        } else {
            configuration.title ?: getString(im.vector.lib.ui.styles.R.string.lockscreen_title)
        }
    }

    private fun setupPinEditTexts(binding: FragmentLockScreenBinding) {
        val editTexts = listOfNotNull(
                binding.pinDigit1,
                binding.pinDigit2,
                binding.pinDigit3,
                binding.pinDigit4
        )

        editTexts.forEachIndexed { index, editText ->
            editText.setOnKeyListener { _, _, _ ->
                if (editText.text?.length == 1 && index < editTexts.lastIndex) {
                    editTexts[index + 1].requestFocus()
                }
                false
            }
        }

        binding.pinDigit4.setOnEditorActionListener { _, _, _ ->
            val pinCode = editTexts.joinToString("") { it.text.toString() }
            if (pinCode.length == 4) {
                viewModel.handle(LockScreenAction.PinCodeEntered(pinCode))
            }
            true
        }
    }

    private fun clearPinInputs() {
        views.pinDigit1.setText("")
        views.pinDigit2.setText("")
        views.pinDigit3.setText("")
        views.pinDigit4.setText("")
        views.pinDigit1.requestFocus()
    }

//    private fun setupFingerprintButton(view: ImageView?) {
//        view?.setOnClickListener {
//            showBiometricPrompt()
//        }
//    }

    private fun setupLeftButton(view: TextView, lockScreenMode: LockScreenMode, configuration: LockScreenConfiguration) = with(view) {
        isVisible = lockScreenMode == LockScreenMode.VERIFY && configuration.leftButtonVisible
        configuration.leftButtonTitle?.let { text = it }
        setOnClickListener(onLeftButtonClickedListener)
    }

    private fun showBiometricPrompt() {
        viewModel.handle(LockScreenAction.ShowBiometricPrompt(requireActivity()))
    }
}
