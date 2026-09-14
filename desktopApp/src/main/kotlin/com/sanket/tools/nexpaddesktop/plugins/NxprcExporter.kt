package com.sanket.tools.nexpaddesktop.plugins

import com.sanket.tools.nexpad.nxprc.NxprcDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

object NxprcExporter {

    /**
     * Prompts the user with a native file dialog to save an NxprcDocument to disk.
     */
    suspend fun exportToFile(doc: NxprcDocument): Result<File> = withContext(Dispatchers.IO) {
        try {
            val fileName = "${doc.manifest.id.removePrefix("rc.")}.nxprc"
            val dialog = FileDialog(null as Frame?, "Export .nxprc Component", FileDialog.SAVE).apply {
                file = fileName
                isVisible = true
            }

            val dir = dialog.directory
            val chosenFile = dialog.file

            if (dir == null || chosenFile == null) {
                return@withContext Result.failure(IllegalStateException("Export canceled by user."))
            }

            val finalFile = File(dir, if (chosenFile.endsWith(".nxprc")) chosenFile else "$chosenFile.nxprc")
            val binaryBytes = NxprcDocument.encodeToBytes(doc)
            finalFile.writeBytes(binaryBytes)

            Result.success(finalFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
