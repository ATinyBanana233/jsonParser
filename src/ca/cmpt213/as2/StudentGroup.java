package ca.cmpt213.as2;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A class that represents the student groups: evaluations (List<StudentEvaluation>), studentGroupName (String).
 * @author Bei Bei Li
 */
public class StudentGroup implements Comparator<StudentEvaluation> {

    //ascending order
    //create another reverseSort() after sorting might be more semantically clear if needed
    @Override
    public int compare(StudentEvaluation eva1, StudentEvaluation eva2) {
        return eva1.getStudentEmail().compareTo(eva2.getStudentEmail());
    }

    //the student evaluations inside a group
    private List<StudentEvaluation> evaluations = new ArrayList<>();

    //the csv identifies groups by iterative numbers
    //but each student belongs to a team
    //this is the team number inside the student emails
    //all students in the same team are put into one group
    private String studentGroupName;


    /**
     * Constructor that takes a parameter to instantiate the StudentGroup class
     * @param studentGroupName The name of the group; the team # extracted from student emails
     */
    public StudentGroup(String studentGroupName) {
        this.studentGroupName = studentGroupName;
    }

    /**
     * Constructor to instantiate the StudentGroup class with default studentGroupName = null
     */
    public StudentGroup() {
        this.studentGroupName = null;
    }

    /**
     * Method to retrieve the student group name
     * @return The name of the student group
     */
    public String getStudentGroupName() {
        return this.studentGroupName;
    }

    /**
     * Method to retrieve the group size
     * @return The size of the group (the number of students inside the group)
     */
    public int getGroupSize() {
        return this.evaluations.size();
    }

    /**
     * Method to retrieve pointer to the list of evaluations inside the group
     * @return The pointer reference to the evaluations list (which contains the evaluations)
     */
    public List<StudentEvaluation> getEvaluations() {
        return evaluations;
    }

    /**
     * Method to check if the student is in the group
     * @param studentEva The StudentEvaluation of the student to be checked
     * @return A boolean value to indicate if the student is in the group
     */
    public boolean isStudentInGroup(StudentEvaluation studentEva) {
        boolean isExist = false;
        for (StudentEvaluation eachStudent : evaluations) {
            if (studentEva.getStudentEmail().compareTo(eachStudent.getStudentEmail()) == 0) {
                isExist = true;
                break;
            }
        }
        return isExist;
    }

    /**
     * Method to add the a student evaluation to the group; sorts after each add
     * @param studentEva The StudentEvaluation to be added
     */
    public void addStudentEva(StudentEvaluation studentEva) {
        boolean alreadyExist = isStudentInGroup(studentEva);

        if (alreadyExist == false) {
            this.evaluations.add(studentEva);
            java.util.Collections.sort(evaluations, this);
        }
    }

    //It is an error if any student in a group does not provide feedback about all other students in
    //that group.
    //check all students in a group submitted a feedback for others
    /**
     * Method to check if all evaluations contain feedbacks for all students inside a group
     * @return A boolean value to indicate if all evaluations contain feedbacks
     * for all students inside a group
     */
    public boolean hasAllEvaluation() {

        boolean isInEachEva = true;

        for (StudentEvaluation eachEva : evaluations) {

            String iAmTheFeedBackTarget = eachEva.getStudentEmail();

            for (StudentEvaluation comparedEva : evaluations) {

                isInEachEva = comparedEva.isInTargetStudents(iAmTheFeedBackTarget);
                if (isInEachEva == false) {
                    System.out.println("ERROR: " + comparedEva.getStudentEmail() +
                            " did not provide feedback for " + iAmTheFeedBackTarget);
                    break;
                }

            }
            if (isInEachEva == false) {
                break;
            }
        }

        return isInEachEva;

    }

    //It is an error if any student who is mentioned in the feedback of another student in the
    //group fails to submit a JSON feedback file.
    //check a student provided feed back for all team members
    /**
     * Method to check if all students who should be inside the group submitted their evaluations
     * @return A boolean value to indicate if all students who should be inside the group
     * submitted their evaluations
     */
    public boolean allEvaluationsReceived() {

        boolean targetHasEvaluation = false;

        if (evaluations.size() == 1) {

            for (StudentEvaluation eachEva : evaluations) {
                if (eachEva.getGroupSize() == 1) {
                    targetHasEvaluation = true;
                }
            }
        } else {

            for (StudentEvaluation eachEva : evaluations) {

                //get all the feedbacks inside an evaluation
                List<String> teamMembers = eachEva.getTargetStudents();

                //for each feeback target
                for (String checkMember : teamMembers) {

                    //check if they submitted their evaluation
                    for (StudentEvaluation otherEva : evaluations) {

                        if (otherEva.getStudentEmail().compareTo(eachEva.getStudentEmail()) != 0) {
                            targetHasEvaluation = otherEva.isMySelf(checkMember);
                            if (targetHasEvaluation == true) {
                                break;
                            }
                        }
                    }
                    if (targetHasEvaluation == false) {
                        System.out.println("ERROR: " + eachEva.getStudentEmail() + " in team " +
                                studentGroupName + " mentioned " +
                                checkMember + ", but " + checkMember + " did not provide an evaluation");
                        break;
                    }
                }
            }
        }

        return targetHasEvaluation;
    }

    /**
     * Method to print the group data (calculates the average score)
     * @param printer The PrintWriter object to produce output
     */
    public void printGroup(PrintWriter printer) {

        //for each student A inside the group
        for (StudentEvaluation evaA : evaluations) {

            String targetStudent = evaA.getStudentEmail();
            String sourceStudent = null;
            Double feedbackScore = 0d;
            String feedbackComment = null;
            Double studentTotal = 0d;

            //go to other students BCD's evaluation to get student A's feedbacks
            for (StudentEvaluation evaBCD : evaluations) {

                //leave student A's own feedback for last
                if (evaBCD.getStudentEmail() == targetStudent) {
                    continue;
                } else {
                    sourceStudent = evaBCD.getStudentEmail();

                    feedbackScore = evaBCD.findFeedbackById(targetStudent).getContribution().getScore();
                    studentTotal += feedbackScore;

                    feedbackComment = changeQuote(evaBCD.findFeedbackById(targetStudent).getContribution().getComment());

                    printer.printf(",%s,%s,%.1f,%s,,%n", sourceStudent, targetStudent, feedbackScore, feedbackComment);
                }

            }

            //now print student A's own feedback
            sourceStudent = "-->";
            feedbackScore = evaA.getGroup().get(0).getContribution().getScore();
            feedbackComment = changeQuote(evaA.getGroup().get(0).getContribution().getComment());
            printer.printf(",%s,%s,%.1f,%s%n", sourceStudent, targetStudent, feedbackScore, feedbackComment);

            //print student A's average score excluding self and confidential comment
            String confComment = changeQuote(evaA.getConfidentialComments());

            int numberOfStudentsExcludingSelf = evaluations.size() - 1;

            //if only one student in group, no average score from other students
            if ((numberOfStudentsExcludingSelf) == 0) {
                printer.printf(",-->,%s,avg NaN /0,,,%s%n", targetStudent, confComment); //TAT I hate linefeeds
            } else {
                double scoreAverage = studentTotal / numberOfStudentsExcludingSelf;
                double maxScoreCanBeReceived = 20d;
                double maxPossibleAvgScore = maxScoreCanBeReceived * numberOfStudentsExcludingSelf;

                printer.printf(",-->,%s,avg %.1f /%d,,,%s%n", targetStudent, scoreAverage, numberOfStudentsExcludingSelf, confComment);


            }

        }

    }

    //helper function to change double quote in student comments to single quote
    //return the changed string
    private String changeQuote(String changeString) {
        String result = changeString.replace("\\n", "%n");
        return "\"" + result.replace("\"", "\'") + "\"";
    }

    //helper function for toString()
    private String listToString(List<StudentEvaluation> evaluations){
        String listToStringResult = "";
        for (int i = 0; i < evaluations.size(); i++){
            listToStringResult += " " + evaluations.get(i).toString();
        }
        return listToStringResult;
    }

    /**
     * Method to override the default toString() to display information for debugging and logging purpose.
     * @return A string displaying the class info
     */
    @Override
    public String toString(){
        return getClass().getName() +
                "\n[Evaluation:" + listToString(this.evaluations) +
                ", Student Group Name:" + this.studentGroupName + "]";
    }

}
