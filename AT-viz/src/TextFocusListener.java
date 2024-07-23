import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextFocusListener implements FocusListener {
        private JTextField tField;
        private JFormattedTextField ftField;
        private boolean isFormatted = false;
        public TextFocusListener(JTextField tf) {
            tField = tf;
        }
        public TextFocusListener(JFormattedTextField ftf) {
            ftField = ftf;
            isFormatted = true;
        }
        @Override
        public void focusGained(FocusEvent e) {
            if(isFormatted) {
                ftField.setText("");
                ftField.setFont(new Font("Dialog", Font.ITALIC, 12));
                ftField.setForeground(new Color(255, 100, 100));
            }
            else {
                tField.setText("");
                tField.setFont(new Font("Dialog", Font.ITALIC, 12));
                tField.setForeground(new Color(255, 100, 100));
            }
        }

        @Override
        public void focusLost(FocusEvent e) {
            if(isFormatted) {
                try {
                    ftField.setValue(Integer.parseInt(ftField.getText()));
                } catch (NumberFormatException nfe) {
                    ftField.setValue(null);
                    //Possibly remove so that it doesn't delete values when you don't want it to
                    //But this is intentional so that you *can* clear values manually/one-by-one
                }
                ftField.postActionEvent();
                ftField.setFont(new Font("Dialog", Font.PLAIN, 12));
                ftField.setForeground(new Color(0, 0, 0));
            }
            else {
                tField.postActionEvent();
                tField.setFont(new Font("Dialog", Font.PLAIN, 12));
                tField.setForeground(new Color(0, 0, 0));
            }
        }
}
