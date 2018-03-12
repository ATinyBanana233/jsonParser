package ca.cmpt213.as2;

import java.util.List;
import java.util.ArrayList;
//An annotation that indicates this member should be exposed for JSON serialization or deserialization.
import com.google.gson.annotations.Expose;
//An annotation that indicates this member should be serialized or deserialized to JSON with the provided name value as its field name.
import com.google.gson.annotations.SerializedName;

/**
 * A class that represents the student evaluations: group (List<Group>), confidentialComments (String).
 * @author Bei Bei Li
 */
public class StudentEvaluation {

    @SerializedName("group")
    @Expose
    //has two boolean flags: deserialize and serialize, to allow skipping the field for one phase.
    //the group of feedbacks in the evaluation
    private List<Group> group;

    @SerializedName("confidential_comments")
    @Expose
    private String confidentialComments;

    /**
     * Method to retrieve pointer to the list of feedbacks inside the evaluation
     * @return The pointer reference to the group list (which contains the feedbacks)
     */
    public List<Group> getGroup(){
        return group;
    }

    /**
     * Method to retrieve the confidential comment in the evaluation
     * @return The confidential comment (String)
     */
    public String getConfidentialComments(){
        return confidentialComments;
    }

    /**
     * Method to retrieve the number of feedbacks in the evaluation
     * @return The number of feedbacks (Int)
     */
    public int getGroupSize(){
        return this.group.size();
    }

    /**
     * Method to retrieve the name of whom submitted the evaluation
     * @return The name of the evaluation owner (String)
     */
    public String getGroupName() {
        int studentSelf = 0;
        return this.group.get(0).getGroupName();
    }

    /**
     * Method to retrieve the sfu email of whom submitted the evaluation
     * @return The sfu email of the evaluation owner (String)
     */
    public String getStudentEmail() {
        int studentSelf = 0;
        return this.group.get(0).getSfuEmail();
    }

    /**
     * Method to retrieve a list of student emails from the feedbacks in the evaluation excluding self
     * @return A List<String> of sfu emails
     */
    public List<String> getTargetStudents(){
        List<String> TargetStudents = new ArrayList<>();
        //check
        for ( int otherStudents = 1; otherStudents < this.group.size(); otherStudents++) {
            TargetStudents.add(group.get(otherStudents).getSfuEmail());
        }
        return TargetStudents;
    }

    /**
     * Method to check if the sfu email given is in one of the feedbacks in the evaluation
     * @param checkIsIn The sfu email to be checked
     * @return A boolean value to indicate if the sfu email given is in one of the feedbacks
     * in the evaluation
     */
    public boolean isInTargetStudents(String checkIsIn){

        boolean isIn = false;
        List<String> TargetStudents = new ArrayList<>();

        //get the feedback emails
        for ( int otherStudents = 0; otherStudents < this.group.size(); otherStudents++) {
            TargetStudents.add(group.get(otherStudents).getSfuEmail());
        }

        //check
        for (String others : TargetStudents){
            if (checkIsIn.compareTo(others) == 0){
                isIn = true;
            }
        }

        return isIn;
    }

    /**
     * A method to check if the sfu email given is the owner of the evaluation
     * @param studentID The sfu email to be checked
     * @return A boolean value to indicate if the sfu email given is the owner of the evaluation
     */
    public boolean isMySelf (String studentID) {
        boolean isMe = false;
        if (getStudentEmail().compareTo(studentID) == 0){
            isMe = true;
        }

        return isMe;

    }

    /**
     * A method to check if the sum of score in the file is not (20 * number of group members), with a tolerance of 0.1.
     * @return A boolean value to indicate if the sum of score in the file is not (20 * number of group members), with a tolerance of 0.1.
     */
    public boolean isSumScoreWithinRange() {

        boolean isWithin = true;

        int numberOfStudents = getGroupSize();
        double maxPossibleSum = 20d * numberOfStudents;
        double currentSum = 0d;

        //Sum of scores in the file is not (20 * number of group members), with a tolerance of 0.1
        for (Group eachFeedBack : this.group){
            currentSum += eachFeedBack.getContribution().getScore();
        }

        if (!(Math.abs(currentSum - maxPossibleSum) < 0.1)){
            isWithin = false;
            System.out.println("ERROR: " + getStudentEmail() + "'s evaluation - " +
                    "the sum of score in the file is not (20 * number of group members), with a tolerance of 0.1");
        }

        return isWithin;
    }

    /**
     * A method to find the feedback by sfu email
     * @param target The sfu email of the feedback wanted
     * @return The feedback (Group) wanted
     */
    public Group findFeedbackById(String target){
        Group found = null;
        for (Group eachFeedback : group){
            if (eachFeedback.getSfuEmail().compareTo(target) == 0){
                found = eachFeedback;
            }
        }
        return found;
    }

    //helper function for toString()
    private String listToString(List<Group> group){
        String listToStringResult = "";
        for (int i = 0; i < group.size(); i++){
            listToStringResult += " " + group.get(i).toString();
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
                "\n[Group:" + listToString(this.group) +
                ", Confidential Comments:" + this.confidentialComments + "]";
    }

}
