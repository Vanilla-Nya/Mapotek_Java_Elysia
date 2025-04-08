/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TextButton extends JPanel {
    public TextButton() {
        JLabel clickableText = new JLabel("Click Me");
        clickableText.setForeground(Color.BLUE);
        clickableText.setCursor(new Cursor(Cursor.HAND_CURSOR));  

        clickableText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Text clicked!");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                clickableText.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                clickableText.setForeground(Color.BLUE);
            }
        });

        add(clickableText);
    }
} 

