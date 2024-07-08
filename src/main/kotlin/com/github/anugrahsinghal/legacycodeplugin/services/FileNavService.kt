// FileNavService.kt
package com.github.anugrahsinghal.legacycodeplugin.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.search.GlobalSearchScope
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

@Service
class FileNavService(private val project: Project) : Disposable {
    private val server = embeddedServer(Netty, port = 8080) {
        routing {
            // curl 'localhost:8080/openFile?path=/Users/anugrah.singhal/personal/codecrafters-git-java/src/main/java/CatFileCommand.java'
            // requires the full absolute path to the file
            get("/openFile") {
                val filePath = call.request.queryParameters["path"]
                if (filePath != null) {
                    openFile(filePath)
                    call.respond(HttpStatusCode.OK, "File opened: $filePath")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Missing 'path' parameter")
                }
            }
            // curl 'localhost:8080/navigateToMethod?class=CloneCommand&method=clone'
            get("/navigateToMethod") {
                val className = call.request.queryParameters["class"]
                val methodName = call.request.queryParameters["method"]
                if (className != null && methodName != null) {
                    navigateToMethod(className, methodName)
                    call.respond(HttpStatusCode.OK, "Navigated to method: $className.$methodName")
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Missing 'class' or 'method' parameter")
                }
            }
        }
    }

    init {
//        thisLogger().info("before start")
        server.start(wait = false)
//        thisLogger().info("after start")
    }

    private fun openFile(filePath: String) {
        ApplicationManager.getApplication().invokeLater {
            val virtualFile = LocalFileSystem.getInstance().findFileByPath(filePath)
            if (virtualFile != null) {
                FileEditorManager.getInstance(project).openFile(virtualFile, true)
            } else {
                com.intellij.openapi.ui.Messages.showErrorDialog("File not found", "Error")
            }
        }
    }

    private fun navigateToMethod(className: String, methodName: String) {
        ApplicationManager.getApplication().invokeLater {
            val psiClass = JavaPsiFacade.getInstance(project).findClass(className, GlobalSearchScope.allScope(project))
            if (psiClass == null) {
                com.intellij.openapi.ui.Messages.showErrorDialog("$className::$methodName not found", "Error")
            } else {
                val psiMethod = psiClass.findMethodsByName(methodName, false).firstOrNull()
                if (psiMethod != null) {
                    psiMethod.navigate(true)
                } else {
                    psiClass.navigate(true)
                }
            }
        }
    }

    override fun dispose() {
        server.stop(1000, 2000)
    }
}
