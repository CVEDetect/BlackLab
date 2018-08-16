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

import nl.inl.blacklab.search.results.DocGroup;

public class DocGroupPropertySize extends DocGroupProperty {
    
    DocGroupPropertySize(DocGroupPropertySize prop, boolean invert) {
        super(prop, invert);
    }
    
    public DocGroupPropertySize() {
        super();
    }
    
    @Override
    public PropertyValueInt get(DocGroup result) {
        return new PropertyValueInt(result.size());
    }

    @Override
    public boolean defaultSortDescending() {
        return !reverse;
    }

    @Override
    public int compare(DocGroup a, DocGroup b) {
        return reverse ? b.size() - a.size() : a.size() - b.size();
    }

    @Override
    public String serialize() {
        return serializeReverse() + "size";
    }

    @Override
    public DocGroupProperty reverse() {
        return new DocGroupPropertySize(this, true);
    }

    @Override
    public String getName() {
        return "group: size";
    }
}
