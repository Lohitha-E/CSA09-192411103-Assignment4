import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Task 4 - the DEFECTIVE program from the question (class renamed, main added so it can run). */
public class DrawPadBroken extends Frame {
    int count = 0; Label status = new Label("Clicks: 0");
    public DrawPadBroken() {
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            public void mouseclicked(MouseEvent e) {          // defect 1: wrong name (lower-case c)
                count++;                                      // defect 2: label is never updated
                Graphics g = getGraphics(); g.fillOval(e.getX(), e.getY(), 6, 6);   // defect 3: not repainted
            }
        });
        setSize(400, 300); setVisible(true);
    }
    public static void main(String[] args) { new DrawPadBroken(); }
}
