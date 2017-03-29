package com.rapid_2_24_17;

import com.gluonhq.charm.glisten.control.AppBar;
import com.gluonhq.charm.glisten.mvc.View;
import com.gluonhq.charm.glisten.visual.MaterialDesignIcon;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import static javafx.scene.layout.BackgroundSize.AUTO;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class BasicView extends View {

    // Login View variables
    private PreparedStatement pstmt;
    TextField userNameTF = new TextField();
    PasswordField passWordPF = new PasswordField();
    Label notificationLabel = new Label();
    String userName, passWord;
    Connection conn;

    // Selection View variables
    ObservableList<String> chapterList = FXCollections.observableArrayList();
    ObservableList<String> sectionList = FXCollections.observableArrayList();
    ObservableList<String> questionList = FXCollections.observableArrayList();
    ComboBox chapterCB = new ComboBox(chapterList);
    ComboBox sectionsCB = new ComboBox();
    ComboBox questionsCB = new ComboBox();
    String questionSelected;
    String chapSelected;
    String sectionSelected;

    // Question View variables
    String a, b, c, d, e = "";
    String key, hint, userAnswer, currentQuestion = "";
    CheckBox aBtn, bBtn, cBtn, dBtn, eBtn;
    RadioButton aRBtn, bRBtn, cRBtn, dRBtn, eRBtn;
    final ToggleGroup group = new ToggleGroup();
    Label questionLabel = new Label("");
    Button hintBtn = new Button("Hint");
    Button nextBtn = new Button("Next question");
    Button answerBtn = new Button("Submit");
    int correctCount, wrongCount;

    public BasicView(String name) {
        super(name);
        setCenter(loginView());
    }

    //=========================LOGIN VIEW==============================\\
    public VBox loginView() {
        // initailize database access
        initializeJdbc();

        Text title = new Text("Account Login");

        userNameTF.setPromptText("Enter username");
        userNameTF.setFocusTraversable(false);
        userNameTF.setMaxWidth(200);

        passWordPF.setPromptText("Enter password");
        passWordPF.setFocusTraversable(false);
        passWordPF.setMaxWidth(200);

        title.setScaleX(2);
        title.setScaleY(2);
        title.setStyle("-fx-font-weight: bold");

        notificationLabel.setTextFill(Color.web("#FF0000"));

        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: darkblue");

        Button newAccBtn = new Button("Create New Account");
        newAccBtn.setStyle("-fx-background-color: darkblue");

        loginBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                userName = userNameTF.getText();
                passWord = passWordPF.getText();
                if (userName.isEmpty() && passWord.isEmpty()) {
                    notificationLabel.setText("Please enter a username and a password.");
                } else if (userName.isEmpty()) {
                    notificationLabel.setText("Please enter a username.");
                } else if (passWord.isEmpty()) {
                    notificationLabel.setText("Please enter a password.");
                } else {
                    try {
                        ResultSet rs = pstmt.executeQuery("select * from " + userName + " where username = '" + userName + "' and password = '" + passWord + "';");
                        System.out.println("Login successful!");
                        //initialize and generate next view (selection view);
                        reinitializeSelectionView(); // in case user returns to login view and relogs in
                        setCenter(selectionView());
                    } catch (SQLException ex) {
                        //if sqlException occurs username must not exist.
                        notificationLabel.setText("Invalid username/password.");
                    }
                }
            }
        }
        );

        newAccBtn.setOnAction(
                new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e
            ) {
                try {
                    createNewAccount();
                } catch (SQLException ex) {
                    System.out.println("SQLException occurred.");
                }
            }
        }
        );

        VBox loginVBox = new VBox(30.0, title, userNameTF, passWordPF, loginBtn, newAccBtn, notificationLabel);

        loginVBox.setStyle(
                "-fx-background-color: beige");
        loginVBox.setAlignment(Pos.CENTER);
        return loginVBox;
    }

    private void initializeJdbc() {
        try {
            //need this dependency in gluons gradle for JDBC driver access ->   compile 'mysql:mysql-connector-java:5.1.40'
            // Load the JDBC driver 
            Class.forName("com.mysql.jdbc.Driver");
            System.out.println("Driver loaded");
            // Establish a connection
            conn = DriverManager.getConnection("jdbc:mysql://liang.armstrong.edu:3306/team2", "team2", "tiger");
            //conn = DriverManager.getConnection("jdbc:mysql://us-cdbr-iron-east-04.cleardb.net:3306/heroku_e776c68d59d1bdf?reconnect=true", "b1c154c0428d00", "30a2e4ed");
            System.out.println("Database connected");
            // Create a Statement
            pstmt = conn.prepareStatement("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createNewAccount() throws SQLException {
        userName = userNameTF.getText();
        passWord = passWordPF.getText();
        notificationLabel.setTextFill(Color.web("#FF0000"));
        //handle empty username or password
        if (userName.isEmpty() && passWord.isEmpty()) {
            notificationLabel.setText("Please enter a username and a password.");
        } else if (userName.isEmpty()) {
            notificationLabel.setText("Please enter a username.");
        } else if (passWord.isEmpty()) {
            notificationLabel.setText("Please enter a password.");
        } else {
            try {
                DatabaseMetaData dbm = conn.getMetaData();
                ResultSet tables = dbm.getTables(null, null, userName, null);
                if (tables.next()) {
                    notificationLabel.setText("Username already exists. Please try another.");
                } else {
                    Statement stmt = conn.createStatement();
                    String createTable = "CREATE TABLE " + userName
                            + "(chapter INT(2), "
                            + " correct_answers INT(11), "
                            + " wrong_answers INT(11), "
                            + " username VARCHAR(45), "
                            + " password VARCHAR(45), "
                            + " PRIMARY KEY ( chapter ))";

                    stmt.executeUpdate(createTable);
                    //populate chapter numbers 1-44 and username/password
                    for (int i = 1; i < 45; i++) {
                        pstmt = conn.prepareStatement("insert into " + userName
                                + "(chapter, correct_answers, wrong_answers, username, password) values(?,?,?,?,?)");

                        pstmt.setInt(1, i); //chapter
                        pstmt.setInt(2, 0); //correct_answers
                        pstmt.setInt(3, 0); //incorrect_answers
                        pstmt.setString(4, userName); //username
                        pstmt.setString(5, passWord); //password
                        pstmt.executeUpdate();
                    }
                    notificationLabel.setText("Account created!");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    public void reinitializeLoginView() {
        userNameTF.clear();
        passWordPF.clear();
        userName = "";
        passWord = "";
        notificationLabel.setText("");
        setCenter(loginView());
    }

    //=============================SELECTION VIEW==============================\\
    public VBox selectionView() {
        Button submitBtn = new Button("Submit");
        Button statisticsBtn = new Button("My Statistics");
        submitBtn.setStyle("-fx-background-color: darkblue");
        statisticsBtn.setStyle("-fx-background-color: darkblue");

        // populate chapterList if it is empty
        if (chapterList.isEmpty()) {
            try {
                URL url = new URL("https://morning-ridge-37817.herokuapp.com/chapterTitles.txt");
                Scanner scanner = new Scanner(url.openStream());
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    chapterList.add(line);
                }

            } catch (MalformedURLException ex) {
                System.out.println("URL not working...");
            } catch (IOException ex) {
                System.out.println("IO Exception...");
            }
        }
        chapterCB.setPromptText("Select chapter");
        chapterCB.setPrefSize(150, 20);

        sectionsCB.setPromptText("Select section");
        sectionsCB.setPrefSize(150, 20);

        questionsCB.setPromptText("Select question");
        questionsCB.setPrefSize(150, 20);

        // chapter combobox selection
        chapterCB.setOnAction((e) -> {
            try {
                //split chapter titles into example: "chapter1" instead of the whole title in order to access txt file names such as "chapter1.txt"
                chapSelected = chapterCB.getValue().toString().substring(0, 10).replaceAll("\\s", "");
                chapSelected = chapSelected.replace("C", "c");
                // get current chapter sections
                setSections();
            } catch (NullPointerException ex) {
                //catch NullPointer due to erasing combobox values after each new selection
            }
        });
        // sections combobox selection
        sectionsCB.setOnAction((e) -> {
            try {
                sectionSelected = sectionsCB.getValue().toString();
                //set current sections questions
                setQuestions();
            } catch (NullPointerException ex) {
                //catch NullPointer due to erasing combobox values after each new selection
            }
        });
        // questions combobox selection
        questionsCB.setOnAction((e) -> {
            try {
                questionSelected = questionsCB.getValue().toString().replace(".", "");
                questionSelected = questionSelected.replaceAll(" ", "");//replace all . and spaces for QuestionView
                questionSelected = questionSelected.replaceAll("\t", ""); //replace tabs that are present sometimes
            } catch (NullPointerException ex) {
                //catch NullPointer due to erasing combobox values after each new selection
            }
        });
        // Selection View submit, load Question View
        submitBtn.setOnAction((ActionEvent event) -> {
            setCenter(questionView());
        });

        statisticsBtn.setOnAction((ActionEvent event) -> {
            setCenter(statisticsView());
            //getStatisticsForAllChapters();
        });

        Text space = new Text("\n\n\n\n\n"); // formatting due to background picture
        VBox selectionVBox = new VBox(20.0, space, chapterCB, sectionsCB, questionsCB, submitBtn, statisticsBtn);

        //Background for Selection View
        BackgroundSize backgroundSize = new BackgroundSize(AUTO, 615, false, false, false, false);
        BackgroundImage myBI = new BackgroundImage(new Image("https://morning-ridge-37817.herokuapp.com/intro10book.jpg"),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
                backgroundSize);
        selectionVBox.setBackground(new Background(myBI)); // set background picture
        selectionVBox.setAlignment(Pos.CENTER);

        return selectionVBox;
    }

    public void setSections() {
        //initialize comboboxes/lists back to empty for each new chapter selected
        sectionsCB.getItems().clear();
        sectionList.clear();
        questionList.clear();

        // get values for sections and questions from specified chapter
        try {
            URL url = new URL("https://morning-ridge-37817.herokuapp.com/" + chapSelected + ".txt");
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                //add sections to sectionList
                if (line.matches("~")) {
                    //replace everything except degits
                    sectionList.add(scanner.nextLine());
                }
            }
            //add values from ObservableLists to section and question comboboxes
            sectionsCB.getItems().addAll(sectionList);
        } catch (MalformedURLException ex) {
            System.out.println("URL not working...");
        } catch (IOException ex) {
        }
    }

    public void setQuestions() {
        questionsCB.getItems().clear();
        questionList.clear();
        // populate question combo box according to chapter selected.
        try {
            URL url = new URL("https://morning-ridge-37817.herokuapp.com/" + chapSelected + ".txt");
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals(sectionSelected)) {
                    for (int i = 0; i < 100; i++) { //size of possible sections....just did 100 for the moment.
                        line = scanner.nextLine();
                        if (line.matches("#")) {
                            String questionNum = scanner.nextLine().substring(0, 3); //get question number for that section
                            questionList.add(questionNum);
                        }
                        if (line.matches("~") || line.matches("@")) {
                            break;
                        } else {
                        }
                    }

                }
            }
            questionsCB.getItems().addAll(questionList);
        } catch (MalformedURLException ex) {
            System.out.println("URL not working...");
        } catch (IOException ex) {
        }
    }

    public void reinitializeSelectionView() {
        chapterCB.getSelectionModel().clearSelection();
        sectionsCB.getSelectionModel().clearSelection();
        questionsCB.getSelectionModel().clearSelection();
    }

    //===============================QUESTION VIEW==================================\\
    public VBox questionView() {
        hintBtn.setVisible(false);
        questionLabel.setWrapText(true);
        questionLabel.setStyle("-fx-font-weight:bold");

        Label hintLabel = new Label("");

        //images and image size
        Image checkMark = new Image("https://morning-ridge-37817.herokuapp.com/check.png");
        Image xImg = new Image("https://morning-ridge-37817.herokuapp.com/x.png");
        ImageView imageView = new ImageView();
        imageView.setFitHeight(35);
        imageView.setFitWidth(35);
        //get questions
        try {
            fetchQuestionAndSetValuesForQuestion();
            //generates radio or checkboxes for question
            if (key.length() == 1) {
                generateCorrectAmountOfRadioButtons();
            } else {
                generateCorrectAmountOfCheckBoxes();
            }
        } catch (NullPointerException ex) {
            System.out.println("NullPointer exception.");
        } catch (NoSuchElementException ex) {
            //Reached end of chapter
            hideOptions();
        }

        answerBtn.setStyle("-fx-background-color: darkblue");
        //on submit answer button click
        answerBtn.setOnAction((ActionEvent e) -> {
            //gets checkbox values and stores into String userAnswer
            if (key.length() == 1) {
                getRadioButtonValues();
            } else {
                getCheckBoxValues();
            }
            System.out.println("User answer = " + userAnswer);
            if (userAnswer.matches(key)) {
                //correct -> checkmark image
                imageView.setImage(checkMark);
                incrementCorrectAnswer();
                //initilize userAnswer back to empty each submit.
                userAnswer = "";
            } else {
                //incorrect -> X image
                imageView.setImage(xImg);
                incrementWrongAnswer();
                //initilize userAnswer back to empty after each submit
                userAnswer = "";
                System.out.println("Wrong, Key is:" + key);
            }
        });
        hintBtn.setStyle("-fx-background-color: darkblue");
        //hint button action
        hintBtn.setOnAction((ActionEvent e1) -> {
            hintLabel.setWrapText(true);
            hintLabel.setText(hint);
        });

        Button returnBtn = new Button("Return");
        returnBtn.setStyle("-fx-background-color: darkblue");
        returnBtn.setOnAction((ActionEvent x) -> {
            showOptions();
            reinitializeQuestionView();
            setCenter(selectionView());
        });
        nextBtn.setStyle("-fx-background-color: darkblue");
        nextBtn.setOnAction((ActionEvent x) -> {
            int nextQuestion;
            nextQuestion = Integer.parseInt(questionSelected);
            //increment to next question
            questionSelected = String.valueOf(++nextQuestion);
            reinitializeQuestionView();
            setCenter(questionView());
        });
        HBox hBox = new HBox(62.0, answerBtn, hintBtn, nextBtn);
        hBox.setAlignment(Pos.CENTER);
        if (key.length() == 1) {
            VBox questionVBox = new VBox(10.0, questionLabel, aRBtn, bRBtn, cRBtn, dRBtn, eRBtn, imageView, hBox, hintLabel, returnBtn);
            questionVBox.setAlignment(Pos.CENTER_LEFT);
            questionVBox.setStyle("-fx-background-color: beige");
            return questionVBox;
        } else {
            VBox questionVBox = new VBox(10.0, questionLabel, aBtn, bBtn, cBtn, dBtn, eBtn, imageView, hBox, hintLabel, returnBtn);
            questionVBox.setAlignment(Pos.CENTER_LEFT);
            questionVBox.setStyle("-fx-background-color: beige");
            return questionVBox;
        }
    }

    public void fetchQuestionAndSetValuesForQuestion() {
        // Find values for sections and questions from specified chapter
        int countHashtag = 0; //counter for hashtags (this will get which question they want
        try {
            URL url = new URL("https://morning-ridge-37817.herokuapp.com/" + chapSelected + ".txt");
            Scanner scanner = new Scanner(url.openStream());
            while (scanner.hasNextLine()) { //hasNextLine() lets you get to chap 1 #37 and #38
                String delim = scanner.next();
                //counts all #'s and if its = to the question number selected then store all the values of the question and put in label.
                if (delim.matches("#") || delim.matches("@")) {
                    countHashtag++;
                    if (Integer.parseInt(questionSelected) == countHashtag) {
                        for (int i = 0; i < 400; i++) { //get every part of question.... some questions are long
                            String line = scanner.nextLine();
                            //store each component of the question (set line = "", so that we can add answers with radiobuttons?
                            if (line.startsWith("a.")) {
                                a = line;
                                line = "";
                            }
                            if (line.startsWith("b.")) {
                                b = line;
                                line = "";
                            }
                            if (line.startsWith("c.")) {
                                c = line;
                                line = "";
                            }
                            if (line.startsWith("d.")) {
                                d = line;
                                line = "";
                            }
                            if (line.startsWith("e.")) {
                                e = line;
                                line = "";
                            }
                            if (line.startsWith("Key:") || (line.contains("key:"))) { //add line to string key then break from loop because it is the end of the question.
                                key = line.substring(4, line.length());
                                if (line.length() > 9) { // means there is a hint
                                    key = line.substring(4, line.indexOf(" "));
                                    int beginningIndexOfHint = line.indexOf(" ");
                                    hint = line.substring(beginningIndexOfHint + 1, line.length());
                                }
                                break;
                            }
                            currentQuestion += line;
                        }
                        break;
                    }
                }
            }
            scanner.close();
            //if key contains more than one answer inform the user.
            if (key.length() > 1) {
                questionLabel.setText(currentQuestion + " \n*Select all that apply.");
            } else {
                questionLabel.setText(currentQuestion);
            }

        } catch (MalformedURLException ex) {
            System.out.println("URL not working...");
        } catch (IOException ex) {
            System.out.println("IO Exception...");
        }
    }

    public void getRadioButtonValues() {
        if (group.getSelectedToggle() != null) {
            userAnswer = group.getSelectedToggle().getUserData().toString();
            userAnswer = userAnswer.substring(0, 1);
        }
    }

    public void getCheckBoxValues() {
        if (aBtn.isSelected()) {
            userAnswer = "a";
        }
        if (bBtn.isSelected()) {
            userAnswer += "b";
        }
        if (cBtn.isSelected()) {
            userAnswer += "c";
        }
        if (dBtn.isSelected()) {
            userAnswer += "d";
        }
        if (eBtn.isSelected()) {
            userAnswer += "e";
        }
        // get rid of initial value null in front of actual answers (this occurs sometimes)
        if (userAnswer.contains("null")) {
            userAnswer = userAnswer.substring(4, userAnswer.length());
        }
    }

    public void generateCorrectAmountOfCheckBoxes() {
        if (!a.isEmpty()) {
            aBtn = new CheckBox(a);
            aBtn.setWrapText(true);
            aBtn.setUserData(a);
        }
        if (!b.isEmpty()) {
            bBtn = new CheckBox(b);
            bBtn.setWrapText(true);
            bBtn.setUserData(b);
        }
        if (!c.isEmpty()) {
            cBtn = new CheckBox(c);
            cBtn.setWrapText(true);
            cBtn.setUserData(c);
        }
        if (!d.isEmpty()) {
            dBtn = new CheckBox(d);
            dBtn.setWrapText(true);
            dBtn.setUserData(d);
        }

        if (!e.isEmpty()) {
            eBtn = new CheckBox(e);
            eBtn.setWrapText(true);
            eBtn.setUserData(e);
        }
        //Only need this for these for c d and e because all other questions have atleast options: a,b
        if (c.isEmpty()) { //hide radio button c in case there is no d in the question (must still created though otherwise error)
            cBtn = new CheckBox(c);
            cBtn.setVisible(false);
        }
        if (d.isEmpty()) { //hide radio button d in case there is no d in the question (must still created though otherwise error)
            dBtn = new CheckBox(d);
            dBtn.setVisible(false);
        }
        if (e.isEmpty()) { //hide radio button e in case there is no e in the question. (must still created though otherwise error)
            eBtn = new CheckBox(e);
            eBtn.setVisible(false);
        }
        if (!hint.isEmpty()) {
            hintBtn.setVisible(true); // hint make hintBtn visible
        }
    }

    public void generateCorrectAmountOfRadioButtons() {
        if (!a.isEmpty()) {
            aRBtn = new RadioButton(a);
            aRBtn.setWrapText(true);
            aRBtn.setUserData(a);
            aRBtn.setToggleGroup(group);
        }
        if (!b.isEmpty()) {
            bRBtn = new RadioButton(b);
            bRBtn.setWrapText(true);
            bRBtn.setUserData(b);
            bRBtn.setToggleGroup(group);
        }
        if (!c.isEmpty()) {
            cRBtn = new RadioButton(c);
            cRBtn.setWrapText(true);
            cRBtn.setUserData(c);
            cRBtn.setToggleGroup(group);
        }
        if (!d.isEmpty()) {
            dRBtn = new RadioButton(d);
            dRBtn.setWrapText(true);
            dRBtn.setUserData(d);
            dRBtn.setToggleGroup(group);
        }

        if (!e.isEmpty()) {
            eRBtn = new RadioButton(e);
            eRBtn.setWrapText(true);
            eRBtn.setUserData(e);
            eRBtn.setToggleGroup(group);
        }
        //Only need this for these for c d and e because all other questions have atleast options: a,b
        if (c.isEmpty()) { //hide radio button c in case there is no d in the question (must still created though otherwise error)
            cRBtn = new RadioButton(c);
            cRBtn.setVisible(false);
        }
        if (d.isEmpty()) { //hide radio button d in case there is no d in the question (must still created though otherwise error)
            dRBtn = new RadioButton(d);
            dRBtn.setVisible(false);
        }
        if (e.isEmpty()) { //hide radio button e in case there is no e in the question. (must still created though otherwise error)
            eRBtn = new RadioButton(e);
            eRBtn.setVisible(false);
        }
        if (!hint.isEmpty()) {
            hintBtn.setVisible(true); // hint make hintBtn visible
        }
    }

    public void reinitializeQuestionView() {
        a = "";
        b = "";
        c = "";
        d = "";
        e = "";
        key = "";
        hint = "";
        userAnswer = "";
        currentQuestion = "";
        questionLabel.setText("");
    }

    //Increment correct_answer in database
    public void incrementCorrectAnswer() {
        String chapNum = chapSelected.replaceAll("\\D+", "");
        //int chapNum = Integer.parseInt(chapNumStr);
        try {
            pstmt.executeUpdate("Update " + userName + " set correct_answers = correct_answers + 1 where chapter = " + chapNum + ";");
        } catch (SQLException ex) {
            System.out.println("SQL Exception.");
        }
    }
    // Increment wrong_answer in database
    public void incrementWrongAnswer() {
        String chapNum = chapSelected.replaceAll("\\D+", "");
        try {
            pstmt.executeUpdate("Update " + userName + " set wrong_answers = wrong_answers + 1 where chapter = " + chapNum + ";");
        } catch (SQLException ex) {
            System.out.println("SQL Exception.");
        }
    }

    // End of chapter so hide options other than return to Selection View
    public void hideOptions() {
        questionLabel.setStyle("-fx-font-weight:bold");
        questionLabel.setText("END OF CHAPTER");
        aBtn.setVisible(false);
        bBtn.setVisible(false);
        cBtn.setVisible(false);
        dBtn.setVisible(false);
        eBtn.setVisible(false);
        hintBtn.setVisible(false);
        nextBtn.setVisible(false);
        answerBtn.setVisible(false);
    }

    // set all options that might be hidden to visible
    public void showOptions() {
        hintBtn.setVisible(true);
        nextBtn.setVisible(true);
        answerBtn.setVisible(true);
    }

    //============================STATISTICS VIEW==============================\\
    public VBox statisticsView() {
        Button returnBtn = new Button("Return");
        returnBtn.setStyle("-fx-background-color: darkblue");
        returnBtn.setOnAction((ActionEvent x) -> {
            showOptions();
            reinitializeQuestionView();
            setCenter(selectionView());
        });
        TextArea ta = new TextArea(getStatisticsForAllChapters());
        ta.setEditable(false);
        ta.setPrefHeight(500);
        VBox statisticsVBox = new VBox(10.0, ta, returnBtn);
        statisticsVBox.setStyle("-fx-background-color: beige");
        statisticsVBox.setAlignment(Pos.TOP_LEFT);
        return statisticsVBox;
    }

    public String getStatisticsForAllChapters() {
        String display = "";
        ArrayList<String> correctAnswersList = new ArrayList<>();
        ArrayList<String> wrongAnswersList = new ArrayList<>();
        int correctTotal = 0;
        int wrongTotal = 0;
        try {
            //get correct answers per chapter
            ResultSet rs = pstmt.executeQuery("select correct_answers from " + userName + ";");
            int i = 0;
            while (rs.next()) {
                correctAnswersList.add(i, "Correct: " + rs.getInt(1)); //index 1, correct: " index 3
                i++;
            }
            //get incorrect answers per chapter
            rs = pstmt.executeQuery("select wrong_answers from " + userName + ";");
            int j = 0;
            while (rs.next()) {
                wrongAnswersList.add(j, " Wrong: " + rs.getInt(1));
                j++;
            }
            //get correct TOTAL
            rs = pstmt.executeQuery("select SUM(correct_answers) from " + userName + ";");
            if (rs.next()) {
                correctTotal = rs.getInt(1);
            }
            rs = pstmt.executeQuery("select SUM(wrong_answers) from " + userName + ";");
            if (rs.next()) {
                wrongTotal = rs.getInt(1);
            }
            //get incorrect TOTAL
        } catch (SQLException ex) {
            System.out.println("SQL Exception.");
        }

        for (int i = 0; i < 44; i++) {
            display += chapterList.get(i) + "\n" + correctAnswersList.get(i) + "\t" + wrongAnswersList.get(i) + "\n" + "-----------------------------------------------------------------------------------" + "\n";
        }
        display += "TOTAL CORRECT: " + correctTotal + "\nTOTAL WRONG: " + wrongTotal;
        return display;
    }

    //===============================APP_BAR=====================================\\
    @Override
    protected void updateAppBar(AppBar appBar) {
        appBar.setTitleText("Java Multiple Choice Questions");
        appBar.setStyle("-fx-background-color: darkblue");
        appBar.getActionItems().add(MaterialDesignIcon.POWER_SETTINGS_NEW.button(e -> reinitializeLoginView()));
    }
}
