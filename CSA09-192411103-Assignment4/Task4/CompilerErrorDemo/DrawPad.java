import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Task 4.4 - same listener with @Override and the misspelled name: the compiler rejects it. */
public class DrawPad {
    public static void main(String[] args) {
        new MouseAdapter() {
            @Override public void mouseclicked(MouseEvent e) { }
        };
    }
}
