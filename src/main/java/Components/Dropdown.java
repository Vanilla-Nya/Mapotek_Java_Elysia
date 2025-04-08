package Components;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.List;

public class Dropdown extends JComboBox<String> {
    private JTextField editor;
    private List<String> items;
    private Timer debounceTimer;
    private boolean userStartedTyping = false; // Flag to check if user has started typing
    private String lastInput = "";
    private boolean isFirstTime = true;

    public Dropdown(boolean isSearchable, boolean register, String selectedItem) {
        super();
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(); // Model terpisah
        setModel(model);
        this.items = new ArrayList<>();
        setEditable(isSearchable);

        editor = (JTextField) getEditor().getEditorComponent();

        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (isSearchable) {
                    userStartedTyping = true;
                    startDebounce(isSearchable, register, selectedItem);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (isSearchable) {
                    userStartedTyping = true;
                    startDebounce(isSearchable, register, selectedItem);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (isSearchable) {
                    userStartedTyping = true;
                    startDebounce(isSearchable, register, selectedItem);
                }
            }
        });
    }

    public void setItems(List<String> newItems, boolean isSearchable, boolean register, String selectedItem) {
        items.clear();
        items.addAll(newItems);
        updateComboBox(isSearchable, register, selectedItem);
    }

    private void startDebounce(boolean isSearchable, boolean register, String selectedItem) {
        if (debounceTimer != null && debounceTimer.isRunning()) {
            debounceTimer.stop();
        }

        debounceTimer = new Timer(500, e -> updateComboBox(isSearchable, register, selectedItem));
        debounceTimer.setRepeats(false);
        debounceTimer.start();
    }

    private void updateComboBox(boolean isSearchable, boolean register, String selectedItem) {
        SwingUtilities.invokeLater(() -> {
            String input = editor.getText();
            List<String> filteredItems = new ArrayList<>();
            
            // Check if the input changed before continuing
            if (input.equals(lastInput) && !isFirstTime) {
                return; // No change, stop the update
            }
            lastInput = input; // Update the last input
            
            if (isSearchable) {
                // Filter items based on user input
                for (String item : items) {
                    if (item.toLowerCase().contains(input.toLowerCase())) {
                        filteredItems.add(item);
                    }
                }
                if (isFirstTime && register) {
                    filteredItems = items;  // Show all items initially if it is the first time
                }
            } else {
                filteredItems = items;  // Show all items if not searchable
            }
            
            // Update the model only if the items have changed
            if (!filteredItems.isEmpty()) {
                // Update the model
                setModel(new DefaultComboBoxModel<>(filteredItems.toArray(String[]::new)));
                setSelectedItem(lastInput);
            } else {
                if (register && !isSearchable) {
                    setModel(new DefaultComboBoxModel<>(items.toArray(String[]::new)));
                }
                setPopupVisible(false); // Hide the popup if no matches
            }
            
            // Hide or show the popup based on the number of filtered items
            if (!filteredItems.isEmpty() && isSearchable) {
                if (lastInput.equals(filteredItems.get(0))) {
                    setPopupVisible(false);
                } else {
                    setPopupVisible(true);
                }
            } else {
                setPopupVisible(false);
            }
            
            isFirstTime = false;
        });
    }
}