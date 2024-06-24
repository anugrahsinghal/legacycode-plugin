package com.github.anugrahsinghal.legacycodeplugin.listeners

import com.github.anugrahsinghal.legacycodeplugin.services.FileNavService
import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.wm.IdeFrame

internal class MyApplicationActivationListener : ApplicationActivationListener {

    override fun applicationActivated(ideFrame: IdeFrame) {
        thisLogger().warn("Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.")
        // needed so that the Service is Actually Created
        // when we fetch the service then it's singleton instance is created
        ideFrame.project?.service<FileNavService>()
    }
}
