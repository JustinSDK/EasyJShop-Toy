package cc.openhome.util;

import java.util.ArrayList;
import java.awt.Image;

public class ImageMementoManager {
    private ArrayList<Image> mementoList = new ArrayList<>();
    private int undoIndex = -1, redoIndex = 0;
    private int maxUndo = 15;
    
    public Image undoImage() {
        Image image = null;
        if(undoIndex >= 0) {
            image = mementoList.get(undoIndex);
            undoIndex--;
            redoIndex--;
        }
        return image;
    }
    
    public Image redoImage() {
        Image image = null;
        if(redoIndex < mementoList.size() -1) {
            redoIndex++;
            undoIndex++;
            image = mementoList.get(redoIndex);
            if(redoIndex == mementoList.size() -1) {
                mementoList.remove(redoIndex);
            }
        }
        return image;
    }
    
    public void addImage(Image image) {
        undoIndex++;
        redoIndex++;
        mementoList.add(undoIndex, image);
        
        if(mementoList.size() > getMaxUndo()) {
            clearUndo(5);
        }
        
        int remove = mementoList.size() - undoIndex - 1;
        for(int i = 1; i <= remove; i++) {            
            mementoList.remove(undoIndex+1);
        }
    }
    
    public void clearUndo(int number) {
        for(int i = 0; i < number; i++) {
            mementoList.remove(0);
        }
        undoIndex = undoIndex - number;
        redoIndex = redoIndex - number;
    }
    
    public int getUndoIndex() {
        return undoIndex;
    }
    public int getRedoIndex() {
        return redoIndex;
    }
    
    public int getMaxUndo() {
        return maxUndo;
    }
    public void setMaxUndo(int maxUndo) {
        this.maxUndo = maxUndo;
    }
    
    public boolean isUndoable() {
        return !(getUndoIndex() == -1);
    }
    
    public boolean isFirstUndo() {
        return (mementoList.size() == getRedoIndex());
    }
    
    public boolean isRedoable() {
        return !isFirstUndo();
    }
}
