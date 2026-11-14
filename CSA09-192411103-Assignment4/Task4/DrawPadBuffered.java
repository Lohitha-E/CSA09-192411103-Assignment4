import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Label;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Alternative design: dots are drawn ONCE onto an off-screen image; paint() copies that
 * one image to the screen. Repaint cost no longer depends on the number of clicks n,
 * but memory is width x height pixels (fixed) and the points themselves are not kept.
 */
public class DrawPadBuffered extends Frame {
    private static final int W = 400, H = 300;
    private final BufferedImage canvas = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
    private final Label status = new Label("Clicks: 0");
    private int count = 0;

    public DrawPadBuffered() {
        super("Draw Pad (BufferedImage)");
        add(status, BorderLayout.SOUTH);
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                Graphics2D g = canvas.createGraphics();
                g.setColor(Color.BLACK);
                g.fillOval(e.getX() - 3, e.getY() - 3, 6, 6);   // O(1) per click
                g.dispose();
                status.setText("Clicks: " + (++count));
                repaint();
            }
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        setSize(W, H);
    }

    @Override public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(canvas, 0, 0, null);                        // one copy, independent of n
    }

    public static void main(String[] args) { new DrawPadBuffered().setVisible(true); }
}
