package sample;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Controller implements Initializable {


    private List<String> attNames;
    String[][] data;

    private int supportThresh;
    private double confThresh;

    private List<Integer> stableAttributes;
    private int decisionAttribute;

    private String decisionValueFrom;
    private String decisionValueTo;

    List<Rule> certainRules = null;
    List<Rule> possibleRules = null;

    List<Rule> allRules = new ArrayList<>();

    List<ActionRule> actionRules = new ArrayList<>();


    @FXML
    TextField inputDataFile;
    @FXML
    TextField inputNameFile;
    @FXML
    TextField outputDataFile;

    @FXML
    ComboBox<String> delimiterBox;

    @FXML
    TextField supportTextField;
    @FXML
    TextField confTextField;

    @FXML
    TextField decAttributeTextField;
    @FXML
    ComboBox<String> decValueFromBox;
    @FXML
    ComboBox<String> decValueToBox;

    @FXML
    ListView<String> stableListView;

    @Override // This method is called by the FXMLLoader when initialization is complete
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        delimiterBox.getItems().setAll("comma", "tab", "space");

        stableListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // For debugging
        inputDataFile.setText("C:\\Users\\Sam\\Documents\\data.txt");
        inputNameFile.setText("C:\\Users\\Sam\\Documents\\names.txt");

        delimiterBox.getSelectionModel().selectFirst();

        decAttributeTextField.setText("3");
        decValueFromBox.getItems().addAll("2");
        decValueFromBox.getSelectionModel().selectFirst();
        decValueToBox.getItems().addAll("1");
        decValueToBox.getSelectionModel().selectFirst();

        confTextField.setText("100");
        supportTextField.setText("1");

        outputDataFile.setText("C:\\Users\\Sam\\Documents\\ar_output.txt");
    }

    // Expects data and names files to be selected to function
    public void loadInputs() {
        System.out.println("Load Inputs button pressed.");

        String dataPath = inputDataFile.getText();
        String namePath = inputNameFile.getText();

        List<String> dataContent = null;
        List<String> nameContent = null;

        try {
            dataContent = Files.readAllLines(Paths.get(dataPath));
            nameContent = Files.readAllLines(Paths.get(namePath));
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Invalid Input File Paths", ButtonType.OK);
            alert.showAndWait();
        }

        // Parse Data file.

        String delimiter = delimiterBox.getValue();

        if(delimiter.equals("comma")) {
            delimiter = ",";
        } else if (delimiter.equals("tab")) {
            delimiter = "\t";
        } else {
            delimiter = " ";
        }

        int numAttributes = dataContent.get(0).split(delimiter).length;

        String[][] dataMatrix = new String[dataContent.size()][numAttributes];

        for (int i = 0; i < dataContent.size(); i++) {
            String[] currEntry = dataContent.get(i).split(delimiter);
            dataMatrix[i] = currEntry;
        }

        data = dataMatrix;

        // Parse Name file.
        attNames = nameContent;

        // Update attribute list with names.
        stableListView.getItems().addAll(attNames);

        System.out.println("Input Processing Completed!");


    }


    public void runLERS(ActionEvent actionEvent) {
        // String[][] s = new String[][] {{"3", "4", "1"}, {"2", "8", "2"}, {"2", "3", "3"}, {"1", "1", "1"}};
        //String[][] s = new String[][] {{"2", "1", "3", "2"}, {"1", "2", "1", "1"}, {"1", "2", "2", "1"},
        //        {"1", "1", "1", "2"}, {"2", "2", "2", "2"}, {"1", "2", "3", "2"}};


        allRules.clear();
        actionRules.clear();

        // First, save current necessary input from forms.
        supportThresh = Integer.parseInt(supportTextField.getText());
        confThresh = Double.parseDouble(confTextField.getText()) * .01;

        int decAttribute = Integer.parseInt(decAttributeTextField.getText());

        stableAttributes = stableListView.getSelectionModel().getSelectedIndices();

        decisionValueFrom = decValueFromBox.getValue();
        decisionValueTo = decValueToBox.getValue();

        computeLERS(data, decAttribute);
        System.out.println("-- Computing Action Rules! --");
        computeActionRules();
    }

    // Input: t - n x m table where n = # of entries and m = # of attributes
    //        d - index of decision attribute (between 0 and m - 1)
    private void computeLERS(String[][] t, int d) {

        // ******************************ASSEMBLE************************************************

        List<AttributeGroup> attributeGroups = new ArrayList<>();
        List<AttributeGroup> decisionAttributeGroups = new ArrayList<>();

        // Outer loop - attribute level
        for (int i = 0; i < t[0].length; i++) {

            HashMap<String, Set<Integer>> hm = new HashMap<>();
            Set<String> currVals = new TreeSet<>();

            // Inner loop - entry level
            for (int j = 0; j <t.length; j++) {

                String keyString = t[j][i];

                // Skip unspecified attributes
                if (keyString.equals("?"))
                    continue;

                Set<Integer> currVal = hm.get(keyString);

                if (currVal != null) {
                    // Add entry #.
                    currVal.add(j);
                } else {
                    // Create new value.
                    Set<Integer> newVal = new TreeSet<>();
                    newVal.add(j);
                    hm.put(keyString, newVal);
                }
            }

            // Create set of attribute values here?
            Set<String> keys = hm.keySet();
            for (String k : keys) {
                ArrayList<String> attVals = new ArrayList<>();

                for (int a = 1; a < t[0].length; a++) {
                    attVals.add(null);
                }

                // Add current attribute's hashmap to the list. Leave the rest blank. These will be filled in as
                // merging happens later on.
                attVals.add(i, k);

                AttributeGroup ag = new AttributeGroup(attVals, hm.get(k));

                // Separate the decision attribute sets from others.
                if (i == d) {
                    decisionAttributeGroups.add(ag);
                } else {
                    attributeGroups.add(ag);
                }
            }

        }


        // ******************************MAIN LOOP************************************************


        // Main loop. Check for subsets of decision attributes.

        int desiredSize = 2;
        certainRules = new ArrayList<>();
        possibleRules = new ArrayList<>();

        boolean setsRemain = true;

        while (setsRemain) {

            System.out.println(String.format("-- Iteration %d --", desiredSize - 1));

            List<Rule> currCertainRules = new ArrayList<>();
            List<Rule> currPossibleRules = new ArrayList<>();

            List<AttributeGroup> currUnmarked = new ArrayList<>();

            for (AttributeGroup attGroup : attributeGroups) {
                for (AttributeGroup decGroup : decisionAttributeGroups) {

                    // Find intersection of attGroup and decGroup.

                    attGroup.marked = null;
                    // This is a possible rule.
                    // Count the overlap. (Note, maybe eliminate the outer if statement)
                    int numOverLap = 0;
                    for (Integer e : attGroup.entries) {
                        if (decGroup.entries.contains(e)) {
                            numOverLap++;
                        }
                    }

                    // Only create rules for fully or partially overlapping groups.
                    if (numOverLap == 0)
                        continue;

                    double currConf = (double) numOverLap / attGroup.entries.size();

                    // Must pass support and confidence thresholds!
                    if (numOverLap < supportThresh || currConf < confThresh)
                        continue;

                    Rule newRule = new Rule(
                            attGroup,
                            decGroup,
                            currConf,
                            numOverLap,
                            attNames);


                    if (numOverLap == attGroup.entries.size()) {
                        currCertainRules.add(newRule);
                    } else {
                        currPossibleRules.add(newRule);
                        currUnmarked.add(attGroup);
                    }

                }
            }


            // Rules have been created. From the current possible ones, attempt to combine them.
            List<AttributeGroup> newAttGroups = new ArrayList<>();
            for (int i = 0; i < currPossibleRules.size(); i++) {

                for (int j = i + 1; j < currPossibleRules.size(); j++) {
                    // Attempts to combine sets. If fails, returns null
                    AttributeGroup combinedGroup = AttributeGroup.combine(currUnmarked.get(i), currUnmarked.get(j), desiredSize);
                    if (combinedGroup != null) {
                        newAttGroups.add(combinedGroup);
                    }
                }
            }

            desiredSize++;

            // New AttributeGroups have all been created.
            // Remove duplicates!
            List<AttributeGroup> uniqueAttGroups = new ArrayList<>();
            for (AttributeGroup a : newAttGroups) {
                if (!uniqueAttGroups.contains(a))
                    uniqueAttGroups.add(a);
            }

            List<Rule> uniqueCertainRules = new ArrayList<>();
            for (Rule r : currCertainRules) {
                if (!uniqueCertainRules.contains(r))
                    uniqueCertainRules.add(r);
            }

            List<Rule> uniquePossibleRules = new ArrayList<>();
            for (Rule r : currPossibleRules) {
                if (!uniquePossibleRules.contains(r))
                    uniquePossibleRules.add(r);
            }



            attributeGroups = uniqueAttGroups;


            // TODO Print Certain rules.

            // TODO Print Possible rules with support and confidence.

            certainRules.addAll(uniqueCertainRules);
            possibleRules.addAll(uniquePossibleRules);


            setsRemain = !uniqueAttGroups.isEmpty();
        }

        System.out.println("No more extraction can be done!");
        allRules.addAll(certainRules);
        allRules.addAll(possibleRules);

    }

    public void computeActionRules() {
        // Look for elements with From
        // Look for elements with To

        for (int i = 0; i < certainRules.size(); i++) {
            Rule fromRule = certainRules.get(i);
            if (fromRule.decisionGroup.attVals.contains(decisionValueFrom)) {
                for (int j = i + 1; j < certainRules.size(); j++) {
                    Rule toRule = certainRules.get(j);
                    if (toRule.decisionGroup.attVals.contains(decisionValueTo)) {
                        // This could be an action rule!
                        // Check if stable attributes are the same. They must be.
                        // Identify flexible attributes that are different.

                        List<String[]> toFromPairs = new ArrayList<>();

                        for (int k = 0; k < attNames.size(); k++) {
                            String fromVal = fromRule.attributeGroup.attVals.get(k);
                            String toVal = toRule.attributeGroup.attVals.get(k);

                            if (fromVal == null && toVal == null) {
                                continue;
                            }


                            if (stableAttributes.contains(k)) {

                                // Ensure they are the same.
                                if (fromVal == null) {
                                    toFromPairs.add(new String[] {Integer.toString(k), toVal, toVal});
                                } else if (toVal == null) {
                                    toFromPairs.add(new String[] {Integer.toString(k), fromVal, fromVal});
                                } else if (fromVal.equals(toVal)) {
                                    toFromPairs.add(new String[] {Integer.toString(k), fromVal, toVal});
                                } else {
                                    break;
                                }

                            } else {
                                // It's a flexible attribute.
                                if (fromVal != null && toVal != null) {
                                    toFromPairs.add(new String[]{Integer.toString(k), fromVal, toVal});
                                }

                            }
                        }

                        ActionRule newAR = new ActionRule(toFromPairs);
                        actionRules.add(newAR);
                    }
                }
            }
        }
    }
}
