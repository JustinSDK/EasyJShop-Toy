package cc.openhome.menu;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class OpenableFileFilter extends FileFilter {
    public boolean accept(File file) {
        if(file.isDirectory())
            return true;
                                                                                
        int i = file.getName().lastIndexOf('.');
        
        if(i == -1)
            return false;
                                                                                
        String extname = file.getName().substring(i).toLowerCase();
        
        if(extname.equals(".jpg") || extname.equals(".gif") || extname.equals(".png")) 
            return true;
                                                                                
        return false;
    }
                                                                                
    public String getDescription() {
        return "*.jpg  *.gif  *.png";
    }
}
