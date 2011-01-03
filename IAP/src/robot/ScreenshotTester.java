package robot;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

public class ScreenshotTester {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final JFrame f = new JFrame("Screenshot Tester");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                f.setLayout(new BorderLayout(10, 10));

                final JPanel preview = new JPanel();
                preview.setBorder(new TitledBorder("Screenshot"));
                f.add(preview, BorderLayout.CENTER);

                final JPanel testPanel = new JPanel(new GridLayout(3, 1));
                testPanel.add(new JComboBox(new String[] { "a", "b" }));
                testPanel.add(new JComboBox(new String[] { "c", "d" }));
                testPanel.add(new JComboBox(new String[] { "e", "f" }));
                f.add(testPanel, BorderLayout.NORTH);

                Action screenshotAction = new AbstractAction("Screenshot") {
                    @Override
                    public void actionPerformed(ActionEvent ev) {
                        try {
                            Rectangle region = f.getBounds();
                            BufferedImage img = new Robot().createScreenCapture(region);
                            preview.removeAll();
                            preview.add(new JLabel(new ImageIcon(img)));
                            f.pack();
                        } catch (AWTException e) {
                            JOptionPane.showMessageDialog(f, e);
                        }
                    }
                };

                f.getRootPane().getActionMap().put(screenshotAction, screenshotAction);
                f.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                        KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), screenshotAction);
                f.pack();
                f.setLocationRelativeTo(null);
                f.setVisible(true);
            }
        });
    }

}