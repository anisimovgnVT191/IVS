package UI.`Helper Functions`

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.awt.Dimension
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JOptionPane

fun getFileFromDialog():String?{
    val frame = Frame()
    frame.size = Dimension(800, 600)
    val fd = FileDialog(frame)
    fd.isVisible = true
    val res = "${fd.directory}${fd.file}"

    if(res.contains(".txt"))
        return res
    else{
        val frame = Frame()
        frame.size = Dimension(800, 900)
        JOptionPane.showMessageDialog(frame,"Не тот тип файла","Ты еблан?", JOptionPane.WARNING_MESSAGE)
        return null
    }
}
fun imageFromFile(file: File): ImageBitmap {
    return org.jetbrains.skija.Image.makeFromEncoded(file.readBytes()).asImageBitmap()
}
fun resourceExist(path:String):Boolean{
    return File("src/main/resources/$path").exists()
}