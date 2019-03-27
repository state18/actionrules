package ar;

import java.util.List;

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




    @Override
    public String toString() {
        // (a1, c5) -> d6
        // (a1, c5) -> d6 -- Support:2 -- Confidence 50%

        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < this.attributeGroup.attVals.size(); i++) {
            String currVal = this.attributeGroup.attVals.get(i);
            String currName = this.attNames.get(i);
            if (currVal != null) {
                sb.append(currName);
                sb.append(" - ");
                sb.append(currVal);
                sb.append(", ");
            }
        }

        // Remove final comma and space
        sb.delete(sb.length() - 2, sb.length() - 1);

        sb.append(")");
        sb.append(" -> ");

        // Add decision attName - attVal

        for (int i = 0; i < decisionGroup.attVals.size(); i++) {
            if (decisionGroup.attVals.get(i) != null) {
                sb.append(this.attNames.get(i));
                sb.append(" - ");
                sb.append(this.decisionGroup.attVals.get(i));
                break;
            }
        }

        sb.append(String.format(" *** SUPPORT: %d *** CONFIDENCE: %.2f percent", this.support, this.confidence * 100));

        return sb.toString();
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
