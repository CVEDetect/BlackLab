package nl.inl.blacklab.search.lucene;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermStates;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreMode;

/**
 * Return n-grams containing or within the hits from the source spans.
 */
public class SpanQueryFilterNGrams extends BLSpanQueryAbstract {

    /** How to expand the hits */
    final SpanQueryPositionFilter.Operation op;

    /** Minimum number of tokens to expand */
    final int min;

    /** Maximum number of tokens to expand (MAX_UNLIMITED = infinite) */
    final int max;

    /** How to adjust left n-gram border relative to the filter clause */
    private final int leftAdjust;

    /** How to adjust right n-gram border relative to the filter clause */
    private final int rightAdjust;

    public SpanQueryFilterNGrams(BLSpanQuery clause, SpanQueryPositionFilter.Operation op, int min, int max, int leftAdjust, int rightAdjust) {
        super(clause);
        this.op = op;
        this.min = min;
        this.max = max == -1 ? MAX_UNLIMITED : max;
        if (min > this.max)
            throw new IllegalArgumentException("min > max");
        if (min < 0 || this.max < 0)
            throw new IllegalArgumentException("min, max cannot be negative");
        this.leftAdjust = leftAdjust;
        this.rightAdjust = rightAdjust;
    }

    @Override
    public BLSpanQuery rewrite(IndexReader reader) throws IOException {
        List<BLSpanQuery> rewritten = rewriteClauses(reader);
        if (rewritten == null)
            return this;
        return new SpanQueryFilterNGrams(rewritten.get(0), op, min, max, leftAdjust, rightAdjust);
    }

    @Override
    public boolean matchesEmptySequence() {
        return clauses.get(0).matchesEmptySequence() && min == 0;
    }

    @Override
    public BLSpanQuery noEmpty() {
        if (!matchesEmptySequence())
            return this;
        int newMin = min == 0 ? 1 : min;
        return new SpanQueryFilterNGrams(clauses.get(0).noEmpty(), op, newMin, max, leftAdjust, rightAdjust);
    }

    @Override
    public BLSpanWeight createWeight(IndexSearcher searcher, ScoreMode scoreMode, float boost) throws IOException {
        BLSpanWeight weight = clauses.get(0).createWeight(searcher, scoreMode, boost);
        return new SpanWeightFilterNGrams(weight, searcher, scoreMode.needsScores() ? getTermStates(weight) : null, boost);
    }

    class SpanWeightFilterNGrams extends BLSpanWeight {

        final BLSpanWeight weight;

        public SpanWeightFilterNGrams(BLSpanWeight weight, IndexSearcher searcher, Map<Term, TermStates> terms, float boost)
                throws IOException {
            super(SpanQueryFilterNGrams.this, searcher, terms, boost);
            this.weight = weight;
        }

        @Override
        public void extractTerms(Set<Term> terms) {
            weight.extractTerms(terms);
        }

        @Override
        public void extractTermStates(Map<Term, TermStates> contexts) {
            weight.extractTermStates(contexts);
        }

        @Override
        public BLSpans getSpans(final LeafReaderContext context, Postings requiredPostings) throws IOException {
            BLSpans spansSource = weight.getSpans(context, requiredPostings);
            if (spansSource == null)
                return null;
            BLSpans filtered = new SpansFilterNGramsRaw(context.reader(), clauses.get(0).getField(),
                    spansSource, op, min, max, leftAdjust, rightAdjust);

            // Re-sort the results if necessary (if we expanded a non-fixed amount to the left)
            BLSpanQuery q = (BLSpanQuery) weight.getQuery();
            if (q != null && !q.hitsStartPointSorted())
                return BLSpans.ensureStartPointSorted(filtered);

            return filtered;
        }

    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + leftAdjust;
        result = prime * result + max;
        result = prime * result + min;
        result = prime * result + ((op == null) ? 0 : op.hashCode());
        result = prime * result + rightAdjust;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        SpanQueryFilterNGrams other = (SpanQueryFilterNGrams) obj;
        if (leftAdjust != other.leftAdjust)
            return false;
        if (max != other.max)
            return false;
        if (min != other.min)
            return false;
        if (op != other.op)
            return false;
        if (rightAdjust != other.rightAdjust)
            return false;
        return true;
    }

    @Override
    public String toString(String field) {
        return "FILTERNGRAMS(" + clauses.get(0) + ", " + op + ", " + min + ", " + inf(max)
                + ")";
    }

    @Override
    public boolean hitsAllSameLength() {
        return min == max;
    }

    @Override
    public int hitsLengthMin() {
        return min;
    }

    @Override
    public int hitsLengthMax() {
        return max;
    }

    @Override
    public boolean hitsEndPointSorted() {
        return hitsAllSameLength();
    }

    @Override
    public boolean hitsStartPointSorted() {
        return clauses.get(0).hitsStartPointSorted() && clauses.get(0).hitsLengthMax() >= min;
    }

    @Override
    public boolean hitsHaveUniqueStart() {
        return min == max;
    }

    @Override
    public boolean hitsHaveUniqueEnd() {
        return min == max;
    }

    @Override
    public boolean hitsAreUnique() {
        return clauses.get(0).hitsAreUnique() && clauses.get(0).hitsLengthMax() >= min;
    }

    @Override
    public long reverseMatchingCost(IndexReader reader) {
        int numberOfExpansionSteps = max == MAX_UNLIMITED ? 50 : max - min + 1;
        return clauses.get(0).reverseMatchingCost(reader) * numberOfExpansionSteps;
    }

    @Override
    public int forwardMatchingCost() {
        int numberOfExpansionSteps = max == MAX_UNLIMITED ? 50 : max - min + 1;
        return clauses.get(0).forwardMatchingCost() * numberOfExpansionSteps;
    }

}
