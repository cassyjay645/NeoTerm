package io.neoterm.frontend.floating

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import io.neoterm.backend.TerminalSession
import io.neoterm.frontend.shell.ShellParameter
import io.neoterm.frontend.client.BasicSessionCallback
import io.neoterm.frontend.client.BasicViewClient
import io.neoterm.utils.TerminalUtils

/**
 * @author kiva
 */
class TerminalDialog(val context: Context) {

    interface SessionFinishedCallback {
        fun onSessionFinished(dialog: TerminalDialog, finishedSession: TerminalSession?)
    }

    private var termWindowView = WindowTermView(context)
    private var terminalSessionCallback: BasicSessionCallback
    private var dialog: AlertDialog? = null
    private var terminalSession: TerminalSession? = null
    private var sessionFinishedCallback: SessionFinishedCallback? = null
    private var cancelListener: DialogInterface.OnCancelListener? = null

    init {
        termWindowView.setTerminalViewClient(BasicViewClient(termWindowView.terminalView))

        terminalSessionCallback = object : BasicSessionCallback(termWindowView.terminalView) {
            override fun onSessionFinished(finishedSession: TerminalSession?) {
                sessionFinishedCallback?.onSessionFinished(this@TerminalDialog, finishedSession)
                super.onSessionFinished(finishedSession)
            }
        }
    }

    fun execute(executablePath: String, arguments: Array<String>?): TerminalDialog {
        if (terminalSession != null) {
            terminalSession?.finishIfRunning()
        }

        dialog = AlertDialog.Builder(context)
                .setView(termWindowView.rootView)
                .setOnCancelListener {
                    terminalSession?.finishIfRunning()
                    cancelListener?.onCancel(it)
                }
                .create()

        val parameter = ShellParameter()
                .executablePath(executablePath)
                .arguments(arguments)
                .callback(terminalSessionCallback)
                .systemShell(false)
        terminalSession = TerminalUtils.createShellSession(context, parameter)
        termWindowView.attachSession(terminalSession)
        return this
    }

    fun onDismiss(cancelListener: DialogInterface.OnCancelListener?): TerminalDialog {
        this.cancelListener = cancelListener
        return this
    }

    fun setTitle(title: String?): TerminalDialog {
        dialog?.setTitle(title)
        return this
    }

    fun onFinish(finishedCallback: SessionFinishedCallback): TerminalDialog {
        this.sessionFinishedCallback = finishedCallback
        return this
    }

    fun show(title: String?) {
        dialog?.setTitle(title)
        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)
        dialog?.show()
    }

    fun dismiss(): TerminalDialog {
        dialog?.dismiss()
        return this
    }

    fun imeEnabled(enabled: Boolean): TerminalDialog {
        if (enabled) {
            termWindowView.setInputMethodEnabled(true)
        }
        return this
    }
}