package mehrin.loginpage.Util;

import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class AutoCompleteHelper {

    /**
     * Attaches an auto-complete context menu to a standard JavaFX TextField.
     *
     * @param textField          The TextField to add autoComplete to.
     * @param fetchSuggestions   A function that takes the current text and returns
     *                           a list of suggestions.
     * @param onSuggestionChosen A callback triggered when the user selects a
     *                           suggestion. The String passed is the chosen item.
     */
    public static void setupAutoComplete(TextField textField,
            Function<String, List<String>> fetchSuggestions,
            Consumer<String> onSuggestionChosen) {

        ContextMenu popup = new ContextMenu();
        popup.getStyleClass().add("autocomplete-popup"); // Optional for custom CSS filtering

        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                popup.hide();
                return;
            }

            // Only fetch suggestions if the text actually changed (avoids re-triggering
            // when we set the value programmatically)
            if (!newValue.equals(oldValue)) {
                List<String> suggestions = fetchSuggestions.apply(newValue);

                if (suggestions == null || suggestions.isEmpty()) {
                    popup.hide();
                } else {
                    popup.getItems().clear();

                    int count = 0;
                    for (String suggestion : suggestions) {
                        if (count++ >= 10)
                            break; // Limit to 10 suggestions

                        Label label = new Label(suggestion);
                        // Ensure the label fits within the popup width
                        label.prefWidthProperty().bind(textField.widthProperty().subtract(10));
                        label.setStyle("-fx-text-fill: black; -fx-padding: 5 10;");

                        CustomMenuItem item = new CustomMenuItem(label, true); // true to hide on click
                        item.setOnAction(actionEvent -> {
                            textField.setText(suggestion);
                            popup.hide();
                            if (onSuggestionChosen != null) {
                                onSuggestionChosen.accept(suggestion);
                            }
                        });
                        popup.getItems().add(item);
                    }

                    if (!popup.isShowing()) {
                        popup.setMinWidth(textField.getWidth());
                        // Show just below the textfield
                        popup.show(textField, Side.BOTTOM, 0, 0);
                    }
                }
            }
        });

        // Hide when losing focus
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                popup.hide();
            }
        });
    }
}
