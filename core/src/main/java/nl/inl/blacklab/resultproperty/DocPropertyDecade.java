/*******************************************************************************
 * Copyright (c) 2010, 2012 Institute for Dutch Lexicology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package nl.inl.blacklab.resultproperty;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.util.BytesRef;

import nl.inl.blacklab.search.BlackLabIndex;
import nl.inl.blacklab.search.results.DocResult;

/**
 * For grouping DocResult objects by decade based on a stored field containing a
 * year.
 */
public class DocPropertyDecade extends DocProperty {

    private BlackLabIndex index;
    
    private String fieldName;
    
    private DocPropertyStoredField docPropStoredField;

    DocPropertyDecade(DocPropertyDecade prop, boolean invert) {
        super(prop, invert);
        index = prop.index;
        fieldName = prop.fieldName;
        docPropStoredField = prop.docPropStoredField;
    }

    public DocPropertyDecade(BlackLabIndex index, String fieldName) {
        this.index = index;
        this.fieldName = fieldName;
        docPropStoredField = new DocPropertyStoredField(index, fieldName);
    }

    public int get(int docId) {
        String strYear = docPropStoredField.get(docId);
        int year;
        try {
            year = Integer.parseInt(strYear);
            year -= year % 10;
        } catch (NumberFormatException e) {
            year = HitPropertyDocumentDecade.UNKNOWN_VALUE;
        }
        return year;
    }

    @Override
    public PropertyValueDecade get(DocResult result) {
        String strYear = docPropStoredField.get(result).value;
        int year;
        try {
            year = Integer.parseInt(strYear);
            year -= year % 10;
        } catch (NumberFormatException e) {
            year = HitPropertyDocumentDecade.UNKNOWN_VALUE;
        }
        return new PropertyValueDecade(year);
    }

    /**
     * Compares two docs on this property
     * 
     * @param a first doc
     * @param b second doc
     * @return 0 if equal, negative if a < b, positive if a > b.
     */
    @Override
    public int compare(DocResult a, DocResult b) {
        String strYearA = a.identity().luceneDoc().get(fieldName);
        if (strYearA == null)
            strYearA = "";
        String strYearB = b.identity().luceneDoc().get(fieldName);
        if (strYearB == null)
            strYearB = "";
        if (strYearA.length() == 0) { // sort missing year at the end
            if (strYearB.length() == 0)
                return 0;
            else
                return reverse ? -1 : 1;
        }
        if (strYearB.length() == 0) // sort missing year at the end
            return reverse ? 1 : -1;
        int year1;
        try {
            year1 = Integer.parseInt(strYearB);
            year1 -= year1 % 10;
        } catch (NumberFormatException e) {
            year1 = HitPropertyDocumentDecade.UNKNOWN_VALUE;
        }
        int year2;
        try {
            year2 = Integer.parseInt(strYearB);
            year2 -= year2 % 10;
        } catch (NumberFormatException e) {
            year2 = HitPropertyDocumentDecade.UNKNOWN_VALUE;
        }

        return reverse ? year2 - year1 : year1 - year2;
    }

    @Override
    public String name() {
        return "decade";
    }

    public static DocPropertyDecade deserialize(BlackLabIndex index, String info) {
        return new DocPropertyDecade(index, PropertySerializeUtil.unescapePart(info));
    }

    @Override
    public String serialize() {
        return serializeReverse() + PropertySerializeUtil.combineParts("decade", fieldName);
    }

    @Override
    public DocProperty reverse() {
        return new DocPropertyDecade(this, true);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
        DocPropertyDecade other = (DocPropertyDecade) obj;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        return true;
    }

    @Override
    public Query query(BlackLabIndex index, PropertyValue value) {
        int decade = Integer.parseInt(value.toString());
        String lowerValue = Integer.toString(decade);
        String upperValue = Integer.toString(decade + 9);
        return new TermRangeQuery(fieldName, new BytesRef(lowerValue), new BytesRef(upperValue), true, true);
    }
    
    @Override
    public boolean canConstructQuery(BlackLabIndex index, PropertyValue value) {
        return true;
    }

}
