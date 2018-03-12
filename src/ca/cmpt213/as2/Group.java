package ca.cmpt213.as2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * A class that represents the feedback in each evaluation: name (String), sfuEmail (String), contribution (Contribution).
 * @author Bei Bei Li
 */
public class Group {
    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("sfu_email")
    @Expose
    private String sfuEmail;

    @SerializedName("contribution")
    @Expose
    private Contribution contribution;

    /**
     * Method to retrieve the name in the feedback
     * @return The name of the feedback (String)
     */
    public String getName() {
        return name;
    }

    /**
     * Method to retrieve the sfu email in the feedback
     * @return The sfu email of the feedback (String)
     */
    public String getSfuEmail() {
        return sfuEmail;
    }

    /**
     * Method to retrieve the contribution object in the feedback
     * @return The contribution of the feedback (Contribution)
     */
    public Contribution getContribution() {
        return contribution;
    }

    /**
     * Method to retrieve the contribution object in the feedback
     * @return The contribution of the feedback (Contribution)
     */
    public String getGroupName(){
        String[] groupNameElements = this.sfuEmail.split("-");
        return groupNameElements[1];

    }

    /**
     * Method to override the default toString() to display information for debugging and logging purpose.
     * @return A string displaying the class info
     */
    @Override
    public String toString(){
        return getClass().getName() +
                "[Name:" + this.name +
                ", SFU Email:" + this.sfuEmail +
                ", Contribution:" + contribution.toString() + "]";
    }
}
