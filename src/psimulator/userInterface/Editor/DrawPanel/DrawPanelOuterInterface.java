
package psimulator.userInterface.Editor.DrawPanel;

import java.util.Observer;
import javax.swing.JPanel;

/**
 *
 * @author Martin
 */
public abstract class DrawPanelOuterInterface extends JPanel implements ToolChangeOuterInterface{
   
    // USED BY EDITOR PANEL
    public abstract boolean canUndo();
    public abstract boolean canRedo();
    public abstract void undo();
    public abstract void redo();
    public abstract boolean canZoomIn();
    public abstract boolean canZoomOut();
    public abstract void zoomIn();
    public abstract void zoomOut();
    public abstract void zoomReset();
    public abstract void addObserverToZoomManager(Observer obsrvr);
}