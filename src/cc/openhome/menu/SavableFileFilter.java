package cc.openhome.menu;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class SavableFileFilter extends FileFilter {
    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        String extName = file.getName().toLowerCase();
        return extName.endsWith(".jpg") || extName.endsWith(".png");
    }
                                                                                
    @Override
    public String getDescription() {
        return "*.jpg  *.png";
    }
}