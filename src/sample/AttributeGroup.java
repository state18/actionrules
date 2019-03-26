package sample;

import sun.reflect.generics.tree.Tree;

import java.lang.reflect.Array;
import java.util.*;

public class AttributeGroup {

    // Indexed by total number of attributes.
    // public ArrayList<HashMap<String, Set<Integer>>> attLookup;

    public ArrayList<String> attVals;

    // Which attribute(s) comprise this group?
    //public Set<Integer> attributes;
    // What are the actual values of these attributes? (Same length as attributes field)
    //public Set<String> attributeValues;
    // Which entries from input file have these values?
    // public ArrayList<Set<Integer>> entries;
    public Set<Integer> entries;
    // True if this AttributeGroup is a subset of one of the decision attribute's classes.
    public Set<String> marked = null;


    public AttributeGroup(ArrayList<String> attVals, Set<Integer> entries) {
        this.attVals = attVals;
        this.entries = entries;
    }

    // Combine two AttributeGroup
    public static AttributeGroup combine(AttributeGroup a, AttributeGroup b, int desiredSize) {

        // Two AttributeGroups can be combined if...
        // There's no overlap between attributes OR
        // If there is overlap, the overlapping attributes must not share the same value.

        // I believe this can be accomplished simply by comparing attribute values only since they are
        // the concatenation of attribute index and value.

        int numAttributes = a.attVals.size();

        // For each attribute,
        // If present in both sets, they MUST be the same value, else return null. Add to same set.
        // If not present in either set... Nothing happens.
        // If present in a but not b or present in b but not a, add to diff set.
        // sameSet.size() + diffSet.size() must equal desiredSize.

        ArrayList<String> newAttVals = new ArrayList<>();

        int numSame = 0;
        int numDiff = 0;

        for (int i = 0; i < numAttributes; i++) {
            String aVal = a.attVals.get(i);
            String bVal = b.attVals.get(i);

            // Are they both not present?
            if (aVal == null && bVal == null) {
                newAttVals.add(null);
                continue;
            }

            if (aVal == null) {
                newAttVals.add(bVal);
                numDiff++;

            } else if (bVal == null) {
                newAttVals.add(aVal);
                numDiff++;

            } else {
                // aVal and bVal both exist. They MUST be the same.
                if (aVal.equals(bVal)) {
                    newAttVals.add(aVal);
                    numSame++;
                } else {
                    return null;
                }
            }
        }

        // sameSet size + diffSet size must equal desired size.
        int totalSize = numSame + numDiff;
        if (totalSize != desiredSize)
            return null;

        Set<Integer> newEntries = new TreeSet<>();
        for (Integer e : a.entries) {
            if (b.entries.contains(e)) {
                newEntries.add(e);
            }
        }

        return new AttributeGroup(newAttVals, newEntries);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object other) {

        if (!(other instanceof AttributeGroup)) {
            return false;
        }

        AttributeGroup a = (AttributeGroup) other;

        return this.attVals.equals(a.attVals);

    }
}
