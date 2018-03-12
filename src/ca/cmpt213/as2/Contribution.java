package ca.cmpt213.as2;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * A class that represents the contribution in each feedback: score (double), comment (String).
 * @author Bei Bei Li
 */
public class Contribution {

    @SerializedName("score")
    @Expose
    private double score = -1;

    @SerializedName("comment")
    @Expose
    private String comment;

    /**
     * Method to retrieve the score in the contribution
     * @return The score of the contribution (Contribution)
     */
    public double getScore() {
        return score;
    }

    /**
     * Method to retrieve the comment in the contribution
     * @return The comment of the contribution (Contribution)
     */
    public String getComment() {
        return comment;
    }

    /**
     * Method to override the default toString() to display information for debugging and logging purpose.
     * @return A string displaying the class info
     */
    @Override
    public String toString() {
        return getClass().getName() +
                "[Score:" + this.score +
                ", Comment:" + this.comment + "]";
    }
}
