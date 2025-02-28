package nl.inl.blacklab.server.lib.results;

import java.util.Collections;
import java.util.Set;

import nl.inl.blacklab.search.BlackLabIndex;
import nl.inl.blacklab.search.indexmetadata.Annotation;

public class ResultAnnotationInfo {

    private Annotation annotation;

    private boolean showValues;

    private Set<String> terms = Collections.emptySet();

    private boolean valueListComplete = true;

    ResultAnnotationInfo(BlackLabIndex index, Annotation annotation, boolean showValues) {
        this.annotation = annotation;
        this.showValues = showValues;
        if (showValues && !index.isEmpty()) {
            boolean[] valueListCompleteArray = { true }; // array because we have to access them from the closures
            terms = WebserviceOperations.getAnnotationValues(index, annotation, valueListCompleteArray);
            valueListComplete = valueListCompleteArray[0];
        }
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public boolean isShowValues() {
        return showValues;
    }

    public Set<String> getTerms() {
        return terms;
    }

    public boolean isValueListComplete() {
        return valueListComplete;
    }
}
