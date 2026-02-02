package mehrin.loginpage;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private VBox chartContainer;

    @FXML
    private Label totalBooksLabel;

    @FXML
    private Label issuedBooksLabel;

    @FXML
    private Label totalStudentsLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBookStatusChart();
    }

    private void setupBookStatusChart() {
        int availableBooks = 120;
        int issuedBooks = 30;

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Books Status");

        PieChart.Data slice1 = new PieChart.Data("Available", availableBooks);
        PieChart.Data slice2 = new PieChart.Data("Issued", issuedBooks);

        pieChart.getData().addAll(slice1, slice2);
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);

        // ✅ Make chart fill available space nicely
        pieChart.setPrefSize(400, 400);  // adjust as needed
        pieChart.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Clear and add
        chartContainer.getChildren().clear();
        chartContainer.getChildren().add(pieChart);

        // Center the chart in the StackPane
        StackPane.setAlignment(pieChart, javafx.geometry.Pos.CENTER);
    }
}
