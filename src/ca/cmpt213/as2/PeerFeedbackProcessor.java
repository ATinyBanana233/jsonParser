/*
    How does GSON map; we need constructor??
        -Libraries like GSON, Jackson or the Java Persistence API (JPA)
        generally use a no-argument (default) construtor to instantiate an
        object and set its fields via reflection.
        In newer versions of GSON, you do not even have to declare a default constructor anymore, see here.

*/

/*
Sorry for the confusion, but Group class really should be named as StudentFeedback
and Contribution should be named as StudentContribution

Design:
- Main will initiate a list of student groups
   - The StudentGroup represents a group; each group has students
       - Each StudentEvaluation represents a student inside a group
            - in each StudentEvaluation...
                - each Group object is the feedback that the evaluation owner gave to members of the group
                    - feedback contains the target student name, email, and contribution object
                        - each Contribution object has score and comment
 */

package ca.cmpt213.as2;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * The class that holds the main application for the JSON peer feedback processor program.
 * @author Bei Bei Li
 */
public class PeerFeedbackProcessor {

    /**
     * Main function to run the JSON peer feedback processor; it exactly takes 2 command arguments.
     * @param args Two arguments from the user to specify (1)input .JSON files' and (2)output .csv file's directory path
     */
    public static void main(String[] args) {

        //check argument errors
        argsChecker(args);

        String inputPath = args[0];
        String outputPath = args[1];

        //search for .JSON files
        List<File> jsonFiles = new ArrayList<>();
        jsonFiles = recursiveSearchJson(inputPath, jsonFiles);

        //map .JSON files to java objects
        List<StudentEvaluation> studentEvaluations = new ArrayList<>();
        studentEvaluations = mapJson(jsonFiles, studentEvaluations);

        //check required fields are mapped
        checkMapJson(studentEvaluations);

        //grouping students
        List<StudentGroup> teams = new ArrayList<>();
        teams = groupingStudents(teams, studentEvaluations);

        //error check on student evaluations in each group
        hasAllFeedbackErrorCheck(teams);
        hasAllStudentMentionedEvaluationErrorCheck(teams);
        checkEvaluationScoreSum(teams);

        //one should in one group error check temporarily missing
        //...
        //...

        //identify and create output file path

        //default file path and name;
        String filePathName = outputPath + "group_feedback.csv";

        //if output path does not have / at the end...
        Pattern regexPatternForEndSlash = Pattern.compile(".*[/]");
        Matcher regexMatcherForEndSlash = regexPatternForEndSlash.matcher(outputPath);

        //if output path includes .csv
        Pattern regexPatterForEndCsv = Pattern.compile(".*.csv");
        Matcher regexMatcherForEndCsv = regexPatterForEndCsv.matcher(outputPath);

        if (regexMatcherForEndSlash.matches() == false && regexMatcherForEndCsv.matches() == false){
            outputPath = outputPath + "/";
            filePathName = outputPath + "group_feedback.csv";
        }

        if (regexMatcherForEndCsv.matches() == true) {
            filePathName = outputPath;
        }

        File fileTarget = new File(filePathName);

        if (fileTarget != null) {

            //create csv
            try {
                PrintWriter printer = null;

                try {

                    printer = new PrintWriter(fileTarget);
                    printCSV(printer, teams);

                } catch (IOException io) {
                    System.out.println("ERROR: IOException caught during csv forming phase");
                    throw new Exception();
                } finally {
                    printer.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("ERROR: Enable to create csv file, " +
                    "error in creating instantiate file object for the csv file");
            exitProg();
        }


    }

    /**
     * Validate the command line arguments' correctness.
     * @param args Two arguments from the user to specify (1)input .JSON files' and (2)output .csv file's directory path
     */
    private static void argsChecker(String[] args) {

        //check the number of arguments
        int validArgsNumber = 2;
        if (args.length != validArgsNumber) {
            System.out.println("ERROR: 2 arguments are expected:");
            System.out.println("    1. directory path for the input .JSON files");
            System.out.println("    2. directory path for the out .csv file");
            exitProg();
        }

        //check if both paths exist
        String userInputPath = args[0];
        String userOutputPath = args[1];
        File inputPath = new File(userInputPath);
        File outputPath = new File(userOutputPath);

        if (!inputPath.exists()) {
            System.out.println("ERROR: Check inputted arguments' correctness; input directory path does not exist");
            exitProg();
        } else if (!outputPath.exists()) {
            System.out.println("ERROR: check inputted arguments' correctness; output directory path does not exist");
            exitProg();
        }

    }

    /**
     * Recursively finds all the .JSON (case insensitive) files in the path user provided
     * @param inputPath The path user provided that contains .JSON files to be processed
     * @return A List<File> reference pointer that contains all the found .JSON files
     */
    //do not construct the list in the method, it needs to be declared in main for referencing purpose
    //no refresh if in main, but if it is declared in the function, it gets declared every time in each recursion call
    private static List<File> recursiveSearchJson(String inputPath, List<File> searchedJson){

        //create filter for .json files
        FileFilter jsonFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                //if case sensitive
                //Pattern regexPattern = Pattern.compile(".*.[jJ][sS][oO][nN]");
                //Matcher regexMatcher = regexPattern.matcher(file.getName());
                //return regexMatcher.matches();
                return file.getName().endsWith(".json");
            }
        };

        //search for .json files
        File inputDirectory = new File(inputPath);
        File[] fileList = inputDirectory.listFiles();

        if ((fileList != null) && (fileList.length >= 1)){
            //base case
            for (File eachFile : Objects.requireNonNull(inputDirectory.listFiles(jsonFilter))){
                searchedJson.add(eachFile);
            }
            //recursion
            for (File subFile : fileList){
                if (subFile.isDirectory()){
                    recursiveSearchJson(subFile.getAbsolutePath(), searchedJson);
                }
            }
        }

        //if path contains no .json file
        if (searchedJson.size() < 1 || fileList.length < 1){
            System.out.println("ERROR: no .JSON file found in the given path");
            exitProg();
        }

        return searchedJson;
    }


    /**
     * Map .json files to java objects
     * @param jsonFiles The list of .json files found
     * @param studentEvaluations The pointer reference for resulting StudentEvaluation list
     * @return The pointer reference of resulting StudentEvaluation list
     */
    private static List<StudentEvaluation> mapJson(List<File> jsonFiles, List<StudentEvaluation> studentEvaluations){

        Gson gson = new Gson();

        for(File eachJsonFile : jsonFiles){

            try {
                FileReader fileReader = null;
                JsonReader jsonReader = null;

                try {
                    fileReader = new FileReader(eachJsonFile);
                    jsonReader = new JsonReader(fileReader);

                    StudentEvaluation currentEvaluationRead = gson.fromJson(jsonReader, StudentEvaluation.class);
                    studentEvaluations.add(currentEvaluationRead);

                } catch (FileNotFoundException fileNotFound) {
                    System.out.println("ERROR: FileNotFoundException caught in " +
                            eachJsonFile.getAbsolutePath());
                    throw new Exception();
                } catch (JsonSyntaxException jsonSyntax) {
                    System.out.println("ERROR: JsonSyntaxException caught in " +
                            eachJsonFile.getAbsolutePath());
                    throw new Exception();
                } catch (JsonParseException jsonParse) {
                    System.out.println("ERROR: JsonParseException caught in " +
                            eachJsonFile.getAbsolutePath());
                    throw new Exception();
                } finally {
                    fileReader.close();
                    jsonReader.close();
                }

            } catch (Exception exception) {
                exception.printStackTrace();
                exitProg();
            }

        }
        return studentEvaluations;
    }

    //helper function to group student together
    //return the pointer reference to a list of student groups
    private static List<StudentGroup> groupingStudents(List<StudentGroup> teams, List<StudentEvaluation> studentEvaluations){

        boolean alreadyFoundInGroup = false;

        for (StudentEvaluation eachEva : studentEvaluations){

            String teamName = eachEva.getGroupName();

            StudentGroup currentGroup = isTeamExists(teamName, teams);

            if (currentGroup == null) {
                currentGroup = new StudentGroup(teamName);
                teams.add(currentGroup);
            }

            if (currentGroup.isStudentInGroup(eachEva) == false ){
                currentGroup.addStudentEva(eachEva);
            }
            else {
                System.out.println("ERROR: student evaluation duplicated");
                exitProg();
            }

        }

        return teams;
    }

    //helper function to check if team exists
    //returns null if team does not exist and a pointer to the group if team exists
    private static StudentGroup isTeamExists(String teamName, List<StudentGroup> teams){
        //compareTo() can compare with null; return int;
        StudentGroup result = null;
        if (teams.size() > 0){
            for (StudentGroup eachTeam : teams){
                if (eachTeam.getStudentGroupName().compareTo(teamName) == 0){
                    result = eachTeam;
                    break;
                }
            }
        }
        return result;
    }

    //check if students in a group provided feedback for everyone
    private static void hasAllFeedbackErrorCheck(List<StudentGroup> teams){

        for (StudentGroup eachTeam : teams){
            if (eachTeam.hasAllEvaluation() == false ){
                System.out.println("ERROR: someone in team " + eachTeam.getStudentGroupName() +
                        " did not provide feedback for all team members");
                exitProg();
            }
        }
    }

    //check if all evaluations are received in a team
    private static void hasAllStudentMentionedEvaluationErrorCheck(List<StudentGroup> teams){

        for (StudentGroup eachTeam : teams) {
            if (eachTeam.allEvaluationsReceived() == false ){
                System.out.println("ERROR: someone in team " + eachTeam.getStudentGroupName() +
                        " is missing his/her evaluation");
                exitProg();
            }
        }
    }

    //check sum score in evaluation is sum of scores in the file is not (20 * number of group members), with a tolerance of 0.1.
    private static void checkEvaluationScoreSum(List<StudentGroup> teams) {

        for (StudentGroup eachTeam : teams) {
            for (StudentEvaluation eachEva : eachTeam.getEvaluations()){

                if (eachEva.isSumScoreWithinRange() == false){
                    System.out.println("ERROR: check score sum for " + eachEva.getStudentEmail() + "in team " + eachTeam.getStudentGroupName());
                    exitProg();
                }

            }
        }
    }

    //check if all required fields are mapped
    private static void checkMapJson(List<StudentEvaluation> studentEvaluations){

        boolean isExit = false;
        if (studentEvaluations == null){
            System.out.println("ERROR: JSON files not mapped error detected");
            isExit = true;

        }
        for (StudentEvaluation eachEva : studentEvaluations) {
            if (eachEva.getGroup() == null){
                System.out.println("ERROR: Missing required fields in JSON files");
                isExit = true;
            }
            else {
                for (Group eachGroup : eachEva.getGroup()){
                    if (eachGroup.getName() == null
                            || eachGroup.getSfuEmail() == null
                            || eachGroup.getContribution() == null) {
                        System.out.println("ERROR: Missing required fields in " +
                                eachEva.getStudentEmail() + "'s evaluation");
                        isExit = true;
                    }
                    else {
                        if (eachGroup.getContribution().getScore() < 0) {
                            System.out.println("ERROR: Negative score detected in " +
                                    eachEva.getStudentEmail() + "'s evaluation");
                            isExit = true;
                        }
                        if (eachGroup.getContribution().getComment() == null) {
                            System.out.println("ERROR: Missing required fields in " +
                                    eachEva.getStudentEmail() + "'s evaluation");
                            isExit = true;
                        }
                    }
                }
            }
            if (eachEva.getConfidentialComments() == null ) {
                System.out.println("ERROR: Missing required fields in " +
                        eachEva.getStudentEmail() + "'s evaluation");
                isExit = true;
            }
        }
        if (isExit == true) {
            exitProg();
        }

    }

    //helper function to print the csv
    private static void printCSV(PrintWriter printer, List<StudentGroup> teams){

        printer.println("Group#,Source Student,Target Student,Score,Comment,,Private");

        for (int groupIndex = 0; groupIndex < teams.size(); groupIndex++){
            int displayedGroupIndex = groupIndex + 1;

            //print index
            printer.println("Group " + displayedGroupIndex);
            teams.get(groupIndex).printGroup(printer);
            printer.println();
        }

    }

    //helper function to exit the program
    private static void exitProg() {
        final int FAILURE = -1;
        System.out.println("Now exiting program.");
        System.exit(FAILURE);
    }


}
