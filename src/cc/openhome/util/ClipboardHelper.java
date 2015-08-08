package cc.openhome.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

public class ClipboardHelper {
    public static void imageToClipboard(Transferable transferableImage) {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(transferableImage, null);
    }
    
    public static Image getImageFromClipboard() {
        Transferable clipboardContents =
                Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        
        if(clipboardContents != null) {
            try {
                if(clipboardContents.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    return (Image) clipboardContents.getTransferData(DataFlavor.imageFlavor);
                }
            } 
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }        
        return null;
    }
}
