import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/** Task 4 - Complete the Code (TODO 1 and TODO 2 filled in). */
public class DrawPad extends Frame {
    private final List<Point> points = new ArrayList<>();
    private final Label status = new Label("Clicks: 0");

    public DrawPad() {
        super("Draw Pad");
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            // TODO 1: correct override - exact name, @Override checked by the compiler
            @Override public void mouseClicked(MouseEvent e) {
                points.add(e.getPoint());                       // store the point
                status.setText("Clicks: " + points.size());     // update the label
                repaint();                                      // ask AWT to call paint()
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        setSize(400, 300);
    }

    @Override public void paint(Graphics g) {
        super.paint(g);                                         // paint the label first
        // TODO 2: every stored point is a 6 x 6 filled circle centred on the point
        for (Point p : points) g.fillOval(p.x - 3, p.y - 3, 6, 6);
    }

    public static void main(String[] args) { new DrawPad().setVisible(true); }
}
