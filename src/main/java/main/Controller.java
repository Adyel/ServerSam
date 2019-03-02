package main;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import model.fx.TableViewModel;
import model.orm.Genre;
import model.orm.MovieDetails;
import org.controlsfx.control.CheckComboBox;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import util.DBConnect;

import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Controller implements Initializable {

//
//
//    Connection connection;
//    {
//        try {
//            connection = DBConnect.getConnection();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    @FXML
    Circle dbStatus;

    @FXML
    private TableView<TableViewModel> table;

    @FXML
    private TableColumn<TableViewModel, String> title;

    @FXML
    private TableColumn<TableViewModel, Integer> year;

    @FXML
    private TableColumn<TableViewModel, Double> rating;

    @FXML
    private TableColumn<TableViewModel, String> director;

    @FXML
    private TableColumn<TableViewModel, Boolean> subtitle;

    @FXML
    TextField searchField;

    @FXML
    CheckComboBox<String> checkComboBox;

    ObservableList<TableViewModel> tableViewModelObservableList = FXCollections.observableArrayList();
    List<MovieDetails> movieDetailsList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        year.setCellValueFactory(new PropertyValueFactory<>("year"));
        rating.setCellValueFactory(new PropertyValueFactory<>("rating"));
        director.setCellValueFactory(new PropertyValueFactory<>("director"));
        subtitle.setCellValueFactory(new PropertyValueFactory<>("subtitle"));


        SessionFactory factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .addAnnotatedClass(MovieDetails.class)
                .buildSessionFactory();


        Session session = factory.openSession();
        session.beginTransaction();

        movieDetailsList = session.createQuery("SELECT DISTINCT movie FROM MovieDetails movie").getResultList();
        List<Genre> genreList = session.createQuery("FROM Genre").getResultList();

        session.getTransaction().commit();


        // INFO: Add all genre to CheckComboBox
        ObservableList<String> genres = FXCollections.observableArrayList(genreList.stream().map(Genre::getName).collect(Collectors.toList()));
        checkComboBox.getItems().addAll(genres);

        checkComboBox.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
//                System.out.println(checkComboBox.getCheckModel().getCheckedIndices());
//                System.out.println(checkComboBox.getCheckModel().getCheckedItems().toString());

                String query;

                if (checkComboBox.getCheckModel().getCheckedIndices().isEmpty()){
                    query = "FROM MovieDetails ";
                }else {
                    query = "SELECT DISTINCT movie FROM MovieDetails movie JOIN movie.genres genre WHERE genre.name IN (";

                    for (String genre : checkComboBox.getCheckModel().getCheckedItems()){
                        query = query + "'" + genre + "', ";
                    }

                    query = query.substring(0, query.length() - 2) + " )";
                }

                System.out.println(query);

                Session session = factory.openSession();
                session.beginTransaction();
                movieDetailsList = session.createQuery(query).getResultList();
                session.getTransaction().commit();

                tableViewModelObservableList.clear();
                tableViewModelObservableList = movieDetailsList.stream().map(TableViewModel::new).collect(Collectors.toCollection(FXCollections::observableArrayList));
                table.setItems(tableViewModelObservableList);
            }
        });

        tableViewModelObservableList = movieDetailsList.stream().map(TableViewModel::new).collect(Collectors.toCollection(FXCollections::observableArrayList));

        FilteredList<TableViewModel> tableViewModelFilteredList = new FilteredList<>(tableViewModelObservableList, e -> true);

        searchField.setOnKeyReleased(event -> {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                tableViewModelFilteredList.setPredicate( (Predicate<? super TableViewModel>) tableViewModel -> {
                    if (newValue == null || newValue.isEmpty()){
                        return true;
                    }else if (tableViewModel.getTitle().toLowerCase().contains(newValue.toLowerCase())){
                        return true;
                    }else if (tableViewModel.getYear().toString().contains(newValue)){
                        return true;
                    }


                    return false;
                });
            });

            SortedList<TableViewModel> sortedTableViewModel = new SortedList<>(tableViewModelFilteredList);
            sortedTableViewModel.comparatorProperty().bind(table.comparatorProperty());
            table.setItems(sortedTableViewModel);
        });

        table.setItems(tableViewModelObservableList);

    }


    //    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//
////        this.dbStatus.setFill(Color.rgb(244, 91, 59));
//
//        // This is Database connection LED
//
//        this.dbStatus.setStroke(Color.BLACK);
//
//        if (connection == null) {
//            this.dbStatus.setFill(Color.rgb(244, 91, 59));
//        } else {
//            this.dbStatus.setFill(Color.rgb(107, 244, 66));
//        }
//
//
//
//        title.setCellValueFactory(new PropertyValueFactory<>("title"));
//        year.setCellValueFactory(new PropertyValueFactory<>("year"));
//
//        try {
//            Connection connection = DBConnect.getConnection();
//            ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM Movie_List");
//
//            while (resultSet.next()){
//                list.add(new TableViewModel(resultSet.getString("Movie_Name"), resultSet.getInt("Year")));
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        table.setItems(list);
//
//    }

}
