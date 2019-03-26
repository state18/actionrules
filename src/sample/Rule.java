package sample;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Rule {

    public AttributeGroup attributeGroup;
    public AttributeGroup decisionGroup;

    private double confidence;
    private int support;

    private List<String> attNames;


    public Rule(AttributeGroup ag, AttributeGroup dg, double confidence, int support, List<String> attNames) {
        this.attributeGroup = ag;
        this.decisionGroup = dg;
        this.confidence = confidence;
        this.support = support;
        this.attNames = attNames;
    }

    public double getConfidence() {
        return confidence;
    }

    public int getSupport() {
        return support;
    }

    @Override
    public String toString() {
        // (a1, c5) -> d6
        // (a1, c5) -> d6 -- Support:2 -- Confidence 50%



        return null;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object other) {

        if (!(other instanceof Rule)) {
            return false;
        }

        Rule r = (Rule) other;

        return this.attributeGroup.equals(r.attributeGroup) && this.decisionGroup.equals(r.decisionGroup);

    }
}
